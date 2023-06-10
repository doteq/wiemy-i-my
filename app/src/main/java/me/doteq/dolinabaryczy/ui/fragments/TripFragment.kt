package me.doteq.dolinabaryczy.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.carousel.CarouselLayoutManager
import com.johnnylambada.orientation.OrientationConsumer
import com.johnnylambada.orientation.OrientationReporter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.adapters.NearestPointsAdapter
import me.doteq.dolinabaryczy.databinding.FragmentTripBinding
import me.doteq.dolinabaryczy.services.TripService
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import me.doteq.dolinabaryczy.utilities.Constants.ACTION_START_OR_RESUME_SERVICE
import me.doteq.dolinabaryczy.utilities.Constants.ACTION_STOP_SERVICE
import me.doteq.dolinabaryczy.utilities.Utilities.circularAverage
import me.doteq.dolinabaryczy.utilities.Utilities.toDistanceString
import javax.inject.Inject


@AndroidEntryPoint
class TripFragment : Fragment(R.layout.fragment_trip) {

    lateinit var binding: FragmentTripBinding
    private val viewModel: MainViewModel by activityViewModels()
    var bearing: Float? = null
    var azimuthArray = Array(20) { 0.0 }


    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        TripService.trackedPoi.value?.let {
            viewModel.setTrackedPoi(it)
        }
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTripBinding.bind(view)

        lifecycle.addObserver(OrientationReporter(requireContext(), onCompassUpdate))

        checkPermissions()
        binding.showHidePermissions.setOnClickListener {
            if (binding.permissionsLayout.visibility == View.VISIBLE) {
                binding.permissionsLayout.visibility =  View.GONE
                binding.permissionsHideIcon.rotation = 0f
            } else {
                binding.permissionsLayout.visibility =  View.VISIBLE
                binding.permissionsHideIcon.rotation = 180f
            }


        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackedPoi.collect {
                it?.let {
                    TripService.trackedPoi.value = it
                    sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                    binding.poiTitle.text = it.name
                    binding.nextPoiCardView.visibility = View.VISIBLE
                    binding.nearestPointsCarousel.visibility = View.GONE
                    binding.extendedFab.visibility = View.GONE
                    binding.navigationBackButton.visibility = View.VISIBLE
                    binding.routingButton.visibility = View.VISIBLE
                    binding.showHidePermissions.visibility = View.GONE
                    viewModel.setPermissionsVisible(false)
                } ?: run {
                    TripService.trackedPoi.value = null
                    sendCommandToService(ACTION_STOP_SERVICE)
                    binding.nextPoiCardView.visibility = View.GONE
                    binding.nearestPointsCarousel.visibility = View.GONE
                    binding.extendedFab.visibility = View.VISIBLE
                    binding.navigationBackButton.visibility = View.GONE
                    binding.routingButton.visibility = View.GONE
                    checkPermissions()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.permissionsVisible.collect {
                if (it) {
                    binding.permissionsLayout.visibility = View.VISIBLE
                    binding.permissionsHideIcon.rotation = 180f
                } else {
                    binding.permissionsLayout.visibility = View.GONE
                    binding.permissionsHideIcon.rotation = 0f
                }
            }
        }

        binding.nearestPointsCarousel.layoutManager = CarouselLayoutManager()

        binding.extendedFab.setOnClickListener {
            if (viewModel.nearestPoints.value.isNotEmpty()) {
                val adapter = NearestPointsAdapter(viewModel.nearestPoints.value)
                adapter.setOnSuggestionClickListener {
                    viewModel.setTrackedPoi(it)

                }
                binding.nearestPointsCarousel.adapter = adapter
                TransitionManager.beginDelayedTransition(binding.mapUiContainer)
                binding.nearestPointsCarousel.visibility = View.VISIBLE
                binding.extendedFab.visibility = View.GONE
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.points.collect { points ->
                binding.pointsTextView.text = points.toString()
            }
        }

        TripService.direction.observe(viewLifecycleOwner) { direction ->
            bearing = direction.bearing
            direction.distance?.let {
                binding.textViewDistance.text = it.toDistanceString()
                binding.textViewDistance.visibility = View.VISIBLE
                binding.poiStatus.visibility = View.VISIBLE
                binding.poiStatus.text =
                    if (it < 100) "Jesteś w pobliżu punktu" else "Podejdź bliżej aby odblokować quizy"
            } ?: run {
                binding.textViewDistance.visibility = View.GONE
                binding.poiStatus.visibility = View.GONE
            }

        }

        binding.nextPoiCardView.setOnClickListener {
            viewModel.trackedPoi.value?.let {
                viewModel.setSelectedPoi(it)
            }

            findNavController().navigate(R.id.action_tripFragment_to_locationFragment)
        }

        binding.navigationBackButton.setOnClickListener {
            viewModel.setTrackedPoi(null)
        }

        binding.routingButton.setOnClickListener {
            viewModel.trackedPoi.value?.let {
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${it.lat}, ${it.lon}&mode=b")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }

    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TripService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private val onCompassUpdate = OrientationConsumer { azimuth: Float, _, _ ->
        var rotation = 0F
        bearing?.let {
            azimuthArray = azimuthArray.copyOfRange(1, azimuthArray.size) + -azimuth.toDouble()
            rotation = (azimuthArray.circularAverage() * 180.0 / Math.PI).toFloat() + it
        }
        binding.imageView.rotation = rotation
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ ->
            checkPermissions()
        }
    private fun checkPermissions() {
        var showButton = false
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                binding.notificationRequestCardView.visibility = View.GONE
            }
            android.os.Build.VERSION.SDK_INT < 33 -> {
                binding.notificationRequestCardView.visibility = View.GONE
            }
            else -> {
                showButton = true
                binding.notificationRequestCardView.setOnClickListener {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }

            }
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                binding.locationRequestCardView.visibility = View.GONE
            }
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (android.os.Build.VERSION.SDK_INT < 29) {
                    binding.locationRequestCardView.visibility = View.GONE
                }
                showButton = true
                binding.locationRequestTitle.text =
                    "Włącz lokalizację w tle, aby dostać powiadomienie, kiedy znajdziesz się blisko punktu"
                binding.locationRequestDescription.text =
                    """Kliknij aby przejść do ustawień i wybierz opcję "Zawsze zezwalaj" """
                binding.locationRequestCardView.setOnClickListener {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }
            }

            else -> {
                showButton = true
                binding.locationRequestCardView.setOnClickListener {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }

            }
        }

        if (
            binding.notificationRequestCardView.visibility == View.GONE &&
            binding.locationRequestCardView.visibility == View.GONE
        ) viewModel.setPermissionsVisible(false)
        binding.showHidePermissions.visibility = if (showButton) View.VISIBLE else View.GONE
    }


}