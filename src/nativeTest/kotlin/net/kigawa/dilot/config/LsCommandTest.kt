package net.kigawa.dilot.config

import net.kigawa.dilot.command.LsCommand
import kotlin.test.Test
import kotlin.test.assertEquals

class LsCommandTest {

    private fun makeCmd(initial: DilotConfig = DilotConfig()): Pair<MutableList<String>, LsCommand> {
        val repo = InMemoryLsRepo(initial)
        val output = mutableListOf<String>()
        val cmd = LsCommand({ repo }) { output.add(it) }
        return output to cmd
    }

    @Test
    fun printsNothingForEmptyConfig() {
        val (output, cmd) = makeCmd()
        cmd.run(emptyArray())
        assertEquals(emptyList(), output)
    }

    @Test
    fun printsSingleProjectTabSeparated() {
        val config = DilotConfig(
            projects = listOf(ProjectEntry("my-project", "https://github.com/example/repo.git", "2026-05-14T00:00:00Z"))
        )
        val (output, cmd) = makeCmd(config)
        cmd.run(emptyArray())
        assertEquals(listOf("my-project\thttps://github.com/example/repo.git\t2026-05-14T00:00:00Z"), output)
    }

    @Test
    fun printsMultipleProjectsInOrder() {
        val config = DilotConfig(
            projects = listOf(
                ProjectEntry("alpha", "https://github.com/a/a.git", "2026-05-14T00:00:00Z"),
                ProjectEntry("beta", "git@github.com:b/b.git", "2026-05-20T12:00:00Z"),
            )
        )
        val (output, cmd) = makeCmd(config)
        cmd.run(emptyArray())
        assertEquals(2, output.size)
        assertEquals("alpha\thttps://github.com/a/a.git\t2026-05-14T00:00:00Z", output[0])
        assertEquals("beta\tgit@github.com:b/b.git\t2026-05-20T12:00:00Z", output[1])
    }
}

private class InMemoryLsRepo(private val initial: DilotConfig) : ConfigRepository {
    override fun load(): Result<DilotConfig> = Result.success(initial)
    override fun save(config: DilotConfig): Result<Unit> = Result.success(Unit)
}
