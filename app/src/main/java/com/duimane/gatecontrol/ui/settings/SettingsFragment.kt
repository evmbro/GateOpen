package com.duimane.gatecontrol.ui.settings

import android.content.Context
import android.content.SharedPreferences
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.duimane.gatecontrol.R
import com.duimane.gatecontrol.api.GateApiService
import com.duimane.gatecontrol.api.model.TokenResponse
import com.duimane.gatecontrol.constants.Constants
import kotlinx.android.synthetic.main.fragment_settings.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {

    private lateinit var serverUrlField: EditText
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var saveButton: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onResume() {
        super.onResume()
        clearUserInput()
        setStatusMessage("")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        serverUrlField = root.findViewById(R.id.server_url_field)
        usernameField = root.findViewById(R.id.username_field)
        passwordField = root.findViewById(R.id.password_field)
        saveButton = root.findViewById(R.id.save_button)
        progressBar = root.findViewById(R.id.settings_pb)
        statusText = root.findViewById(R.id.settings_status_text)

        saveButton.setOnClickListener { saveButtonTapped() }

        return root
    }

    private fun saveButtonTapped() {
        val baseUrl = serverUrlField.text.toString()
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()

        disableUserInput()

        try {
            GateApiService.configure(baseUrl)
            GateApiService.instance()?.getToken(username, password)?.enqueue(object: Callback<TokenResponse> {

                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    enableUserInput()
                    clearUserInput()
                    val token = response.body()?.token
                    if (token is String) {
                        saveSettings(baseUrl, username, password, token)
                        setStatusMessage("Successfully connected to Gate!")
                    } else {
                        setStatusMessage("Error while connecting to Gate. Please try later.")
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    enableUserInput()
                    clearUserInput()
                    setStatusMessage("Error while connecting to Gate. Please try later.")
                }

            })
        } catch (ex: Exception) {
            enableUserInput()
            clearUserInput()
            setStatusMessage("Unexpected error. Check your configuration data and try again.")
        }
    }

    private fun saveSettings(baseUrl: String, username: String, password: String, token: String) {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPreferences.edit()) {
            putString(Constants.BASE_URL_SHARED_PREF_KEY, baseUrl)
            putString(Constants.USERNAME_SHARED_PREF_KEY, username)
            putString(Constants.PASSWORD_SHARED_PREF_KEY, password)
            putString(Constants.TOKEN_SHARED_PREF_KEY, token)
            commit()
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
        statusText.setText(text)
    }

}