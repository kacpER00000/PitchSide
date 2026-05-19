package com.example.pitchside

import android.content.Context
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
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pitchside.databinding.ActivityMainBinding
import com.example.pitchside.managers.SessionManager
import com.example.pitchside.workers.DatabaseSyncWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.topToolbar
        setSupportActionBar(toolbar)

        supportActionBar?.title = "PitchSide"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.scheduledFragment, R.id.navigation_home, R.id.favouriteFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            invalidateOptionsMenu()
        }

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
        scheduleDailyDatabaseSync(applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val loginItem = menu?.findItem(R.id.action_login)
        val accountItem = menu?.findItem(R.id.action_account)

        val isLogged = SessionManager.isLoggedIn()

        loginItem?.isVisible = !isLogged
        accountItem?.isVisible = isLogged

        if (isLogged) {
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

    fun scheduleDailyDatabaseSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DatabaseSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailySync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}