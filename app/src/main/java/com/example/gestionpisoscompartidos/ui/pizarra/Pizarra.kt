package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.databinding.ActivityTestPizarraBinding
import kotlinx.coroutines.launch

class Pizarra : Fragment() {
    companion object {
        fun newInstance() = Pizarra()
    }

    private val viewModel: PizarraViewModel by viewModels()
    private var drawView: PizarraView? = null
    private lateinit var binding: ActivityTestPizarraBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_pizarra, container, false)
        drawView = rootView.findViewById(R.id.pizarraView)

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

        binding = ActivityTestPizarraBinding.inflate(layoutInflater)

        setupClickListeners()

        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawView = null // evita fugas de memoria
    }

    private fun setupClickListeners() {
        binding.btnBlack.setOnClickListener {
            viewModel.onColorSelected(1)
        }

        binding.btnWhite.setOnClickListener {
            viewModel.onColorSelected(8)
        }

        binding.btnRed.setOnClickListener {
            viewModel.onColorSelected(2)
        }

        binding.btnBlue.setOnClickListener {
            viewModel.onColorSelected(4)
        }

        binding.btnGreen.setOnClickListener {
            viewModel.onColorSelected(3)
        }
    }
}
