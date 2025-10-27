package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R

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

        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        drawView?.setModel(viewModel)
        drawView?.onTouchCallback = { x, y ->
            Log.d("Pizarra", "Tocado en: ($x, $y)")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawView = null // evita fugas de memoria
    }
}
