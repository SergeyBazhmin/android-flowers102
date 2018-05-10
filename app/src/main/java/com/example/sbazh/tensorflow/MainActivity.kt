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
import com.example.sbazh.tensorflow.classifier.*
import com.example.sbazh.tensorflow.classifier.tensorflow.ImageClassifierFactory
import com.example.sbazh.tensorflow.utils.ImageUtils
import com.example.sbazh.tensorflow.utils.Labels
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.*
import android.content.ContentValues
import android.net.Uri
import java.util.Collections.rotate
import android.R.attr.bitmap
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.opengl.Visibility
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    private lateinit var classifier: Classifier
    private lateinit var labels: ArrayList<String>
    private var flower = ""
    private lateinit var mCurrentPhotoPath : String

    companion object {
        val SELECT_FILE = 1
        val REQUEST_CAMERA = 0
        val MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 123
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelectPhoto.setOnClickListener { selectImage() }
        btnClassify.setOnClickListener { classifyCurrentImage() }
        btnShow.setOnClickListener{
            if(isNetworkConnected()) {
                val intent = Intent(this, ShowImagesActivity::class.java)
                intent.putExtra("flower", flower)
                startActivity(intent)
            } else {
                toast("Please Connect to the Internet")
            }
        }
        btnShow.visibility = View.GONE
        btnInfo.setOnClickListener{
            if (flower.isNotEmpty() && isNetworkConnected()) {
                val intent = Intent(this, WikiInfoActivity::class.java)
                intent.putExtra("flower", flower)
                startActivity(intent)
            } else {
                toast("Please Connect to the Internet")
            }
        }
        btnInfo.visibility = View.GONE
        classLabel.visibility = View.GONE
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
        ivImage.drawable?.let {
            val bitmap = (it as BitmapDrawable).bitmap
            val cropped = ImageUtils.getCroppedBitmap(bitmap)
            doAsync {
                val result = classifier.recognizeImage(cropped)
                uiThread {
                    if (result.confidence >= TRESHHOLD) {
                        classLabel.text = "${result.title} ${result.confidence}"
                        flower = result.title
                        btnShow.visibility = View.VISIBLE
                        btnInfo.visibility = View.VISIBLE
                        classLabel.visibility = View.VISIBLE
                    } else
                        classLabel.text = "It is not a flower"
                }
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

        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            mCurrentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, REQUEST_CAMERA)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK)
        {
            when(requestCode)
            {
                SELECT_FILE -> onSelectFromGalleryResult(data)
                REQUEST_CAMERA -> onCaptureImageResult()
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


    private fun onCaptureImageResult(){
        val cursor = contentResolver.query(Uri.parse(mCurrentPhotoPath),
                Array(1) {android.provider.MediaStore.Images.ImageColumns.DATA},
                null, null, null)
        cursor.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        lateinit var rotatedBitmap : Bitmap
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90f)
        }
        ivImage.setImageBitmap(rotatedBitmap)
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                matrix, true)
    }
    private fun isNetworkConnected() : Boolean{
        val cm =  getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }
}
