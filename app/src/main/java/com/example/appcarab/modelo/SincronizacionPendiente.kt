package com.example.appcarab.modelo

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

class SincronizacionPendiente(
    context: Context,
    private val dao: PendienteSincronizacionDao,
    private val api: ApiRestRepository,
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    suspend fun sincronizar(tipo: String, clave: String, contenido: String): ResultadoSync {
        val pendiente = PendienteSincronizacion(tipo, clave, contenido)
        dao.guardar(pendiente)
        val resultado = api.sincronizar(tipo, clave, contenido)
        if (resultado.sincronizado) {
            confirmar(pendiente)
        } else {
            programarReintento()
        }
        return resultado
    }

    suspend fun verificarYReintentar(): Result<ApiHealth> {
        val resultado = api.health()
        if (resultado.isSuccess && dao.cantidad() > 0) {
            if (!procesarPendientes()) programarReintento()
        }
        return resultado
    }

    suspend fun procesarPendientes(): Boolean {
        try {
            for (pendiente in dao.pendientes()) {
                val resultado = api.sincronizar(pendiente.tipo, pendiente.clave, pendiente.contenido)
                if (!resultado.sincronizado) return false
                confirmar(pendiente)
            }
            return dao.cantidad() == 0
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return false
        }
    }

    private suspend fun confirmar(pendiente: PendienteSincronizacion) {
        dao.confirmar(
            pendiente.tipo,
            pendiente.clave,
            pendiente.actualizadoEn,
            pendiente.contenido,
        )
    }

    suspend fun programarSiHayPendientes() {
        if (dao.cantidad() > 0) programarReintento()
    }

    fun programarReintento() {
        val request = OneTimeWorkRequestBuilder<ReintentoSincronizacionWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            NOMBRE_TRABAJO,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    companion object {
        const val NOMBRE_TRABAJO = "sincronizacion-api-pendiente"
    }
}
