package com.example.androidsdksampleapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_payment_succeeded.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class payment_succeeded : AppCompatActivity() {

    private val url:String = "https://checkoutapi-demo.bill24.net"

    //unique token is given by bill24 to biller
    private val token:String = "f91d077940cf44ebbb1b6abdebce0f0a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_succeeded)
        //transaction data received from sdk as an array
        val trandata = intent.getStringArrayExtra("tran_data")!!
        Log.d("tran_data",trandata.toString())

        //to verify transaction and initialize widgets with its responses
        verifyTransaction(trandata)

        continueBtn.setOnClickListener {
            startActivity(Intent(applicationContext,homescreen::class.java))
            finish()
        }
    }

    fun verifyTransaction(tran_data:Array<String>){
        val json = """
            {"tran_id": "${tran_data[0]}"}
        """.trimIndent()
        val client = OkHttpClient()
        val answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

        val request = Request.Builder().header("token",token)
            .header("Accept","application/json")
            .url(url+"/transaction/verify").post(answer.toString().toRequestBody(mediaTypeJson))
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Exception","$e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responses = response.body!!.string()
                Log.d("asd",responses)
                val jsonObject = JSONObject(responses)
                val responseData = jsonObject.optJSONObject("data")!!
                runOnUiThread {
                    kotlin.run {
                        val orderId = "Order #"+tran_data[3]
                        val totalFee = responseData.optString("fee_amount")+"0 USD"
                        val dateTime = responseData.optString("tran_date")
                        val subTotal = responseData.optString("tran_amount")+"0 USD"
                        val totalAmount = responseData.optString("total_amount")+"0 USD"

                        bankName.text = tran_data[1]
                        bankRef.text = tran_data[2]
                        orderid.text = orderId
                        fee.text = totalFee
                        date.text = dateTime
                        subtotal.text = subTotal
                        total.text = totalAmount
                    }
                }
            }
        })

    }

}