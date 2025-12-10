package es.mirumi.es.ui.activities

import android.os.Bundle
import es.mirumi.es.R

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
