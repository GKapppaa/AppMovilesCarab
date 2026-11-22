package com.example.appcarab

import com.example.appcarab.modelo.Usuario
import com.example.appcarab.modelo.UsuarioDao

// DAO falso: guarda los usuarios en una lista y cuenta las consultas.
class FakeUsuarioDao : UsuarioDao {
    val usuarios = mutableListOf<Usuario>()
    var consultasPorEmail = 0
    var consultasPorId = 0
    private var siguienteId = 1

    override suspend fun insertar(usuario: Usuario): Long {
        val guardado = if (usuario.id == 0) usuario.copy(id = siguienteId++) else usuario
        usuarios.add(guardado)
        return guardado.id.toLong()
    }

    override suspend fun actualizar(usuario: Usuario) {
        val indice = usuarios.indexOfFirst { it.id == usuario.id }
        if (indice >= 0) usuarios[indice] = usuario
    }

    override suspend fun buscarPorEmail(email: String): Usuario? {
        consultasPorEmail++
        return usuarios.firstOrNull { it.email == email }
    }

    override suspend fun buscarPorId(usuarioId: Int): Usuario? {
        consultasPorId++
        return usuarios.firstOrNull { it.id == usuarioId }
    }

    override suspend fun actualizarPerfil(usuarioId: Int, nombre: String, email: String): Int {
        val indice = usuarios.indexOfFirst { it.id == usuarioId }
        if (indice < 0) return 0
        usuarios[indice] = usuarios[indice].copy(nombre = nombre, email = email)
        return 1
    }
}
