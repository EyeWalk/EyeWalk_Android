package com.insane.eyewalk.fragment

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.insane.eyewalk.fragment.CameraFragment.ButtonState.*
import com.insane.eyewalk.utils.Tools
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Base64
import androidx.lifecycle.lifecycleScope
import com.insane.eyewalk.model.api.FeatureType
import com.insane.eyewalk.model.api.Image
import com.insane.eyewalk.model.api.ImageContent
import com.insane.eyewalk.model.api.ImageRequest
import com.insane.eyewalk.model.input.UserRegisterInput
import com.insane.eyewalk.service.GoogleService
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.UserService
import kotlinx.coroutines.launch


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
        cameraExecutor = Executors.newSingleThreadExecutor()

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
            mediaDir
        else
            requireActivity().filesDir
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
                    val b64 = convertImageToBase64(savedUri)
                    lifecycleScope.launch {
                        try {
                            val imageRequest = ImageRequest(requests = listOf(Image(image = ImageContent(content = b64), features = listOf(FeatureType(type = "LABEL_DETECTION")))))
                            val analysis = GoogleService.analyzeImage(imageRequest)
                            if (analysis.isSuccessful) {
                                analysis.body()?.let {
                                    setButtonState(RESULT, it.responses[0].labelAnnotations[0].description)
                                    println(it.responses[0].labelAnnotations[0].score)
                                }
                            } else {
                                println("************ ERROR PROCESSING IMAGE **************")
                                setButtonState(RESULT, "Imagem não identificada")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        println("************* END PROCESS *************")
                    }
                    cameraShutter.stop()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.CAM_TAG, "onError: ${exception.message}",exception)
                    cameraShutter.stop()
                }
            }
        )
    }

    /**
     * Method to convert a image file to Base64
     * @param imageUri The saved image uri
     */
    private fun convertImageToBase64(imageUri: Uri): String {
        val imageFile = BitmapFactory.decodeFile(imageUri.path)
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageFile.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun encodeImage(path: String): String? {
        val imagefile = File(path)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(imagefile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val bm = BitmapFactory.decodeStream(fis)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val b = baos.toByteArray()
        //Base64.de
        return Base64.encodeToString(b, Base64.DEFAULT)
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