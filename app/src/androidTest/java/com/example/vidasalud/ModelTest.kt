package com.example.vidasalud


import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.example.vidasalud.model.Usuario
import com.google.firebase.firestore.IgnoreExtraProperties
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Modifier

class ModelTest {

    // ---------- Comentario ----------
    @Test
    fun comentario_constructor_defaults() {
        val c = Comentario()
        assertEquals("", c.id)
        assertEquals("", c.userId)
        assertEquals("", c.userName)
        assertEquals("", c.mensaje)
        assertEquals("", c.fotoUrl)
        assertTrue(c.timestamp > 0)
    }

    @Test
    fun comentario_constructor_con_valores() {
        val now = System.currentTimeMillis()
        val c = Comentario(
            id = "c1",
            userId = "u1",
            userName = "Ana",
            mensaje = "Hola",
            fotoUrl = "http://foto",
            timestamp = now
        )
        assertEquals("c1", c.id)
        assertEquals("u1", c.userId)
        assertEquals("Ana", c.userName)
        assertEquals("Hola", c.mensaje)
        assertEquals("http://foto", c.fotoUrl)
        assertEquals(now, c.timestamp)
    }

    @Test
    fun comentario_copy() {
        val c1 = Comentario(id = "c1", mensaje = "Original")
        val c2 = c1.copy(mensaje = "Modificado")
        assertEquals("c1", c2.id)
        assertEquals("Modificado", c2.mensaje)
        assertNotEquals(c1, c2)
    }

    @Test
    fun comentario_equals_hashcode() {
        val c1 = Comentario(id = "1", mensaje = "Hola")
        val c2 = Comentario(id = "1", mensaje = "Hola")
        val c3 = Comentario(id = "2", mensaje = "Hola")
        assertEquals(c1, c2)
        assertNotEquals(c1, c3)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    // ---------- Like ----------
    @Test
    fun like_constructor_defaults() {
        val l = Like()
        assertEquals("", l.id)
        assertEquals("", l.userId)
        assertTrue(l.timestamp > 0)
    }

    @Test
    fun like_constructor_con_valores() {
        val ts = 123456789L
        val l = Like(id = "L1", userId = "userX", timestamp = ts)
        assertEquals("L1", l.id)
        assertEquals("userX", l.userId)
        assertEquals(ts, l.timestamp)
    }

    @Test
    fun like_copy() {
        val l1 = Like(id = "1", userId = "a", timestamp = 100)
        val l2 = l1.copy(userId = "b")
        assertEquals("1", l2.id)
        assertEquals("b", l2.userId)
        assertEquals(100, l2.timestamp)
    }

    @Test
    fun like_equals_hashcode() {
        val l1 = Like(id = "x", userId = "u", timestamp = 99)
        val l2 = Like(id = "x", userId = "u", timestamp = 99)
        val l3 = Like(id = "y", userId = "u", timestamp = 99)
        assertEquals(l1, l2)
        assertNotEquals(l1, l3)
    }

    // ---------- Usuario ----------
    @Test
    fun usuario_constructor_defaults() {
        val u = Usuario()
        assertEquals("", u.uid)
        assertEquals("", u.correo)
        assertEquals("", u.nombre)
        assertEquals("", u.rol)
        assertNull(u.clave)
        assertNull(u.fotoUrl)
        assertNull(u.estatura)
        // ... otros campos nulos
    }

    @Test
    fun usuario_constructor_con_valores() {
        val u = Usuario(
            uid = "uid123",
            correo = "test@example.com",
            nombre = "Carlos",
            rol = "admin",
            clave = "secret",
            fechaRegistro = "2025-01-01",
            fotoUrl = "http://foto.jpg",
            estatura = 1.75f,
            sexo = "M",
            pesoActual = 80.5f,
            fechaPesoActual = "2025-06-01",
            pesoAnterior = 82.0f,
            fechaPesoAnterior = "2025-05-01",
            pesoAnteanterior = 83.5f,
            fechaPesoAnteanterior = "2025-04-01"
        )
        assertEquals("uid123", u.uid)
        assertEquals("test@example.com", u.correo)
        assertEquals("Carlos", u.nombre)
        assertEquals("admin", u.rol)
        assertEquals("secret", u.clave)
        assertEquals("http://foto.jpg", u.fotoUrl)
        assertEquals(1.75f, u.estatura)
        assertEquals(80.5f, u.pesoActual)
    }

    @Test
    fun usuario_copy() {
        val u1 = Usuario(uid = "u1", nombre = "Juan", pesoActual = 70f)
        val u2 = u1.copy(pesoActual = 68f, nombre = "Juanito")
        assertEquals("u1", u2.uid)
        assertEquals("Juanito", u2.nombre)
        assertEquals(68f, u2.pesoActual)
    }

    @Test
    fun usuario_equals_hashcode() {
        val u1 = Usuario(uid = "1", correo = "a@b.com")
        val u2 = Usuario(uid = "1", correo = "a@b.com")
        val u3 = Usuario(uid = "2", correo = "a@b.com")
        assertEquals(u1, u2)
        assertNotEquals(u1, u3)
        assertEquals(u1.hashCode(), u2.hashCode())
    }

    @Test
    fun usuario_tiene_anotacion_IgnoreExtraProperties() {
        val annotation = Usuario::class.java.getAnnotation(IgnoreExtraProperties::class.java)
        assertNotNull("Usuario debe tener @IgnoreExtraProperties para Firestore", annotation)
    }

    @Test
    fun usuario_propiedades_tienen_nombres_correctos_para_firestore() {
        val camposEsperados = setOf(
            "uid", "correo", "nombre", "rol", "clave", "fechaRegistro", "fotoUrl",
            "estatura", "sexo", "pesoActual", "fechaPesoActual", "pesoAnterior",
            "fechaPesoAnterior", "pesoAnteanterior", "fechaPesoAnteanterior"
        )
        val propiedades = Usuario::class.java.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()
        assertEquals(camposEsperados, propiedades)
    }

    @Test
    fun comentario_propiedades_correctas() {
        val campos = setOf("id", "userId", "userName", "mensaje", "fotoUrl", "timestamp")
        val props = Comentario::class.java.declaredFields.map { it.name }.toSet()
        assertEquals(campos, props)
    }

    @Test
    fun like_propiedades_correctas() {
        val campos = setOf("id", "userId", "timestamp")
        val props = Like::class.java.declaredFields.map { it.name }.toSet()
        assertEquals(campos, props)
    }
}
