package net.kigawa.dilot.config

import net.kigawa.dilot.command.NewCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewCommandTest {

    private fun makeRepo(initial: DilotConfig = DilotConfig()): Pair<InMemoryConfigRepository, NewCommand> {
        val repo = InMemoryConfigRepository(initial)
        return repo to NewCommand { repo }
    }

    @Test
    fun addsProjectToConfig() {
        val (repo, cmd) = makeRepo()
        cmd.run(arrayOf("my-project", "https://github.com/example/repo.git"))
        val saved = repo.savedConfig!!
        assertEquals(1, saved.projects.size)
        assertEquals("my-project", saved.projects[0].name)
        assertEquals("https://github.com/example/repo.git", saved.projects[0].coreUrl)
    }

    @Test
    fun acceptsGitSshUrl() {
        val (repo, cmd) = makeRepo()
        cmd.run(arrayOf("proj", "git@github.com:example/repo.git"))
        assertEquals("git@github.com:example/repo.git", repo.savedConfig!!.projects[0].coreUrl)
    }

    @Test
    fun preservesExistingProjects() {
        val existing = DilotConfig(
            projects = listOf(ProjectEntry("existing", "https://github.com/a/b.git", "2026-01-01T00:00:00Z"))
        )
        val (repo, cmd) = makeRepo(existing)
        cmd.run(arrayOf("new-proj", "https://github.com/c/d.git"))
        assertEquals(2, repo.savedConfig!!.projects.size)
    }
}

private class InMemoryConfigRepository(private val initial: DilotConfig) : ConfigRepository {
    var savedConfig: DilotConfig? = null

    override fun load(): Result<DilotConfig> = Result.success(initial)
    override fun save(config: DilotConfig): Result<Unit> {
        savedConfig = config
        return Result.success(Unit)
    }
}
