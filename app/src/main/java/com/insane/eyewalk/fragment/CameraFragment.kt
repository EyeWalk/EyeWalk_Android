package com.insane.eyewalk.fragment

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.insane.eyewalk.R
import com.insane.eyewalk.config.Constants
import com.insane.eyewalk.config.Player
import com.insane.eyewalk.databinding.FragmentCameraBinding
import com.insane.eyewalk.utils.Tools
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.insane.eyewalk.fragment.CameraFragment.ButtonState.*

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraShutter: Player


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCameraBinding.inflate(inflater, container, false)
        outputDirectory = getOutputDirectory()
        cameraShutter = Player(requireContext(),R.raw.shutter)
        setClickListeners()
        return binding.root
    }

    override fun onPause() {
        cameraExecutor.shutdown()
        super.onPause()
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionGranted()) {
            startCamera()
        } else {
            Tools.Show.message(requireContext(), "É necessário o acesso a camera para utilizar o identificador.")
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.CAMERA_REQUIRED_PERMISSIONS,
                Constants.CAMERA_REQUEST_CODE_PERMISSION
            )
        }
    }

    private fun setClickListeners() {
        binding.btnCaptureCamera.setOnClickListener {
            takePhoto()
        }
        binding.btnProcessCamera.setOnClickListener {
            Tools.Show.message(activity, "Processando imagem")
        }
        binding.btnResultCamera.let {btn ->
            btn.setOnClickListener {
                Tools.Show.message(activity, "Falando: ${btn.text}")
            }
        }
        binding.ivCapturedImage.let { iv ->
            iv.setOnClickListener {
                iv.visibility = View.GONE
                setButtonState(CAPTURE)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.mCameraView.surfaceProvider
                    )
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    requireActivity(), cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(Constants.CAM_TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    private fun takePhoto() {
        cameraShutter.play()
        setButtonState(PROCESS)
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(Constants.CAMERA_FILE_NAME_FORMAT,
            Locale.getDefault()).format(System.currentTimeMillis())+".jpg")

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(requireContext()),
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    identifyImage(savedUri)
                    cameraShutter.stop()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.CAM_TAG, "onError: ${exception.message}",exception)
                    cameraShutter.stop()
                }
            }
        )
    }

    private fun setButtonState(state: ButtonState, text: String = "") {
        val btnCapture: Button = binding.btnCaptureCamera
        val btnProcess: Button = binding.btnProcessCamera
        val btnResult: Button = binding.btnResultCamera

        btnCapture.visibility = View.GONE
        btnProcess.visibility = View.GONE
        btnResult.visibility = View.GONE

        when(state) {
            CAPTURE -> {
                btnCapture.visibility = View.VISIBLE
            }
            PROCESS -> {
                btnProcess.visibility = View.VISIBLE
            }
            RESULT -> {
                btnResult.visibility = View.VISIBLE
                btnResult.text = text
            }
        }
    }

    private fun identifyImage(uri: Uri) {
        binding.ivCapturedImage.let{ iv ->
            iv.setImageURI(uri)
            iv.visibility = View.VISIBLE
            setButtonState(RESULT, "descrição da foto")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE_PERMISSION) {
            if (allPermissionGranted())
                startCamera()
        } else {
            Tools.Show.message(requireContext(),"É necessário permitir o acesso a camera.")
        }
    }

    private fun allPermissionGranted() =
        Constants.CAMERA_REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }

    enum class ButtonState {
        CAPTURE,
        PROCESS,
        RESULT
    }

}