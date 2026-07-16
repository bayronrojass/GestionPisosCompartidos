package es.mirumi.es.ui.pizarra.postits

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.data.repository.repositories.RepositoryPostIt
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.dtos.PostItDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DraggableViewModel(
    private val casaId: Long,
    private val location: String,
    private val postItRepository: RepositoryPostIt = RepositoryPostIt(),
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _postIts = MutableStateFlow<List<PostItState>>(emptyList())
    val postIts: StateFlow<List<PostItState>> = _postIts.asStateFlow()

    // House members — used by the ExpandedPostIt control panel's "Enviar nota a" chips.
    // Fetched once on VM init from the same endpoint the Tareas / Gastos screens use,
    // so the chip section is populated the moment the user opens any Post-It.
    private val _miembros = MutableStateFlow<List<Usuario>>(emptyList())
    val miembros: StateFlow<List<Usuario>> = _miembros.asStateFlow()
    private val casaRepository = RepositoryCasa(NetworkModule.casaApiService)

    init {
        startPeriodicSync()
        loadMiembros()
    }

    private fun loadMiembros() {
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: return@launch
                val response = casaRepository.getPisoMiembros(token, casaId)
                if (response.isSuccessful) {
                    _miembros.value = response.body() ?: emptyList()
                } else {
                    Log.e("DraggableViewModel", "Error loading miembros: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DraggableViewModel", "Exception loading miembros: ${e.message}")
            }
        }
    }

    private fun startPeriodicSync() {
        viewModelScope.launch {
            while (true) {
                Log.d("SYNC_POSTITS", "Sincronizando Post-its para la ubicación: $location")
                syncPostIts()
                delay(60000L)
            }
        }
    }

    private suspend fun syncPostIts() {
        try {
            val postitIdsFromApi = postItRepository.getPostIts(casaId, location) ?: emptyList()

            val newPostItStates =
                postitIdsFromApi.map { dto ->
                    PostItState(
                        id = dto.id ?: 0L,
                        offset = Offset(dto.posicionX, dto.posicionY),
                        location = dto.localizacion,
                        lienzoId = dto.lienzoId ?: 0L,
                        isExpanded = _postIts.value.find { it.id == dto.id }?.isExpanded ?: false,
                        tipo = dto.tipo ?: "DIBUJO",
                        rutaAudio = dto.rutaAudio,
                        colorNota = dto.colorNota,
                    )
                }
            _postIts.value = newPostItStates
            Log.d(
                "SYNC_POSTITS",
                "Sincronización completada. Total de Post-its: ${newPostItStates.size}",
            )
        } catch (e: Exception) {
            println("Error al sincronizar los Post-its: ${e.message}")
        }
    }

    fun addNewPostIt() {
        viewModelScope.launch {
            try {
                postItRepository.createPostIt(casaId = casaId, location, sessionManager.fetchCurrentUserId())
                syncPostIts()
            } catch (e: Exception) {
                println("Error al crear el Post-it: ${e.message}")
            }
        }
    }

    fun removePostIt(id: Long) {
        val postItToRemove = _postIts.value.find { it.id == id } ?: return
        _postIts.update { currentList -> currentList.filterNot { it.id == id } }
        viewModelScope.launch {
            try {
                postItRepository.deletePostIt(id)
                println("Post-it con id $id eliminado en el servidor.")
            } catch (e: Exception) {
                println("Error al eliminar el Post-it en el servidor: ${e.message}")
                _postIts.update { currentList -> currentList + postItToRemove }
            }
        }
    }

    fun toggleExpand(id: Long) {
        _postIts.update { currentList ->
            currentList.map { postIt ->
                val newExpandedState = if (postIt.id == id) !postIt.isExpanded else false
                postIt.copy(isExpanded = newExpandedState)
            }
        }
    }

    fun updatePostItPosition(
        id: Long,
        dragAmount: Offset,
    ) {
        _postIts.update { currentList ->
            currentList.map { postIt ->
                if (postIt.id == id) {
                    postIt.copy(offset = postIt.offset + dragAmount)
                } else {
                    postIt
                }
            }
        }
    }

    fun onDragEnd(postId: Long) {
        viewModelScope.launch {
            val postItToUpdate = _postIts.value.find { it.id == postId }
            if (postItToUpdate != null) {
                try {
                    postItRepository.updatePostItPosition(
                        PostItDTO(
                            id = postItToUpdate.id,
                            posicionX = postItToUpdate.offset.x,
                            posicionY = postItToUpdate.offset.y,
                            localizacion = postItToUpdate.location,
                            lienzoId = postItToUpdate.lienzoId,
                        ),
                    )
                } catch (e: Exception) {
                    Log.e("DRAG_END", "Error al actualizar la posición del Post-it: ${e.message}")
                }
            }
        }
    }

    /**
     * Optimistic local update of a Post-It's `colorNota`. Called from `ExpandedPostIt`
     * the instant the user taps a new pastel — updates the `_postIts` StateFlow source
     * of truth so subsequent recompositions (including a close/reopen cycle) see the
     * new value **before** the 60-second `syncPostIts` tick catches up.
     *
     * Without this, `ExpandedPostIt`'s `remember(state.id, state.colorNota)` would see
     * `state.colorNota == null` on reopen (the sync hasn't refreshed yet) and reset the
     * pastel picker back to yellow — even though the PUT to the backend already
     * succeeded and the server has the correct value.
     */
    fun updatePostItColorLocal(
        postItId: Long,
        newColorHex: String?,
    ) {
        _postIts.update { list ->
            list.map { if (it.id == postItId) it.copy(colorNota = newColorHex) else it }
        }
    }

    fun crearPostItDeAudio(archivo: File) {
        viewModelScope.launch {
            try {
                postItRepository.createAudioPostIt(
                    casaId = casaId,
                    location = location,
                    archivo = archivo,
                )
                syncPostIts()
            } catch (e: Exception) {
                Log.e("AUDIO", "Error al crear el Post-it de audio: ${e.message}")
            }
        }
    }
}
