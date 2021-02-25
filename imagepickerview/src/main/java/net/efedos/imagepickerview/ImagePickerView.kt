package net.efedos.imagepickerview

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.gonzalogaleano.android.imagepickerview.GetProperImageRotation
import com.gonzalogaleano.android.imagepickerview.RealPathUtil.getRealPath
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerView @JvmOverloads
constructor(private val ctx: Context, private val attributeSet: AttributeSet? = null, private val defStyleAttr: Int = 0)
    : androidx.appcompat.widget.AppCompatImageView(ctx, attributeSet, defStyleAttr) {

    private val CAMERA_PHOTOS_PERMISIONS: Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val GALLERY_PHOTOS_PERMISIONS: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    private var CAMERA_PHOTOS_PERMISIONS_GRANTED = false
    private var GALLERY_PHOTOS_PERMISIONS_GRANTED = false

    private var REQUEST_GALLERY_IMAGE = 100
    private var REQUEST_CAMERA_IMAGE = 200

    private var REQUEST_GALLERY_PERMISIOS: Int = 101
    private var REQUEST_CAMERA_PERMISIONS: Int = 201

    private var tmpBitmap: Bitmap? = null
    private var bitmap: Bitmap? = null
    private lateinit var dialog: AlertDialog
    var savedPhotoPath: String? = null
    private val tag: String = ImagePickerView::class.java.simpleName

    var onReset: () -> Unit = {}
    var onPhotoLoaded: () -> Unit = {}

    init {

//        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val rootView = inflater.inflate(R.layout.image_picker_view_layout, this )

        val idString = id.toString()
        REQUEST_GALLERY_IMAGE += idString.substring(idString.length - 4).toInt()
        REQUEST_CAMERA_IMAGE += idString.substring(idString.length - 4).toInt()
        REQUEST_GALLERY_PERMISIOS += idString.substring(idString.length - 4).toInt()
        REQUEST_CAMERA_PERMISIONS += idString.substring(idString.length - 4).toInt()

        println("REQUEST_GALLERY_IMAGE: $REQUEST_GALLERY_IMAGE")
        println("REQUEST_CAMERA_IMAGE: $REQUEST_CAMERA_IMAGE")
        println("REQUEST_GALLERY_PERMISIOS: $REQUEST_GALLERY_PERMISIOS")
        println("REQUEST_CAMERA_PERMISIONS: $REQUEST_CAMERA_PERMISIONS")


        setOnClickListener {
            seleccionarOrigen()
        }

        setOnLongClickListener {
            Log.d(tag, savedPhotoPath.toString())
            bitmap?.let {
                showEditDialog()
            }?: run{
                Log.e(tag, "bitmap is null?")
                Toast.makeText(ctx, "No hay imagen", Toast.LENGTH_SHORT).show()
            }
            true
        }

    }

    private fun showEditDialog() {

        Log.d(tag, ".showEditDialog()")

        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.image_picker_editor_layout)

        val imgPreview = dialog.findViewById(R.id.imgPreview) as ImageView
        imgPreview.setImageBitmap(bitmap)
        val btnSave = dialog.findViewById(R.id.btnSave) as Button
        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
        val btnDiscart = dialog.findViewById(R.id.btnDiscart) as Button
        val btnRotate = dialog.findViewById(R.id.btnRotate) as Button

        btnRotate.setOnClickListener {
            tmpBitmap?.let {
                tmpBitmap = tmpBitmap?.let { it1 -> rotate(it1) }
            }?: run {
                tmpBitmap = bitmap?.let { it1 -> rotate(it1) }
            }
            imgPreview.setImageBitmap( tmpBitmap )
        }

        btnDiscart.setOnClickListener {

            val alertDialog: AlertDialog.Builder = AlertDialog.Builder( ctx )
            alertDialog.setTitle("Descartar Foto")
            alertDialog.setMessage("¿Quieres descartar esta foto?")
            alertDialog.setPositiveButton(
                "Si, descartar"
            ) { _, _ ->
                reset()
                dialog.dismiss()
            }
            alertDialog.setNegativeButton(
                "No descartar"
            ) { _, _ -> }
            val alert: AlertDialog = alertDialog.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()

        }

        btnSave.setOnClickListener {
            tmpBitmap?.let {
                bitmap = tmpBitmap
                tmpBitmap = null
                setImageBitmap( bitmap )
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            tmpBitmap = null
            dialog.dismiss()
        }

        val window: Window? = dialog.getWindow()
        window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        dialog.show()

    }


    private fun reset() {
        bitmap = null
        savedPhotoPath = null
        tmpBitmap = null
        setImageBitmap(bitmap)
        setImageResource( android.R.drawable.ic_menu_camera )
        onReset()
    }


    private fun seleccionarOrigen() {
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Seleccionar origen")

        builder.setNegativeButton("Cámara") { dialog, which ->
            if ( checkPermissionTakeCameraPhotos(ctx) )
                takeCameraPicture()
            else
                requestCameraPhotosPermissions(ctx)
        }

        builder.setPositiveButton("Galería") { dialog, which ->
            if ( checkPermissionTakeGalleryPhotos(ctx) )
                pickImageFromGallery()
            else
                requestGalleryPhotosPermissions(ctx)
        }

        dialog = builder.create()
        dialog.show()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        if ( resultCode == Activity.RESULT_OK ) {

            when( requestCode ) {
                REQUEST_GALLERY_IMAGE -> {

                    intent?.let {
                        intent.data?.let {
                            savedPhotoPath = getRealPath(ctx, it)
                            useImage()
                        }
                    }

                }

                REQUEST_CAMERA_IMAGE -> useImage()

                else -> {
                    showToastLogsMessage("$requestCode no filtered")
                }
            }

        } else {
            showToastLogsMessage("resultCode: $resultCode")
        }

    }

    private fun useImage() {
        savedPhotoPath?.let {
            Log.w(tag, savedPhotoPath.toString())
//            BitmapFactory.decodeFile( savedPhotoPath )
            val rotatedImageFile =
                GetProperImageRotation.getRotatedImageFile(File(savedPhotoPath.toString()), context)

            bitmap = BitmapFactory.decodeFile(rotatedImageFile?.absolutePath)
//                            .compressBitmap(20)
            setImageBitmap(bitmap)
            onPhotoLoaded()
        }?: run { Log.e(tag, "savedPhotoPath is null?: $savedPhotoPath") }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if ( grantResults.isEmpty() ) {
            showToastLogsMessage("GrantResult empty?: $grantResults")
        }
        else {

            when ( requestCode ) {

                REQUEST_GALLERY_PERMISIOS -> {

                    when {
                        GALLERY_PHOTOS_PERMISIONS.checkGranted(grantResults) -> {
                            GALLERY_PHOTOS_PERMISIONS_GRANTED = true
                            pickImageFromGallery()
                        }
                        else -> {
                            showToastLogsMessage("Permiso denegado")
                        }
                    }

                }

                REQUEST_CAMERA_PERMISIONS -> {

                    when {
                        CAMERA_PHOTOS_PERMISIONS.checkGranted(grantResults) -> {
                            CAMERA_PHOTOS_PERMISIONS_GRANTED = true
                            takeCameraPicture()
                        }
                        else -> {
                            showToastLogsMessage("Permiso denegado")
                        }
                    }

                    return

                } else -> {
                showToastLogsMessage("requestCode: $requestCode caso no contemplado")
            }

            }

        }

    }

    private fun showToastLogsMessage(msg: String) {
//        Toast.makeText( ctx, msg, Toast.LENGTH_SHORT ).show()
        Log.e(tag, msg)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        (ctx as Activity).startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    private fun takeCameraPicture() {

        Log.w(tag, this.id.toString())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile(ctx)

        val uri: Uri = FileProvider.getUriForFile(
            ctx,
            ctx.packageName + ".provider",
            file
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        (ctx as Activity).startActivityForResult(intent, REQUEST_CAMERA_IMAGE)

    }

    private fun createFile(ctx: Context): File {
        // Create an image file token
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            savedPhotoPath = absolutePath
        }
    }

    private fun checkPermissions(ctx: Context, permissions: Array<String>): Boolean {
        val permissionsChecked = permissions.filter { permision ->
            when( permision ){
                Manifest.permission.CAMERA -> ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                Manifest.permission.READ_EXTERNAL_STORAGE -> ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                else -> {
                    val msg = "$permision no ha sido agregado a la lista"
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                    Log.e(this.tag, msg)
                    false
                }
            }
        }
        return permissionsChecked == permissions
    }

    private fun checkPermissionTakeCameraPhotos(ctx: Context): Boolean{
        return CAMERA_PHOTOS_PERMISIONS_GRANTED || checkPermissions(ctx, CAMERA_PHOTOS_PERMISIONS)
    }

    private fun checkPermissionTakeGalleryPhotos(ctx: Context): Boolean{
        return GALLERY_PHOTOS_PERMISIONS_GRANTED || checkPermissions(ctx, GALLERY_PHOTOS_PERMISIONS)
    }

    private fun requestCameraPhotosPermissions(ctx: Context) {
        ActivityCompat.requestPermissions(ctx as Activity, CAMERA_PHOTOS_PERMISIONS, REQUEST_CAMERA_PERMISIONS)
    }

    private fun requestGalleryPhotosPermissions(ctx: Context) {
        ActivityCompat.requestPermissions(ctx as Activity, GALLERY_PHOTOS_PERMISIONS, REQUEST_GALLERY_PERMISIOS)
    }

    fun scaledToBase64( idealWith: Int = 750, quality: Int = 80 ): String {

        return bitmap?.let {
//            val newWithPercent = idealWith * 100 / it.width
//            val idealHeight = it.height * it.width.percentRelateTo(idealWith) / 100
            Bitmap.createScaledBitmap(
                it,
                idealWith,
                it.height.scaleInPercent(it.width.percentRelativeTo(idealWith)),
                false
            )
                .toBase64( quality )
        } ?: run {
            return ""
        }
    }

    private fun rotate( bitmap: Bitmap, degrees: Float = 90f ): Bitmap = bitmap.let {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap( it, 0, 0, it.width, it.height, matrix, true)
    }

}


fun Int.percentRelativeTo(calculateFrom: Int): Int{
    return calculateFrom * 100 / this
}

fun Int.scaleInPercent(percent: Int): Int {
    return this * percent / 100
}

fun ByteArray.toBase64 (): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        Base64.getEncoder().encodeToString(this)
    else
        android.util.Base64.encodeToString(this, android.util.Base64.DEFAULT)
}

fun Array<String>.checkGranted(grantResults: IntArray): Boolean {
    var grantedSuccess = true
    forEachIndexed { index, s ->
        if ( grantResults[index] != PackageManager.PERMISSION_GRANTED )
            grantedSuccess = false
    }
    return grantedSuccess
}

fun Bitmap.compressJPEGBitmap( quality: Int ): Bitmap {

    val stream = ByteArrayOutputStream()

    compress(Bitmap.CompressFormat.JPEG, quality, stream)

    val byteArray = stream.toByteArray()

    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun Bitmap.toBase64( format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 80 ): String {
    return this.convertBitmapToByteArray( format, quality )
        ?.toBase64()
        .toString()
}

fun Bitmap.toBase64( quality: Int = 80 ): String {
    return this.convertBitmapToByteArray( Bitmap.CompressFormat.JPEG, quality )
        ?.toBase64()
        .toString()
}

fun Bitmap.convertBitmapToByteArray( format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 80 ): ByteArray? {

    val baos = ByteArrayOutputStream()

    return baos.let {
        compress( format, quality, baos)
        baos.toByteArray()
    }

}
