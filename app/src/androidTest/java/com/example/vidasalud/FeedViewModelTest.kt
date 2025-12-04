package com.example.vidasalud

import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FeedRepositoryTest {

    private lateinit var fakeRepo: FakeFeedRepository

    @Before
    fun setup() {
        fakeRepo = FakeFeedRepository()
    }

    @Test
    fun `enviarcomentariosealmacenacorrectamente`() = runTest {
        val comentario = Comentario(
            id = "",
            userId = "user123",
            userName = "Pedro",
            mensaje = "Este es un comentario de prueba",
            fotoUrl = "url_fake"
        )

        val resultado = fakeRepo.enviarComentario(comentario)

        assertTrue(resultado.isSuccess)
        assertEquals(1, fakeRepo.comentariosSimulados.size)
        assertEquals("Este es un comentario de prueba", fakeRepo.comentariosSimulados.first().mensaje)
    }

    @Test
    fun `enviarcomentariofallacuandoenviarComentarioFalladoestrue`() = runTest {
        fakeRepo.enviarComentarioFallado = true

        val comentario = Comentario(
            userId = "userX",
            userName = "Test",
            mensaje = "No deberia guardarse"
        )

        val resultado = fakeRepo.enviarComentario(comentario)

        assertTrue(resultado.isFailure)
        assertEquals(0, fakeRepo.comentariosSimulados.size)
    }

    @Test
    fun `agregarlikeagregalikealcomentario`() = runTest {
        val comentarioId = "coment1"
        fakeRepo.likesSimulados[comentarioId] = mutableListOf()

        val resultado = fakeRepo.agregarLike(comentarioId, "user123")
        val likes = fakeRepo.obtenerLikes(comentarioId)

        assertTrue(resultado.isSuccess)
        assertEquals(1, likes.size)
        assertEquals("user123", likes.first().userId)
    }

    @Test
    fun `quitarlikeeliminacorrectamente`() = runTest {
        val comentarioId = "coment2"
        fakeRepo.likesSimulados[comentarioId] = mutableListOf(
            Like(userId = "userA", timestamp = 1000L),
            Like(userId = "userB", timestamp = 2000L)
        )

        val resultado = fakeRepo.quitarLike(comentarioId, "userA")
        val likes = fakeRepo.obtenerLikes(comentarioId)

        assertTrue(resultado.isSuccess)
        assertEquals(1, likes.size)
        assertEquals("userB", likes.first().userId)
    }

    @Test
    fun `escucharcomentariosretornalistasimulada`() {
        val comentario = Comentario(
            id = "1",
            userId = "abc",
            userName = "Juan",
            mensaje = "Hola!",
            fotoUrl = ""
        )

        fakeRepo.comentariosSimulados.add(comentario)

        fakeRepo.escucharComentarios { lista ->
            assertEquals(1, lista.size)
            assertEquals("Hola!", lista.first().mensaje)
        }
    }
}
