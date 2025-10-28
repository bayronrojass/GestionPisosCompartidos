package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import kotlinx.coroutines.launch

class Pizarra : Fragment() {
    companion object {
        fun newInstance() = Pizarra()
    }

    private val viewModel: PizarraViewModel by viewModels()
    private var drawView: PizarraView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_pizarra, container, false)
        drawView = rootView.findViewById(R.id.pizarraView)
        val buttonContainer: LinearLayout = rootView.findViewById(R.id.buttonContainer)
        buttonContainer.bringToFront()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        drawView?.setModel(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bitmapState.collect { bitmap ->
                bitmap?.let {
                    drawView?.setBackgroundBitmap(it)
                }
            }
        }

        setupClickListeners(view)

        drawView?.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawView = null // evita fugas de memoria
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
    }
}
