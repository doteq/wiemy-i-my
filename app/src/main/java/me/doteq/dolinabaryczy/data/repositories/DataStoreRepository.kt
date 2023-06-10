package me.doteq.dolinabaryczy.data.repositories

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

    fun getPoints(): Flow<Int>

    suspend fun addPoints(value: Int)

}