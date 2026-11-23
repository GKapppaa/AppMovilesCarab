package com.example.appcarab

import com.example.appcarab.modelo.ResultadoSync
import com.example.appcarab.modelo.Sincronizador

// Sincronización falsa: solo cuenta llamadas y devuelve un resultado fijo.
class FakeSincronizador : Sincronizador {
    var llamadas = 0
    var sincronizado = true
    val tipos = mutableListOf<String>()
    val claves = mutableListOf<String>()

    override suspend fun sincronizar(
        tipo: String,
        clave: String,
        contenido: String,
    ): ResultadoSync {
        llamadas++
        tipos.add(tipo)
        claves.add(clave)
        return ResultadoSync(sincronizado, "resultado de prueba")
    }
}
