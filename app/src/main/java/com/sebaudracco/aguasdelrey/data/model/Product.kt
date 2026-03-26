package com.sebaudracco.aguasdelrey.data.model

/**
 * Decisión de diseño:
 * - id cambia de String (UUID local) a Int para mapear con id_producto de BD.
 * - idDetalle es nuevo: necesitamos el id del renglón de detalle_pedido para
 *   el POST /api/entrega — es el identificador de "esta línea de este pedido".
 * - cantidadEntregada se agrega separado de quantity (cantidad original del pedido)
 *   para que el repartidor pueda ajustar sin perder el valor original.
 * - price pasa a Double para operar aritméticamente (precio_unitario es numeric en BD).
 */
data class Product(
    val idDetalle: Int,          // id_detalle en detalle_pedido
    val idProducto: Int,         // id_producto en producto
    val description: String,     // nombre del producto
    val quantity: Int,           // cantidad original del pedido
    var cantidadEntregada: Int,  // cantidad que realmente se entrega (ajustable)
    var delivered: Boolean,      // si el switch está activado
    val price: Double            // precio_unitario histórico del detalle
)
