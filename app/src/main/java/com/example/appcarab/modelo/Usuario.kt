package com.example.appcarab.modelo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuario", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(defaultValue = "'Estudiante'") val nombre: String,
    val email: String,
    @ColumnInfo(name = "password") val passwordHash: String,
    val passwordSalt: String,
    val role: String = "ESTUDIANTE"
)
