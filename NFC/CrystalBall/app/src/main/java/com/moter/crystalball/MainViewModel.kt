package com.moter.crystalball

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moter.crystalball.utl.DefaultStateDelegate
import com.moter.crystalball.utl.PromptHelper
import com.moter.crystalball.utl.StateDelegate
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(),
    StateDelegate<MainScreenState> by DefaultStateDelegate(initialState = MainScreenState()) {

    private fun generateFortuneTelling(prompt: String) {
        viewModelScope.launch {
            val response = GeminiChatProvider.generativeModel.generateContent(prompt)
            response.text?.let {
                updateState {
                    copy(
                        response = it,
                        isPlaying = false,
                        isDisplayingResult = true
                    )
                }
            }
        }
    }

    fun handleEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.FortuneTelling -> generateFortuneTelling(event.prompt)
            is MainScreenEvent.NfcScanned -> handleNfcScanned(event.tagValue)
            is MainScreenEvent.DismissResult -> handleDismissResult()
        }
    }

    private fun handleDismissResult() {
        updateState {
            copy(isDisplayingResult = false)
        }
    }

    private fun handleNfcScanned(tagValue: String) {
        val prompt = PromptHelper.generatePrompt(tagValue)
        handleEvent(MainScreenEvent.FortuneTelling(prompt.second))
        updateState {
            copy(
                isPlaying = true,
                nfcValue = tagValue,
                emotionalStates = prompt.first
            )
        }
    }
}


data class MainScreenState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isPlaying: Boolean = false,
    val isDisplayingResult: Boolean = false,
    val nfcValue: String = "",
    val emotionalStates: String = "",
    val response: String = "",
)


sealed interface MainScreenEvent {
    data class FortuneTelling(val prompt: String) : MainScreenEvent

    data class NfcScanned(val tagValue: String) : MainScreenEvent

    object DismissResult : MainScreenEvent
}