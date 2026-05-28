package net.kigawa.dilot.config

import net.kigawa.dilot.command.OpenCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenCommandTest {

    private fun makeCmd(
        config: DilotConfig,
        commands: Map<String, Boolean> = emptyMap(),
        runResults: Map<String, Int> = emptyMap(),
    ): Pair<MutableList<String>, OpenCommand> {
        val repo = InMemoryOpenRepo(config)
        val executedCommands = mutableListOf<String>()
        val cmd = OpenCommand(
            repoFactory = { repo },
            commandChecker = { cmd -> commands[cmd] ?: false },
            runner = { shellCmd ->
                executedCommands.add(shellCmd)
                val key = runResults.keys.firstOrNull { shellCmd.startsWith(it) }
                runResults[key] ?: 0
            },
        )
        return executedCommands to cmd
    }

    private val singleProjectConfig = DilotConfig(
        projects = listOf(ProjectEntry("my-project", "https://github.com/example/repo.git", "2026-05-14T00:00:00Z"))
    )

    @Test
    fun skipsInstallWhenDevcontainerExists() {
        val (executed, cmd) = makeCmd(
            config = singleProjectConfig,
            commands = mapOf("devcontainer" to true),
        )
        cmd.run(arrayOf("my-project"))
        assertTrue(executed.none { it.startsWith("npm install") })
    }

    @Test
    fun installsDevcontainerWhenMissing() {
        val (executed, cmd) = makeCmd(
            config = singleProjectConfig,
            commands = mapOf("devcontainer" to false, "npm" to true),
        )
        cmd.run(arrayOf("my-project"))
        assertTrue(executed.any { it == "npm install -g @devcontainers/cli" })
    }

    @Test
    fun runsDevcontainerAfterInstall() {
        val (executed, cmd) = makeCmd(
            config = singleProjectConfig,
            commands = mapOf("devcontainer" to false, "npm" to true),
        )
        cmd.run(arrayOf("my-project"))
        val installIndex = executed.indexOfFirst { it == "npm install -g @devcontainers/cli" }
        val upIndex = executed.indexOfFirst { it.startsWith("devcontainer up") }
        assertTrue(installIndex >= 0 && upIndex > installIndex)
    }
}

private class InMemoryOpenRepo(private val config: DilotConfig) : ConfigRepository {
    override fun load(): Result<DilotConfig> = Result.success(config)
    override fun save(config: DilotConfig): Result<Unit> = Result.success(Unit)
}
