package com.example.sbazh.tensorflow

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.sbazh.tensorflow.api.FlickrApiService
import com.example.sbazh.tensorflow.classifier.*
import com.example.sbazh.tensorflow.classifier.tensorflow.ImageClassifierFactory
import com.example.sbazh.tensorflow.utils.ImageUtils
import com.example.sbazh.tensorflow.utils.Labels
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.*


class MainActivity : AppCompatActivity() {
    private val ivImage: ImageView by lazy { findViewById<ImageView>(R.id.ivImage) }
    private val btnSelect: Button by lazy { findViewById<Button>(R.id.btnSelectPhoto) }
    private val btnClassify: Button by lazy { findViewById<Button>(R.id.btnClassify) }
    private val btnShow: Button by lazy { findViewById<Button>(R.id.btnShow) }
    private val classLabel: TextView by lazy { findViewById<TextView>(R.id.classLabel) }

    private lateinit var classifier: Classifier
    private lateinit var labels: ArrayList<String>
    private var flower = ""

    companion object {
        val SELECT_FILE = 1
        val REQUEST_CAMERA = 0
        val MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 123
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelect.setOnClickListener { selectImage() }
        btnClassify.setOnClickListener { classifyCurrentImage() }
        btnShow.setOnClickListener{
            val intent = Intent(this, ShowImagesActivity::class.java)
            intent.putExtra("flower",flower)
            startActivity(intent)
        }

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }

        labels = Labels.loadLabels(assets, LABELS_FILE_NAME)



        createClassifier()
    }

    private fun createClassifier()
    {
        classifier = ImageClassifierFactory.create(
                assets,
                GRAPH_FILE_NAME,
                IMAGE_SIZE,
                labels,
                GRAPH_INPUT_NODE,
                GRAPH_OUTPUT_NODE
        )

    }

    private fun classifyCurrentImage(){
        val bitmap = (ivImage.drawable as BitmapDrawable).bitmap
        val cropped = ImageUtils.getCroppedBitmap(bitmap)
        doAsync {
            val result = classifier.recognizeImage(cropped)
            uiThread {
                if (result.confidence >= TRESHHOLD) {
                    classLabel.text = "${result.title} ${result.confidence}"
                    flower = result.title
                }
                else
                    classLabel.text = "It is not a flower"
            }
        }
    }


    private fun selectImage(){
        val items = arrayOf("Take photo", "Choose from gallery", "Cancel")
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Add photo").setItems(items, {
            dialog, which ->
            when(items[which])
            {
                "Take photo" -> cameraIntent()
                "Choose from gallery" -> galleryIntent()
                "Cancel" -> dialog.dismiss()
            }
        })

        builder.show()
    }

    private fun galleryIntent()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select file"), SELECT_FILE)
    }

    private fun cameraIntent(){

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK)
        {
            when(requestCode)
            {
                SELECT_FILE -> onSelectFromGalleryResult(data)
                REQUEST_CAMERA -> onCaptureImageResult(data)
            }
        }
    }


    private fun onSelectFromGalleryResult(data: Intent?){
        var bm: Bitmap? = null
        data?.let {
            try {
                bm = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, it.data)
            }catch (e: IOException)
            {
                e.printStackTrace()
            }
        }
        ivImage.setImageBitmap(bm)
    }


    private fun onCaptureImageResult(data: Intent?){
        val thumbNail = data?.extras?.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()

        thumbNail.compress(Bitmap.CompressFormat.JPEG,100, bytes)
        val dest = File(Environment.getExternalStorageDirectory(),
                "${System.currentTimeMillis()}.jpg")
        var fo: FileOutputStream
        try {
            dest.createNewFile()
            fo = FileOutputStream(dest)
            fo.use { it.write(bytes.toByteArray()) }
        }catch (fe: FileNotFoundException) {
            fe.printStackTrace()
        }catch (io: IOException) {
            io.printStackTrace()
        }
        ivImage.setImageBitmap(thumbNail)
    }
}
