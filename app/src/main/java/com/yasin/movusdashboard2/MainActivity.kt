package com.yasin.movusdashboard2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yasin.movusdashboard2.databinding.ActivityMainBinding
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val handler = Handler(Looper.getMainLooper())
    private val apiUpdateRunnable = object : Runnable {
        override fun run() {
            fetchDataFromApi()
            handler.postDelayed(this, 10000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        handler.post(apiUpdateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(apiUpdateRunnable)
    }

    private fun fetchDataFromApi() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://192.168.1.193:5000/getLatestDaysNo")
            .get()
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to retrieve days numbers", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()

                    val jsonArray = JSONArray(responseData)
                    val jsonObject = jsonArray.getJSONObject(0)
                    val days = jsonObject.getString("days")

                    Log.d("MainActivity", "Fetched days: $days")

                    runOnUiThread {
                        binding?.tvSistemNo?.text = days
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to retrieve days numbers", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
