package net.kigawa.dilot.config

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.mkdir

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformMkdir(path: String) {
    mkdir(path, 0x1EDu.toUShort())
}
