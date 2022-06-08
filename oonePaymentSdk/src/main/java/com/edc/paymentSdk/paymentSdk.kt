package com.oone.paymentSdk

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.marozzi.roundbutton.RoundButton
import io.socket.client.IO

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.security.GeneralSecurityException
import java.util.*

import javax.crypto.SecretKey
import kotlin.collections.ArrayList


import android.net.wifi.WifiManager
import com.oone.paymentSdk.R
import io.socket.emitter.Emitter

import kotlinx.android.synthetic.main.sdk_bottomsheet.*
import kotlinx.android.synthetic.main.sdk_payment_processing.*
import kotlinx.android.synthetic.main.sdk_payment_succeeded.*
import kotlinx.android.synthetic.main.sdk_save_acc_cell.*


open class paymentSdk @JvmOverloads constructor(supportFragmentManager: FragmentManager, paylater:Activity? = MainActivity(), sessionId:String,
                                                clientID:String, activity:Activity, payment_succeeded:Activity,
                                                language:String, continue_shopping:Activity,
                                                environment:String, theme_mode: String, function:(callback: Any) -> Unit): BottomSheetDialogFragment()

{
    internal var supportFragmentManager: FragmentManager = supportFragmentManager
    internal var paylater:Activity? = paylater
    internal val payment_succeeded = payment_succeeded
    internal val activity = activity
    internal var sessionId = sessionId
    internal var clientID = clientID
    internal lateinit var secretKey: SecretKey
    internal var bankCodeList: ArrayList<String> = ArrayList()
    internal var bankIdList: ArrayList<String> = ArrayList()
    internal var isFavouriteList: ArrayList<Any> = ArrayList()
    internal var supportTokenize:ArrayList<Any> = ArrayList()
    internal lateinit var order_details:String
    internal lateinit var orderID:String
    internal var support_deeplink: ArrayList<String> = ArrayList()
    internal var in_app: ArrayList<String> = ArrayList()
    internal var language:String = language
    internal var continue_shopping = continue_shopping
    internal lateinit var selected_payment_method_button:JSONObject
    internal lateinit var rememberAccLabel: JSONObject
    internal lateinit var paymentConfirmBtn : JSONObject
    internal lateinit var saveAccLabel : JSONObject
    internal lateinit var paymentMethodBtn:JSONObject
    internal lateinit var savedAccLabelColor:String
    internal lateinit var confirmbtnColor:String
    internal lateinit var favouriteStyle:JSONObject
    internal val uri = URI.create(config().socket_url)
    internal lateinit var url:String
    internal var bankPaymentIsOpened = false
    internal var count = 0
    internal var custom_font = ResourcesCompat.getFont(activity, R.font.kh9)
    internal lateinit var socketID:String
    internal lateinit var socket :io.socket.client.Socket
    internal val environment:String = environment
    internal val function = function
    internal val theme_mode = theme_mode
    internal lateinit var text_color:String
    internal var count1: Int = 0
    internal lateinit var order_detail: JSONObject
    override fun onResume() {
        super.onResume()
        bankPaymentIsOpened = false
        if (count1 > 0){
            val payment_succeeded_dialog = Dialog(activity)
            payment_succeeded_dialog.setContentView(R.layout.sdk_payment_succeeded)
            payment_succeeded_dialog.setCanceledOnTouchOutside(false)
            payment_succeeded_dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.WHITE))
            if (theme_mode == "dark") {
                payment_succeeded_dialog.pm_succeeded_layout.setBackgroundColor(Color.parseColor("#454545"))
                payment_succeeded_dialog.pmsucceededLabel.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.orderIdLabel.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.orderIdValue.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.pmLabel.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.pmMethodValue.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.bankRefLabel.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.bankRefvalue.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.totalLabel.setTextColor(Color.parseColor("#ffffff"))
                payment_succeeded_dialog.totalValue.setTextColor(Color.parseColor("#ffffff"))

            }
            payment_succeeded_dialog.continueShoppingBtn.setOnClickListener {
                try {
                    socket.disconnect()
                    payment_succeeded_dialog.dismiss()

                    this.dialog!!.dismiss()
//                    activity.startActivity(Intent(activity,continue_shopping::class.java))
                    this.dismiss()
                    supportFragmentManager.beginTransaction().detach(this).commit()
                }
                catch (ex:Exception){
                    Log.d("ex",ex.toString())
                }


            }
            activity.runOnUiThread {
                kotlin.run {

                    if (language != "en"){
                        payment_succeeded_dialog.pmsucceededLabel.text = "ការទូទាត់ប្រាក់បានជោគជ័យ"
                        payment_succeeded_dialog.orderIdLabel.text = "លេខបញ្ជាទិញ"
                        payment_succeeded_dialog.bankRefLabel.text = "លេខកូដយោង"
                        payment_succeeded_dialog.pmLabel.text = "ទូទាត់តាម"
                        payment_succeeded_dialog.totalLabel.text = "ទឹកប្រាក់"
                        payment_succeeded_dialog.continueShoppingBtn.text = "បន្ត"
                        payment_succeeded_dialog.pmMethodValue.text = order_detail.optString("bank_name_kh")
                    }
                    else{
                        payment_succeeded_dialog.pmMethodValue.text = order_detail.optString("bank_name_en")

                    }
                    payment_succeeded_dialog.orderIdValue.text = "#"+order_detail.optString("order_ref")

                    payment_succeeded_dialog.bankRefvalue.text = order_detail.optString("bank_ref")
                    payment_succeeded_dialog.totalValue.text = order_detail.optString("total_amount")+"0 USD"
                }
            }
        }
    }


    internal fun socket(){
        val data = data()
        val client:OkHttpClient = OkHttpClient()
        val secretKey:SecretKey = data.makePbeKey("client".toCharArray())!!
        val token:String = """
            ${Base64.encodeToString(data.cbcEncrypt(secretKey,"""
                {"app_id":"sdk","room_name":"$orderID"}
            """.trimIndent()),Base64.DEFAULT)}
        """.trimIndent()

        val options = IO.Options.builder().setAuth(mapOf("token" to "$token")).build()
        socket = IO.socket(uri,options)

        activity.runOnUiThread {
            kotlin.run {
                val dialog = Dialog(activity)
                dialog.setContentView(R.layout.sdk_payment_processing)
                dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                if (theme_mode == "dark") {
                    dialog.pm_processing_relative.setBackgroundColor(Color.parseColor("#454545"))
                    dialog.processingText.setTextColor((Color.parseColor("#ffffff")))

                }
                if (language != "en"){
                    dialog.processingText.text = "កំពុងប្រតិបត្តិការបង់ប្រាក់"
                    dialog.processingText.setTypeface(custom_font)

                }
                dialog.setCanceledOnTouchOutside(false)
                val payment_succeeded_dialog = Dialog(activity)
                payment_succeeded_dialog.setContentView(R.layout.sdk_payment_succeeded)
                payment_succeeded_dialog.setCanceledOnTouchOutside(false)
                payment_succeeded_dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.WHITE))
                if (theme_mode == "dark") {
                     payment_succeeded_dialog.pm_succeeded_layout.setBackgroundColor(Color.parseColor("#454545"))
                    payment_succeeded_dialog.pmsucceededLabel.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.orderIdLabel.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.orderIdValue.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.pmLabel.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.pmMethodValue.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.bankRefLabel.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.bankRefvalue.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.totalLabel.setTextColor(Color.parseColor("#ffffff"))
                    payment_succeeded_dialog.totalValue.setTextColor(Color.parseColor("#ffffff"))

                }
                payment_succeeded_dialog.continueShoppingBtn.setOnClickListener {
                    socket.disconnect()
                    payment_succeeded_dialog.dismiss()

                    this.dialog!!.dismiss()
//                    activity.startActivity(Intent(activity,continue_shopping::class.java))
                    this.dismiss()
                    supportFragmentManager.beginTransaction().detach(this).commit()

                }
                    socket.on("payment_processing", Emitter.Listener {
                        count1 = count1 + 1
                        Log.d("it[1]","processing")
                        activity.runOnUiThread {

                            kotlin.run {
                                try{
                                    if (it[0].toString() != socket.id().toString()) {

                                    Log.d("it[0]",it.toString())

                                    dialog.show()

                                }
                                }
                                catch (ex: Exception){
                                    Log.d("ex",ex.toString())

                                }

                            }
                        }
                    })
                    socket.on("payment_success", Emitter.Listener {
                        Log.d("it[2]", "success")
                        activity.runOnUiThread {
                            kotlin.run {
                                socket.disconnect()
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
                                val request = Request.Builder().url("$url/payment/verify").post(answer.toString().toRequestBody(mediaTypeJson)).build()
                                client.newCall(request).enqueue(object : Callback{
                                    override fun onFailure(call: Call, e: IOException) {
                                        Log.d("Exception","$e")
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val responsed = response
                                        val responses = responsed.body!!.string()
                                        val status_code = responsed.code
                                        Log.d("payment_success",responses)
                                        dialog.dismiss()
                                        if (status_code != 200) {
                                            if (status_code == 500) {
                                                activity.runOnUiThread {
                                                    kotlin.run {
                                                        Toast.makeText(activity,"Internal Server Error",
                                                            Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            else{
                                                activity.runOnUiThread {
                                                    kotlin.run {
                                                        Toast.makeText(activity,"${JSONObject(responses).optString("message")}",
                                                            Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            dialog.dismiss()
                                            return

                                        }
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
                                            val decryptJson = JSONObject(decrypted_data)
                                            order_detail = decryptJson
                                            activity.runOnUiThread {
                                                kotlin.run {

                                                    if (language != "en"){
                                                        payment_succeeded_dialog.pmsucceededLabel.text = "ការទូទាត់ប្រាក់បានជោគជ័យ"
                                                        payment_succeeded_dialog.orderIdLabel.text = "លេខបញ្ជាទិញ"
                                                        payment_succeeded_dialog.bankRefLabel.text = "លេខកូដយោង"
                                                        payment_succeeded_dialog.pmLabel.text = "ទូទាត់តាម"
                                                        payment_succeeded_dialog.totalLabel.text = "ទឹកប្រាក់"
                                                        payment_succeeded_dialog.continueShoppingBtn.text = "បន្ត"
                                                        payment_succeeded_dialog.pmMethodValue.text = decryptJson.optString("bank_name_kh")
                                                    }
                                                    else{
                                                        payment_succeeded_dialog.pmMethodValue.text = decryptJson.optString("bank_name_en")

                                                    }
                                                    payment_succeeded_dialog.orderIdValue.text = "#"+decryptJson.optString("order_ref")

                                                    payment_succeeded_dialog.bankRefvalue.text = decryptJson.optString("bank_ref")
                                                    payment_succeeded_dialog.totalValue.text = decryptJson.optString("total_amount")+"0 USD"
                                                    dialog.dismiss()

                                                    payment_succeeded_dialog.show()
                                                }
                                            }


                                        }
                                        catch (e:Exception){
                                            Log.d("$e","$e")
                                        }
                                    }

                                })
//                                dialog.dismiss()
//
//                                payment_succeeded_dialog.show()

                            }
                        }
                    })
                }
            }
        socket.connect()
        socket.on(io.socket.client.Socket.EVENT_CONNECT,Emitter.Listener {
            if (socket.id() != null) {
                socketID = socket.id()
            }
        })





    }


    val data = data()



    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        socket = IO.socket(uri)
        socket.disconnect()

    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {


            // for expand to specific height
            behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            //Resources.getSystem().displayMetrics.heightPixels is to get screen height
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.sdk_bottomsheet,container,false)
        view.setBackgroundResource(android.R.color.transparent)
        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState!=null) {
            bankPaymentIsOpened = savedInstanceState.getBoolean("isopened")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isopened",true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (theme_mode == "dark") {
            payment_method_layout.setBackgroundColor((Color.parseColor("#454545")))
            linearlayout.setBackgroundColor((Color.parseColor("#454545")))
            linear.setBackgroundColor((Color.parseColor("#454545")))
            scrollIndicator.setBackgroundColor(Color.parseColor("#ffffff"))
            horizontal_view.setBackgroundColor(Color.parseColor("#ffffff"))
        }
        if (environment == "prod") {
            url = config().sdk_api_url_prod
        }
        else{
            url = config().sdk_api_url_demo
        }
        switchbutton.isEnabled = false
        progressbar.visibility = View.VISIBLE
        progressbar.bringToFront()

        pmMethodLabel.typeface = custom_font

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
                val url: String = "$url/payment/widget-init"
                val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                val request =
                    Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson))
                        .build()


                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        activity.runOnUiThread {
                            kotlin.run {
                                Toast.makeText(activity,e.localizedMessage!!.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responsess = response
                        val responses = responsess.body!!.string()
                        Log.d("responses",responses+responsess.code.toString())
                        val status_code = responsess.code

                        if (status_code != 200) {
                            if (status_code == 500) {
                                activity.runOnUiThread {
                                    kotlin.run {
                                        Toast.makeText(activity,"Internal Server Error",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            else{
                                activity.runOnUiThread {
                                    kotlin.run {
                                        Toast.makeText(activity,"${JSONObject(responses).optString("message")}",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            return
                        }

                        println("response: ${responses}")
                        val jsonObject = JSONObject(responses)

                        // if its value is jsonobject or jsonArray => we set it base on its type

                        val encrypted_data: String =
                            jsonObject.getJSONObject("data").optString("encrypted_data")

                        Log.d(
                            "data",
                            "${jsonObject.getJSONObject("data").optString("encrypted_data")}"
                        )
                        try {
                            secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                            val decrypted_data = data.cbcDecrypt(
                                secretKey,
                                Base64.decode(encrypted_data, Base64.DEFAULT)
                            )

                            val pmJson = JSONObject(decrypted_data.toString())
                            Log.d("decryptedd",decrypted_data.toString())
                            order_details = pmJson.optJSONObject("order_detail")!!.toString()
                            orderID = pmJson.optJSONObject("order_detail")!!.optString("order_id")



                            val pmList = pmJson.optJSONArray("payment_methods")
                            val existingAcc = pmJson.optJSONArray("existing_accounts")
                            val dict_style = pmJson.optJSONObject("style")
                            selected_payment_method_button = dict_style.optJSONObject("selected_payment_method_button")
                            rememberAccLabel = dict_style.optJSONObject("remember_account_label")
                            paymentConfirmBtn = dict_style.optJSONObject("payment_confirm_button")
                            saveAccLabel = dict_style.optJSONObject("saved_account_label")
                            paymentMethodBtn = dict_style.optJSONObject("payment_method_button")
                            favouriteStyle = dict_style.optJSONObject("favorite_bank")
                            activity.runOnUiThread {
                                kotlin.run {
                                    if (language == "en") {
                                        pmMethodLabel.text = pmJson.optJSONObject("style").optJSONObject("widget_frame")
                                            .optString("display_text_en")
                                    }
                                    else{
                                        pmMethodLabel.text = pmJson.optJSONObject("style").optJSONObject("widget_frame")
                                            .optString("display_text_kh")
                                    }
                                    confirmbtnColor = dict_style.optJSONObject("payment_confirm_button").
                                    optString("background_color")

//                                    confirmbtn.setPadding(dict_style.optJSONObject("payment_confirm_button").
//                                    optString("border_size").split("p")[0].toInt())
                                    val builder = RoundButton.newBuilder()
                                    builder.withBackgroundColor(Color.parseColor(dict_style.optJSONObject("payment_confirm_button").
                                    optString("background_color")))
                                        .withTextColor(Color.parseColor(dict_style.optJSONObject("payment_confirm_button").
                                        optString("text_color")))
                                        .withCornerColor(Color.parseColor(dict_style.optJSONObject("payment_confirm_button").
                                        optString("border_color")))
                                        .withCornerWidth(dict_style.optJSONObject("payment_confirm_button").
                                    optString("border_size").split("p")[0].toInt()*2)
                                        .withCornerRadius(dict_style.optJSONObject("payment_confirm_button").
                                        optString("radius").split("p")[0].toInt()*2)
                                    confirmbtn.setCustomizations(builder)


                                    confirmbtn.setTextSize(20F)
                                    if (language == "en"){
                                        confirmbtn.text = dict_style.optJSONObject("payment_confirm_button").optString("display_text_en")
                                    }
                                    else{
                                        confirmbtn.text = dict_style.optJSONObject("payment_confirm_button").optString("display_text_kh")
                                    }
                                    if (theme_mode == "dark") {
                                        saved_acc_label.setTextColor(Color.parseColor("#ffffff"))

                                    }
                                    else{
                                        saved_acc_label.setTextColor(Color.parseColor(dict_style.optJSONObject("remember_account_label").
                                        optString("text_color")))

                                    }
                                    saved_acc_label.typeface = custom_font
                                    if (language == "en"){

                                        saved_acc_label.text = pmJson.optJSONObject("style").optJSONObject("remember_account_label")
                                            .optString("display_text_en")
                                    }
                                    else{
                                        saved_acc_label.text = pmJson.optJSONObject("style").optJSONObject("remember_account_label")
                                            .optString("display_text_kh")
                                    }
                                }
                            }



                            for (item in 0..pmList!!.length() - 1) {
                                val json = JSONObject(pmList[item].toString())
                                bankCodeList.add(json.optString("bank_code"))
                                bankIdList.add(json.optString("bank_id"))
                                supportTokenize.add(json.optString("support_tokenize"))
                                support_deeplink.add(json.optString("support_deeplink"))
                                in_app.add(json.optString("support_inapp"))
                                isFavouriteList.add(json.optString("favorite"))
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
                                var fee = json.optDouble("fee").toString()  + "0 USD"
                                if (json.optString("bank_id") == "pay_later") {
                                    fee = ""
                                }
                                itemModel.add(
                                    itemModel(
                                        json.optString("bank_logo"),
                                        bank_name,
                                        fee
                                    )
                                )
                            }
                            val adapter = banksAdapter(
                                supportFragmentManager,
                                paylater,
                                itemModel,
                                activity,
                                this@paymentSdk,
                                payment_succeeded = payment_succeeded
                                ,order_details,
                                language = language){
                                function(it)
                            }

                                socket()

                            val linearmanager: LinearLayoutManager =
                                LinearLayoutManager(context)

                            linearmanager.orientation = RecyclerView.VERTICAL

                            activity.runOnUiThread {
                                kotlin.run {
                                    bankRecycler.layoutManager = linearmanager
                                    bankRecycler.adapter = adapter
                                    progressbar.visibility = View.GONE
                                    //hasfixedsize=true otherwise when we go to bankpaymentcontroller
                                    //and back to bottomsheetcontroller we cannot click on recyclerview item
                                    bankRecycler.setHasFixedSize(true)

                                }
                            }
                            if (existingAcc.length() > 0) {
                                savedAccLabelColor = pmJson.optJSONObject("style")!!
                                    .optJSONObject("saved_account_label")!!
                                    .optString("text_color")
                                activity.runOnUiThread {
                                    kotlin.run {
                                        if (theme_mode == "dark") {
                                            pmMethodLabel.setTextColor(Color.parseColor("#ffffff"))
                                        }
                                        else{
                                            pmMethodLabel.setTextColor(Color.parseColor(pmJson.optJSONObject("style")!!
                                                .optJSONObject("widget_frame")!!
                                                .optString("text_color")))

                                        }


                                    }
                                }

                                if (language == "en") {
                                    itemModel.add(
                                        itemModel("", pmJson.optJSONObject("style")!!
                                        .optJSONObject("saved_account_label")!!
                                        .optString("display_text_en"), "")
                                    )
                                }
                                else{
                                    itemModel.add(
                                        itemModel("", pmJson.optJSONObject("style")!!
                                        .optJSONObject("saved_account_label")!!
                                        .optString("display_text_kh"), "")
                                    )
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
                                    bankCodeList.add(json.optString("bank_code")+"$$$"+json.optString("tokenize_id"))
                                    bankIdList.add(json.optString("bank_id")+"$$$"+json.optString("tokenize_id"))

                                }
                                val adapter = banksAdapter(
                                    supportFragmentManager,
                                    paylater,
                                    itemModel,
                                    activity,
                                    this@paymentSdk,
                                    payment_succeeded = payment_succeeded,
                                    order_details,
                                    language = language
                                ){
                                    function(it)
                                }

                                val linearmanager: LinearLayoutManager =
                                    LinearLayoutManager(context)
                                linearmanager.orientation = RecyclerView.VERTICAL
                                activity.runOnUiThread {
                                    kotlin.run {
                                        bankRecycler.layoutManager = linearmanager
                                        bankRecycler.adapter = adapter
                                        progressbar.visibility = View.GONE
                                        bankRecycler.setHasFixedSize(true)

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
