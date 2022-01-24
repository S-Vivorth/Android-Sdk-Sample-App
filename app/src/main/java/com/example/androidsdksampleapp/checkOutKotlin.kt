package com.example.androidsdksampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.androidsdksampleapp.databinding.ActivityMainBinding
import com.example.bill24sk.bottomSheetController
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class checkOutKotlin : AppCompatActivity() {
    var language:String = "en"
    lateinit var sessionId:String
    lateinit var binding:ActivityMainBinding
    val token:String = "f91d077940cf44ebbb1b6abdebce0f0a"
    val url:String = "https://checkoutapi-demo.bill24.net"
    val clientId:String = "W/GkvceL7nCjOF/v+fu5MA+epIQMXMJedMeXvbvEn7I="
    // environment must be either "uat" or "prod" only
    val environment:String = "prod"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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
            // function to create sessionId and call Sdk
            createSessionId()
        }

    }

    fun createSessionId(){

        //create okhttp instance
        val client = OkHttpClient()

        // biller's payload will be given to Sdk
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
                "item_name": "Men T-Shirt",
                "quantity": 1,
                "price": 1,
                "amount": 1,
                "item_ref": "P1001",
                "discount_amount": 0
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
        "continue_shopping_url": "payment/cancel"
    }
        """.trimIndent()

        // convert payload string to JsonObject
        val jsonObject = JSONObject(orderDetailsJson)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

        // unique token will be given by Bill24 to the biller once registered
        val request = Request.Builder().header("token",token)
            .header("Accept","application/json")
            .url("$url/order/init")
            .post(jsonObject.toString().toRequestBody(mediaTypeJson))
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Exception","$e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responsed = response
                val responses = responsed.body!!.string()
                Log.d("gg",responses)

                val checkoutObject = JSONObject(responses)
                if (responsed.code != 200) {
                    Toast.makeText(applicationContext,JSONObject(responses).optString("message")
                    ,Toast.LENGTH_SHORT).show()
                }
                else{
                    sessionId = checkoutObject.optJSONObject("data")!!.optString("session_id")
                    runOnUiThread {
                        kotlin.run {
                            callSdk(checkoutObject)
                        }
                    }
                }
            }
        })
    }

    fun callSdk(checkoutObject: JSONObject){
        if (sessionId != "null") {

            // supportFragmentManager can be get from the activity
            // pay_later() is activity which will be navigated to, when user choose pay later option
            // sessionId is the string which get from checkout response
            // clientID is the string given by bill24 to the biller
            // activity is the current activity
            // payment_succeeded is the activity which will be navigated to, when the payment is succeeded
            // language is the string that specify the language. Language can be "en" or "kh" only.
            // continue_shopping is the activity which will be navigated to, when user press continue shopping button
            // environment is the environment that you want to use

            val bottomsheetFrag = bottomSheetController(supportFragmentManager = supportFragmentManager,paylater = pay_later(),
                sessionId = sessionId,
                clientID = clientId, activity = this@checkOutKotlin
                ,payment_succeeded = payment_succeeded(),language = language,continue_shopping =  homescreen()
            ,environment = environment)

            // to call sdk screen
            bottomsheetFrag.show(supportFragmentManager,"bottomsheetsdk")
        }

        else{
            Toast.makeText(applicationContext,checkoutObject.optString("message"),Toast.LENGTH_SHORT)
                .show()
        }
    }

}

