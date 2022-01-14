package com.example.bill24sk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_bank_controller.*
import kotlinx.android.synthetic.main.bank_payment.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.*
import javax.crypto.SecretKey
import kotlin.collections.ArrayList
import java.io.Serializable
import java.net.NetworkInterface

class bankController : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_controller)

        val data_array = intent.getStringArrayListExtra("data")
//        val a = Class.forName("$data_array").asSubclass(Activity::class.java)

//        button.setOnClickListener {
//            val intent = Intent()
//            intent.setClassName("com.example.myapplication","$data_array")
//            startActivity(intent)
//        }


        var formdata = data_array!![0] as String
        val payment_succeeded = data_array!![1] as String
        val orderID:String = data_array!![2] as String
        val socketID:String = data_array!![3] as String
        val uri = URI.create("https://socketio-demo.bill24.net/")
//        val activity = data_array!![3] as Activity
        val url = "http://203.217.169.102:60096"
        sendProcessing(orderID,socketID)
        webView1.loadDataWithBaseURL(null,"""
                        <html>

                            <body>

                            <script>
                        function submit_form(data) {

                        let form = document.createElement("form");
                        form.setAttribute("action", data.action_url);
                        form.setAttribute("method", "post");
                        for (let key in data.form_data) {
                            let input = document.createElement("input");
                            input.setAttribute("name", key);
                            input.setAttribute("type", "hidden");
                            input.setAttribute("value", data.form_data[key]);
                            form.appendChild(input);
                        }
                        document.getElementsByTagName("body")[0].append(form);
                        form.submit();
                        }
                        submit_form($formdata);

                        </script>
                            </body>
                        </html>
        """.trimIndent(),"text/html", "utf-8", null)
        webView1.settings.javaScriptEnabled = true
        webView1.webViewClient = WebViewClient()
        val data = data()
        val client:OkHttpClient = OkHttpClient()
        val secretKey: SecretKey = data.makePbeKey("client".toCharArray())!!
        val token:String = """
            ${Base64.encodeToString(data.cbcEncrypt(secretKey,"""
                {"app_id":"sdk","room_name":"$orderID"}
            """.trimIndent()),Base64.DEFAULT)}
        """.trimIndent()

        val options = IO.Options.builder().setAuth(mapOf("token" to "$token")).build()
        val socket = IO.socket(uri,options)
        socket.on("payment_success", Emitter.Listener {
            this.runOnUiThread {
                kotlin.run {
                    Log.d("Ittt", Arrays.toString(it))
                    Toast.makeText(this,"Succeeded", Toast.LENGTH_LONG).show()
                    val tran_data = it[0]

                    val intent = Intent()
                    val data_to_pass = arrayOf<String>(JSONObject(tran_data.toString())
                        .optJSONObject("tran_data")!!.optString("trans_id"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("bank_name_en"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("bank_ref"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("order_ref"))
                    intent.putExtra("tran_data", data_to_pass)
                    //need to set intent like this, otherwise it will get error
                    intent.setClassName("com.example.myapplication","$payment_succeeded")
                    this.startActivity(intent)
                    socket.disconnect()
                }
            }

        })
        socket.connect()


    }


    fun sendProcessing(orderID:String,socketID:String){
        var json:String
        val data = data()
        val secretKey = data.makePbeKey("admin".toCharArray())
        val a = NetworkInterface.getNetworkInterfaces()
            .toList()
            .find { networkInterface -> networkInterface.name.equals("wlan0", ignoreCase = true) }
            ?.hardwareAddress
            ?.joinToString(separator = ":") { byte -> "%02X".format(byte)
            }
        val wifiMan =
            applicationContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInf = wifiMan.connectionInfo
        val ipAddress = wifiInf.ipAddress
        val ip = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            json = """
                {"data" : "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey!!,"""
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()))}"}
            """.trimIndent()
        }
        else{
            json = """
                {"data" : "${
                Base64.encodeToString(data.cbcEncrypt(secretKey!!,"""
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()), Base64.DEFAULT)}"
            """.trimIndent()
        }
        val client = OkHttpClient()
        val answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        var request: Request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request = Request.Builder().header("token",java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent())).toString())
                .header("Accept","application/json")
                .url("https://socketio-demo.bill24.net/socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        else{
            request = Request.Builder().header("token",
                Base64.encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent()), Base64.DEFAULT).toString())
                .header("Accept","application/json")
                .url("https://socketio-demo.bill24.net/socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("abc",response.body!!.string())
            }

        })
    }

}