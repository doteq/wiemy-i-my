package me.doteq.dolinabaryczy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.doteq.dolinabaryczy.data.dao.AnswersDao
import me.doteq.dolinabaryczy.data.models.UserAnswerList

@Database(
    entities = [UserAnswerList::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AnswersDatabase : RoomDatabase() {
    abstract val answersDao: AnswersDao
}