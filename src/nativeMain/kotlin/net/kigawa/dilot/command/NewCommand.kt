package net.kigawa.dilot.command

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import net.kigawa.dilot.config.ConfigRepository
import net.kigawa.dilot.config.DilotConfig
import net.kigawa.dilot.config.FileConfigRepository
import net.kigawa.dilot.config.ProjectEntry
import net.kigawa.dilot.config.resolveConfigPath
import kotlin.system.exitProcess

private val NAME_REGEX = Regex("[a-zA-Z0-9_-]{1,64}")
private val URL_REGEX = Regex("(https?://|git@).+")

class NewCommand(private val repoFactory: (String) -> ConfigRepository = ::FileConfigRepository) {

    fun run(args: Array<String>) {
        val parser = ArgParser("dilot new")
        val name by parser.argument(ArgType.String, description = "Project name")
        val gitUrl by parser.argument(ArgType.String, description = "Core repository URL")
        val configPath by parser.option(ArgType.String, fullName = "config", description = "Config file path")

        parser.parse(args)

        if (!NAME_REGEX.matches(name)) {
            printErr("Error: invalid project name '$name'. Use [a-zA-Z0-9_-], 1-64 chars.")
            exitProcess(2)
        }
        if (!URL_REGEX.matches(gitUrl)) {
            printErr("Error: invalid git URL '$gitUrl'. Must start with https://, http://, or git@.")
            exitProcess(2)
        }

        val repo = repoFactory(resolveConfigPath(configPath))

        val config = repo.load().getOrElse {
            printErr("Error: failed to load config: ${it.message}")
            exitProcess(1)
        }

        if (config.projects.any { it.name == name }) {
            printErr("Error: project '$name' already exists.")
            exitProcess(5)
        }

        val updated = config.copy(
            projects = config.projects + ProjectEntry(name = name, coreUrl = gitUrl, createdAt = currentUtcIso8601())
        )

        repo.save(updated).getOrElse {
            printErr("Error: failed to save config: ${it.message}")
            exitProcess(1)
        }

        println("Project \"$name\" created.")
    }
}
