package me.doteq.dolinabaryczy.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.doteq.dolinabaryczy.data.models.*
import me.doteq.dolinabaryczy.data.repositories.DataStoreRepository
import me.doteq.dolinabaryczy.data.repositories.MainRepository
import me.doteq.dolinabaryczy.utilities.Constants.MAP_DEFAULT_LATITUDE
import me.doteq.dolinabaryczy.utilities.Constants.MAP_DEFAULT_LONGITUDE
import me.doteq.dolinabaryczy.utilities.Constants.MAP_DEFAULT_ZOOM
import me.doteq.dolinabaryczy.utilities.MapState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repository: MainRepository,
    val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val locationTrackingData: MutableLiveData<Boolean> = MutableLiveData(false)
    val locationTracking: LiveData<Boolean> = locationTrackingData
    fun setLocationTracking(value: Boolean) {
        locationTrackingData.value = value
    }

    private val mapStateData: MutableLiveData<MapState> = MutableLiveData<MapState>(MapState.MAP_LOCATION_DETAILS)
    val mapState: LiveData<MapState> = mapStateData
    fun setMapState(value: MapState) {
        mapStateData.value = value
    }

    private val _mapPosition: MutableStateFlow<MapPosition> = MutableStateFlow(MapPosition(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE, MAP_DEFAULT_ZOOM))
    val mapPosition: StateFlow<MapPosition> = _mapPosition
    fun setMapPosition(position: MapPosition) {
        _mapPosition.value = position
    }

    private val _selectedPoi: MutableStateFlow<Poi?> = MutableStateFlow(null)
    val selectedPoi: StateFlow<Poi?> = _selectedPoi
    fun setSelectedPoi(poi: Poi?) {
        _selectedPoi.value = poi
    }

    private val _trackedPoi: MutableStateFlow<Poi?> = MutableStateFlow(null)
    val trackedPoi: StateFlow<Poi?> = _trackedPoi
    fun setTrackedPoi(poi: Poi?) {
        _trackedPoi.value = poi
    }

    private val _selectedQuest: MutableStateFlow<PoiQuest?> = MutableStateFlow(null)
    val selectedQuest: StateFlow<PoiQuest?> = _selectedQuest
    fun setSelectedQuest(poiQuest: PoiQuest?) {
        _selectedQuest.value = poiQuest
    }

    private val _nearestPoints: MutableStateFlow<List<Poi>> = MutableStateFlow(emptyList())
    val nearestPoints: StateFlow<List<Poi>> = _nearestPoints
    fun setNearestPoints(nearestPoints: List<Poi>) {
        _nearestPoints.value = nearestPoints
    }

    private val _permissionsVisible: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val permissionsVisible: StateFlow<Boolean> = _permissionsVisible
    fun setPermissionsVisible(permissionsVisible: Boolean) {
        _permissionsVisible.value = permissionsVisible
    }


    val userAnswerLists = repository.getAllAnswerLists()
    fun insertAnswerList(answers: UserAnswerList) {
        viewModelScope.launch {
            repository.insertAnswerList(answers)
        }
    }

    fun deleteAnswerLists(idList: List<String>) {
        viewModelScope.launch {
            repository.deleteAnswerLists(idList)
        }
    }

    val points = dataStoreRepository.getPoints()
    fun addPoints(value: Int) {
        viewModelScope.launch {
            dataStoreRepository.addPoints(value)
        }
    }


    val incorrectAnswersSelected: MutableList<String> = mutableListOf()



}