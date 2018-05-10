package com.example.sbazh.tensorflow

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_wiki_info.*
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

class WikiInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wiki_info)

        val flower = intent.extras.getString("flower")
        wikiLinkText.isClickable = true
        wikiLinkText.movementMethod = LinkMovementMethod.getInstance()
        val WIKI_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&titles=$flower&redirects=1"
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
            if(pages.length() == 0){
                toast("No information found")
                finish()
            }
            val obj = pages.getJSONObject(pages.keys().next())
            wikiText.text = (Html.fromHtml(obj.getString("extract"), Html.FROM_HTML_MODE_COMPACT))
            val id = obj.getString("pageid")
            wikiLinkText.text = (Html.fromHtml("<a href='http://en.wikipedia.org/?curid=$id'> Find Out More! </a>", Html.FROM_HTML_MODE_COMPACT))

        } catch (e: JSONException) {
            Log.e("Parse response", "failed to parse")
        }
    }
}
