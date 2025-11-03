package com.example.gestionpisoscompartidos.ui.piso.listaPisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gestionpisoscompartidos.model.Casa

class ListaCasasViewModel : ViewModel() {
    private val _casas = MutableLiveData<List<Casa>>()
    val casas: LiveData<List<Casa>> = _casas

    private val _mostrarMensajeVacio = MutableLiveData<Boolean>()
    val mostrarMensajeVacio: LiveData<Boolean> = _mostrarMensajeVacio

    fun setCasas(listaCasas: List<Casa>) {
        _casas.value = listaCasas
        _mostrarMensajeVacio.value = listaCasas.isEmpty()
    }
}
