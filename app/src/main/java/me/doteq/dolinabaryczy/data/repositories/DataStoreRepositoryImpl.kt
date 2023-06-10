package me.doteq.dolinabaryczy.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val DATASTORE_NAME = "app_datastore"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
): DataStoreRepository {

    override fun getPoints(): Flow<Int> {
        return context.dataStore.data.map {
            it[intPreferencesKey("points")] ?: 0
        }
    }

    override suspend fun addPoints(value: Int) {
        val preferences = context.dataStore.data.first()
        val currentPoints = preferences[intPreferencesKey("points")]
        context.dataStore.edit {
            it[intPreferencesKey("points")] = value + (currentPoints ?: 0)
        }
    }

}