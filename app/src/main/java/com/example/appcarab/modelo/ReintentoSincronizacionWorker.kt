package com.example.appcarab.modelo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReintentoSincronizacionWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val pendingDao = database.pendienteSincronizacionDao()
        val api = ApiRestRepository()
        val sincronizacion = SincronizacionPendiente(applicationContext, pendingDao, api)
        return if (sincronizacion.procesarPendientes()) Result.success() else Result.retry()
    }
}
