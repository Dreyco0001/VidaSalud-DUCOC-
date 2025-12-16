package com.example.vidasalud.repository

import com.example.vidasalud.model.NotificacionPlan
import com.example.vidasalud.model.Plan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificacionesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val notificacionesCol = db.collection("notificaciones_planes")
    private val usuariosCol = db.collection("usuario")

    private companion object {
        const val EXPIRACION_42_HORAS = 42 * 60 * 60 * 1000L
    }

    // --------------------------------------------------
    // TOMAR PLAN
    // --------------------------------------------------
    suspend fun tomarPlan(
        plan: Plan,
        rolUsuario: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val usuarioSnap = usuariosCol.document(user.uid).get().await()
            val nombreUsuario = usuarioSnap.getString("nombre") ?: "Desconocido"

            val doc = notificacionesCol.document()
            val ahora = System.currentTimeMillis()

            val notificacion = NotificacionPlan(
                id = doc.id,
                planId = plan.id,
                nombrePlan = plan.nombre,
                descripcionPlan = plan.objetivo,
                imagenPlanUrl = plan.imagenUrl,
                precio = plan.precio,
                comidaRecomendada = plan.comidaRecomendada,
                imagenComidaUrl = plan.imagenComidaUrl,
                userId = user.uid,
                userRol = rolUsuario,
                fechaToma = ahora,
                estado = "activo",
                nombreUsuario = nombreUsuario
            )

            doc.set(notificacion).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------
    // OBTENER NOTIFICACIONES
    // ADMIN: TODAS
    // CLIENTE: SOLO LAS SUYAS + NO EXPIRADAS (42H)
    // --------------------------------------------------
    suspend fun obtenerNotificaciones(rol: String): List<NotificacionPlan> {
        val user = auth.currentUser ?: return emptyList()
        val ahora = System.currentTimeMillis()

        val querySnapshot = if (rol == "admin") {
            notificacionesCol.get().await()
        } else {
            notificacionesCol
                .whereEqualTo("userId", user.uid)
                .get()
                .await()
        }

        return querySnapshot.toObjects(NotificacionPlan::class.java)
            .filter { noti ->
                rol == "admin" || (ahora - noti.fechaToma) <= EXPIRACION_42_HORAS
            }
            .sortedByDescending { it.fechaToma }
    }

    // --------------------------------------------------
    // CANCELAR PLAN (12 HORAS)
    // --------------------------------------------------
    suspend fun cancelarPlan(id: String, rol: String): Result<Unit> {
        return try {
            val doc = notificacionesCol.document(id)
            val noti = doc.get().await()
                .toObject(NotificacionPlan::class.java)
                ?: return Result.failure(Exception("Notificación no encontrada"))

            val ahora = System.currentTimeMillis()
            val limite = 12 * 60 * 60 * 1000L

            if (rol != "admin" && ahora - noti.fechaToma > limite) {
                return Result.failure(
                    Exception("No se puede cancelar después de 12 horas")
                )
            }

            doc.update("estado", "cancelado").await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------
    // ELIMINAR NOTIFICACIÓN
    // ADMIN: CUALQUIERA
    // CLIENTE: SOLO LA SUYA
    // --------------------------------------------------
    suspend fun eliminarNotificacion(
        id: String,
        rol: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val doc = notificacionesCol.document(id)
            val noti = doc.get().await()
                .toObject(NotificacionPlan::class.java)
                ?: return Result.failure(Exception("Notificación no encontrada"))

            if (rol != "admin" && noti.userId != user.uid) {
                return Result.failure(Exception("Acción no autorizada"))
            }

            doc.delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
