package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R

class GestionUsuariosPiso : Fragment() {
    companion object {
        fun newInstance() = GestionUsuariosPiso()
    }

    private val viewModel: GestionUsuariosPisoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_gestion_usuarios_piso, container, false)
}
