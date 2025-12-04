package com.example.vidasalud

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// =============================
// MODELO FAKE DE USUARIO
// =============================
data class FakeUsuario(
    val uid: String,
    val correo: String,
    val nombre: String,
    val clave: String,
    val rol: String = "cliente"
)

// =============================
// REPOSITORIO SIMULADO LOCAL
// =============================
class FakeAuthRepository {
    private val usuarios = mutableMapOf<String, FakeUsuario>()

    fun register(correo: String, clave: String, nombre: String): FakeUsuario? {
        if (correo.isBlank() || clave.isBlank() || nombre.isBlank()) return null
        if (usuarios.values.any { it.correo == correo }) return null // correo repetido

        val newUser = FakeUsuario(
            uid = "uid_${usuarios.size + 1}",
            correo = correo,
            nombre = nombre,
            clave = clave
        )

        usuarios[newUser.uid] = newUser
        return newUser
    }

    fun login(correo: String, clave: String): FakeUsuario? {
        return usuarios.values.find { it.correo == correo && it.clave == clave }
    }

    fun countUsers(): Int = usuarios.size
}

// =============================
// TESTS UNITARIOS DEL REGISTRO/LOGIN
// =============================
class AuthLogicTest {

    private lateinit var repo: FakeAuthRepository

    @Before
    fun setup() {
        repo = FakeAuthRepository()
    }

    // ---------- REGISTRO ----------
    @Test
    fun `registrarusuariocorrecto`() {
        val user = repo.register("test@mail.com", "123456", "Juan")
        assertNotNull(user)
        assertEquals(1, repo.countUsers())
        assertEquals("Juan", user?.nombre)
    }

    @Test
    fun `registrofallaconcamposvacios`() {
        val user = repo.register("", "", "")
        assertNull(user)
        assertEquals(0, repo.countUsers())
    }

    @Test
    fun `registrofallasielcorreoyaexiste`() {
        repo.register("test@mail.com", "1234", "Ana")
        val fail = repo.register("test@mail.com", "2222", "Pedro")

        assertNull(fail)
        assertEquals(1, repo.countUsers())
    }

    // ---------- LOGIN ----------
    @Test
    fun `loginexitosocondatoscorrectos`() {
        repo.register("user@mail.com", "1234", "Carlos")

        val loginUser = repo.login("user@mail.com", "1234")

        assertNotNull(loginUser)
        assertEquals("Carlos", loginUser?.nombre)
    }

    @Test
    fun `loginfallaconclaveincorrecta`() {
        repo.register("user@mail.com", "1234", "Carlos")

        val loginUser = repo.login("user@mail.com", "9999")

        assertNull(loginUser)
    }

    @Test
    fun `loginfallasielcorreonoexiste`() {
        val loginUser = repo.login("no@existe.com", "1234")
        assertNull(loginUser)
    }
}
