package com.example.gestionpisoscompartidos.ui.pizarra.postit
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryImagen
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModel
import kotlinx.coroutines.launch

class PostIt(
    context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    postItContainer: ViewGroup,
    mainContainer: ViewGroup,
    viewModel: PizarraViewModel,
    val config: PostItConfig,
) {
    private val repositoryPostIt = RepositoryPostIt()
    private val repositoryImagen = RepositoryImagen()
    private val postItManager = PostItManager(context, viewLifecycleOwner, postItContainer, mainContainer, viewModel, config)

    fun initializePostIt() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dto = repositoryPostIt.createPostIt(config.casaId)
                if (dto != null) {
                    postItManager.createNewPostIt(dto)
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

    fun initializeImage(
        bitmap: Bitmap,
        uri: Uri,
        contentResolver: ContentResolver,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dto = repositoryImagen.createImagen(config.casaId, uri, contentResolver)
                if (dto != null) {
                    config.width = dto.width
                    config.height = dto.height
                    postItManager.createNewPostIt(dto, bitmap)
                }
            } catch (e: Exception) {
                Log.e("PostIt", "Error creating postit: ${e.message}")
            }
        }
    }

    fun loadExistingImage(postItId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            postItManager.createExistingPostIt(postItId)
        }
    }

    fun cleanup() {
        postItManager.cleanup()
    }
}
