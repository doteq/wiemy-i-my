package me.doteq.dolinabaryczy.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.doteq.dolinabaryczy.data.models.UserAnswerList

@Dao
interface AnswersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerList(answers: UserAnswerList)

    @Query("SELECT * FROM answers")
    fun getAll(): Flow<List<UserAnswerList>>

    @Query("DELETE FROM answers WHERE questId in (:idList)")
    suspend fun deleteBulk(idList: List<String>)
}