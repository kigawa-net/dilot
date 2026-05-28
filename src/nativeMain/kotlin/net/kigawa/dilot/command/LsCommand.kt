package net.kigawa.dilot.command

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import net.kigawa.dilot.config.ConfigRepository
import net.kigawa.dilot.config.FileConfigRepository
import net.kigawa.dilot.config.resolveConfigPath
import kotlin.system.exitProcess

class LsCommand(
    private val repoFactory: (String) -> ConfigRepository = ::FileConfigRepository,
    private val printer: (String) -> Unit = ::println,
) {

    fun run(args: Array<String>) {
        val parser = ArgParser("dilot ls")
        val configPath by parser.option(ArgType.String, fullName = "config", description = "Config file path")

        parser.parse(args)

        val repo = repoFactory(resolveConfigPath(configPath))
        val config = repo.load().getOrElse {
            printErr("Error: failed to load config: ${it.message}")
            exitProcess(1)
        }

        config.projects.forEach { project ->
            printer("${project.name}\t${project.coreUrl}\t${project.createdAt}")
        }
    }
}
