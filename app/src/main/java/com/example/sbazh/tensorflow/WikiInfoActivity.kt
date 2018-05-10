package com.example.sbazh.tensorflow

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_wiki_info.*
import org.json.JSONException
import org.json.JSONObject

class WikiInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wiki_info)

        val flower = intent.extras.getString("flower")
        val WIKI_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=$flower&redirects=1"
        WIKI_URL.httpGet().responseString { request, response, result ->
            //do something with response
            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    Log.e("SHOW_IMAGES_ACTIVITY", ex.message)
                }
                is Result.Success -> {
                    val data = result.get()
                    parseResponse(data)
                }
            }
        }
    }

    private fun parseResponse(response: String) {
        try {
            val pages = JSONObject(response).getJSONObject("query").getJSONObject("pages")
            Log.d("wikipedia", pages.toString(4))
            pages.keys().forEach {
                val obj = pages.getJSONObject(it)
                wikiText.text = obj.getString("extract")
            }
        } catch (e: JSONException) {
            Log.e("Parse response", "failed to parse")
        }
    }
}
