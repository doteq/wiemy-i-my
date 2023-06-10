package me.doteq.dolinabaryczy.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.data.models.Answer
import me.doteq.dolinabaryczy.data.models.Poi
import me.doteq.dolinabaryczy.data.models.PoiQuest
import me.doteq.dolinabaryczy.data.models.UserAnswerList
import me.doteq.dolinabaryczy.databinding.DialogQuizSummaryBinding
import me.doteq.dolinabaryczy.databinding.FragmentQuizBinding
import me.doteq.dolinabaryczy.databinding.FragmentQuizQuestionBinding
import me.doteq.dolinabaryczy.databinding.ItemQuizAnswerBinding
import me.doteq.dolinabaryczy.ui.viewmodels.MainViewModel
import me.doteq.dolinabaryczy.utilities.Constants
import java.util.Date

class QuizFragment: Fragment(R.layout.fragment_quiz) {
    lateinit var binding: FragmentQuizBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentQuizBinding.bind(view)

        val selectedQuest = viewModel.selectedQuest.value!!

        var checkedTime = false
        var checkedEnd = false

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userAnswerLists.collect { answers ->
                answers.find { it.questId == selectedQuest.id }?.let { userAnswerList ->
                    val timeDiff = Date().time - userAnswerList.lastTryTime.time
                    if (timeDiff < 86400000 && !checkedTime && userAnswerList.answers.count { it == Answer.ANSWER_NONE} == 0) {
                        showTimeoutDialog(86400000 - timeDiff)
                        return@collect
                    }
                    checkedTime = true
                    val collectedPoints =
                        (userAnswerList.answers.count { it == Answer.ANSWER_CORRECT } * Constants.CORRECT_ANSWER_POINTS) +
                                (userAnswerList.answers.count { it == Answer.ANSWER_HALF_POINTS } * Constants.HALF_POINTS_ANSWER_POINTS)

                    if (!checkedEnd && userAnswerList.answers.count { it == Answer.ANSWER_NONE} == 0) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Quiz został już ukończony")
                            .setMessage("Odebrano już $collectedPoints punktów za ten quiz. Możesz spróbować ponownie, ale te punkty zostaną zresetowane.")
                            .setCancelable(false)
                            .setPositiveButton("Spróbuj ponownie") { _, _ ->
                                insertEmptyAnswerList(selectedQuest)
                                viewModel.addPoints(-collectedPoints)
                            }
                            .setNegativeButton("Powrót") { _, _ ->
                                findNavController().popBackStack()
                            }
                            .create()
                            .show()
                        return@collect
                    } else {
                        userAnswerList.answers.let {
                            renderChips(it)
                            loadCurrentQuestion(answers)
                        }
                    }
                    checkedEnd = true



                } ?: run {
                    insertEmptyAnswerList(selectedQuest)
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun loadCurrentQuestion(userAnswerLists: List<UserAnswerList>) {
        val selectedQuest = viewModel.selectedQuest.value!!
        val userAnswers = userAnswerLists.find { it.questId == selectedQuest.id }!!.answers
        val questionIndex = userAnswers.indexOfFirst { it == Answer.ANSWER_NONE }

        if (questionIndex < 0) {
            showSummaryDialog(userAnswers)
            return
        }

        val question = selectedQuest.questions[questionIndex]


        var answerTries = viewModel.incorrectAnswersSelected.size
        val questionBinding = FragmentQuizQuestionBinding.inflate(layoutInflater)


        binding.questionContainer.removeAllViews()
        questionBinding.root.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.nav_default_enter_anim
            )
        )
        binding.questionContainer.addView(questionBinding.root)


        questionBinding.questionTitle.text = question.question

