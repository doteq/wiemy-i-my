package me.doteq.dolinabaryczy.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.databinding.FragmentQuestContentBinding
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class QuestContentFragment: Fragment(R.layout.fragment_quest_content) {

    lateinit var binding: FragmentQuestContentBinding
    private val viewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentQuestContentBinding.bind(view)

        binding.questTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.selectedQuest.value?.let {quest ->
            binding.questTopAppBar.title = quest.title
            binding.contentTextView.text = quest.content
            quest.fact?.let {
                binding.questCardView.visibility = View.VISIBLE
                binding.factTextView.text = it
            }
            binding.extendedFab.setOnClickListener {
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
                        findNavController().navigate(R.id.action_questContentFragment_to_quizFragment)
                    }
                    .addOnFailureListener {
                        findNavController().navigate(R.id.action_questContentFragment_to_quizFragment)
                    }
            }
        }
    }
}