package net.kigawa.dilot.config

import kotlinx.serialization.Serializable

@Serializable
data class DilotConfig(
    val version: Int = 1,
    val projects: List<ProjectEntry> = emptyList(),
)

@Serializable
data class ProjectEntry(
    val name: String,
    val coreUrl: String,
    val createdAt: String,
)
