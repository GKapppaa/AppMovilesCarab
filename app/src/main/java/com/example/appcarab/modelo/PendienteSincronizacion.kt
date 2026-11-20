package com.example.appcarab.modelo

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(
    tableName = "pendiente_sincronizacion",
    primaryKeys = ["tipo", "clave"],
)
data class PendienteSincronizacion(
    val tipo: String,
    val clave: String,
    val contenido: String,
    val actualizadoEn: Long = System.currentTimeMillis(),
)

@Dao
interface PendienteSincronizacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(pendiente: PendienteSincronizacion)

    @Query("SELECT * FROM pendiente_sincronizacion ORDER BY actualizadoEn LIMIT :limite")
    suspend fun pendientes(limite: Int = 50): List<PendienteSincronizacion>

    @Query("DELETE FROM pendiente_sincronizacion WHERE tipo = :tipo AND clave = :clave AND actualizadoEn = :actualizadoEn AND contenido = :contenido")
    suspend fun confirmar(tipo: String, clave: String, actualizadoEn: Long, contenido: String)

    @Query("SELECT COUNT(*) FROM pendiente_sincronizacion")
    suspend fun cantidad(): Int
}
