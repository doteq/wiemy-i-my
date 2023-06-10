package me.doteq.dolinabaryczy.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.data.models.Answer
import me.doteq.dolinabaryczy.data.models.Poi
import me.doteq.dolinabaryczy.data.models.UserAnswerList
import me.doteq.dolinabaryczy.databinding.FragmentLocationBinding
import me.doteq.dolinabaryczy.databinding.ItemQuestBinding
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import me.doteq.dolinabaryczy.utilities.Utilities
import javax.inject.Inject

@AndroidEntryPoint
class LocationFragment: Fragment(R.layout.fragment_location) {

    lateinit var binding: FragmentLocationBinding
    private val viewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLocationBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userAnswerLists.collect { answerLists ->
                updateView(viewModel.selectedPoi.value!!, answerLists)
            }
        }


        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackedPoi.collect {
                binding.topAppBar.menu[0].apply {
                    if (viewModel.trackedPoi.value?.id != viewModel.selectedPoi.value?.id) {
                        icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_my_location_24)
                        title = "Śledź"
                    } else {
                        icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_location_disabled_24)
                        title = "Przestań śledzić"
                    }
                }
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.track_menu_button -> {
                    viewModel.setTrackedPoi(if (viewModel.trackedPoi.value?.id != viewModel.selectedPoi.value?.id) viewModel.selectedPoi.value else null)
                    true
                }

                else -> false
            }
        }

    }


    @SuppressLint("MissingPermission")
    private fun updateView(poi: Poi, answerLists: List<UserAnswerList>) {
        poi.quests.forEach { quest ->
            val questBinding = ItemQuestBinding.inflate(layoutInflater)
            questBinding.questTitle.text = quest.title
            questBinding.questSubtitle.text = quest.subtitle

            answerLists.find { it.questId == quest.id }?.answers?.let { answers ->
                val correct = answers.count { it == Answer.ANSWER_CORRECT }
                val notCorrect = answers.count { it == Answer.ANSWER_INCORRECT || it == Answer.ANSWER_HALF_POINTS }
                if (correct == quest.questions.size) {
                    questBinding.statusIndicator.setImageResource(R.drawable.ic_baseline_check_24)
                } else if (notCorrect > 0) {
                    questBinding.statusIndicator.setImageResource(R.drawable.ic_baseline_remove_24)
                }
            }

            questBinding.questItemCardView.setOnClickListener {
                viewModel.setSelectedQuest(quest)
                if (quest.displayContent) {
                    findNavController().navigate(R.id.action_locationFragment_to_questContentFragment)
                } else {
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { location: Location? ->

                            val nextLocation = Location("nextPointLocation")
                            nextLocation.latitude = viewModel.selectedPoi.value!!.lat
                            nextLocation.longitude = viewModel.selectedPoi.value!!.lon

                            location?.distanceTo(nextLocation)?.let {
                                if (it > 100) {
                                    Snackbar.make(
                                        binding.root,
                                        "Musisz być w pobliżu punktu aby rozwiązać quiz",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }
                            }
                            findNavController().navigate(R.id.action_locationFragment_to_quizFragment)
                        }
                        .addOnFailureListener {
                            findNavController().navigate(R.id.action_locationFragment_to_quizFragment)
                        }
                }

            }
            binding.locationContent.addView(questBinding.root)
        }
        binding.topAppBar.title = poi.name

        Utilities.getPointDrawable(poi.id).let {
            binding.imageView.setImageResource(it ?: 0)
            binding.imageCardView.visibility = View.VISIBLE
        }
    }
}