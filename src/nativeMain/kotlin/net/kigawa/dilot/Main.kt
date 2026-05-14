package net.kigawa.dilot

import net.kigawa.dilot.command.NewCommand
import net.kigawa.dilot.command.printErr
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printErr("Usage: dilot <command> [options]")
        printErr("Commands: new")
        exitProcess(2)
    }

    when (args[0]) {
        "new" -> NewCommand().run(args.drop(1).toTypedArray())
        else -> {
            printErr("Unknown command: ${args[0]}")
            exitProcess(2)
        }
    }
}
