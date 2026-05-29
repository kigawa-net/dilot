package net.kigawa.dilot.command

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.readlink

@OptIn(ExperimentalForeignApi::class)
internal actual fun getBinaryDir(): String? = memScoped {
    val buf = allocArray<ByteVar>(4096)
    val len = readlink("/proc/self/exe", buf, 4095u)
    if (len > 0L) buf.toKString().substringBeforeLast('/') else null
}
