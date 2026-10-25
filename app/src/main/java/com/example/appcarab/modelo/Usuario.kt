package com.example.appcarab.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey

// Crea una tabla llamada "personas".
@Entity(tableName = "usuario")
data class Usuario (

    // Identificador único autoincremental.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // email de usuario
    val email: String,
    // contraseña del usuario
    val password: String,
    // aqui se coloca el rol si es alumno o profesor
    val role: String
)

/*
    Equivale conceptualmente a crear esta tabla SQL:

    CREATE TABLE personas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    edad INTEGER NOT NULL
    );
*/