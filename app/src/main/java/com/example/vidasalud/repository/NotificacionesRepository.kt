package com.example.vidasalud.repository

import com.example.vidasalud.model.NotificacionPlan
import com.example.vidasalud.model.Plan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificacionesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("notificaciones_planes")

    // --------------------------------------------------
    // TOMAR PLAN (CLIENTE O ADMIN)
    // --------------------------------------------------
    suspend fun tomarPlan(
        plan: Plan,
        rolUsuario: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))

            val doc = collection.document()
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
                estado = "activo"
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
    // CLIENTE: SOLO LAS SUYAS
    // --------------------------------------------------
    suspend fun obtenerNotificaciones(rol: String): List<NotificacionPlan> {
        val user = auth.currentUser ?: return emptyList()

        val query = if (rol == "admin") {
            collection
        } else {
            collection.whereEqualTo("userId", user.uid)
        }

        return query.get().await().toObjects(NotificacionPlan::class.java)
    }

    // --------------------------------------------------
    // CANCELAR PLAN (ANTES DE 12 HORAS)
    // --------------------------------------------------
    suspend fun cancelarPlan(id: String, rol: String): Result<Unit> {
        return try {
            val doc = collection.document(id)
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
    // ELIMINAR NOTIFICACIÓN (SOLO ADMIN)
    // --------------------------------------------------
    suspend fun eliminarNotificacion(id: String, rol: String): Result<Unit> {
        return try {
            if (rol != "admin") {
                return Result.failure(Exception("Acción no autorizada"))
            }

            collection.document(id).delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