        (question.incorrectAnswers + question.correctAnswer).shuffled().forEach { answer ->
            val answerBinding = ItemQuizAnswerBinding.inflate(layoutInflater)
            questionBinding.questionAnswersContainer.addView(answerBinding.root)
            answerBinding.answerTitle.text = answer

            if (viewModel.incorrectAnswersSelected.contains(answer)) {
                answerBinding.root.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.incorrect_answer)
                )
            }

            answerBinding.root.setOnClickListener {
                answerTries++
                if (answer == question.correctAnswer) {
                    answerBinding.root.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_answer)
                    )

                    val answerType = when (answerTries) {
                        1 -> Answer.ANSWER_CORRECT
                        2 -> Answer.ANSWER_HALF_POINTS
                        else -> Answer.ANSWER_INCORRECT
                    }

                    val answerPoints = when (answerType) {
                        Answer.ANSWER_CORRECT -> Constants.CORRECT_ANSWER_POINTS
                        Answer.ANSWER_HALF_POINTS -> Constants.HALF_POINTS_ANSWER_POINTS
                        Answer.ANSWER_INCORRECT -> 0
                        else -> 0
                    }

                    viewModel.addPoints(answerPoints)

                    userAnswerLists.find { it.questId == selectedQuest.id }?.let {
                        val updated = it.copy(
                            answers = it.answers.mapIndexed { i, answer ->
                                if (i == questionIndex) answerType
                                else answer
                            },
                            lastTryTime = Date()
                        )

                        viewModel.incorrectAnswersSelected.clear()

                        viewModel.insertAnswerList(updated)
                    }

                } else {
                    answerBinding.root.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.incorrect_answer)
                    )
                    viewModel.incorrectAnswersSelected += answer
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderChips(answers: List<Answer>) {
        binding.questionsChips.removeAllViews()
        answers.forEachIndexed { i, answer ->
            binding.questionsChips.addView(
                Chip(requireContext()).apply {
                    text = "${i + 1}"
                    val color = when (answer) {
                        Answer.ANSWER_CORRECT -> R.color.correct_answer
                        Answer.ANSWER_HALF_POINTS -> R.color.half_points_answer
                        Answer.ANSWER_INCORRECT -> R.color.incorrect_answer
                        else -> null
                    }

                    color?.let {
                        setChipBackgroundColorResource(it)
                        chipStrokeWidth = 0F
                    }
                }
            )
        }
    }

    private fun insertEmptyAnswerList(selectedQuest: PoiQuest) {
        viewModel.insertAnswerList(
            UserAnswerList(
                selectedQuest.id,
                List(selectedQuest.questions.size) { Answer.ANSWER_NONE },
                Date(0)
            )
        )
    }

    private fun showTimeoutDialog(remainingMs: Long) {
        val minutes = (remainingMs / 1000) / 60
        val hours = minutes / 60
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quiz został już ukończony")
            .setMessage("Możesz spróbować ponownie za ${if (minutes > 60) hours.toString() + "h" else minutes.toString() + "min"}")
            .setCancelable(false)
            .setPositiveButton("Powrót") { _, _ ->
                findNavController().popBackStack()
            }
            .create()
            .show()
    }

    private fun showSummaryDialog(answers: List<Answer>) {
        val dialogBinding = DialogQuizSummaryBinding.inflate(layoutInflater)

        val correctCount = answers.count { it == Answer.ANSWER_CORRECT }
        val halfPointsCount = answers.count { it == Answer.ANSWER_HALF_POINTS }
        val incorrectCount = answers.count { it == Answer.ANSWER_INCORRECT }

        val points =
            (correctCount * Constants.CORRECT_ANSWER_POINTS) + (halfPointsCount * Constants.HALF_POINTS_ANSWER_POINTS)

        dialogBinding.pointsTextView.text = points.toString()
        dialogBinding.correctAnswersCountTextView.text = correctCount.toString()
        dialogBinding.halfPointsAnswersCountTextView.text = halfPointsCount.toString()
        dialogBinding.incorrectAnswersCountTextView.text = incorrectCount.toString()

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("Powrót") { _, _ ->
                findNavController().popBackStack()
            }
            .create()
            .show()
    }


}