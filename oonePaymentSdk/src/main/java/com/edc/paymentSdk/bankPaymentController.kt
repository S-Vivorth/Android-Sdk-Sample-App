package com.oone.paymentSdk

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import android.webkit.WebViewClient
import com.oone.paymentSdk.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.sdk_bank_payment.*
import kotlinx.android.synthetic.main.sdk_bottomsheet.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.util.*
import javax.crypto.SecretKey
import android.content.Intent
import android.net.Uri

import android.webkit.WebView
import androidx.core.content.ContextCompat.startActivity
import android.widget.Toast

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.view.WindowManager
import androidx.annotation.RequiresApi
import android.net.http.SslError

import android.webkit.SslErrorHandler





class bankPaymentController(formdata:String, payment_succeeded:Activity, orderID:String, activity: Activity,
                            paymentSdk: paymentSdk, socketID:String, function:(callback: Any) -> Unit) : BottomSheetDialogFragment() {

    var formdata = formdata
    val payment_succeeded:Activity = payment_succeeded
    val orderID:String = orderID
    val uri = URI.create(config().socket_url)
    val activity = activity
    var paymentSdk = paymentSdk
    val socketID = socketID
    val function = function
    var isFinishPayment = false
    lateinit var socket:Socket
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.sdk_bank_payment,container,false)
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser == false){
            paymentSdk.bankPaymentIsOpened = true
        }
        else{
            paymentSdk.bankPaymentIsOpened = false
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
                """.trimIndent()),Base64.DEFAULT)}"}
            """.trimIndent()
        }
        val client = OkHttpClient()
        val answer = JSONObject(json.replace("\n","").replace("\r",""))
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        var request:Request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request = Request.Builder().header("token",java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent())).toString())
                .header("Accept","application/json")
                .url("${config().socket_url}socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        else{
            request = Request.Builder().header("token",Base64.encodeToString(data.cbcEncrypt(secretKey,"""
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent()),Base64.NO_WRAP).toString())
                .header("Accept","application/json")
                .url("${config().socket_url}socket/send").post(answer.toString().toRequestBody(mediaTypeJson))
                .build()
        }
        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paymentSdk.progressbar.visibility = View.GONE
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        sendProcessing()
        paymentSdk.socket.disconnect()
        Log.d("formdata",formdata.toString())
        paymentSdk.bankPaymentIsOpened = true
        if (paymentSdk.theme_mode == "dark") {
            webView.setBackgroundColor(Color.parseColor("#454545"))
            relative.setBackgroundColor(Color.parseColor("#454545"))
            bank_payment_indicator.setBackgroundColor(Color.parseColor("#ffffff"))
        }
        webView.clearView()
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.settings.domStorageEnabled = true
        try {

            if (JSONObject(formdata).optJSONObject("form_data").toString() == "{}"){

                webView.webViewClient = Browser(activity = activity)
                webView.loadUrl(JSONObject(formdata).optString("action_url"))

            }
            else{
                webView.loadDataWithBaseURL(null,"""
                        <html>
                            <body>
                            <script>
                        function submit_form(data) {
                        console.log("javascriptt data");
                        console.log(data.action_url);
                        var form = document.createElement("form");
                        form.setAttribute("action", data.action_url);
                        form.setAttribute("method", "post");
                        for (var key in data.form_data) {
                            var input = document.createElement("input");
                            input.setAttribute("name", key);
                            input.setAttribute("type", "hidden");
                            input.setAttribute("value", data.form_data[key]);
                            form.appendChild(input);
                        }
                        document.getElementsByTagName("body")[0].appendChild(form);
                        form.submit();
                        }
                        submit_form(${formdata});

                        </script>
                            </body>
                        </html>
        """.trimIndent(),"text/html", "utf-8", null)
            }
        }
        catch (ex: java.lang.Exception){
            exc_handling(ex.toString())
        }




        val data = data()
        val client:OkHttpClient = OkHttpClient()
        val secretKey:SecretKey = data.makePbeKey("client".toCharArray())!!
        val token:String = """
            ${Base64.encodeToString(data.cbcEncrypt(secretKey,"""
                {"app_id":"sdk","room_name":"$orderID"}
            """.trimIndent()),Base64.NO_WRAP)}
        """.trimIndent()

        val options = IO.Options.builder().setAuth(mapOf("token" to "$token")).build()
        socket = IO.socket(uri,options)
        socket.on("payment_success", Emitter.Listener {
            activity.runOnUiThread {
                kotlin.run {
                    try {
                        socket.disconnect()

                    }
                    catch (ex: Exception){
                        Log.d("exeption", ex.toString())
                    }

                    isFinishPayment = true
                    function(it)
//                    val tran_data = it[0]
//
//                    val intent = Intent(activity,payment_succeeded::class.java)
//                    val data_to_pass = arrayOf<String>(JSONObject(tran_data.toString())
//                        .optJSONObject("tran_data")!!.optString("trans_id"),
//                        JSONObject(tran_data.toString())
//                            .optJSONObject("tran_data")!!.optString("bank_name_en"),
//                        JSONObject(tran_data.toString())
//                            .optJSONObject("tran_data")!!.optString("bank_ref"),
//                        JSONObject(tran_data.toString())
//                            .optJSONObject("tran_data")!!.optString("order_ref"))
//                    intent.putExtra("tran_data", data_to_pass)

//                    activity.startActivity(intent)
//                    activity.finish()
                    dismiss()
                    paymentSdk.dismiss()
                }

            }

        })
        try {
            socket.connect()

        }
        catch (ex: Exception){
            Log.d("exception",ex.toString())
        }


    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        try {
            paymentSdk.socket.connect()
            socket.disconnect()
        } catch (ex: java.lang.Exception) {
            Log.d("exception", ex.toString())
        }
    }
    fun exc_handling(message: String) {
        activity.runOnUiThread {
            kotlin.run {
                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
    internal class Browser(activity: Activity) : WebViewClient() {
        lateinit var activity: Activity
        init {
            this.activity = activity
        }


        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.contains("plb://payment")) {

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    activity.startActivity(intent)
                    return true
                } catch (ex: java.lang.Exception) {
                    try {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=kh.com.phillipbank.smartsmsbanking")))
                    } catch (e: ActivityNotFoundException) {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kh.com.phillipbank.smartsmsbanking")))
                    }
                }
                return true
            }
            else{
                return false
            }

        }
    }
}

