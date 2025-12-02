package com.example.gestionpisoscompartidos.ui.pizarra.p2

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

class DraggableViewModel : ViewModel() {
    private val _postIts = MutableStateFlow<List<PostItState>>(emptyList())
    val postIts: StateFlow<List<PostItState>> = _postIts.asStateFlow()

    fun addPostIt() {
        _postIts.update { currentList -> currentList + PostItState() }
    }

    fun removePostIt(id: String) {
        _postIts.update { currentList -> currentList.filterNot { it.id == id } }
    }

    fun toggleExpand(id: String) {
        _postIts.update { currentList ->
            currentList.map { postIt ->
                if (postIt.id == id) {
                    postIt.copy(isExpanded = !postIt.isExpanded)
                } else {
                    postIt.copy(isExpanded = false)
                }
            }
        }
    }

    fun updatePostItPosition(
        id: String,
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
