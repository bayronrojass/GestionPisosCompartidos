package com.example.gestionpisoscompartidos.ui.pizarra.p2

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
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
        loadPostIts()
    }

    private fun loadPostIts() {
        viewModelScope.launch {
            try {
                val postits = postItRepository.getPostIts(casaId, location)
                if (postits.isNullOrEmpty()) {
                    return@launch
                }
                for (post in postits) {
                    addExistingPostIt(post)
                }
            } catch (e: Exception) {
                println("Error al cargar los Post-its: ${e.message}")
            }
        }
    }

    suspend fun addExistingPostIt(postItId: Long) {
        val details = postItRepository.getPostItDetails(postItId)

        val newState =
            PostItState(
                details!!.id,
                Offset(details.posicionX, details.posicionY),
                details.localizacion,
                details.lienzoId,
                false,
            )
        _postIts.update { currentList -> currentList + newState }
    }

    fun addNewPostIt() {
        viewModelScope.launch {
            try {
                val details = PostItDTO(0, 0, 0f, 0f, 0, 0, location)
                val newPostItFromApi = postItRepository.createPostIt(casaId = casaId, details)

                val newState =
                    PostItState(
                        newPostItFromApi!!.id,
                        Offset(newPostItFromApi.posicionX, newPostItFromApi.posicionY),
                        newPostItFromApi.localizacion,
                        newPostItFromApi.lienzoId,
                        false,
                    )

                _postIts.update { currentList -> currentList + newState }
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
                if (postIt.id == id) {
                    postIt.copy(isExpanded = !postIt.isExpanded)
                } else {
                    postIt
                }
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
