package me.doteq.dolinabaryczy.ui.fragments

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.QueriedFeature
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.SourceQueryOptions
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.data.models.MapPosition
import me.doteq.dolinabaryczy.data.models.Poi
import me.doteq.dolinabaryczy.data.models.PoiQuest
import me.doteq.dolinabaryczy.databinding.FragmentMapBinding
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import me.doteq.dolinabaryczy.utilities.Constants.MAP_DEFAULT_SELECTION_ZOOM
import me.doteq.dolinabaryczy.utilities.Utilities
import me.doteq.dolinabaryczy.utilities.Utilities.bitmapFromDrawableRes
import me.doteq.dolinabaryczy.utilities.Utilities.isDarkThemeOn


@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), OnMoveListener, OnMapClickListener, OnMapLoadedListener {

    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainViewModel by activityViewModels()
    private var userLocation: Point? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapBinding.bind(view)

        viewModel.locationTracking.observeForever {
            if (it) moveToUserLocation()
        }

        initMap()

    }

    private fun initMap() {
        viewModel.mapPosition.value.let {
            binding.mapView.getMapboxMap().setCamera(
                getCameraFromPosition(it.lat, it.lon, it.zoom)
            )
        }

        if (findNavController().currentDestination?.id == R.id.locationFragment)
            viewModel.selectedPoi.value?.let {
                binding.mapView.getMapboxMap().flyTo(
                    getCameraFromPosition(it.lat, it.lon, 15.0)
                )
            }

        binding.mapView.location.addOnIndicatorPositionChangedListener {
            userLocation = it
            if (viewModel.locationTracking.value == true) moveToUserLocation()
            getPOIs()
        }

        binding.mapView.getMapboxMap().loadStyleUri(
            if (isDarkThemeOn(resources)) "mapbox://styles/doteq/ckpgtc3lf2gfz17l93fdxjh7k" else "mapbox://styles/doteq/clf7bo3n4001401qoy59a7w34"
        ) {
            if (Utilities.hasLocationPermissions(requireContext())) {
                binding.mapView.location.enabled = true
                binding.mapView.location.pulsingEnabled = true
            }
        }

        binding.mapView.scalebar.enabled = false
        binding.mapView.compass.fadeWhenFacingNorth = false
        binding.mapView.gestures.addOnMoveListener(this@MapFragment)

        binding.mapView.getMapboxMap().addOnMapClickListener(this@MapFragment)
        binding.mapView.getMapboxMap().addOnMapLoadedListener(this@MapFragment)

        pointAnnotationManager = binding.mapView.annotations.createPointAnnotationManager(
            AnnotationConfig(null, "tracked_symbols", "tracked_symbols")
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackedPoi.collect { poi ->
                if (poi == null) {
                    pointAnnotationManager.deleteAll()
                    setPointsVisibility(true)
                } else {
                    pointAnnotationManager.deleteAll()
                    bitmapFromDrawableRes(
                        requireContext(),
                        R.drawable.ic_trip_point_selected
                    )?.let {
                        val options = PointAnnotationOptions()
                            .withPoint(Point.fromLngLat(poi.lon,poi.lat))
                            .withIconImage(it)
                            .withData(GsonBuilder().serializeNulls().create().toJsonTree(poi))

                        pointAnnotationManager.create(options)
                    }
                    setPointsVisibility(false)
                }
            }
        }

    }


    override fun onMapClick(point: Point): Boolean {
        binding.mapView.getMapboxMap().queryRenderedFeatures(
            RenderedQueryGeometry(binding.mapView.getMapboxMap().pixelForCoordinate(point)),
            RenderedQueryOptions(listOf("symbols", "tracked_symbols"), null)
        ) { result ->
            onFeatureClicked(result) { query ->
                val poi = query.feature.getProperty("custom_data")?.toString()?.let {
                     Json.decodeFromString<Poi>(it)
                }
                val quests = poi?.quests ?: Json.decodeFromString<List<PoiQuest>>(query.feature.getProperty("quests").asString)
                val geometry = query.feature.geometry() as Point
                viewModel.setSelectedPoi(Poi(
                    poi?.id ?: query.feature.getProperty("id").asString,
                    poi?.name ?: query.feature.getProperty("name").asString,
                    geometry.latitude(),
                    geometry.longitude(),
                    quests
                ))
                findNavController().navigate(R.id.action_tripFragment_to_locationFragment)
            }
        }
        return true
    }

    private fun onFeatureClicked(
        expected: Expected<String, List<QueriedFeature>>,
        onFeatureClicked: (QueriedFeature) -> Unit
    ) {
        if (expected.isValue && expected.value?.size!! > 0) {
            expected.value?.get(0)?.feature?.let {
                onFeatureClicked.invoke(expected.value!![0])
            }
        }
    }

    private fun moveToUserLocation() {
        userLocation?.let {
            binding.mapView.camera.flyTo(
                CameraOptions.Builder()
                    .center(userLocation)
                    .zoom(MAP_DEFAULT_SELECTION_ZOOM)
                    .build()
            )
        }
    }

    private fun getPOIs() {
        binding.mapView.getMapboxMap().querySourceFeatures(
            "composite",
            SourceQueryOptions(listOf("dolinabaryczy_points"), Value.nullValue())
        ) { expected ->
            expected.value?.let { featuresList ->
                viewModel.setNearestPoints(featuresList.distinctBy { it.feature.getProperty("id") }.map { query ->
                    val quests = Json.decodeFromString<List<PoiQuest>>(query.feature.getProperty("quests").asString)
                    val geometry = query.feature.geometry() as Point

                    var distance: Float? = null

                    userLocation?.let {
                        val result = FloatArray(2)
                        Location.distanceBetween(geometry.latitude(), geometry.longitude(), it.latitude(), it.longitude(), result)
                        distance = result[0]
                    }

                    Poi(
                        query.feature.getProperty("id").asString,
                        query.feature.getProperty("name").asString,
                        geometry.latitude(),
                        geometry.longitude(),
                        quests,
                        distance
                    )
                }.sortedBy { it.distance })
            }
        }
    }

    override fun onDestroyView() {
        binding.mapView.getMapboxMap().cameraState.apply {
            viewModel.setMapPosition(MapPosition(this.center.latitude(), this.center.longitude(), this.zoom))
        }
        super.onDestroyView()
    }

    private fun getCameraFromPosition(latitude: Double, longitude: Double, zoom: Double = 12.0): CameraOptions {
        return CameraOptions.Builder()
            .center(Point.fromLngLat(longitude, latitude))
            .zoom(zoom)
            .build()
    }

    private fun setPointsVisibility(visible: Boolean) {
        binding.mapView.getMapboxMap().getStyle {
            it.getLayer("symbols")?.visibility(if (visible) Visibility.VISIBLE else Visibility.NONE)
        }
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        viewModel.setLocationTracking(false)
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {}

    override fun onMoveEnd(detector: MoveGestureDetector) {}
    override fun onMapLoaded(eventData: MapLoadedEventData) {
        getPOIs()
    }
}