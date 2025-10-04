package com.example.gestionpisoscompartidos.ui.pizarra

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gestionpisoscompartidos.R

class Pizarra : Fragment() {

    companion object {
        fun newInstance() = Pizarra()
    }

    private val viewModel: PizarraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pizarra, container, false)
    }
}