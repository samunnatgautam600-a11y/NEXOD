package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.QuizQuestion
import com.example.data.model.WebSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromWebSourceList(value: List<WebSource>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, WebSource::class.java)
        return moshi.adapter<List<WebSource>>(type).toJson(value)
    }

    @TypeConverter
    fun toWebSourceList(value: String?): List<WebSource> {
        if (value.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, WebSource::class.java)
        return moshi.adapter<List<WebSource>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromQuizQuestionList(value: List<QuizQuestion>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
        return moshi.adapter<List<QuizQuestion>>(type).toJson(value)
    }

    @TypeConverter
    fun toQuizQuestionList(value: String?): List<QuizQuestion> {
        if (value.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, QuizQuestion::class.java)
        return moshi.adapter<List<QuizQuestion>>(type).fromJson(value) ?: emptyList()
    }
}
