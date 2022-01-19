package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import com.example.bill24sk.MainActivity
import com.example.bill24sk.bottomSheetController
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.flutter.embedding.android.FlutterActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var button: Button
    var language:String = "en"
    lateinit var sessionId:String


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
            val jsonObject = JSONObject(orderDetailsJson)
            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder().header("token","f91d077940cf44ebbb1b6abdebce0f0a")
                .header("Accept","application/json")
                .url("https://checkoutapi-staging.bill24.net/order/init")
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

                    sessionId = checkoutObject.optJSONObject("data").optString("session_id")
                    runOnUiThread {
                        kotlin.run {
                    if (sessionId!="null") {

                                val bottomsheetFrag = bottomSheetController(supportFragmentManager = supportFragmentManager,paylater = pay_later(),
                                    sessionId = "$sessionId",
                                    clientID = "W/GkvceL7nCjOF/v+fu5MA+epIQMXMJedMeXvbvEn7I=", activity = this@MainActivity
                                    ,payment_succeeded = payment_succeeded(),language = language,homescreen())

                                bottomsheetFrag.show(supportFragmentManager,"bottomsheet")
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
