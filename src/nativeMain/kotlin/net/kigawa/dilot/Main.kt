package net.kigawa.dilot

import net.kigawa.dilot.command.LsCommand
import net.kigawa.dilot.command.NewCommand
import net.kigawa.dilot.command.OpenCommand
import net.kigawa.dilot.command.printErr
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printErr("Usage: dilot <command> [options]")
        printErr("Commands: new, o, ls")
        exitProcess(2)
    }

    when (args[0]) {
        "new" -> NewCommand().run(args.drop(1).toTypedArray())
        "o" -> OpenCommand().run(args.drop(1).toTypedArray())
        "ls" -> LsCommand().run(args.drop(1).toTypedArray())
        else -> {
            printErr("Unknown command: ${args[0]}")
            exitProcess(2)
        }
    }
}
