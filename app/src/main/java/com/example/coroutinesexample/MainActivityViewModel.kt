package com.example.coroutinesexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

sealed class State {
    data object Running : State()
    data object Finished : State()
    data object Clear : State()
    data object Cancel : State()

    data class NewData(val newText: String) : State()
}

class MainActivityViewModel : ViewModel() {
    private var longRunningTaskJob: Job? = null

    private val _state = MutableSharedFlow<State>()
    val state = _state.asSharedFlow()

    private fun emitState(newState: State) {
        viewModelScope.launch {
            _state.emit(newState)
        }
    }

    fun clearText() {
        emitState(State.Clear)
    }

    private fun onNewData(newText: String) {
        emitState(State.NewData(newText))
    }

    fun startLongRunningTask() {
        cancelLongRunningTask()
        clearText()

        emitState(State.Running)

        longRunningTaskJob = viewModelScope.launch {
            try {
                for (i in 1..10) {
                    onNewData("Running task $i\n")
                    delay(1000)
                }

                emitState(State.Finished)
            } catch (e: CancellationException) {
                emitState(State.Cancel)
            }
        }
    }

    fun cancelLongRunningTask() {
        longRunningTaskJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelLongRunningTask()
    }

    // data fetch example
    private suspend fun doSomething(): String {
        delay(500)

        return "Hello"
    }

    private suspend fun doSomethingElse(): String {
        delay(1500)

        return "World"
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            val asyncMillis = measureTimeMillis {
                val result1 = async { doSomething() }
                val result2 = async { doSomethingElse() }

                println("result1: ${result1.await()}, result2: ${result2.await()}")
            }

            println("asyncMillis: $asyncMillis")

            val sequentialMillis = measureTimeMillis {
                val result1 = doSomething()
                val result2 = doSomethingElse()

                println("result1: $result1, result2: $result2")
            }

            println("sequentialMillis: $sequentialMillis")

            val fetchedInText = "fetched async in $asyncMillis ms\nsequential in: $sequentialMillis ms\n"

            emitState(State.NewData(fetchedInText))
        }
    }
}