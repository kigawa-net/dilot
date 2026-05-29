package net.kigawa.dilot.command

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import net.kigawa.dilot.config.FileConfigRepository
import net.kigawa.dilot.config.ConfigRepository
import net.kigawa.dilot.config.resolveConfigPath
import platform.posix.getenv
import platform.posix.readlink
import platform.posix.stat
import platform.posix.system
import kotlin.system.exitProcess

class OpenCommand(
    private val repoFactory: (String) -> ConfigRepository = ::FileConfigRepository,
    private val commandChecker: (String) -> Boolean = ::commandExists,
    private val runner: (String) -> Int = { cmd -> system(cmd) ?: -1 },
    private val fileExists: (String) -> Boolean = ::fileExistsImpl,
    private val templateResolver: () -> String? = ::resolveTemplatePath,
) {

    @OptIn(ExperimentalForeignApi::class)
    fun run(args: Array<String>) {
        val parser = ArgParser("dilot o")
        val name by parser.argument(ArgType.String, description = "Project name")
        val configPath by parser.option(ArgType.String, fullName = "config", description = "Config file path")

        parser.parse(args)

        val repo = repoFactory(resolveConfigPath(configPath))
        val config = repo.load().getOrElse {
            printErr("Error: failed to load config: ${it.message}")
            exitProcess(1)
        }

        val project = config.projects.find { it.name == name }
        if (project == null) {
            printErr("Error: project '$name' not found.")
            exitProcess(4)
        }

        if (!commandChecker("devcontainer")) {
            if (!commandChecker("npm")) {
                printErr("Error: npm not found. Please install Node.js and npm.")
                exitProcess(1)
            }
            println("Installing devcontainer CLI ...")
            val installResult = runner("npm install -g @devcontainers/cli")
            if (installResult != 0) {
                printErr("Error: failed to install devcontainer CLI (exit $installResult).")
                exitProcess(1)
            }
        }

        val home = getenv("HOME")?.toKString() ?: "."
        val repoName = project.coreUrl.trimEnd('/').substringAfterLast('/').removeSuffix(".git")
        val repoPath = "$home/dilot/$name/$repoName"

        if (!dirExists(repoPath)) {
            println("Cloning ${project.coreUrl} into $repoPath ...")
            val cloneResult = runner("git clone ${shellEscape(project.coreUrl)} ${shellEscape(repoPath)}")
            if (cloneResult != 0) {
                printErr("Error: git clone failed (exit $cloneResult).")
                exitProcess(1)
            }
        }

        val devcontainerConfig = "$repoPath/.devcontainer/devcontainer.json"
        if (!fileExists(devcontainerConfig)) {
            println("Generating .devcontainer from template ...")
            val templatePath = templateResolver()
            if (templatePath == null) {
                printErr("Error: devcontainer template not found.")
                exitProcess(1)
            }
            val mkdirResult = runner("mkdir -p ${shellEscape("$repoPath/.devcontainer")}")
            if (mkdirResult != 0) {
                printErr("Error: failed to generate .devcontainer: mkdir failed.")
                exitProcess(1)
            }
            val cpResult = runner("cp ${shellEscape(templatePath)} ${shellEscape(devcontainerConfig)}")
            if (cpResult != 0) {
                printErr("Error: failed to generate .devcontainer: cp failed.")
                exitProcess(1)
            }
        }

        println("Starting devcontainer for '$name' ...")
        val upResult = runner("devcontainer up --workspace-folder ${shellEscape(repoPath)}")
        if (upResult != 0) {
            printErr("Error: devcontainer up failed (exit $upResult).")
            exitProcess(1)
        }

        runner("devcontainer exec --workspace-folder ${shellEscape(repoPath)} bash")
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun dirExists(path: String): Boolean = memScoped {
    val st = alloc<stat>()
    platform.posix.stat(path, st.ptr) == 0
}

@OptIn(ExperimentalForeignApi::class)
private fun fileExistsImpl(path: String): Boolean = memScoped {
    val st = alloc<stat>()
    platform.posix.stat(path, st.ptr) == 0
}

private fun commandExists(cmd: String): Boolean =
    system("which ${shellEscape(cmd)} > /dev/null 2>&1") == 0

@OptIn(ExperimentalForeignApi::class)
private fun resolveTemplatePath(): String? {
    val envDir = getenv("DILOT_TEMPLATE_DIR")?.toKString()
    if (envDir != null) {
        val path = "$envDir/devcontainer.json"
        if (fileExistsImpl(path)) return path
    }

    val binaryDir = memScoped {
        val buf = allocArray<ByteVar>(4096)
        val len = readlink("/proc/self/exe", buf, 4095u)
        if (len > 0L) buf.toKString().substringBeforeLast('/') else null
    }
    if (binaryDir != null) {
        val path = "$binaryDir/templates/devcontainer.json"
        if (fileExistsImpl(path)) return path
    }

    val home = getenv("HOME")?.toKString() ?: return null
    val path = "$home/.dilot/templates/devcontainer.json"
    if (fileExistsImpl(path)) return path

    return null
}

private fun shellEscape(s: String): String = "'${s.replace("'", "'\\''")}'"
