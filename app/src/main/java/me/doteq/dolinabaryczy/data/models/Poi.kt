package me.doteq.dolinabaryczy.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Poi(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val quests: List<PoiQuest>,
    val distance: Float? = null
)