package me.doteq.dolinabaryczy.ui
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.databinding.ActivityMainBinding
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import me.doteq.dolinabaryczy.utilities.Constants

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToTripFragmentIfNeeded(intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTripFragmentIfNeeded(intent)
    }

    private fun navigateToTripFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRIP_FRAGMENT) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navHostFragment.navController.navigate(R.id.action_global_tripFragment)
        }
    }
}