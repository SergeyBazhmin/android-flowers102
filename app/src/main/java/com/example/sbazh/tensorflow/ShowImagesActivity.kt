package com.example.sbazh.tensorflow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.ProgressBar
import com.example.sbazh.tensorflow.adapters.GridViewAdapter
import com.example.sbazh.tensorflow.api.FlickrApiService
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_images_layout)

        gridView.adapter = gridViewAdapter
        var flower = intent.extras.getString("flower")
        Log.d("lolkek",flower)
        if (flower == "")
            flower = "flower"
        val FEED_URL = "https://api.flickr.com/services/rest/?&safe_search=safe&sort=relevance&format=json&nojsoncallback=1" +
                "&method=flickr.photos.search&text=$flower&api_key=67a33cd303d55e4f567684ba6eedbd84&per_page=26&media=photos&extras=url_s"

        doAsync {
            var res = 0
            try {
                val url = URL(FEED_URL)
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "GET"
                val code = con.responseCode
                if (code == 200) {
                    val reader = BufferedReader(InputStreamReader(con.inputStream))
                    val json = StringBuffer()
                    reader.useLines {
                        it.forEach { json.append(it) }
                    }
                    res = 1
                    parseResponse(json.toString())
                }
                else
                    res = 0
            }catch (e: Exception)
            {
                Log.e("SHOW_IMAGES_ACTIVITY", e.message)
            }
            uiThread {
                if (res == 1)
                    gridViewAdapter.setGridData(gridData)
                else
                    toast("failed to fetch data")
                progressBar.visibility = View.GONE
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