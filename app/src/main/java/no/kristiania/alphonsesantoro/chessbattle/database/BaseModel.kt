package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

open class BaseModel

interface BaseDao<T> {
    @Insert
    fun insert(vararg entity: T): List<Long>

    @Insert
    fun insert(entity: T): Long

    @Update
    fun update(entity: T): Int

    @Delete
    fun delete(entity: T)
}