package com.duimane.gatecontrol.ui.gate

import android.app.usage.ConfigurationStats
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.duimane.gatecontrol.R
import com.duimane.gatecontrol.api.GateApiService
import com.duimane.gatecontrol.api.model.ImageResponse
import com.duimane.gatecontrol.api.model.TokenResponse
import com.duimane.gatecontrol.constants.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class GateFragment : Fragment() {

    private lateinit var gateImageView: ImageView
    private lateinit var openGateButton: Button
    private lateinit var refreshImageButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        refreshToken()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_gate, container, false)
        gateImageView = root.findViewById(R.id.gate_image)
        openGateButton = root.findViewById(R.id.gate_open_button)
        refreshImageButton = root.findViewById(R.id.gate_image_refresh_button)
        progressBar = root.findViewById(R.id.gate_pb)
        statusText = root.findViewById(R.id.gate_status_text)
        refreshImageButton.setOnClickListener { loadImage() }
        return root
    }

    private fun refreshToken() {
        disableUserInput()
        showProgressBar()
        try {
            val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            val url = sharedPreferences.getString(Constants.BASE_URL_SHARED_PREF_KEY, null)
            val username = sharedPreferences.getString(Constants.USERNAME_SHARED_PREF_KEY, null)
            val password = sharedPreferences.getString(Constants.PASSWORD_SHARED_PREF_KEY, null)
            val token = sharedPreferences.getString(Constants.TOKEN_SHARED_PREF_KEY, null)

            if (url == null || username == null || password == null || token == null) {
                hideProgressBar()
                setStatusText("Configure connection to Gate on Settings screen!")
                return
            }

            GateApiService.configure(url)
            GateApiService.instance()?.getToken(username, password)?.enqueue(
                object: Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>
                    ) {
                        if (response.code() == 200) {
                            val token = response.body()!!.token
                            saveSettings(url, username, password, token)
                            setStatusText("Configuration loaded successfully.")
                            enableUserInput()
                            hideProgressBar()
                        } else {
                            hideProgressBar()
                            setStatusText("Configure connection to Gate on Settings screen!")
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        hideProgressBar()
                        setStatusText("Configure connection to Gate on Settings screen!")
                    }
                }
            )
        } catch (ex: Exception) {
            hideProgressBar()
            setStatusText("Unexpected error occured. Configure connection to Gate on Settings screen!")
        }
    }

    private fun loadImage() {
        disableUserInput()
        showProgressBar()
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val token = sharedPreferences.getString(Constants.TOKEN_SHARED_PREF_KEY, null)
        if (token == null) {
            hideProgressBar()
            setStatusText("Unexpected error occured. Configure connection to Gate on Settings screen!")
        } else {
            GateApiService.instance()?.getImage("Token $token")?.enqueue(
                object: Callback<ImageResponse> {
                    override fun onResponse(
                        call: Call<ImageResponse>,
                        response: Response<ImageResponse>
                    ) {
                        hideProgressBar()
                        enableUserInput()
                        if (response.code() == 200) {
                            val imageData = response.body()?.photo!!
                            val decodedImageData = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedImageData, 0, decodedImageData.size)
                            gateImageView.setImageBitmap(bitmap)
                        } else {
                            setStatusText("Error while loading image. Check configuration on Settings screen!")
                        }
                    }

                    override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                        hideProgressBar()
                        enableUserInput()
                        setStatusText("Error while loading image. Check configuration on Settings screen!")
                    }
                }
            )
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
        openGateButton.isEnabled = true
        refreshImageButton.isEnabled = true
    }

    private fun disableUserInput() {
        openGateButton.isEnabled = false
        refreshImageButton.isEnabled = false
    }

    private fun showProgressBar() {
        progressBar.isVisible = true
    }

    private fun hideProgressBar() {
        progressBar.isVisible = false
    }

    private fun setStatusText(text: String) {
        statusText.setText(text)
    }

}