package es.mirumi.es.utils

import kotlin.math.abs
import kotlin.math.min

// Modelo para el plan de pagos simplificado
data class Deuda(
    val de: String,
    val para: String,
    val cantidad: Double,
)

object DebtCalculator {
    /**
     * Algoritmo de simplificación de deudas.
     * Convierte una lista de deudas brutas en el mínimo número de transacciones necesarias.
     */
    fun simplificar(deudasOriginales: List<Deuda>): List<Deuda> {
        val balanceMap = mutableMapOf<String, Double>()

        // 1. Calcular balance neto de cada persona
        for (deuda in deudasOriginales) {
            // El que paga la deuda (deudor) pierde saldo en su balance neto (se vuelve negativo)
            // El que recibe (acreedor) gana saldo (se vuelve positivo)
            // Nota: Aquí 'deuda.de' es quien DEBE pagar, 'deuda.para' es quien DEBE recibir.
            balanceMap[deuda.de] = balanceMap.getOrDefault(deuda.de, 0.0) - deuda.cantidad
            balanceMap[deuda.para] = balanceMap.getOrDefault(deuda.para, 0.0) + deuda.cantidad
        }

        // Filtramos balances cercanos a 0 para evitar errores de punto flotante
        val deudores = balanceMap.filter { it.value < -0.01 }.keys.toMutableList()
        val acreedores = balanceMap.filter { it.value > 0.01 }.keys.toMutableList()
        val resultado = mutableListOf<Deuda>()

        // Ordenar para priorizar cerrar las deudas más grandes primero
        deudores.sortBy { balanceMap[it] } // Los más negativos primero
        acreedores.sortByDescending { balanceMap[it] } // Los más positivos primero

        var i = 0
        var j = 0

        // 2. Algoritmo Greedy (Voraz)
        while (i < deudores.size && j < acreedores.size) {
            val deudorId = deudores[i]
            val acreedorId = acreedores[j]

            val montoDeudor = abs(balanceMap[deudorId]!!)
            val montoAcreedor = balanceMap[acreedorId]!!

            // La cantidad a transaccionar es el mínimo entre lo que debe uno y lo que espera el otro
            val cantidad = min(montoDeudor, montoAcreedor)

            // Redondeamos a 2 decimales
            val cantidadRedondeada = Math.round(cantidad * 100.0) / 100.0

            if (cantidadRedondeada > 0) {
                resultado.add(Deuda(deudorId, acreedorId, cantidadRedondeada))
            }

            // Ajustar balances restantes
            val remanenteDeudor = montoDeudor - cantidad
            val remanenteAcreedor = montoAcreedor - cantidad

            // Actualizamos mapa (aunque usamos variables locales para el control del bucle)
            balanceMap[deudorId] = -remanenteDeudor
            balanceMap[acreedorId] = remanenteAcreedor

            // Si el deudor ya pagó casi todo (margen error 0.01), pasamos al siguiente
            if (remanenteDeudor < 0.01) {
                i++
            }
            // Si el acreedor ya cobró casi todo, pasamos al siguiente
            if (remanenteAcreedor < 0.01) {
                j++
            }
        }

        return resultado
    }
}
