package com.example.myapplication
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.myapplication.databinding.ActivityPaymentSucceededBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class payment_succeeded : AppCompatActivity() {
    val url = "http://203.217.169.102:50209"
    lateinit var binding:ActivityPaymentSucceededBinding
    override fun onStart() {
        super.onStart()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentSucceededBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val tran_data = intent.getStringArrayExtra("tran_data")!!
        Log.d("iddd", Arrays.toString(tran_data))
//        tran_data = tran_data!!.replace("[","")
//        tran_data = tran_data!!.replace("]","")
//        val jsonObject = JSONObject(tran_data)
//        Log.d("ssssss",tran_data)

        val json = """
            {"tran_id": "${tran_data[0]}"}
        """.trimIndent()
        val client = OkHttpClient()
        val answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

        val request = Request.Builder().header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQYXltZW50R2F0ZXdheSIsInN1YiI6IkVEQyIsImhhc2giOiJCQ0ZEQzE1MC0zMjRGLTQzRjQtQkQ3Qi0zMTVGN0Y5NDM3NDAifQ.OZ9AqnbRucNmVlJzQt6kqkRjDDDPjMAN81caYwqKuX4")
            .header("Accept","application/json")
            .url(url+"/transaction/verify/v1").post(answer.toString().toRequestBody(mediaTypeJson))
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Error","$e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responsess = response
                val responses = responsess.body!!.string()
                Log.d("code",responsess.code.toString())
//                Log.d("hehe",responses)
                val jsonObject = JSONObject(responses)

                runOnUiThread {
                    kotlin.run {

                        binding.bankName.text = tran_data[1]
                        binding.bankRef.text = tran_data[2]
                        binding.orderid.text = "Order #"+tran_data[3]
                        if (responsess.code == 200) {
                            binding.fee.text = jsonObject.optJSONObject("data").optString("fee_amount")+"0 USD"
                            binding.date.text = jsonObject.optJSONObject("data").optString("tran_date")
                            binding.subtotal.text = jsonObject.optJSONObject("data").optString("tran_amount")+"0 USD"
                            binding.total.text = jsonObject.optJSONObject("data").optString("total_amount")+"0 USD"
                        }

                    }
                }

            }

        })
        binding.continueBtn.setOnClickListener {
            startActivity(Intent(applicationContext,homescreen::class.java))
            finish()
        }

    }
}