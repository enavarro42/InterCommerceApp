package com.inter.intercommerceapp.data.local.db

import androidx.room.TypeConverter

private const val IMAGES_SEPARATOR = "||"

class Converters {

    @TypeConverter
    fun fromImages(images: List<String>): String = images.joinToString(IMAGES_SEPARATOR)

    @TypeConverter
    fun toImages(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(IMAGES_SEPARATOR)
}
