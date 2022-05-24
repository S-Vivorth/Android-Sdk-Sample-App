package com.bill24.myapplication

import amk.sdk.deeplink.AMKDeeplink
import amk.sdk.deeplink.entity.model.AMKDLMerchant
import amk.sdk.deeplink.entity.model.AMKDeeplinkData
import amk.sdk.deeplink.entity.model.SourceInfo
import amk.sdk.deeplink.entity.response.AMKDLResponse
import amk.sdk.deeplink.presenter.AMKResponseCallback
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import java.lang.Exception
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

//            openAmkDeeplink()
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
                "amount": 1,
                "company_name": "EDC",
                "company_code": "EDC",
                "consumer_name": "ដេវីដ ចន",
                "consumer_name_latin" : "David John"
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
                                environment = environment,theme_mode = ""){

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
    fun openAmkDeeplink() {
        val secretKey = """
        -----BEGIN PRIVATE KEY-----
        MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCqNzuR/Dq6H20B
        A3wh4emTUfXCgStgNNXaeE7hBvtz5+lVA13K8ZuUz4fcNGIltc6BJ5H23LfsNh/c
        qQ+UAqJmwus8EGxr5bcrSITBmefkhJBufWB7FqV6+YsJE+uE1i8jz/arUAXVC93t
        3/Gitclqoc6OGbeNKF9ZkJ2xTYMUsOFastGSjt04enCfsQF59bD77/XrhUPFxQ11
        KnfZfbwytjbRnFA56GayOve32vDplMp2vJkQGNpRoy4KhtyIpz3YQ6eoLEgaoQge
        j7xXRi/GodKnQgKOb9pGKZvPlGIBR5CF9+zN/TCcXTkPqtXqKzCOvUvwN6YzCEnh
        EdOZWx/tAgMBAAECggEAM+0/ngx9efRGU63Ve5yongm92IWBTwsvRkO3hIyVv0k7
        dHTfcx774IzjHHlai7iH8/y3WcEB1uy4EZ/9oaCgHItQKfW0rcHZfDnWTh1+kccj
        LKHRAhvphbeFA9Lw4YhZvyodTSvPa6wAGyZbV9DvTjlogw8zYLu1QuSMRt+nonLH
        0JXxGqG0qGcQYR4QLsGTvgjDGUrMCCWxH12LeB466RAnhTphAAHX3A8UfIXbislw
        NU075xhZuAjEcRUqulAlQsWSe6iPJBCF2qEj88cVjd7fWgL/tFSaG1iFaxk3dueG
        kF8lojPkkg52LN0M+LcufzxRHkqfUSwonuGSzFHdYQKBgQDfyvkatkhgNm7PmgGg
        UGryb+Efb1E8kvyER4q73WYB70f5yAhOnnnh3EgObCQXGN+oUBAr2Oqs55YwDm22
        DNXTvEB/rhEDwUH2EggGNjnswNCv2aPI+olvEYisnOZmgJQww1/2AMKp+WTpqk35
        5EyGW2KAYEWLsowhzvupgO691QKBgQDCtlwUP7403bxITDGVn+JSEpAFQl3hvvho
        ifLTIHLONYLlu0ewSzNqRId2QKjazkZjM39681bIZRBG22NHJEnINer9p7wYBln2
        ajh4SNS5ekFj1rEkCVAw5Qcn0wJbVHYZP0ZIwjkLI1WgUOyD2rDsrRbZnSM9RMjO
        iox00t+tuQKBgA3MjSmZfcL4+EIyw9DnxIBoZ6Axk/fBNHLPmn1U/Ho4D98V93Up
        jmhf1c2V22/VJ81QCn85o9a/fOI/sYIdLn4cyHlW+VOa8f9DQ11msJGpnfSJ3fCB
        ikHf+eZy0j4VxY1wLpWTnG0wpIlH6AD1k8ZhEiTKSt9/Rea7xYbBHXd1AoGAJDhF
        8qJU4IKqxowd4SZntDqtvby1uAuNK+0VVX7AvGkp21A2Kq4id08eH7oxbtpWL5fh
        y94+M3LRT0z6L76pVuvotZyhGZr82yCxNnbd007RoR/LvddZqm7AIQFYe+K/QT0K
        9vfiIpdFE1haVsC0jqI4EOzxJDGKZRlSvVyIrUkCgYBSrELnjKsnjjpBmwxqKrN6
        zaFG+V3X5k0UAd2aWD/5iHtUUyPOY15gAbFTiryTdNhiUYVv35zdJ9fj5Hud0RAF
        33Fsoz/Aef6+8JPj49nNHTbq7wpuisTOdJBdxigWoDlMpIgVQXO8QJF2ccpP/H5Y
        4Dr1I1YsxtE56jftb6Oo0Q==
        -----END PRIVATE KEY-----
        """.trimIndent()
        AMKDeeplink.init("e78e80fc-a20a-4d91-a7ff-1e6a0be126c0",
        secretKey,"http://apiserver/api/oauth/token")
        val sourceInfo = SourceInfo()
        sourceInfo.appIconUrl = "https://admin.edc.com.kh/images/logo/Edc_logo_khmer.png"
        sourceInfo.appName = "EDC Mobile"
        sourceInfo.appDeeplinkCallback = "https://testdeeplink.page.link/y84c"
        val amkdlMerchant: AMKDLMerchant = AMKDLMerchant()
        amkdlMerchant.merchantAccount = "70000686"
        amkdlMerchant.merchantName = "EDC"
        amkdlMerchant.receiverName = "EDC"
        amkdlMerchant.currency = "KHR"
        amkdlMerchant.amount = 10000.00
        amkdlMerchant.merchantCity = "Phnom Penh"
        amkdlMerchant.billNumber = "BILL0021"
        amkdlMerchant.mobileNumber = "012434555"
        amkdlMerchant.storeLabel = "Prek Pnov Branch"
        amkdlMerchant.terminalLabel = "T002"
        amkdlMerchant.referenceId = "12sfd3243700234234s"
        amkdlMerchant.sourceInfo = sourceInfo

        AMKDeeplink.getInstance().generateMerchantDeeplink(
            amkdlMerchant
        ) {
            if (it.status.code == 0){
                val shortLink: String = it.getData().getShortLink()
                val referenceId: String = it.getData()
                    .getReferenceId()
                Log.d("SortLink", shortLink)
                print("SortLink : $shortLink")
                print("ReferenceId : $referenceId")
                try {
                    val url = shortLink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (ex: Exception) {
                    val package_name = "com.domain.acledabankqr"
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${package_name}")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${package_name}")))
                    }
                }
            }
            else{
                Log.d("exceptionn",it.status.message)
            }

        }
    }
    fun openDeeplink() {

        try {
            val url = "market://com.domain.acledabankqr/app/?creditAccount=123456&paymentAmount=10&paymentAmountCcy=USD&paymentPurpose=purpose desc"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (ex: Exception) {
            val package_name = "com.domain.acledabankqr"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${package_name}")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${package_name}")))
            }
        }
    }
}



