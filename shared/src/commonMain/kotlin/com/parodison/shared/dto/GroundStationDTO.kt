package com.parodison.shared.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class GroundStationDTO(
    val id: Uuid,
    val name: String,
)