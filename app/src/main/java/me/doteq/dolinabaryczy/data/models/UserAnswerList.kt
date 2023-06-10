package me.doteq.dolinabaryczy.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "answers")
data class UserAnswerList(
    @PrimaryKey
    val questId: String,
    val answers: List<Answer>,
    val lastTryTime: Date
)

enum class Answer(val value: Int) {
    ANSWER_CORRECT(1),
    ANSWER_HALF_POINTS(2),
    ANSWER_INCORRECT(3),
    ANSWER_NONE(0);
}
