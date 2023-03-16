package com.duimane.gateopen.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.duimane.gateopen.databinding.FragmentSettingsBinding
import com.duimane.gateopen.service.TokenService

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var serverUrlField: EditText
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var saveButton: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        serverUrlField = binding.serverUrlField
        usernameField = binding.usernameField
        passwordField = binding.passwordField
        saveButton = binding.saveButton
        progressBar = binding.settingsPb
        statusText = binding.settingsStatusText
        saveButton.setOnClickListener { saveButtonTapped() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        clearUserInput()
        setStatusMessage("")
    }

    private fun saveButtonTapped() {
        val baseUrl = serverUrlField.text.toString()
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()

        if (baseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            setStatusMessage("Error: some filed values are missing!")
            return
        }

        setStatusMessage("")
        disableUserInput()
        try {
            TokenService.fetchAndStore(
                baseUrl,
                username,
                password,
                activity,
                onComplete = {
                    enableUserInput()
                    clearUserInput()
                    setStatusMessage("Connected to Gate. Configuration saved!")
                },
                onError = { error ->
                    enableUserInput()
                    setStatusMessage(error)
                }
            )
        } catch (ex: Exception) {
            enableUserInput()
            clearUserInput()
            setStatusMessage("Unexpected error. Check your configuration data and try again.")
        }
    }

    private fun enableUserInput() {
        saveButton.isEnabled = true
        serverUrlField.isEnabled = true
        usernameField.isEnabled = true
        passwordField.isEnabled = true
        statusText.isVisible = true
        progressBar.isVisible = false
    }

    private fun disableUserInput() {
        saveButton.isEnabled = false
        serverUrlField.isEnabled = false
        usernameField.isEnabled = false
        passwordField.isEnabled = false
        statusText.isVisible = false
        progressBar.isVisible = true
    }

    private fun clearUserInput() {
        serverUrlField.setText("")
        usernameField.setText("")
        passwordField.setText("")
    }

    private fun setStatusMessage(text: String) {
        statusText.text = text
    }

}
