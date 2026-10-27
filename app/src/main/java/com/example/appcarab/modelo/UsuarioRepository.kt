package com.example.appcarab.modelo

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun registrarUsuario(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val correoNormalizado = email.trim().lowercase()
        if (usuarioDao.buscarPorEmail(correoNormalizado) != null) {
            false
        } else {
            val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
            usuarioDao.insertar(
                Usuario(
                    email = correoNormalizado,
                    passwordHash = hashPassword(password, salt),
                    passwordSalt = salt.toHex(),
                )
            )
            true
        }
    }

    suspend fun validarSesion(email: String, password: String): Usuario? = withContext(Dispatchers.IO) {
        val usuario = usuarioDao.buscarPorEmail(email.trim().lowercase())
        when {
            usuario == null -> null
            usuario.passwordSalt.isBlank() && usuario.passwordHash == password -> {
                val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
                val actualizado = usuario.copy(passwordHash = hashPassword(password, salt), passwordSalt = salt.toHex())
                usuarioDao.actualizar(actualizado)
                actualizado
            }
            usuario.passwordSalt.isBlank() -> null
            else -> usuario.takeIf { hashPassword(password, it.passwordSalt.hexToBytes()) == it.passwordHash }
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded.toHex()
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private companion object {
        const val SALT_BYTES = 16
        const val ITERATIONS = 120_000
        const val HASH_BITS = 256
    }
}
