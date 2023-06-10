package me.doteq.dolinabaryczy.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import me.doteq.dolinabaryczy.data.dao.AnswersDao
import me.doteq.dolinabaryczy.data.models.UserAnswerList

class MainRepositoryImpl(
    private val answersDao: AnswersDao,
) : MainRepository {

    override suspend fun insertAnswerList(answers: UserAnswerList) {
        answersDao.insertAnswerList(answers)
    }

    override fun getAllAnswerLists(): Flow<List<UserAnswerList>> {
        return answersDao.getAll()
    }

    override suspend fun deleteAnswerLists(idList: List<String>) {
        answersDao.deleteBulk(idList)
    }
}