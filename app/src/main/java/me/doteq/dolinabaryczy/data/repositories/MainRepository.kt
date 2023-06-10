package me.doteq.dolinabaryczy.data.repositories

import kotlinx.coroutines.flow.Flow
import me.doteq.dolinabaryczy.data.models.UserAnswerList

interface MainRepository {

    suspend fun insertAnswerList(answers: UserAnswerList)

    fun getAllAnswerLists(): Flow<List<UserAnswerList>>

    suspend fun deleteAnswerLists(idList: List<String>)
}