package com.duimane.gateopen.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.duimane.gateopen.api.GateApiService
import com.duimane.gateopen.databinding.FragmentGateBinding
import com.duimane.gateopen.model.response.ImageResponse
import com.duimane.gateopen.model.response.OpenGateResponse
import com.duimane.gateopen.service.TokenService
import com.duimane.gateopen.util.ImageUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GateFragment : Fragment() {

    private var _binding: FragmentGateBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var gateImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var openGateButton: View
    private lateinit var refreshImageButton: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGateBinding.inflate(inflater, container, false)
        bindViews()
        return binding.root
    }

    private fun bindViews() {
        gateImageView = binding.gateImage
        progressBar = binding.gatePb
        statusText = binding.gateStatusText
        openGateButton = binding.gateOpenButton
        refreshImageButton = binding.gateImageRefreshButton
        openGateButton.setOnClickListener { openGate() }
        refreshImageButton.setOnClickListener { loadImage() }
    }

    override fun onResume() {
        super.onResume()
        setStatusText("")
        loadImage()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    private fun loadImage(withRetryAttempt: Boolean = true) {
        setStatusText("")
        GateApiService.instance()?.let { api ->
            disableUserInput()
            showProgressBar()
            api.getImage().enqueue(object: Callback<ImageResponse> {
                override fun onResponse(
                    call: Call<ImageResponse>,
                    response: Response<ImageResponse>
                ) {
                    hideProgressBar()
                    enableUserInput()

                    val photo = response.body()?.photo
                    if (response.code() == 200 && photo != null) {
                        setStatusText("Image loaded!")
                        gateImageView.setImageBitmap(ImageUtils.base64ToBitmap(photo))
                    } else if (response.code() == 401) {
                        if (!withRetryAttempt) {
                            setStatusText("Unauthorized! Please configure connection at Settings screen.")
                            return
                        }
                        disableUserInput()
                        showProgressBar()
                        TokenService.fetchAndStore(
                            activity,
                            onComplete = {
                                loadImage(false)
                            },
                            onError = { error ->
                                hideProgressBar()
                                enableUserInput()
                                setStatusText(error)
                            }
                        )
                    } else {
                        setStatusText("Unexpected error occurred.")
                    }
                }

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                    hideProgressBar()
                    enableUserInput()
                    setStatusText("Unexpected error occurred.")
                }
            })
        } ?: run {
            setStatusText("Not connected to Gate. Please configure connection at Settings screen.")
        }
    }

    private fun openGate(withRetryAttempt: Boolean = true) {
        setStatusText("")
        GateApiService.instance()?.let { api ->
            disableUserInput()
            showProgressBar()
            api.openGate().enqueue(object: Callback<OpenGateResponse> {
                override fun onResponse(
                    call: Call<OpenGateResponse>,
                    response: Response<OpenGateResponse>
                ) {
                    hideProgressBar()
                    enableUserInput()
                    val message = response.body()?.message
                    if (response.code() == 200 && message is String) {
                        setStatusText("Action sent!")
                    } else if (response.code() == 401) {
                        if (!withRetryAttempt) {
                            setStatusText("Unauthorized! Please configure connection at Settings screen.")
                            return
                        }
                        disableUserInput()
                        showProgressBar()
                        TokenService.fetchAndStore(
                            activity,
                            onComplete = {
                                openGate(false)
                            },
                            onError = { error ->
                                hideProgressBar()
                                enableUserInput()
                                setStatusText(error)
                            }
                        )
                    } else {
                        setStatusText("Unexpected error occurred.")
                    }
                }

                override fun onFailure(call: Call<OpenGateResponse>, t: Throwable) {
                    hideProgressBar()
                    enableUserInput()
                    setStatusText("Not connected to Gate. Please configure connection at Settings screen.")
                }
            })
        } ?: run {
            setStatusText("Not connected to Gate. Please configure connection at Settings screen.")
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
        if (text.isEmpty()) {
            statusText.text = ""
            statusText.visibility = View.GONE
        } else {
            statusText.text = text
            statusText.visibility = View.VISIBLE
        }
    }

}
