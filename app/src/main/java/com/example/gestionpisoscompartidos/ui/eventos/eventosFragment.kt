package com.example.gestionpisoscompartidos.ui.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.piso.crearCasa.eventosViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class eventosFragment : Fragment() {
    private val viewModel: eventosViewModel by viewModels()

    private lateinit var fabAdd: FloatingActionButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)

        fabAdd.setOnClickListener {
            showEventDialog()
        }
    }

    companion object {
        fun newInstance() = eventosFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_eventos, container, false)

    private fun showEventDialog() {
        val dialog = eventDialogFragment()
        dialog.show(childFragmentManager, "¡Crea un evento!")
    }
}
