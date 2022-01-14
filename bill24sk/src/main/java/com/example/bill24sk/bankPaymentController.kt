package com.example.bill24sk

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bank_payment.*

import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.bottomsheet.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.NetworkInterface
import java.net.URI
import java.util.*
import javax.crypto.SecretKey
import kotlin.collections.ArrayList


class bankPaymentController(formdata:String,payment_succeeded:Activity,orderID:String,activity: Activity,
bottomSheetController: bottomSheetController,socketID:String) : BottomSheetDialogFragment() {

    var formdata = formdata
    val payment_succeeded:Activity = payment_succeeded
    val orderID:String = orderID
    val uri = URI.create("https://socketio-demo.bill24.net/")
    val activity = activity
    var bottomSheetController = bottomSheetController
    val url = "https://sdkapi-demo.bill24.net"
    val socketID = socketID
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bank_payment,container,false)
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser == false){
            bottomSheetController.bankPaymentIsOpened = true
        }
        else{
            bottomSheetController.bankPaymentIsOpened = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels) * 1).toInt()

        }
    }
    fun sendProcessing(){
        var json:String
        val data = data()
        val secretKey = data.makePbeKey("admin".toCharArray())
        val a = NetworkInterface.getNetworkInterfaces()
            .toList()
            .find { networkInterface -> networkInterface.name.equals("wlan0", ignoreCase = true) }
            ?.hardwareAddress
            ?.joinToString(separator = ":") { byte -> "%02X".format(byte)
            }
            Log.d("MacAddresss",a.toString())
//        val wifiMan =
//            activity.applicationContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInf = wifiMan.connectionInfo
//        val ipAddress = wifiInf.ipAddress
//        val ip = String.format(
//            "%d.%d.%d.%d",
//            ipAddress and 0xff,
//            ipAddress shr 8 and 0xff,
//            ipAddress shr 16 and 0xff,
//            ipAddress shr 24 and 0xff
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            json = """
                {"data" : "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey!!,"""
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()))}"}
            """.trimIndent()
        }
        else{
            json = """
                {"data" : "${Base64.encodeToString(data.cbcEncrypt(secretKey!!,"""
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()),Base64.DEFAULT)}"
            """.trimIndent()
        }
        val client = OkHttpClient()
        val answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        var request:Request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request = Request.Builder().header("token",java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent())).toString())
                .header("Accept","application/json")
                .url("https://socketio-demo.bill24.net/socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        else{
            request = Request.Builder().header("token",Base64.encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent()),Base64.DEFAULT).toString())
                .header("Accept","application/json")
                .url("https://socketio-demo.bill24.net/socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("abc",response.body!!.string())
            }

        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendProcessing()
        bottomSheetController.bankPaymentIsOpened = true
        webView.loadDataWithBaseURL(null,"""
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
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        val data = data()
        val client:OkHttpClient = OkHttpClient()
        val secretKey:SecretKey = data.makePbeKey("client".toCharArray())!!
        val token:String = """
            ${Base64.encodeToString(data.cbcEncrypt(secretKey,"""
                {"app_id":"sdk","room_name":"$orderID"}
            """.trimIndent()),Base64.DEFAULT)}
        """.trimIndent()

        val options = IO.Options.builder().setAuth(mapOf("token" to "$token")).build()
        val socket = IO.socket(uri,options)
        socket.on("payment_success", Emitter.Listener {
            activity.runOnUiThread {
                kotlin.run {
                    socket.disconnect()
                    Log.d("Ittt", Arrays.toString(it))
                    Toast.makeText(activity,"Succeeded",Toast.LENGTH_LONG).show()
                    val tran_data = it[0]

                    val intent = Intent(activity,payment_succeeded::class.java)
                    val data_to_pass = arrayOf<String>(JSONObject(tran_data.toString())
                        .optJSONObject("tran_data")!!.optString("trans_id"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("bank_name_en"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("bank_ref"),
                        JSONObject(tran_data.toString())
                            .optJSONObject("tran_data")!!.optString("order_ref"))
                    intent.putExtra("tran_data", data_to_pass)
                    activity.startActivity(intent)
                }
            }

        })
        socket.connect()


    }

}