package com.example.bill24sk

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.FragmentTransitionSupport
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.bottomsheet.*
import kotlinx.android.synthetic.main.payment_processing.*
import kotlinx.android.synthetic.main.payment_succeeded.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Array
import java.net.URI
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.Security
import java.util.*
import java.util.Base64.getEncoder

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import kotlin.collections.ArrayList




class bottomSheetController(supportFragmentManager: FragmentManager,paylater:Activity,sessionId:String,
                            clientID:String,activity:Activity,payment_succeeded:Activity,
                            language:String): BottomSheetDialogFragment()
{
    var supportFragmentManager: FragmentManager = supportFragmentManager
    var paylater:Activity = paylater
    val payment_succeeded = payment_succeeded
    val activity = activity
    var sessionId = sessionId
    var clientID = clientID
    lateinit var secretKey: SecretKey
    var bankIdList: ArrayList<String> = ArrayList()
    var supportTokenize:ArrayList<Any> = ArrayList()
    lateinit var order_details:String
    lateinit var orderID:String
    var support_deeplink: ArrayList<String> = ArrayList()
    var language:String = language
    lateinit var selected_payment_method_button:JSONObject
    lateinit var savedAccLabelColor:String
    lateinit var confirmbtnColor:String
    val uri = URI.create("https://socketio-demo.bill24.net/")
    var bankPaymentIsOpened = false
    var count = 0
    var custom_font = ResourcesCompat.getFont(activity,R.font.kh9)

    fun setbankPaymentIsOpened(){
        bankPaymentIsOpened = true
    }
    fun print(){
        Log.d("printt","asdadd")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser == false) {
            bankPaymentIsOpened = true
        }
        else{
            bankPaymentIsOpened = false
        }
    }
    fun socket(){
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
        activity.runOnUiThread {
            kotlin.run {
                val dialog = Dialog(activity)
                dialog.setContentView(R.layout.payment_processing)
                if (language != "en"){
                    dialog.processingText.text = "កំពុងប្រតិបត្តិការបង់ប្រាក់"
                    dialog.processingText.setTypeface(custom_font)
                }
                dialog.setCanceledOnTouchOutside(false)
                val payment_succeeded_dialog = Dialog(activity)
                payment_succeeded_dialog.setContentView(R.layout.payment_succeeded)
                payment_succeeded_dialog.setCanceledOnTouchOutside(false)
                payment_succeeded_dialog.continueShoppingBtn.setOnClickListener {
                    this.dialog!!.dismiss()
                    payment_succeeded_dialog.dismiss()
                    Log.d("das","das")
                }
                socket.on("payment_processing", Emitter.Listener {
                    activity.runOnUiThread {
                        kotlin.run {
                            dialog.show()
                        }
                    }

                })
                socket.on("payment_success", Emitter.Listener {
                    activity.runOnUiThread {
                        kotlin.run {
                            Log.d("dataaa",Arrays.toString(it))
                            val jsonObject = JSONObject(it[0].toString())
                            val trans_id = jsonObject.optJSONObject("tran_data").optString("trans_id")
                            val data = data()
                            val client = OkHttpClient()
                            val secretKey = data.makePbeKey("sdkdev".toCharArray())
                            val json:String
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                json = """
                                    {"encrypted_data" : "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(
                                    secretKey!!,"""
                                        {"trans_id" : "$trans_id"}
                                    """.trimIndent()
                                    ))}"}
                                """.trimIndent()
                            }
                            else{
                                json = """
                                    {"encrypted_data" : "${Base64.encodeToString(data.cbcEncrypt(
                                    secretKey!!,"""
                                        {"trans_id" : "$trans_id"}
                                    """.trimIndent()
                                    ),Base64.DEFAULT)}"}
                                """.trimIndent()
                            }
                            val answer = JSONObject(json)
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                            val request = Request.Builder().url("https://sdkapi-demo.bill24.net/payment/verify").post(answer.toString().toRequestBody(mediaTypeJson)).build()
                            client.newCall(request).enqueue(object : Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("Exception","$e")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses = response.body!!.string()
                                    Log.d("responsee",responses)
                                    val jsonObject = JSONObject(responses)
                                    val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                                    try {
                                        var decrypted_data:String
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
                                        Log.d("Decryptedd",decrypted_data.toString())
                                        val decryptJson = JSONObject(decrypted_data)
                                        activity.runOnUiThread {
                                            kotlin.run {
                                                payment_succeeded_dialog.pmsucceededLabel.typeface = custom_font
                                                payment_succeeded_dialog.orderIdLabel.typeface = custom_font
                                                payment_succeeded_dialog.bankRefLabel.typeface = custom_font
                                                payment_succeeded_dialog.pmLabel.typeface = custom_font
                                                payment_succeeded_dialog.totalLabel.typeface = custom_font
                                                payment_succeeded_dialog.continueShoppingBtn.typeface = custom_font
                                                payment_succeeded_dialog.pmMethodValue.typeface = custom_font
                                                if (language != "en"){
                                                    payment_succeeded_dialog.pmsucceededLabel.text = "ការទូទាត់ប្រាក់បានជោគជ័យ"
                                                    payment_succeeded_dialog.pmsucceededLabel.typeface = custom_font
                                                    payment_succeeded_dialog.orderIdLabel.text = "លេខបញ្ជាទិញ"
                                                    payment_succeeded_dialog.orderIdLabel.typeface = custom_font
                                                    payment_succeeded_dialog.bankRefLabel.text = "លេខកូដយោង"
                                                    payment_succeeded_dialog.bankRefLabel.typeface = custom_font
                                                    payment_succeeded_dialog.pmLabel.text = "ទូទាត់តាម"
                                                    payment_succeeded_dialog.totalLabel.text = "ទឹកប្រាក់"
                                                    payment_succeeded_dialog.continueShoppingBtn.text = "បន្តការទិញ"
                                                    payment_succeeded_dialog.pmMethodValue.text = decryptJson.optString("bank_name_kh")
                                                }
                                                else{
                                                    payment_succeeded_dialog.pmMethodValue.text = decryptJson.optString("bank_name_en")

                                                }
                                                payment_succeeded_dialog.orderIdValue.typeface = custom_font
                                                payment_succeeded_dialog.bankRefvalue.typeface = custom_font
                                                payment_succeeded_dialog.totalValue.typeface = custom_font
                                                payment_succeeded_dialog.orderIdValue.text = decryptJson.optString("order_ref")

                                                payment_succeeded_dialog.bankRefvalue.text = decryptJson.optString("bank_ref")
                                                payment_succeeded_dialog.totalValue.text = decryptJson.optString("total_amount")

                                            }
                                        }


                                    }
                                    catch (e:Exception){
                                        Log.d("$e","$e")
                                    }
                                }

                            })
                            dialog.dismiss()

                            payment_succeeded_dialog.show()

                        }
                    }
                })
            }

        }


        socket.connect()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            //for full screen
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // for expand to specific height
            behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels) * 0.95).toInt()
            //Resources.getSystem().displayMetrics.heightPixels is to get screen height
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet,container,false)
        view.setBackgroundResource(android.R.color.transparent)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchbutton.isEnabled = false
        progressbar.visibility = View.VISIBLE
        progressbar.bringToFront()
        pmMethodLabel.typeface = custom_font
        if (language != "en"){
            pmMethodLabel.text = "វិធីទូទាត់ប្រាក់"
        }
        activity.runOnUiThread {
            kotlin.run {

                var itemModel = ArrayList<itemModel>()
                val data = data()
                var client: OkHttpClient = OkHttpClient()


                secretKey = data.makePbeKey("sdkdev".toCharArray())!!

                val json: String = """
            {"encrypted_data": "${
                    Base64.encodeToString(
                        data.cbcEncrypt(
                            secretKey, """
                {"session_id": "$sessionId", "client_id": "$clientID"}
            """.trimIndent()
                        ), Base64.DEFAULT
                    )
                }"}
        """.trimIndent()
                Log.d("Jsonn", json)
                val answer = JSONObject(json)
                Log.d("answerr",answer.toString())
                val url: String = "https://sdkapi-demo.bill24.net/payment/widget-init"
                val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                val request =
                    Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson))
                        .build()


                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("Failedd", "${e}")
                        println("Faileddd: ${e.localizedMessage}")
                        activity.runOnUiThread {
                            kotlin.run {
                                Toast.makeText(activity,e.localizedMessage!!.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responses = response.body!!.string()

                        println("response: ${responses}")
                        val jsonObject = JSONObject(responses)
                        //optString coz encrypted_data value is String
                        // if its value is jsonobject or jsonArray => we set it base on its type

                        val encrypted_data: String =
                            jsonObject.getJSONObject("data").optString("encrypted_data")
                        Log.d(
                            "dataa",
                            "${jsonObject.getJSONObject("data").optString("encrypted_data")}"
                        )
                        try {
                            secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                            val decrypted_data = data.cbcDecrypt(
                                secretKey,
                                Base64.decode(encrypted_data, Base64.DEFAULT)
                            )

                            val pmJson = JSONObject(decrypted_data.toString())
                            order_details = pmJson.optJSONObject("order_detail")!!.toString()
                            orderID = pmJson.optJSONObject("order_detail")!!.optString("order_id")
                            if (bankPaymentIsOpened == false) {
                                socket()
                            }



                            val pmList = pmJson.optJSONArray("payment_methods")
                            val existingAcc = pmJson.optJSONArray("existing_accounts")
                            val dict_style = pmJson.optJSONObject("style")
                            selected_payment_method_button = dict_style.optJSONObject("selected_payment_method_button")
                            activity.runOnUiThread {
                                kotlin.run {
                                    confirmbtnColor = dict_style.optJSONObject("payment_confirm_button").
                                    optString("background_color")
                                    confirmbtn.setBackgroundColor(Color.parseColor(dict_style.optJSONObject("payment_confirm_button").
                                    optString("background_color")))
                                    confirmbtn.setTextColor(Color.parseColor(dict_style.optJSONObject("payment_confirm_button").
                                    optString("text_color")))
                                    confirmbtn.setTextSize(20F)
                                    if (language == "en"){
                                        confirmbtn.text = dict_style.optJSONObject("payment_confirm_button").optString("display_text_en")
                                    }
                                    else{
                                        confirmbtn.text = dict_style.optJSONObject("payment_confirm_button").optString("display_text_kh")
                                    }
                                    save_acc_label.setTextColor(Color.parseColor(dict_style.optJSONObject("remember_account_label").
                                    optString("text_color")))
                                    save_acc_label.typeface = custom_font
                                    if (language == "en"){

                                        save_acc_label.text = pmJson.optJSONObject("style").optJSONObject("remember_account_label")
                                            .optString("display_text_en")
                                    }
                                    else{
                                        save_acc_label.text = pmJson.optJSONObject("style").optJSONObject("remember_account_label")
                                            .optString("display_text_kh")
                                    }
                                }
                            }



                            for (item in 0..pmList!!.length() - 1) {
                                val json = JSONObject(pmList[item].toString())
                                bankIdList.add(json.optString("bank_id"))

                                supportTokenize.add(json.optString("support_tokenize"))
                                support_deeplink.add(json.optString("support_deeplink"))
                                var bank_name:String
                                if (language == "en") {
                                    bank_name = json.optString("bank_name_en")
                                }
                                else {
                                    if (json.optString("bank_name_kh") == ""){
                                        bank_name = json.optString("bank_name_en")
                                    }
                                    else{
                                        bank_name = json.optString("bank_name_kh")
                                    }
                                }
                                itemModel.add(
                                    itemModel(
                                        json.optString("bank_logo"),
                                        bank_name,
                                        json.optDouble("fee").toString() + "0 USD"
                                    )
                                )
                                val adapter = banksAdapter(
                                    supportFragmentManager,
                                    paylater,
                                    itemModel,
                                    activity,
                                    this@bottomSheetController,
                                    payment_succeeded = payment_succeeded
                                ,order_details,
                                language = language)
                                val linearmanager: LinearLayoutManager =
                                    LinearLayoutManager(context)

                                linearmanager.orientation = RecyclerView.VERTICAL
                                activity.runOnUiThread {
                                    kotlin.run {
                                        bankRecycler.layoutManager = linearmanager
                                        bankRecycler.adapter = adapter

                                    }
                                }
                            }

                            if (existingAcc.length() > 0) {
                                activity.runOnUiThread {
                                    kotlin.run {
                                        pmMethodLabel.setTextColor(Color.parseColor(pmJson.optJSONObject("style")!!
                                            .optJSONObject("saved_account_label")!!
                                            .optString("text_color")))
                                        savedAccLabelColor = pmJson.optJSONObject("style")!!
                                            .optJSONObject("saved_account_label")!!
                                            .optString("text_color")
                                    }
                                }

                                if (language == "en") {
                                    itemModel.add(itemModel("", pmJson.optJSONObject("style")!!
                                        .optJSONObject("saved_account_label")!!
                                        .optString("display_text_en"), ""))
                                }
                                else{
                                    itemModel.add(itemModel("", pmJson.optJSONObject("style")!!
                                        .optJSONObject("saved_account_label")!!
                                        .optString("display_text_kh"), ""))
                                }


                                for (item in 0..existingAcc.length() - 1) {
                                    val json = JSONObject(existingAcc[item].toString())
                                    var bank_name:String
                                    if (language == "en") {
                                        bank_name = json.optString("bank_name_en")
                                    }
                                    else {
                                        if (json.optString("bank_name_kh") == ""){
                                            bank_name = json.optString("bank_name_en")
                                        }
                                        else{
                                            bank_name = json.optString("bank_name_kh")
                                        }
                                    }
                                    itemModel.add(
                                        itemModel(
                                            json.optString("bank_logo"),
                                            bank_name,
                                            json.optString("account_no").toString()
                                        )
                                    )
                                    bankIdList.add(json.optString("bank_id")+"$$$"+json.optString("tokenize_id"))
                                    val adapter = banksAdapter(
                                        supportFragmentManager,
                                        paylater,
                                        itemModel,
                                        activity,
                                        this@bottomSheetController,
                                        payment_succeeded = payment_succeeded,
                                        order_details,
                                        language = language
                                    )
                                    val linearmanager: LinearLayoutManager =
                                        LinearLayoutManager(context)
                                    linearmanager.orientation = RecyclerView.VERTICAL
                                    activity.runOnUiThread {
                                        kotlin.run {
                                            bankRecycler.layoutManager = linearmanager
                                            bankRecycler.adapter = adapter
                                            progressbar.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                            println(pmList)

                        } catch (e: GeneralSecurityException) {
                            e.printStackTrace()
                        }
                    }
                })

            }
        }

    }

}
