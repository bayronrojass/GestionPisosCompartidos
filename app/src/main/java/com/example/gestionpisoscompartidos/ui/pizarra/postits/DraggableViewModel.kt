package com.example.gestionpisoscompartidos.ui.pizarra.postits

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
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
                delay(10000L)
            }
        }
    }

    private suspend fun syncPostIts() {
        try {
            val postitsFromApi = postItRepository.getPostIts(casaId, location) ?: emptyList()
            val localPostIts = _postIts.value

            val apiMap = postitsFromApi.associateBy { it }
            val localMap = localPostIts.associateBy { it.id }

            val toAdd = apiMap.filterKeys { it !in localMap.keys }
            val toRemove = localMap.filterKeys { it !in apiMap.keys }

            _postIts.update { currentList ->
                val listAfterRemoval = currentList.filterNot { it.id in toRemove.keys }

                val newPostItStates =
                    toAdd.values.map { id ->
                        val newPostItDTO = postItRepository.getPostItDetails(id)
                        PostItState(
                            id = newPostItDTO!!.id,
                            offset = Offset(newPostItDTO.posicionX, newPostItDTO.posicionY),
                            location = newPostItDTO.localizacion,
                            lienzoId = newPostItDTO.lienzoId,
                            isExpanded = false,
                        )
                    }

                // Devolvemos la lista combinada
                listAfterRemoval + newPostItStates
            }
        } catch (e: Exception) {
            println("Error al sincronizar los Post-its: ${e.message}")
        }
    }

    fun addNewPostIt() {
        viewModelScope.launch {
            try {
                val details = PostItDTO(0, 0, 0f, 0f, 0, 0, location)
                postItRepository.createPostIt(casaId = casaId, details)

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
}
