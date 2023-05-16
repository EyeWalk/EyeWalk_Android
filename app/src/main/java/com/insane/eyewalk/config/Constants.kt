package com.insane.eyewalk.config

import android.Manifest

object Constants {

    const val CAM_TAG = "cameraX"
    const val MAP_TAG = "OsmActivity"
    const val CAMERA_FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val CAMERA_REQUEST_CODE_PERMISSION = 123
    const val MAP_REQUEST_CODE_PERMISSION = 100
    val CAMERA_REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    val MAP_REQUIRED_PERMISSIONS = arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )

}