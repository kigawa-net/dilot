package net.kigawa.dilot.command

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.darwin._NSGetExecutablePath

@OptIn(ExperimentalForeignApi::class)
internal actual fun getBinaryDir(): String? = memScoped {
    val buf = allocArray<ByteVar>(4096)
    val size = alloc<UIntVar>()
    size.value = 4095u
    if (_NSGetExecutablePath(buf, size.ptr) == 0) buf.toKString().substringBeforeLast('/') else null
}
