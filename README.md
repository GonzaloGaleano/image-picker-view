[![](https://jitpack.io/v/GonzaloGaleano/image-picker-view.svg)](https://jitpack.io/#GonzaloGaleano/image-picker-view)


# Android - Kotlin - Image-Picker-View
Componente que facilita el proceso de solicitar permisos, obtener las imágenes, ruta de imagen, bitmap y base64 simplificando así el código a utilizar en el activity permitiendo utilizarse más de una instancia en la misma vista.

## Capturas de Pantallas

![foo](https://raw.githubusercontent.com/GonzaloGaleano/image-picker-view/master/docs/main_activity.jpeg "Aplicando en MainActivity")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/image-picker-view/master/docs/choose_image_origin.jpeg "Elegir origen de imagen")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/image-picker-view/master/docs/image_zoom_editor.jpeg "Visualizar y editar rotación")
![foo](https://raw.githubusercontent.com/GonzaloGaleano/image-picker-view/master/docs/discart_prompt.jpeg "Preguntar antes de descartar")


## Implementar librería

[https://jitpack.io/#GonzaloGaleano/image-picker-view/0.1.0]

Paso 1. Agrega el repositorio JitPack a tu archivo build

Agrégalo en tu ./build.gradle de la raíz al final de los repositorios:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Paso 2. Agrga la dependecia

En tu ./app/build.gradle implementa la librería.

```
dependencies {
    implementation 'com.github.GonzaloGaleano:image-picker-view:0.1.0'
}
```

## La vista

Se puede incluir más de una instancia en la vista diferenciados por sus IDs:

```
<net.efedos.imagepickerview.ImagePickerView
    android:id="@+id/myImagePicker1"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:srcCompat="@android:drawable/ic_menu_camera" />

</androidx.constraintlayout.widget.ConstraintLayout>
```


## El Controlador

Para utilizar en el Activity se debe instanciar y conectar cada instancia de la vista con una variable:

```
private lateinit var imagePickerView1: ImagePickerView
private lateinit var imagePickerView2: ImagePickerView
```

onCreate:

```
imagePickerView1 = findViewById( R.id.myImagePicker1 )
imagePickerView2 = findViewById( R.id.myImagePicker2 )
```

## Escuchando los resultados

El manejo de permisos queda delegado y es manejado por la clase ImagePickerView para reducir escritura y evitar código duplacdo.

### onRequestPermissionsResult

Cada instancia debe delegar el método onRequestPermissionResult como en el ejemplo:

```
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    
    imagePickerView1.onRequestPermissionsResult(requestCode, permissions, grantResults)
    imagePickerView2.onRequestPermissionsResult(requestCode, permissions, grantResults)
    ...
}
```

### onActivityResult

Para acceder a las imagenes se debe hacer el llamado de la siguiente manera:

```
override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    super.onActivityResult(requestCode, resultCode, intent)
    
    imagePickerView1.onActivityResult(requestCode, resultCode, intent)
    imagePickerView2.onActivityResult(requestCode, resultCode, intent)
    ...
}
```

## CallBacks

La ejecución de los callBacks son independiente para cada instancia.

### onReset

Para llamar a alguna accion en la pantalla principal después de "DESCARTAR" la imagen previamente obtenida se puede indicar un lambda como en el ejemplo:

```
imagePickerView1.onReset = {
    findViewById<Button>(R.id.btnToTestBase64).visibility = View.GONE
}
```

### onPhotoLoaded

Al finalizar el proceso de la imagen seleccionada se ejecuta el lambda como en el ejemplo:

```
imagePickerView1.onPhotoLoaded = {
    Toast.makeText(this, "Foto cargada!!!!", Toast.LENGTH_SHORT).show()
}
```

## toBase64

Para utilizar como base 64 sería de la siguiente manera:

```
val image1Base64: String = imagePickerView1.scaledToBase64()
```

## POR HACER

### Permitir personalizar vistas

#### layout/image_picker_view_layout.xml

Es la plantilla para el ImagePickerView. Debe ser de tipo ImageView.

#### layout/image_picker_editor_layout.xml

Es la plantilla para el previsualizador/editor de la imagen.
