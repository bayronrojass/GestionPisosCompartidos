package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Bundle
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
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLienzo
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostIt
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostItView
import kotlinx.coroutines.launch

open class Pizarra : Fragment() {
    private var casaId: Long = 0

    companion object {
        fun newInstance(casaId: Long) =
            Pizarra().apply {
                arguments =
                    Bundle().apply {
                        putLong("casa_id", casaId)
                    }
            }
    }

    private val repositoryLienzo = RepositoryLienzo()
    private val repositoryPostIt = RepositoryPostIt()
    private val viewModel: PizarraViewModel = PizarraViewModel(1)
    private var drawView: PizarraView? = null
    private lateinit var postItContainer: FrameLayout
    private lateinit var buttonContainer: LinearLayout
    private lateinit var mainContainer: ConstraintLayout

    private var currentExpandedPostIt: PostItView? = null
    private var postItOverlay: View? = null
    private var expandedPizarraView: PizarraView? = null

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
        val args = PizarraArgs.fromBundle(requireArguments())
        casaId = args.casaId
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        drawView?.setModel(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            loadInitialData()

            drawView?.load()
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
            val post =
                PostIt(
                    context = requireContext(),
                    viewLifecycleOwner = this,
                    postItContainer = postItContainer,
                    mainContainer = mainContainer,
                    viewModel = viewModel,
                    x = 300f,
                    y = 300f,
                    casaId = casaId,
                )

            post.loadExistingPostIt(postItId)
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

        view.findViewById<Button>(R.id.btnUndo).setOnClickListener {
            val post =
                PostIt(
                    context = requireContext(),
                    viewLifecycleOwner = this,
                    postItContainer = postItContainer,
                    mainContainer = mainContainer,
                    viewModel = viewModel,
                    x = 100f,
                    y = 100f,
                    casaId = casaId,
                )
            post.initializePostIt()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawView!!.stop()
        drawView = null
    }
}
