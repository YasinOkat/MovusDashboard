package com.yasin.movusdashboard2

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.yasin.movusdashboard2.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

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
            .url("http://192.168.1.193:5000/getInfo")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to retrieve data", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()

                    val gson = Gson()
                    val myData = gson.fromJson(responseData, MyDataModel::class.java)

                    runOnUiThread {
                        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                        val date = if (!myData.rampa_yanasma.isNullOrEmpty()) {
                            dateFormat.parse(myData.rampa_yanasma)
                        } else {
                            null
                        }

                        val rampa_yanasma_saat = if (date != null) {
                            val calendar = Calendar.getInstance()
                            calendar.time = date
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(Calendar.MINUTE)
                            "$hour:$minute"
                        } else {
                            ""

                        }

                        val dusumAdet = myData.dusum_adet?.toIntOrNull() ?: 0
                        val ilaveAdet = myData.ilave_adet?.toIntOrNull() ?: 0
                        val blokeAdet = myData.bloke_adet?.toIntOrNull() ?: 0
                        val digerAdet = myData.diger_adet?.toIntOrNull() ?: 0

                        val dusumDk = extractNumberFromString(myData.dusum_dk) ?: 0
                        val ilaveDk = extractNumberFromString(myData.ilave_dk) ?: 0
                        val blokeDk = extractNumberFromString(myData.block_dk) ?: 0
                        val digerDk = extractNumberFromString(myData.diger_dk) ?: 0

                        binding?.tvSistemNo?.text = myData.days
                        binding?.tvYuklemeBaslama?.text = rampa_yanasma_saat
                        binding?.tvYuklemeBitis?.text = myData.rampa_cikis
                        binding?.tvDusumDk?.text = dusumDk.toString()
                        binding?.tvDusumAdet?.text = dusumAdet.toString()
                        binding?.tvIlaveAdet?.text = ilaveAdet.toString()
                        binding?.tvBlokeDk?.text = blokeDk.toString()
                        binding?.tvBlokeAdet?.text = blokeAdet.toString()
                        binding?.tvDigerAdet?.text = digerAdet.toString()
                        binding?.tvIlaveDk?.text = ilaveDk.toString()
                        binding?.tvDigerDk?.text = digerDk.toString()
                        binding?.tvVardiyaAmiri?.text = myData.vardiya_amiri?.uppercase()
                        binding?.tvRampaGorevlisi?.text = myData.rampa_sorumlusu?.uppercase()
                        binding?.tvYuklemeci1?.text = myData.yuklemeci?.uppercase()
                        binding?.tvYuklemeci2?.text = myData.yuklemeci2?.uppercase()
                        binding?.tvForkliftOper?.text = myData.forklift_operatoru?.uppercase()

                        val totalAdet = dusumAdet + ilaveAdet + blokeAdet + digerAdet
                        binding?.tvToplamAdet?.text = totalAdet.toString()
                        val totalDk = dusumDk + ilaveDk + blokeDk + digerDk
                        binding?.tvToplamDk?.text = totalDk.toString()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to retrieve data", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }


    private fun extractNumberFromString(input: String?): Int? {
        val numberRegex = "\\d+".toRegex()
        val matchResult = numberRegex.find(input ?: "")
        return matchResult?.value?.toIntOrNull()
    }


}
