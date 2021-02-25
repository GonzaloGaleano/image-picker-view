package com.gonzalogaleano.android.imagepickerview.extensions

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.gonzalogaleano.android.imagepickerview.RealPathUtil
import java.util.*

fun Intent?.getFilePath(context: Context): String {
    return this?.data?.let { data -> RealPathUtil.getRealPath(context, data) ?: "" } ?: ""
}

fun Uri?.getFilePath(context: Context): String {
    return this?.let { uri -> RealPathUtil.getRealPath(context, uri) ?: "" } ?: ""
}

fun ClipData.Item?.getFilePath(context: Context): String {
    return this?.uri?.getFilePath(context) ?: ""
}

fun ByteArray.toBase64 (): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        Base64.getEncoder().encodeToString(this)
        else
        android.util.Base64.encodeToString(this, android.util.Base64.DEFAULT)
}

fun Array<String>.checkGranted(grantResults: IntArray ): Boolean {
    var grantedSuccess = true
    forEachIndexed { index, s ->
        if ( grantResults[index] != android.content.pm.PackageManager.PERMISSION_GRANTED )
            grantedSuccess = false
    }
    return grantedSuccess
}
//RealPathUtilExts::class.java.simpleName,