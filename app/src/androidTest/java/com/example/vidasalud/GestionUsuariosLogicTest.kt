package com.example.vidasalud

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// =============================
// MODELO FAKE PARA USUARIOS
// =============================
data class FakeUsuarioPerfil(
    val uid: String,
    val correo: String,
    val nombre: String,
    val rol: String,
    val clave: String = "123456",
    val fotoUrl: String? = null
)

// =============================
// REPOSITORIO SIMULADO PARA USUARIOS
// =============================
class FakeUsuariosRepository {
    private val usuarios = mutableMapOf<String, FakeUsuarioPerfil>()

    fun agregarUsuario(usuario: FakeUsuarioPerfil) {
        usuarios[usuario.uid] = usuario
    }

    fun obtenerUsuarios(): List<FakeUsuarioPerfil> {
        return usuarios.values.toList()
    }

    fun crearUsuario(correo: String, nombre: String, rol: String, clave: String = "123456"): FakeUsuarioPerfil? {
        if (usuarios.values.any { it.correo == correo }) return null

        val newUser = FakeUsuarioPerfil(
            uid = "uid_${usuarios.size + 1}",
            correo = correo,
            nombre = nombre,
            rol = rol,
            clave = clave
        )

        usuarios[newUser.uid] = newUser
        return newUser
    }

    fun actualizarUsuario(uid: String, nombre: String? = null, rol: String? = null): Boolean {
        val user = usuarios[uid] ?: return false

        val updatedUser = user.copy(
            nombre = nombre ?: user.nombre,
            rol = rol ?: user.rol
        )

        usuarios[uid] = updatedUser
        return true
    }

    fun eliminarUsuario(uid: String): Boolean {
        return usuarios.remove(uid) != null
    }

    fun countUsers(): Int = usuarios.size

    fun esUltimoAdmin(uid: String): Boolean {
        val adminCount = usuarios.values.count { it.rol == "admin" }
        val esAdmin = usuarios[uid]?.rol == "admin"
        return esAdmin && adminCount == 1
    }
}

// =============================
// TESTS UNITARIOS DEL PERFIL/USUARIOS
// =============================
class GestionUsuariosLogicTest {

    private lateinit var repo: FakeUsuariosRepository

    @Before
    fun setup() {
        repo = FakeUsuariosRepository()
    }

    @Test
    fun `obtenerusuariosretornalistacorrectamente`() {
        repo.agregarUsuario(FakeUsuarioPerfil("1", "juan@test.com", "Juan", "cliente"))
        repo.agregarUsuario(FakeUsuarioPerfil("2", "ana@test.com", "Ana", "admin"))

        val usuarios = repo.obtenerUsuarios()

        assertEquals(2, usuarios.size)
        assertEquals("Juan", usuarios.find { it.uid == "1" }?.nombre)
        assertEquals("Ana", usuarios.find { it.uid == "2" }?.nombre)
    }

    @Test
    fun `crearusuarioagregacorrectamente`() {
        val nuevo = repo.crearUsuario("nuevo@test.com", "Pedro", "cliente")

        assertNotNull(nuevo)
        assertEquals(1, repo.countUsers())
        assertEquals("Pedro", nuevo?.nombre)
        assertEquals("nuevo@test.com", nuevo?.correo)
    }

    @Test
    fun `crearusuariofallasicorreoyaexiste`() {
        repo.crearUsuario("existente@test.com", "Juan", "cliente")
        val fallo = repo.crearUsuario("existente@test.com", "Pedro", "cliente")

        assertNull(fallo)
        assertEquals(1, repo.countUsers())
    }

    @Test
    fun `actualizarusuarionombrecorrectamente`() {
        repo.agregarUsuario(FakeUsuarioPerfil("1", "test@test.com", "Original", "cliente"))

        val actualizado = repo.actualizarUsuario("1", nombre = "Actualizado")

        assertTrue(actualizado)
        val usuario = repo.obtenerUsuarios().first()
        assertEquals("Actualizado", usuario.nombre)
    }

    @Test
    fun `actualizarusuarorolcorrectamente`() {
        repo.agregarUsuario(FakeUsuarioPerfil("1", "test@test.com", "Juan", "cliente"))

        val actualizado = repo.actualizarUsuario("1", rol = "admin")

        assertTrue(actualizado)
        val usuario = repo.obtenerUsuarios().first()
        assertEquals("admin", usuario.rol)
    }

    @Test
    fun `eliminarusuarioremuevecorrectamente`() {
        repo.agregarUsuario(FakeUsuarioPerfil("1", "test@test.com", "Juan", "cliente"))
        assertEquals(1, repo.countUsers())

        val eliminado = repo.eliminarUsuario("1")

        assertTrue(eliminado)
        assertEquals(0, repo.countUsers())
    }

    @Test
    fun `nodegradarultimoadmin`() {
        repo.agregarUsuario(FakeUsuarioPerfil("1", "admin@test.com", "Admin", "admin"))

        val esUltimo = repo.esUltimoAdmin("1")

        assertTrue(esUltimo)
    }
}