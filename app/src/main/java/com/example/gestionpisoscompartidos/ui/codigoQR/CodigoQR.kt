package com.example.gestionpisoscompartidos.ui.codigoQR

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R

class CodigoQR : Fragment() {
    companion object {
        fun newInstance() = CodigoQR()
    }

    private val viewModel: CodigoQRViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_codigo_q_r, container, false)
}
