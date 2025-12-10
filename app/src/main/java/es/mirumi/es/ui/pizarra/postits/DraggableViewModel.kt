package es.mirumi.es.ui.pizarra.postits

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.repository.repositories.RepositoryPostIt
import es.mirumi.es.model.dtos.PostItDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DraggableViewModel(
    private val casaId: Long,
    private val location: String,
    private val postItRepository: RepositoryPostIt = RepositoryPostIt(),
) : ViewModel() {
    private val _postIts = MutableStateFlow<List<PostItState>>(emptyList())
    val postIts: StateFlow<List<PostItState>> = _postIts.asStateFlow()

    init {
        startPeriodicSync()
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
                        id = dto.id!!,
                        offset = Offset(dto.posicionX, dto.posicionY),
                        location = dto.localizacion,
                        lienzoId = dto.lienzoId!!,
                        isExpanded = _postIts.value.find { it.id == dto.id }?.isExpanded ?: false,
                    )
                }
            _postIts.value = newPostItStates
            Log.d("SYNC_POSTITS", "Sincronización completada. Total de Post-its: ${newPostItStates.size}")
        } catch (e: Exception) {
            println("Error al sincronizar los Post-its: ${e.message}")
        }
    }

    fun addNewPostIt() {
        viewModelScope.launch {
            try {
                postItRepository.createPostIt(casaId = casaId, location)
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
}
