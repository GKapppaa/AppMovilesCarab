package com.example.appcarab.modelo

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    // Obtiene la lista completa de usuarios registrados
    suspend fun listarUsuarios(): List<Usuario> {
        return usuarioDao.listar()
    }

    // Inserta un nuevo usuario es para el Registro de los usuarios
    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    // Valida las credenciales para el inicio de sesión
    suspend fun login(email: String, password: String): Usuario? {
        return usuarioDao.validarLogin(email, password)
    }
}