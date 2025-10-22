package com.example.gestionpisoscompartidos.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.piso.crearPiso.CrearPiso

class TestPiso : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_piso)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, CrearPiso.newInstance())
            .commit()
    }
}
