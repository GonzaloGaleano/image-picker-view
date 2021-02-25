[![](https://jitpack.io/v/GonzaloGaleano/image-picker-view.svg)](https://jitpack.io/#GonzaloGaleano/image-picker-view)


# Kotlin-Android - Image-Picker-View
Componente que facilita el proceso de solicitar permisos, obtener la imagen, ruta de imagen, bitmap, base64 simplificando así el código a utilizar en el activity permitiendo utilizarse más de una instancia en la misma vista.

## Capturas de Pantallas

![foo](https://raw.githubusercontent.com/GonzaloGaleano/Kotlin-Image-Picker-View/master/docs/main_activity.jpeg "Aplicando en MainActivity")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/Kotlin-Image-Picker-View/master/docs/choose_image_origin.jpeg "Elegir origen de imagen")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/Kotlin-Image-Picker-View/master/docs/image_zoom_editor.jpeg "Visualizar y editar rotación")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/Kotlin-Image-Picker-View/master/docs/discart_prompt.jpeg "Preguntar antes de descartar")


## Implementar librería

[https://jitpack.io/#GonzaloGaleano/image-picker-view/0.1.0]

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```
dependencies {
    implementation 'com.github.GonzaloGaleano:image-picker-view:0.1.0'
}
```

## Primero que nada, a configurar el Manifest

### Permisos Necesarios

```
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
### Provider

```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_path" />
</provider>
```

#### xml/file_path.xml

Donde **PUT_HERE_YOUR_PACKAGE_NAME** debe ser reemplazado por el packete de tu aplicación.

```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="my_images"
        path="Android/data/PUT_HERE_YOUR_PACKAGE_NAME/files/Pictures" />
</paths>
```

## La vista

Para agregar a la vista vasta con incluir como se incluiría un ImageView:

```
<PUT_HERE_YOUR_PACKAGE_NAME.components.imagepickerview.ImagePickerView
      android:id="@+id/MyImagePicker"
      android:layout_width="match_parent"
      android:layout_height="0dp"
      android:layout_weight="1"
      app:srcCompat="@android:drawable/ic_menu_camera"
      />
```

#### layout/image_picker_view_layout.xml

Es la plantilla para el ImagePickerView. Debe ser de tipo ImageView.

#### layout/image_picker_editor_layout.xml

Es la plantilla para el previsualizador/editor de la imagen.

## El Controlador

Para utilizar en el Activity basta con crear la variable para cada instancia en la vista principal.

```
private lateinit var imagePickerView1: ImagePickerView
private lateinit var imagePickerView2: ImagePickerView
```

Instanciando y conectando con el componente ( ejemplo con findViewById ):

```
imagePickerView1 = findViewById( R.id.MyImagePicker1 )
imagePickerView2 = findViewById( R.id.MyImagePicker2 )
```

### CallBacks

La ejecución de los callBacks son independiente para cada instancia.

En el ejemplo solo se agregan los listener para el primer componente instanciado imagePickerView1.

```
imagePickerView1.onReset = {
    findViewById<Button>(R.id.btnToTestBase64).visibility = View.GONE
}

imagePickerView1.onPhotoLoaded = {
    Toast.makeText(this, "Foto cargada!!!!", Toast.LENGTH_SHORT).show()
}
```

### onRequestPermissionsResult

El manejo de permisos queda delegada y manejada por la clase ImagePickerView para escritura y código duplacdo.

Basta con que cada instancia delege el método onRequestPermissionResult como en el ejemplo en el mismo Activity:

```
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    imagePickerView1.onRequestPermissionsResult(requestCode, permissions, grantResults)
    imagePickerView2.onRequestPermissionsResult(requestCode, permissions, grantResults)
}
```

### onActivityResult

Igual que los permisos, el proceso de obtener y manejar la imagen queda delegada a la clase ImagePickerView.

En el código se muestra delegando las dos instancias ya utilizadas de ejemplo.

```
override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    super.onActivityResult(requestCode, resultCode, intent)
    imagePickerView1.onActivityResult(requestCode, resultCode, intent)
    imagePickerView2.onActivityResult(requestCode, resultCode, intent)
}
```

### toBase64

Para utilizar como base 64 sería de la siguiente manera:

```
val image1Base64: String = imagePickerView1.scaledToBase64()
```
