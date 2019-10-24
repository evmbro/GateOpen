package com.duimane.gateopen.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

class ImageUtils {

    companion object {

        fun base64ToBitmap(encodedImage: String): Bitmap {
            val decodedImageData = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedImageData, 0, decodedImageData.size)
        }

    }

}