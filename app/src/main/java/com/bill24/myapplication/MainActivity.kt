package com.bill24.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.bill24.paymentSdk.paymentSdk
import com.example.myapplication.databinding.ActivityMainBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var button: Button
    var language:String = "en"
    lateinit var sessionId:String

    // environment must be "uat" or "prod" only
    val environment:String = "uat"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        var binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchLanguage.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                language = "kh"
            }
            else{
                language = "en"
            }
        }




        binding.button.setOnClickListener {
//            startActivity(
//            FlutterActivity.createDefaultIntent(this)
//        )
            val client = OkHttpClient()
            val orderDetailsJson = """
            {
        "customer": {
            "customer_ref": "C00001",
            "customer_email": "example@gmail.com",
            "customer_phone": "010801252",
            "customer_name": "test customer"
        },
        "billing_address": {
            "province": "Phnom Penh",
            "country": "Cambodia",
            "address_line_2": "string",
            "postal_code": "12000",
            "address_line_1": "No.01, St.01, Toul Kork"
        },
        "description": "Extra note",
        "language": "km",
        "order_items": [
            {
                "consumer_code": "001",
                "amount": 10
            }
        ],
        "payment_success_url": "/payment/success",
        "currency": "USD",
        "amount": 1,
        "pay_later_url": "/payment/paylater",
        "shipping_address": {
            "province": "Phnom Penh",
            "country": "Cambodia",
            "address_line_2": "string",
            "postal_code": "12000",
            "address_line_1": "No.01, St.01, Toul Kork"
        },
        "order_ref":"${binding.orderRef.text}",
        "payment_fail_url": "payment/fail",
        "payment_cancel_url": "payment/cancel",
        "continue_shopping_url": "http://localhost:8090/order"
    }
        """.trimIndent()
            val jsonObject = JSONObject(orderDetailsJson)
            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder().header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJQYXltZW50R2F0ZXdheSIsInN1YiI6IkVEQyIsImhhc2giOiJCQ0ZEQzE1MC0zMjRGLTQzRjQtQkQ3Qi0zMTVGN0Y5NDM3NDAifQ.OZ9AqnbRucNmVlJzQt6kqkRjDDDPjMAN81caYwqKuX4")
                .header("Accept","application/json")
                .url("http://203.217.169.102:50209/order/create/v1")
                .post(jsonObject.toString().toRequestBody(mediaTypeJson))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Exception","$e")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responses = response.body!!.string()
                    Log.d("repp",responses)
                    val checkoutObject = JSONObject(responses)
                    if (checkoutObject.optString("code") != "000") {
                        return
                    }
                    sessionId = checkoutObject.optJSONObject("data").optString("session_id")
                    runOnUiThread {
                        kotlin.run {
                    if (sessionId!="null") {

                                val bottomsheetFrag = paymentSdk(supportFragmentManager = supportFragmentManager,paylater = pay_later(),
                                    sessionId = "$sessionId",
                                    clientID = "fmDJiZyehRgEbBJTkXc7AQ==", activity = this@MainActivity
                                    ,payment_succeeded = payment_succeeded(),language = language,homescreen(),
                                environment = environment){

                                }

                                bottomsheetFrag.show(supportFragmentManager,"sdk_bottomsheet")
                            }

                    else{
                        Toast.makeText(applicationContext,checkoutObject.optString("message"),Toast.LENGTH_SHORT)
                            .show()
                    }
                        }
                    }

                }

            })

//        MainActivity().bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


    }

}
