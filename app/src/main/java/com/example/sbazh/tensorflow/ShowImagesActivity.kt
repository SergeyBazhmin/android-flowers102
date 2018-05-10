package com.example.sbazh.tensorflow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.ProgressBar
import com.example.sbazh.tensorflow.adapters.GridViewAdapter
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class ShowImagesActivity : AppCompatActivity(){

    //private val flickrApiService by lazy { FlickrApiService.create() }
    private val gridView by lazy { findViewById<GridView>(R.id.images)}
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }
    private val gridData: ArrayList<GridItem> by lazy { ArrayList<GridItem>() }
    private val gridViewAdapter by lazy { GridViewAdapter(this,
            R.layout.grid_item_layout,
            gridData) }
    private val FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?&safe_search=safe&sort=relevance&format=json&nojsoncallback=1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_images_layout)

        gridView.adapter = gridViewAdapter
        var flower = intent.extras.getString("flower")
        if (flower == "")
            flower = "flower"
        val FEED_URL =  FLICKR_BASE_URL +
                "&method=flickr.photos.search&text=$flower&api_key=67a33cd303d55e4f567684ba6eedbd84&per_page=26&media=photos&extras=url_s"

        FEED_URL.httpGet().responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    Log.e("SHOW_IMAGES_ACTIVITY", ex.message)
                }
                is Result.Success -> {
                    val data = result.get()
                    parseResponse(data)
                    gridViewAdapter.setGridData(gridData)
                    progressBar.visibility = View.GONE
                }
            }
        }
        progressBar.visibility = View.VISIBLE
    }

    private fun parseResponse(response: String)
    {
        try {
            val jsonObject = JSONObject(response).getJSONObject("photos")
            Log.d("json", jsonObject.toString())
            val photos = jsonObject.optJSONArray("photo")
            var item: GridItem
            for(i in 0 until photos.length()){
                val photo = photos.optJSONObject(i)
                val url = photo.getString("url_s")
                url?.let{
                    item = GridItem(it)
                    gridData.add(item)
                }
            }
        }catch (e: JSONException)
        {
            Log.e("Parse response", "failed to parse")
        }

    }

}