package com.example.gestionpisoscompartidos.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestionpisoscompartidos.R

class TestPizarra : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_pizarra)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, Pizarra.newInstance(1L))
            .commit()
    }
}
