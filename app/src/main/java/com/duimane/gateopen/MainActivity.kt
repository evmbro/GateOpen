package com.duimane.gateopen

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.duimane.gateopen.api.GateApiService
import com.duimane.gateopen.fragment.GateFragment
import com.duimane.gateopen.util.SharedPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_gate, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        configureApiService()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val ft = supportFragmentManager.beginTransaction()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        if (navView.selectedItemId == R.id.navigation_settings) {
            return
        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            navView.isVisible = false
            supportActionBar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            ft.add(container.id, GateFragment(), "fullscreen")
        } else {
            navView.isVisible = true
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            supportFragmentManager.findFragmentByTag("fullscreen")?.let { f ->
                ft.remove(f)
            }
        }
        ft.commit()
    }

    private fun configureApiService() {
        val preferences = SharedPreferences.get(this) ?: return
        GateApiService.configure(
            preferences.baseUrl,
            preferences.token
        )
    }

}
