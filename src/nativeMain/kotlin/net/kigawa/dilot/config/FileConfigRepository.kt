package net.kigawa.dilot.config

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.cinterop.ByteVar
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.fread
import platform.posix.getenv
import platform.posix.rename

private val json = Json { prettyPrint = true; prettyPrintIndent = "  " }

private const val SUPPORTED_VERSION = 1

@OptIn(ExperimentalForeignApi::class)
class FileConfigRepository(private val configPath: String) : ConfigRepository {

    override fun load(): Result<DilotConfig> = runCatching {
        val file = fopen(configPath, "r") ?: return@runCatching DilotConfig()
        try {
            val content = buildString {
                memScoped {
                    val byte = alloc<ByteVar>()
                    while (fread(byte.ptr, 1u, 1u, file).toInt() == 1) {
                        append(byte.value.toInt().toChar())
                    }
                }
            }
            val config = json.decodeFromString<DilotConfig>(content)
            if (config.version > SUPPORTED_VERSION) {
                error("Unsupported config version: ${config.version}")
            }
            config
        } finally {
            fclose(file)
        }
    }

    override fun save(config: DilotConfig): Result<Unit> = runCatching {
        val dir = configPath.substringBeforeLast('/')
        mkdirRecursive(dir)

        val tmpPath = "$configPath.tmp"
        val content = json.encodeToString(config)

        val file = fopen(tmpPath, "w") ?: error("Failed to open temp file: $tmpPath")
        try {
            fputs(content, file)
        } finally {
            fclose(file)
        }
        if (rename(tmpPath, configPath) != 0) error("Failed to rename $tmpPath to $configPath")
    }

    private fun mkdirRecursive(path: String) {
        val parts = path.trimEnd('/').split('/')
        var current = ""
        for (part in parts) {
            current = if (current.isEmpty()) part else "$current/$part"
            if (current.isEmpty()) continue
            platformMkdir(current)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun resolveConfigPath(explicitPath: String?): String {
    if (explicitPath != null) return explicitPath
    val env = getenv("DILOT_CONFIG")?.toKString()
    if (env != null) return env
    val home = getenv("HOME")?.toKString() ?: "."
    return "$home/.dilot/config.json"
}
