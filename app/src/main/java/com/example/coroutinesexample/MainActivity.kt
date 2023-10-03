package com.example.coroutinesexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.coroutinesexample.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { MainActivityViewModel() }

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setupUI()
    }

    private fun setupUI() {
        mainBinding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.state.collect {
                        render(it)
                    }
                }
            }

            this.buttonRun.setOnClickListener {
                viewModel.startLongRunningTask()
            }

            this.buttonClear.setOnClickListener {
                viewModel.clearText()
            }

            this.buttonCancel.setOnClickListener {
                viewModel.cancelLongRunningTask()
            }

            this.buttonFetch.setOnClickListener {
                viewModel.fetchData()
            }
        }
    }

    private fun clearTextView() {
        mainBinding.textOutput.text = ""
    }

    private fun appendText(newText: String) {
        mainBinding.textOutput.append(newText)
    }

    private fun render(state: State) {
        when (state) {
            is State.Running -> {
                appendText("Task started\n")
            }

            is State.Finished -> {
                appendText("Task finished\n")
            }

            is State.Clear -> {
                clearTextView()
            }

            is State.Cancel -> {
                appendText("Task was cancelled\n")
            }

            is State.NewData -> {
                appendText(state.newText)
            }
        }
    }
}