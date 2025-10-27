package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.model.Usuario // <-- ¡Importa tu modelo!
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionUsuariosPisoViewModel : ViewModel() {
    // 1. El StateFlow expone el Modelo de UI
    private val _miembros = MutableStateFlow<List<MiembroPiso>>(emptyList())
    val miembros: StateFlow<List<MiembroPiso>> = _miembros

    // Inyecta tus repositorios y gestor de sesión aquí
    // private val pisoRepository: PisoRepository
    // private val userRepository: UserRepository

    init {
        loadMiembros()
    }

    private fun loadMiembros() {
        viewModelScope.launch {
            // --- INICIO DE DATOS DE EJEMPLO ---
            // (Aquí harías tus llamadas reales al repositorio)

            // 1. Obtén el ID del usuario actual (de SharedPreferences, DataStore, etc.)
            val currentUserId = 2L // Ejemplo: "Juancar" es "TÚ"

            // 2. Obtén los IDs de los admins del piso
            val adminIds = listOf(1L) // Ejemplo: "Manolo" es Admin

            // 3. Obtén la LISTA DE USUARIOS (tu modelo) del piso
            val usuariosDelPiso: List<Usuario> =
                listOf(
                    Usuario(1L, "Manolo", "manolo@mail.com"),
                    Usuario(2L, "Juancar", "juancar@mail.com"),
                    Usuario(3L, "Paula", "paula@mail.com"),
                )
            // --- FIN DE DATOS DE EJEMPLO ---

            // 4. ¡LA TRANSFORMACIÓN!
            // Convierte List<Usuario> en List<MiembroPisoUI>
            val listaMiembrosUI =
                usuariosDelPiso.map { usuario ->
                    MiembroPiso(
                        id = usuario.id,
                        nombre = usuario.nombre,
                        esAdmin = usuario.id in adminIds,
                        esTu = usuario.id == currentUserId,
                        colorIndicator = getColorForUser(usuario.id), // Asigna un color
                    )
                }

            // 5. Emite la lista para la UI
            _miembros.value = listaMiembrosUI
        }
    }

    /** Asigna un color a cada usuario basado en su ID */
    private fun getColorForUser(id: Long): Int {
        val colors =
            listOf(
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_purple,
                android.R.color.holo_blue_light,
            )
        return colors[(id % colors.size).toInt()]
    }

    /** Lógica para eliminar un miembro */
    fun removeMiembro(miembroId: Long) {
        viewModelScope.launch {
            // 1. Llamar al repositorio para eliminar (ej. pisoRepository.removeUser(miembroId))
            // ...

            // 2. Si tiene éxito, actualiza la UI filtrando la lista
            _miembros.value = _miembros.value.filter { it.id != miembroId }
        }
    }
}
