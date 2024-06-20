package com.example.security

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Toast
import com.example.security.sampledata.MailSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraService : Service() {
    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var imageReader: ImageReader
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    public lateinit var pathToImage:String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        startCamera()
        return START_NOT_STICKY
    }

    private fun startCamera() {
        try {
            val cameraId = getFrontFacingCameraId()
            if (cameraId != null) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    stopSelf()
                    return
                }
                cameraManager.openCamera(cameraId, stateCallback, null)
            } else {
                Log.e(TAG, "No front-facing camera found")
                stopSelf()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun getFrontFacingCameraId(): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            stopSelf()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            stopSelf()
        }
    }

    private fun createCaptureSession() {
        try {
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null)

            val surface = imageReader.surface
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureImage()
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    stopSelf()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun captureImage() {
        try {
            captureSession?.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    Log.d(TAG, "Image captured")
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        saveImage(bytes)
        image.close()
    }

    private fun saveImage(imageBytes: ByteArray) {
        try {
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, fileName)
            pathToImage = imageFile.toString()
            FileOutputStream(imageFile).use { fos ->
                fos.write(imageBytes)
            }

            MediaScannerConnection.scanFile(this, arrayOf(imageFile.absolutePath), null, null)
            //Log.d(TAG, "Image saved: $pathToImage")
            sendEmailWithImage(pathToImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun sendEmailWithImage(imagePath: String?) {
        if (imagePath != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val sender = MailSender("russelsonwa1@gmail.com", "zfff ipqf xevi xnjd")
                sender.sendMail(
                    subject = "Intrusion détectée",
                    body = "Une tentative de déverrouillage non autorisée a été détectée.",
                    recipients = listOf("jesuisspo@gmail.com"),
                    attachmentPath = imagePath
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    companion object {
        private const val TAG = "CameraService"
    }
}
