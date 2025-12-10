package es.mirumi.es.ui.tareas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.data.repository.repositories.RepositoryTarea
import es.mirumi.es.model.Tarea
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.requests.TareaRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TareasViewModel(
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryTarea(NetworkModule.tareaApiService)
    private val repositoryCasa = RepositoryCasa(NetworkModule.casaApiService)

    private val _tareas = MutableLiveData<List<Tarea>>()
    val tareas: LiveData<List<Tarea>> = _tareas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _miembros = MutableLiveData<List<Usuario>>()
    val miembros: LiveData<List<Usuario>> = _miembros

    init {
        cargarTareas()
    }

    fun cargarTareas() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _tareas.value = repository.getTareasByCasaId(casaId)
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en cargarTareas", e)
                _error.value = e.message ?: "Error cargando tareas"
                _tareas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearTarea(
        nombre: String,
        descripcion: String?,
        asignadoAId: Long?,
        fechaFin: String?,
        frecuencia: String?,
        prioridad: String?,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val esPeriodica = !frecuencia.isNullOrBlank()
                val request =
                    TareaRequest(
                        nombre,
                        descripcion,
                        false,
                        fechaFin,
                        frecuencia,
                        esPeriodica,
                        asignadoAId,
                        prioridad,
                    )
                repository.crearTarea(casaId, request)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en crearTarea", e)
                _error.value = "Error al crear tarea: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun toggleCompletado(tarea: Tarea) {
        viewModelScope.launch {
            val nuevaFechaFin =
                if (!tarea.completado) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    sdf.format(Date())
                } else {
                    tarea.fechaFin
                }
            val request =
                TareaRequest(
                    tarea.nombre,
                    tarea.descripcion,
                    !tarea.completado,
                    nuevaFechaFin,
                    tarea.frecuencia,
                    tarea.periodica,
                    tarea.asignadoA?.id,
                    tarea.prioridad,
                )
            try {
                repository.actualizarTarea(tarea.id, request)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en toggleCompletado", e)
                _error.value = e.message
            }
        }
    }

    fun borrarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repository.borrarTarea(tarea.id)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en borrarTarea", e)
                _error.value = e.message
            }
        }
    }

    fun editarTarea(
        tarea: Tarea,
        nuevoNombre: String,
        nuevaDesc: String?,
        asignadoAId: Long?,
        nuevaFechaFin: String?,
        nuevaFrecuencia: String?,
        nuevaPrioridad: String?,
    ) {
        viewModelScope.launch {
            // Si asignadoAId es null, enviamos -1L, de lo contrario enviamos el ID real
            val idParaEnviar = asignadoAId ?: -1L
            val esPeriodica = !nuevaFrecuencia.isNullOrBlank()
            val request =
                TareaRequest(
                    nuevoNombre,
                    nuevaDesc,
                    null,
                    nuevaFechaFin,
                    nuevaFrecuencia,
                    esPeriodica,
                    idParaEnviar,
                    nuevaPrioridad,
                )
            try {
                repository.actualizarTarea(tarea.id, request)
                cargarTareas()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun cargarMiembros(token: String) {
        viewModelScope.launch {
            try {
                val response = repositoryCasa.getPisoMiembros(token, casaId)
                if (response.isSuccessful) {
                    _miembros.value = response.body() ?: emptyList()
                } else {
                    Log.e("TareasViewModel", "Error cargando miembros: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Excepción cargando miembros", e)
            }
        }
    }
}
