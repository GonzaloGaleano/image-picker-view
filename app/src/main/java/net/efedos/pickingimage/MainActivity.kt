package net.efedos.pickingimage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.efedos.imagepickerview.ImagePickerView
//import android.R


class MainActivity : AppCompatActivity() {

    private lateinit var imagePickerView1: ImagePickerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagePickerView1 = findViewById(R.id.myImagePicker)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imagePickerView1.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        imagePickerView2.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        imagePickerView3.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        imagePickerView4.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        imagePickerView1.onActivityResult(requestCode, resultCode, intent)
//        imagePickerView2.onActivityResult(requestCode, resultCode, intent)
//        imagePickerView3.onActivityResult(requestCode, resultCode, intent)
//        imagePickerView4.onActivityResult(requestCode, resultCode, intent)
    }
}