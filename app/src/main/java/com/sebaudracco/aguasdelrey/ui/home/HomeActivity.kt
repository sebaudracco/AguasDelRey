package com.sebaudracco.aguasdelrey.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.sebaudracco.aguasdelrey.R
import com.sebaudracco.aguasdelrey.data.LoginRepository
import com.sebaudracco.aguasdelrey.databinding.ActivityHomeBinding
import com.sebaudracco.aguasdelrey.ui.route.SelectRutaActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)

        // FAB oculto en el XML — no asignamos acción
        // binding.appBarHome.fab ya tiene visibility="gone"

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView    = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home2)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // ── Poblar header del drawer con datos del usuario logueado ──────────
        val headerView = navView.getHeaderView(0)
        val tvNombre   = headerView.findViewById<TextView>(R.id.tv_nav_nombre)
        val tvEmail    = headerView.findViewById<TextView>(R.id.tv_nav_email)

        val nombre = LoginRepository.getNombre(applicationContext)
        val email  = LoginRepository.getEmail(applicationContext)

        if (!nombre.isNullOrBlank()) tvNombre.text = nombre
        if (!email.isNullOrBlank())  tvEmail.text  = email

        // ── Interceptar clic en "Reparto" → lanzar SelectRutaActivity ────────
        // El nav graph llevaría a GalleryFragment (vacío).
        // Lo interceptamos antes de que el NavController lo procese.
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_gallery -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this, SelectRutaActivity::class.java))
                    true
                }
                else -> {
                    // Para el resto (nav_home) dejamos que el NavController lo maneje
                    val handled = navController.navigate(item.itemId)
                    drawerLayout.closeDrawers()
                    handled != null
                }
            }
        }
    }

    // Menú vacío → no aparecen los tres puntitos
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home2)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
