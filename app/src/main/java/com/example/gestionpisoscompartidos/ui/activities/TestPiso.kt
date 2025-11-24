package com.example.gestionpisoscompartidos.ui.activities

import android.os.Bundle
import com.example.gestionpisoscompartidos.R

class TestPiso : androidx.appcompat.app.AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_piso)

        supportFragmentManager
            .beginTransaction()
            // .replace(R.id.fragment_container_view, CrearCasa.newInstance())
            .commit()
    }
}
