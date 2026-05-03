package com.example.pitchside

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pitchside.databinding.ActivityMainBinding
import com.example.pitchside.managers.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Konfiguracja Toolbar
        val toolbar: Toolbar = binding.topToolbar
        setSupportActionBar(toolbar)

        // PRZYWRACAMY TYTUŁ: Ustawiamy napis PitchSide
        supportActionBar?.title = "PitchSide"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // 2. Konfiguracja Nawigacji
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.scheduledFragment, R.id.navigation_home, R.id.favouriteFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 3. Odświeżanie menu przy zmianie ekranu
        navController.addOnDestinationChangedListener { _, _, _ ->
            invalidateOptionsMenu()
        }

        // 4. Obsługa dolnego menu
        navView.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(false)
                .setPopUpTo(navController.graph.startDestinationId, false, true)
                .build()
            try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val loginItem = menu?.findItem(R.id.action_login)
        val accountItem = menu?.findItem(R.id.action_account)

        val isLogged = SessionManager.isLoggedIn()

        // Decydujemy co wyświetlić: kłódkę czy napis KONTO
        loginItem?.isVisible = !isLogged
        accountItem?.isVisible = isLogged

        if (isLogged) {
            // Zmieniamy z powrotem na stały napis KONTO zamiast nicku
            accountItem?.title = "KONTO"
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        return when (item.itemId) {
            R.id.action_login -> {
                navController.navigate(R.id.loginFragment)
                true
            }
            R.id.action_account -> {
                // ZAMIAST WYLOGOWANIA, TERAZ NAWIGUJEMY:
                val navController = findNavController(R.id.nav_host_fragment_activity_main)
                navController.navigate(R.id.accountFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}