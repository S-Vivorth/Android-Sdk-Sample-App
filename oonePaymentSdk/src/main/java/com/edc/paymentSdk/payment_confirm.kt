package com.oone.paymentSdk

import android.app.Activity
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.sdk_bottomsheet.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import javax.crypto.SecretKey

class payment_confirm(var environment: String, var activity: Activity, var language:String) {
    internal lateinit var url:String
    internal lateinit var secretKey: SecretKey
    internal lateinit var decrypted_data: String
    fun payment_confirm(token: String, bank_data: String,function: (callback: Any) -> Unit){
        if (environment == "prod") {
            url = config().sdk_api_url_prod
        }
        else{
            url = config().sdk_api_url_demo
        }
        val data = data()
        val url = "${url}/payment/confirm"
        val payload: String = """
            {"url_token": "$token" ,"bank_data": "$bank_data"}
        """.trimIndent()
        Log.d("payload", payload)
        val encypted_payload:String
        secretKey = data.makePbeKey("sdkdev".toCharArray())!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encypted_payload = """
                {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey,payload))}"}
            """.trimIndent()
        }
        else{
            encypted_payload = """
                {"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(secretKey,payload), Base64.DEFAULT)}"}
            """.trimIndent()
        }
        val answer = JSONObject(encypted_payload)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
        val client: OkHttpClient = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responses_data = response
                val responses = responses_data.body!!.string()
                Log.d("responses",responses)
                if (responses_data.code != 200){
                    activity.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(activity,responses, Toast.LENGTH_SHORT).show()
                        }
                    }
                    decrypted_data = responses

                    function("[$decrypted_data]")
                    return
                }
                else{
                    val jsonObject = JSONObject(responses)
                    if (jsonObject.optJSONObject("result").optString("result_code") != "000"){
                        activity.runOnUiThread {
                            kotlin.run {
                                if (language == "kh"){
                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_kh"),
                                        Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_en"),
                                        Toast.LENGTH_SHORT).show()
                                }
                                return@runOnUiThread
                            }

                        }
                        decrypted_data = responses
                        function("[$decrypted_data]")

                        return
                    }
                    else{

                        try {
                            val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                            secretKey = data().makePbeKey("sdkdev".toCharArray())!!
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                decrypted_data = data.cbcDecrypt(
                                    secretKey,
                                    java.util.Base64.getDecoder().decode(encrypted_data)
                                ).toString()
                            } else {
                                decrypted_data = data.cbcDecrypt(
                                    secretKey,
                                    Base64.decode(encrypted_data,Base64.DEFAULT)
                                ).toString()
                            }
                            function("[$decrypted_data]")

                            activity.runOnUiThread {
                                kotlin.run {
                                    Log.d("decrypted", decrypted_data)
                                    Toast.makeText(activity, decrypted_data, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        catch (ex: Exception){
                            decrypted_data = responses
                            function("[${decrypted_data}]")

                            Log.d("exc", ex.toString())
                        }
                    }
                }

            }
        })
    }
}