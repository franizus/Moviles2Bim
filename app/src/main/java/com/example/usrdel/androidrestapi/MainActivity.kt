package com.example.usrdel.androidrestapi

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.ArrayAdapter
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var directorioActualImagen = ""
    var respuestasBarCode = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        "http://172.31.104.26:1337/Entrenador/1".httpGet().responseString { request, response, result ->
            /*Log.i("http-ejemplo", "Request: $request")
            Log.i("http-ejemplo", "Response: $response")
            Log.i("http-ejemplo", "Result: $result")*/
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    Log.i("http-ejemplo", "Error: ${ex.response}")
                }
                is Result.Success -> {
                    val jsonStringEntrenador = result.get()
                    Log.i("http-ejemplo", "Exito: $jsonStringEntrenador")

                    var entrenador: Entrenador? = Klaxon().parse<Entrenador>(jsonStringEntrenador)
                    if (entrenador != null) {
                        Log.i("http-ejemplo", "Nombre: ${entrenador.nombre}")
                        Log.i("http-ejemplo", "Apellido: ${entrenador.apellido}")
                        Log.i("http-ejemplo", "Edad: ${entrenador.edad}")
                        Log.i("http-ejemplo", "Medallas: ${entrenador.medallas}")
                        Log.i("http-ejemplo", "ID: ${entrenador.id}")
                        Log.i("http-ejemplo", "Created At: ${entrenador.createdAtDate}")
                        Log.i("http-ejemplo", "Updated At: ${entrenador.updatedAtDate}")
                        entrenador.pokemons.forEach { pokemon: Pokemon ->
                            Log.i("http-ejemplo", "Nombre: ${pokemon.nombre}")
                            Log.i("http-ejemplo", "Tipo: ${pokemon.tipo}")
                            Log.i("http-ejemplo", "Numero: ${pokemon.numeroPokemon}")
                        }
                    } else {
                        Log.i("http-ejemplo", "Entrenador nulo.")
                    }
                }
            }
        }

        val fotoActual = File(Environment.getExternalStorageDirectory().path + // ->
                "/Android/data/com.example.usrdel.androidrestapi/files/Pictures/JPEG_20180613_145639_6333688883211865798.jpg")

        val fotoActualBitmap = BitmapFactory.decodeFile(fotoActual.getAbsolutePath())

        imageViewCamera.setImageBitmap(fotoActualBitmap)

        btnTomarFoto.setOnClickListener { view ->
            tomarFoto()
        }
    }

    private fun tomarFoto() {
        val archivoImagen = crearArchivo("JPEG_", Environment.DIRECTORY_PICTURES, ".jpg")
        directorioActualImagen = archivoImagen.absolutePath
        enviarIntentFoto(archivoImagen)
    }

    private fun crearArchivo(prefijo: String, directorio: String, extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = prefijo + timeStamp + "_"
        val storageDir = getExternalFilesDir(directorio)
        return File.createTempFile(
                imageFileName, /* prefix */
                extension, /* suffix */
                storageDir      /* directory */
        )
    }

    private fun enviarIntentFoto(archivo: File) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.example.usrdel.androidrestapi.fileprovider",
                archivo)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, TOMAR_FOTO_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            TOMAR_FOTO_REQUEST -> {
                val foto = File(directorioActualImagen)
                val fotoBitmap = BitmapFactory.decodeFile(foto.getAbsolutePath())
                imageViewFoto.setImageBitmap(fotoBitmap)
                obtenerInfoCodigoBarras(fotoBitmap)
            }
        }
    }

    fun obtenerInfoCodigoBarras(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().visionBarcodeDetector

        val result = detector.detectInImage(image)
                .addOnSuccessListener { barCodes ->
                    for (barcode in barCodes) {
                        val bounds = barcode.getBoundingBox()
                        val corners = barcode.getCornerPoints()
                        val rawValue = barcode.getRawValue()

                        respuestasBarCode.add(rawValue.toString())
                    }

                    val adaptadorListView = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, respuestasBarCode)
                    list_view.adapter = adaptadorListView
                }
                .addOnFailureListener {
                    Log.i("info", "------- No reconocio nada")
                }
    }

    companion object {
        val TOMAR_FOTO_REQUEST = 1
    }

}
