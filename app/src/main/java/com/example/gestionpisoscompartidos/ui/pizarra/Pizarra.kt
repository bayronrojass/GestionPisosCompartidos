package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryImagen
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLienzo
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostIt
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostItConfig
import com.tuapp.utils.ImagePicker
import kotlinx.coroutines.launch
import kotlin.random.Random

open class Pizarra : Fragment() {
    private var casaId: Long = 0

    private val repositoryLienzo = RepositoryLienzo()
    private val repositoryPostIt = RepositoryPostIt()
    private val repositoryImagen = RepositoryImagen()
    private val viewModel: PizarraViewModel = PizarraViewModel(1)
    private var drawView: PizarraView? = null
    private lateinit var postItContainer: FrameLayout
    private lateinit var buttonContainer: LinearLayout
    private lateinit var mainContainer: ConstraintLayout
    private lateinit var imagePicker: ImagePicker
    private var postItList: MutableList<PostIt> = mutableListOf()

    companion object {
        fun newInstance(casaId: Long) =
            Pizarra().apply {
                arguments =
                    Bundle().apply {
                        putLong("casa_id", casaId)
                    }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_pizarra, container, false)
        drawView = rootView.findViewById(R.id.pizarraView)
        buttonContainer = rootView.findViewById(R.id.buttonContainer)
        postItContainer = rootView.findViewById(R.id.postItContainer)
        mainContainer = rootView.findViewById(R.id.mainContainer)

        buttonContainer.bringToFront()

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Pizarra", "Cargando...")
        val args = PizarraArgs.fromBundle(requireArguments())
        casaId = args.casaId
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        imagePicker =
            ImagePicker(this) { bitmap, uri ->
                if (uri != null && bitmap != null) {
                    val scaled = repositoryImagen.resizeBitmap(bitmap)
                    val config =
                        PostItConfig(
                            x = Random.nextInt(100, drawView!!.width - 300).toFloat(),
                            y = Random.nextInt(100, drawView!!.height - 300).toFloat(),
                            isImage = true,
                            casaId = casaId,
                        )

                    val post =
                        PostIt(
                            context = requireContext(),
                            viewLifecycleOwner = this,
                            postItContainer = postItContainer,
                            mainContainer = mainContainer,
                            viewModel = viewModel,
                            config,
                        )

                    post.initializeImage(scaled, uri, requireContext().contentResolver)
                    postItList.add(post)
                }
            }

        imagePicker.init()

        drawView?.setModel(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            loadInitialData()

            drawView?.load()
            drawView?.activatedDraw = true
            viewModel.bitmapState.collect { bitmap ->
                bitmap?.let {
                    drawView?.setBackgroundBitmap(it)
                }
            }
        }

        setupClickListeners(view)
    }

    private suspend fun loadInitialData() {
        val response = repositoryLienzo.getLienzo(casaId)
        if (response != null) {
            viewModel.lienzoId = response
        }

        val postitResponse = repositoryPostIt.getPostIts(casaId)

        postitResponse?.forEach { postItId ->
            val config =
                PostItConfig(
                    isImage = false,
                    casaId = casaId,
                )

            val post =
                PostIt(
                    context = requireContext(),
                    viewLifecycleOwner = this,
                    postItContainer = postItContainer,
                    mainContainer = mainContainer,
                    viewModel = viewModel,
                    config,
                )

            post.loadExistingPostIt(postItId)
            postItList.add(post)
        }

        val imageResponse = repositoryImagen.getImagenes(casaId)

        imageResponse?.forEach { postItId ->
            val config =
                PostItConfig(
                    isImage = true,
                    casaId = casaId,
                )

            val post =
                PostIt(
                    context = requireContext(),
                    viewLifecycleOwner = this,
                    postItContainer = postItContainer,
                    mainContainer = mainContainer,
                    viewModel = viewModel,
                    config,
                )

            post.loadExistingImage(postItId)
            postItList.add(post)
        }
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnBlack).setOnClickListener {
            viewModel.onColorSelected(1)
        }

        view.findViewById<Button>(R.id.btnRed).setOnClickListener {
            viewModel.onColorSelected(2)
        }

        view.findViewById<Button>(R.id.btnBlue).setOnClickListener {
            viewModel.onColorSelected(4)
        }

        view.findViewById<Button>(R.id.btnGreen).setOnClickListener {
            viewModel.onColorSelected(3)
        }

        view.findViewById<Button>(R.id.btnWhite).setOnClickListener {
            viewModel.onColorSelected(8)
        }

        view.findViewById<Button>(R.id.btnImagen).setOnClickListener {
            imagePicker.pickPhoto()
        }

        view.findViewById<Button>(R.id.btnPostit).setOnClickListener {
            val config =
                PostItConfig(
                    x = Random.nextInt(100, drawView!!.width - 300).toFloat(),
                    y = Random.nextInt(100, drawView!!.height - 300).toFloat(),
                    isImage = false,
                    casaId = casaId,
                )

            val post =
                PostIt(
                    context = requireContext(),
                    viewLifecycleOwner = this,
                    postItContainer = postItContainer,
                    mainContainer = mainContainer,
                    viewModel = viewModel,
                    config = config,
                )

            post.initializePostIt()
            postItList.add(post)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        postItList.forEach { x -> x.cleanup() }
        drawView!!.stop()
        drawView = null
    }
}
