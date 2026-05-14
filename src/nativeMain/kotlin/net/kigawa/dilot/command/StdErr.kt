package net.kigawa.dilot.command

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fputs
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
fun printErr(msg: String) {
    fputs("$msg\n", stderr)
}
