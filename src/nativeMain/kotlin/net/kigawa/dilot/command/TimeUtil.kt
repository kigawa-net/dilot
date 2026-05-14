package net.kigawa.dilot.command

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gmtime_r
import platform.posix.time
import platform.posix.time_tVar
import platform.posix.tm

@OptIn(ExperimentalForeignApi::class)
fun currentUtcIso8601(): String = memScoped {
    val t = alloc<time_tVar>()
    time(t.ptr)
    val gm = alloc<tm>()
    gmtime_r(t.ptr, gm.ptr)
    val year = (gm.tm_year + 1900).toString().padStart(4, '0')
    val month = (gm.tm_mon + 1).toString().padStart(2, '0')
    val day = gm.tm_mday.toString().padStart(2, '0')
    val hour = gm.tm_hour.toString().padStart(2, '0')
    val min = gm.tm_min.toString().padStart(2, '0')
    val sec = gm.tm_sec.toString().padStart(2, '0')
    "$year-$month-${day}T${hour}:${min}:${sec}Z"
}
