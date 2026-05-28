package com.example.vidasalud

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// =============================
// MODELO FAKE PARA PLANES
// =============================
data class FakePlan(
    val id: String = "",
    val nombre: String,
    val duracion: Int,
    val nivel: String,
    val objetivo: String,
    val precio: Int,
    val comidaRecomendada: String,
    val imagenComidaUrl: String,
    val imagenUrl: String = ""
)

// =============================
// REPOSITORIO SIMULADO PARA PLANES
// =============================
class FakePlanesRepository {
    private val planes = mutableMapOf<String, FakePlan>()

    fun agregarPlan(plan: FakePlan) {
        planes[plan.id] = plan
    }

    fun obtenerPlanes(): List<FakePlan> {
        return planes.values.toList()
    }

    fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        precio: Int,
        comidaRecomendada: String,
        imagenComidaUrl: String,
        imagenUrl: String? = null
    ): FakePlan? {
        if (planes.values.any { it.nombre == nombre }) return null

        val newId = "plan_${planes.size + 1}"
        val newPlan = FakePlan(
            id = newId,
            nombre = nombre,
            duracion = duracion,
            nivel = nivel,
            objetivo = objetivo,
            precio = precio,
            comidaRecomendada = comidaRecomendada,
            imagenComidaUrl = imagenComidaUrl,
            imagenUrl = imagenUrl ?: ""
        )

        planes[newId] = newPlan
        return newPlan
    }

    fun actualizarPlan(id: String, nombre: String? = null, duracion: Int? = null, precio: Int? = null): Boolean {
        val plan = planes[id] ?: return false

        val updatedPlan = plan.copy(
            nombre = nombre ?: plan.nombre,
            duracion = duracion ?: plan.duracion,
            precio = precio ?: plan.precio
        )

        planes[id] = updatedPlan
        return true
    }

    fun eliminarPlan(id: String): Boolean {
        return planes.remove(id) != null
    }

    fun countPlanes(): Int = planes.size
}

// =============================
// TESTS UNITARIOS DE PLANES/CURSOS
// =============================
class GestionPlanesLogicTest {

    private lateinit var repo: FakePlanesRepository

    @Before
    fun setup() {
        repo = FakePlanesRepository()
    }

    @Test
    fun `obtenerplanesretornalistacorrectamente`() {
        repo.agregarPlan(FakePlan(id = "1", nombre = "Yoga", duracion = 30, nivel = "Principiante", objetivo = "Flexibilidad", precio = 100, comidaRecomendada = "Quinoa", imagenComidaUrl = "url1"))
        repo.agregarPlan(FakePlan(id = "2", nombre = "CrossFit", duracion = 45, nivel = "Avanzado", objetivo = "Fuerza", precio = 150, comidaRecomendada = "Pollo", imagenComidaUrl = "url2"))

        val planes = repo.obtenerPlanes()

        assertEquals(2, planes.size)
        assertEquals("Yoga", planes.find { it.id == "1" }?.nombre)
        assertEquals("CrossFit", planes.find { it.id == "2" }?.nombre)
    }

    @Test
    fun `crearplanagregacorrectamenteconcomidarecomendada`() {
        val nuevo = repo.crearPlan(
            nombre = "Plan Saludable",
            duracion = 60,
            nivel = "Intermedio",
            objetivo = "Perder peso",
            precio = 200,
            comidaRecomendada = "Ensalada de quinoa",
            imagenComidaUrl = "https://example.com/comida.jpg"
        )

        assertNotNull(nuevo)
        assertEquals(1, repo.countPlanes())
        assertEquals("Plan Saludable", nuevo?.nombre)
        assertEquals("Ensalada de quinoa", nuevo?.comidaRecomendada)
    }

    @Test
    fun `crearplanfallasinombreduplicado`() {
        repo.crearPlan(nombre = "Plan Unico", duracion = 30, nivel = "Básico", objetivo = "Salud", precio = 100, comidaRecomendada = "Comida1", imagenComidaUrl = "url1")
        val fallo = repo.crearPlan(nombre = "Plan Unico", duracion = 45, nivel = "Avanzado", objetivo = "Fuerza", precio = 150, comidaRecomendada = "Comida2", imagenComidaUrl = "url2")

        assertNull(fallo)
        assertEquals(1, repo.countPlanes())
    }

    @Test
    fun `actualizarplandatoscorrectamente`() {
        repo.agregarPlan(FakePlan(id = "1", nombre = "Original", duracion = 30, nivel = "Básico", objetivo = "Mantener", precio = 100, comidaRecomendada = "Comida", imagenComidaUrl = "url"))

        val actualizado = repo.actualizarPlan("1", nombre = "Actualizado", duracion = 45, precio = 150)

        assertTrue(actualizado)
        val plan = repo.obtenerPlanes().first()
        assertEquals("Actualizado", plan.nombre)
        assertEquals(45, plan.duracion)
        assertEquals(150, plan.precio)
    }

    @Test
    fun `eliminarplanremuevecorrectamente`() {
        repo.agregarPlan(FakePlan(id = "1", nombre = "Plan a Eliminar", duracion = 30, nivel = "Básico", objetivo = "Mantener", precio = 100, comidaRecomendada = "Comida", imagenComidaUrl = "url"))
        assertEquals(1, repo.countPlanes())

        val eliminado = repo.eliminarPlan("1")

        assertTrue(eliminado)
        assertEquals(0, repo.countPlanes())
    }

    @Test
    fun `crearplanconimagenurlopcionalfunciona`() {
        val planConImagen = repo.crearPlan(
            nombre = "Plan con Imagen",
            duracion = 30,
            nivel = "Básico",
            objetivo = "Salud",
            precio = 80,
            comidaRecomendada = "Pescado",
            imagenComidaUrl = "url_pescado",
            imagenUrl = "https://example.com/plan.jpg"
        )

        assertNotNull(planConImagen)
        assertEquals("https://example.com/plan.jpg", planConImagen?.imagenUrl)
    }

    @Test
    fun `crearplansinimagenurlfunciona`() {
        val planSinImagen = repo.crearPlan(
            nombre = "Plan Sin Imagen",
            duracion = 30,
            nivel = "Básico",
            objetivo = "Salud",
            precio = 80,
            comidaRecomendada = "Pescado",
            imagenComidaUrl = "url_pescado",
            imagenUrl = null
        )

        assertNotNull(planSinImagen)
        assertEquals("", planSinImagen?.imagenUrl)
    }
}