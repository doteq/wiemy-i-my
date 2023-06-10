package me.doteq.dolinabaryczy.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import me.doteq.dolinabaryczy.data.models.Answer
import java.util.Date

class Converters {

    @TypeConverter
    fun listToJson(value: List<Answer>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Answer>::class.java).toList()


    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}