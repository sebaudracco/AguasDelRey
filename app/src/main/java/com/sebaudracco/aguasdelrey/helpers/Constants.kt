package com.sebaudracco.aguasdelrey.helpers

/**
 * Decisión: agregamos EXTRA_PEDIDO_ID y EXTRA_CLIENTE_ID como constantes
 * en lugar de strings literales dispersos en el código.
 * Esto sigue el principio DRY (Don't Repeat Yourself) — si necesitamos
 * cambiar el nombre del extra, lo cambiamos en un solo lugar.
 */
class Constants {
    companion object {
        // Existentes — no tocar para no romper código actual
        const val EXTRA_USER_NAME  = "EXTRA_USER_NAME"
        const val EXTRA_USER_ID    = "EXTRA_USER_ID"

        // Nuevas para el CU de entrega
        const val EXTRA_PEDIDO_ID  = "EXTRA_PEDIDO_ID"
        const val EXTRA_CLIENTE_ID = "EXTRA_CLIENTE_ID"
    }
}