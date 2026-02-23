package com.sebaudracco.aguasdelrey.data

import com.sebaudracco.aguasdelrey.data.model.Product
import com.sebaudracco.aguasdelrey.data.model.RutaReparto
import com.sebaudracco.aguasdelrey.data.model.ScheduleTask

/**
 * DataRepository — simula la sincronización con la app web.
 * En el MVP académico los datos están hardcodeados replicando
 * la estructura de la BD PostgreSQL (ruta_reparto, parada_ruta, pedido).
 * En una versión futura esto se reemplaza por llamadas HTTP a la API.
 */
object DataRepository {

    // ── Productos por parada (simulan detalle_pedido) ─────────────────────
    private val pedidosCliente1 = listOf(
        Product("p1-1", "Bidón 20 litros", 3, false, "850.00"),
        Product("p1-2", "Bidón vacío (retiro)", 2, false, "0.00"),
        Product("p1-3", "Abono mensual", 1, false, "2500.00")
    )

    private val pedidosCliente2 = listOf(
        Product("p2-1", "Bidón 20 litros", 2, false, "850.00"),
        Product("p2-2", "Bidón vacío (retiro)", 1, false, "0.00")
    )

    private val pedidosCliente3 = listOf(
        Product("p3-1", "Bidón 20 litros", 4, false, "850.00"),
        Product("p3-2", "Bidón vacío (retiro)", 3, false, "0.00"),
        Product("p3-3", "Café La Virginia 500 gr.", 1, false, "1200.00")
    )

    private val pedidosCliente4 = listOf(
        Product("p4-1", "Bidón 20 litros", 1, false, "850.00"),
        Product("p4-2", "Abono mensual", 1, false, "2500.00")
    )

    private val pedidosCliente5 = listOf(
        Product("p5-1", "Bidón 20 litros", 2, false, "850.00"),
        Product("p5-2", "Bidón vacío (retiro)", 2, false, "0.00"),
        Product("p5-3", "Abono mensual", 1, false, "2500.00")
    )

    private val pedidosCliente6 = listOf(
        Product("p6-1", "Bidón 20 litros", 3, false, "850.00"),
        Product("p6-2", "Bidón vacío (retiro)", 1, false, "0.00")
    )

    // ── Paradas de cada ruta (simulan parada_ruta) ────────────────────────
    private val paradasRuta1 = mutableListOf(
        ScheduleTask("t1-1", "r1", "27 de Abril 370, San Francisco", "Sebastián Baudracco", true, true, false, 1, "08:30", true, false),
        ScheduleTask("t1-2", "r1", "Corrientes 18, San Francisco", "Giuliana Mattio", true, true, false, 2, "09:15", true, false),
        ScheduleTask("t1-3", "r1", "Juan B. Justo 726, San Francisco", "Nicolás Calcagno", true, true, false, 3, "10:00", true, false),
        ScheduleTask("t1-4", "r1", "Ayacucho 468 PB C, San Francisco", "Ezequiel Jiménez", true, true, false, 4, "10:45", true, false)
    )

    private val paradasRuta2 = mutableListOf(
        ScheduleTask("t2-1", "r2", "Belgrano 1245, San Francisco", "María García", true, true, false, 1, "14:00", true, false),
        ScheduleTask("t2-2", "r2", "San Martín 890, San Francisco", "Carlos López", true, true, false, 2, "14:45", true, false),
        ScheduleTask("t2-3", "r2", "Rivadavia 456, San Francisco", "Ana Fernández", true, true, false, 3, "15:30", true, false),
        ScheduleTask("t2-4", "r2", "Tucumán 123, San Francisco", "Roberto Díaz", true, true, false, 4, "16:15", true, false),
        ScheduleTask("t2-5", "r2", "Mendoza 789, San Francisco", "Laura Pérez", true, true, false, 5, "17:00", true, false),
        ScheduleTask("t2-6", "r2", "Entre Ríos 321, San Francisco", "Diego Martínez", true, true, false, 6, "17:45", true, false)
    )

    // ── Rutas disponibles (simulan ruta_reparto) ──────────────────────────
    val rutas = listOf(
        RutaReparto(
            id = "r1",
            nombre = "Ruta Turno Mañana",
            fecha = "22/02/2026",
            turno = "mañana",
            repartidor = "Admin Sistema",
            paradas = paradasRuta1
        ),
        RutaReparto(
            id = "r2",
            nombre = "Ruta Turno Tarde",
            fecha = "22/02/2026",
            turno = "tarde",
            repartidor = "Admin Sistema",
            paradas = paradasRuta2
        )
    )

    // ── Mapa de pedidos por parada ────────────────────────────────────────
    val pedidosPorParada: Map<String, List<Product>> = mapOf(
        "t1-1" to pedidosCliente1,
        "t1-2" to pedidosCliente2,
        "t1-3" to pedidosCliente3,
        "t1-4" to pedidosCliente4,
        "t2-1" to pedidosCliente5,
        "t2-2" to pedidosCliente6,
        "t2-3" to pedidosCliente1,
        "t2-4" to pedidosCliente2,
        "t2-5" to pedidosCliente3,
        "t2-6" to pedidosCliente4
    )

    fun getPedidosByParada(taskId: String): List<Product> {
        return pedidosPorParada[taskId] ?: emptyList()
    }

    fun getRutaById(id: String): RutaReparto? {
        return rutas.find { it.id == id }
    }
}
