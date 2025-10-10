package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R

class CrearPiso : Fragment() {
    companion object {
        fun newInstance() = CrearPiso()
    }

    private val viewModel: CrearPisoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_crear_piso, container, false)
}
