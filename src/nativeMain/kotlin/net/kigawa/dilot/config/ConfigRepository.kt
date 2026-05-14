package net.kigawa.dilot.config

interface ConfigRepository {
    fun load(): Result<DilotConfig>
    fun save(config: DilotConfig): Result<Unit>
}
