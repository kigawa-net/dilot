package net.kigawa.dilot.command

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.windows.GetModuleFileNameA
import platform.windows.MAX_PATH

@OptIn(ExperimentalForeignApi::class)
internal actual fun getBinaryDir(): String? = memScoped {
    val buf = allocArray<ByteVar>(MAX_PATH)
    val len = GetModuleFileNameA(null, buf, MAX_PATH.toUInt())
    if (len > 0u) buf.toKString().substringBeforeLast('\\') else null
}
