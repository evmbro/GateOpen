package com.duimane.gatecontrol.ui

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
import com.duimane.gatecontrol.R
import com.duimane.gatecontrol.api.GateApiService
import com.duimane.gatecontrol.model.response.ImageResponse
import com.duimane.gatecontrol.model.response.OpenGateResponse
import com.duimane.gatecontrol.service.TokenService
import com.duimane.gatecontrol.util.ImageUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GateFragment : Fragment() {

    private lateinit var gateImageView: ImageView
    private lateinit var openGateButton: Button
    private lateinit var refreshImageButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

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
        openGateButton.setOnClickListener { openGate() }
        return root
    }

    override fun onResume() {
        super.onResume()
        setStatusText("")
        loadImage()
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
        statusText.text = text
    }

}