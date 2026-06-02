package com.example.vidasalud

import com.example.vidasalud.model.Comentario
import com.example.vidasalud.ui.screens.social.puedeEditarLocal
import org.junit.Assert.*
import org.junit.Test

class PuedeEditarLocalTest {

    @Test
    fun adminSiemprePuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "otro_usuario",
            userName = "Otro",
            timestamp = System.currentTimeMillis() - 30 * 60 * 1000 // hace 30 minutos
        )

        val puedeEditar = puedeEditarLocal(comentario, "admin_id", true)

        assertTrue(puedeEditar)
    }

    @Test
    fun propioComentarioDentroDe10MinutosPuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "usuario123",
            userName = "Usuario",
            timestamp = System.currentTimeMillis() - 5 * 60 * 1000 // hace 5 minutos
        )

        val puedeEditar = puedeEditarLocal(comentario, "usuario123", false)

        assertTrue(puedeEditar)
    }

    @Test
    fun propioComentarioFueraDe10MinutosNoPuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "usuario123",
            userName = "Usuario",
            timestamp = System.currentTimeMillis() - 12 * 60 * 1000 // hace 12 minutos
        )

        val puedeEditar = puedeEditarLocal(comentario, "usuario123", false)

        assertFalse(puedeEditar)
    }

    @Test
    fun comentarioDeOtroUsuarioNoPuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "otro_usuario",
            userName = "Otro",
            timestamp = System.currentTimeMillis() - 1 * 60 * 1000 // hace 1 minuto
        )

        val puedeEditar = puedeEditarLocal(comentario, "usuario123", false)

        assertFalse(puedeEditar)
    }

    @Test
    fun adminPuedeEditarComentarioViejo() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "otro_usuario",
            userName = "Otro",
            timestamp = System.currentTimeMillis() - 60 * 60 * 1000 // hace 1 hora
        )

        val puedeEditar = puedeEditarLocal(comentario, "admin", true)

        assertTrue(puedeEditar)
    }

    @Test
    fun tiempoExacto10MinutosPuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "usuario123",
            userName = "Usuario",
            timestamp = System.currentTimeMillis() - 10 * 60 * 1000 // exactamente 10 minutos
        )

        val puedeEditar = puedeEditarLocal(comentario, "usuario123", false)

        assertTrue(puedeEditar)
    }

    @Test
    fun tiempoExacto10MinutosUnMilisegundoNoPuedeEditar() {
        val comentario = Comentario(
            id = "1",
            mensaje = "Mensaje",
            userId = "usuario123",
            userName = "Usuario",
            timestamp = System.currentTimeMillis() - (10 * 60 * 1000 + 1) // 10 minutos y 1 ms
        )

        val puedeEditar = puedeEditarLocal(comentario, "usuario123", false)

        assertFalse(puedeEditar)
    }
}