package com.example.gestionpisoscompartidos.ui.pizarra.postit
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModel
import kotlinx.coroutines.launch

class PostIt(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val postItContainer: ViewGroup,
    private val mainContainer: ViewGroup,
    private val viewModel: PizarraViewModel,
    val x: Float,
    val y: Float,
    val casaId: Long,
) {
    private val repositoryPostIt = RepositoryPostIt()
    private val postItManager = PostItManager(context, viewLifecycleOwner, postItContainer, mainContainer, viewModel)

    fun initializePostIt() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dto = repositoryPostIt.createPostIt(casaId)
                if (dto != null) {
                    postItManager.createNewPostIt(dto, x, y)
                }
            } catch (e: Exception) {
                Log.e("PostIt", "Error creating postit: ${e.message}")
            }
        }
    }

    fun loadExistingPostIt(postItId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            postItManager.createExistingPostIt(postItId)
        }
    }

    fun cleanup() {
        postItManager.cleanup()
    }
}
