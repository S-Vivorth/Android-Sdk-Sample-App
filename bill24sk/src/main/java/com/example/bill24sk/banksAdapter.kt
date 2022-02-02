package com.example.bill24sk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import android.util.*
import androidx.core.content.res.ResourcesCompat
import javax.crypto.SecretKey
import kotlin.collections.ArrayList
import android.graphics.drawable.GradientDrawable
import android.util.Base64

import kotlinx.android.synthetic.main.sdk_bank_cell.view.*
import kotlinx.android.synthetic.main.sdk_bottomsheet.*
import kotlinx.android.synthetic.main.sdk_save_acc_cell.view.*

import java.util.*
import java.util.concurrent.TimeUnit

class banksAdapter (supportFragmentManager: FragmentManager,paylater:Activity?,
                   itemModelList: ArrayList<itemModel>,activity:Activity,bottomSheetController: bottomSheetController,
                   payment_succeeded:Activity,order_details:String,language:String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = activity
    lateinit var fragmentManager:FragmentManager
    var paylater: Activity?
    lateinit var myItemModel: ArrayList<itemModel>
    var selectedPosition = -1
    var count = 0
    var bottomSheetController = bottomSheetController
    lateinit var secretKey: SecretKey
    lateinit var payment_succeeded:Activity
    lateinit var activity: Activity
    var order_details:String
    var isopened:Boolean = false
    lateinit var url:String
    var language:String
    var custom_font = ResourcesCompat.getFont(activity,R.font.kh9)
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        init {

        }

    }
    class ViewHolder1(view: View): RecyclerView.ViewHolder(view) {

        init {

        }

    }

init {
    fragmentManager = supportFragmentManager
    this.paylater = paylater
    myItemModel = itemModelList
    this.payment_succeeded = payment_succeeded
    this.activity = activity
    this.order_details = order_details
    this.language = language
}

    companion object {
        const val ABA_SCHEME = "abamobilebank"
        const val ABA_DOMAIN = "ababank.com"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        val view = LayoutInflater.from(activity).inflate(R.layout.sdk_bank_cell,parent,false)
        val view1 = LayoutInflater.from(activity).inflate(R.layout.sdk_save_acc_cell,parent,false)
        if (viewType == 0){
            return ViewHolder(view)
        }
        else{
            return ViewHolder1(view1)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if (myItemModel[position].imageString == "") {
            return 1
        }
        else{
            return 0
        }
    }






    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bottomSheetController.progressbar.visibility = View.GONE
        url = bottomSheetController.url
        if (myItemModel[position].imageString == ""){
            val viewholder1:ViewHolder1 = holder as ViewHolder1
                viewholder1.itemView.savedAccText.text = myItemModel[position].bankName
            viewholder1.itemView.savedAccText.typeface = custom_font
            viewholder1.itemView.savedAccText.setTextColor(Color.parseColor(bottomSheetController.savedAccLabelColor))


        }
        else{
            val viewholder:ViewHolder = holder as ViewHolder
                val current_item_position: itemModel = myItemModel!!.get(position)
                viewholder.itemView.bankName.text = current_item_position.bankName
            viewholder.itemView.bankName.setTextColor(Color.parseColor(bottomSheetController.paymentMethodBtn.optString("text_color")))
            viewholder.itemView.fee.setTextColor(Color.parseColor(bottomSheetController.paymentMethodBtn.optString("text_color")))
                if (bottomSheetController.paymentMethodBtn.optBoolean("convenience_fee_visible") == false && (position < bottomSheetController.supportTokenize.size-1)) {
                    viewholder.itemView.fee.visibility = View.GONE
                }
                else{
                    viewholder.itemView.fee.visibility = View.VISIBLE

                }

                val shape = GradientDrawable()
                shape.cornerRadius = 0F
            viewholder.itemView.card.radius = bottomSheetController.paymentMethodBtn.optString("icon_radius").split("p")[0].toFloat()
            val padding = bottomSheetController.paymentMethodBtn.optString("icon_border_size").split("p")[0].toInt()*3
            viewholder.itemView.bankImage.setPadding(padding,padding,padding,padding)
            viewholder.itemView.bankImage.setBackgroundColor(Color.parseColor(bottomSheetController.paymentMethodBtn.optString("icon_border_color")))

            viewholder.itemView.bankImage.adjustViewBounds = true
            viewholder.itemView.bankName.setTypeface(ResourcesCompat.getFont(activity,R.font.kh9))

                        viewholder.itemView.fee.text = current_item_position.fee
                        viewholder.itemView.fee.typeface = custom_font



                viewholder.itemView.setOnClickListener {

                    bottomSheetController.confirmbtn.isEnabled = true
                    bottomSheetController.confirmbtn.alpha = 1F
                    if (position< bottomSheetController.supportTokenize.size){
                        if(bottomSheetController.supportTokenize[position] == "true"){
                            bottomSheetController.switchbutton.isEnabled = true
                        }
                        else{
                            bottomSheetController.switchbutton.isChecked = false
                            bottomSheetController.switchbutton.isEnabled = false

                        }
                    }
                    else{
                        bottomSheetController.switchbutton.isChecked = false
                        bottomSheetController.switchbutton.isEnabled = false
                    }

                    if(selectedPosition>=0){
                        notifyItemChanged(selectedPosition)
                    }
                    selectedPosition = position

                    notifyItemChanged(selectedPosition)

                }
                context.runOnUiThread {
                    kotlin.run {
                        context.let { Glide.with(it).load(current_item_position.imageString).centerCrop().into(viewholder.itemView.bankImage) }
                    }
                }



                if (selectedPosition == position) {

                    viewholder.itemView.bankName.setTextColor(Color.parseColor(bottomSheetController.selected_payment_method_button
                        .optString("text_color")))
                    viewholder.itemView.fee.setTextColor(Color.parseColor(bottomSheetController.selected_payment_method_button
                        .optString("text_color")))
                    viewholder.itemView.setBackgroundColor(Color.parseColor(bottomSheetController.selected_payment_method_button
                        .optString("background_color")))
                    viewholder.itemView.radioBox.setColorFilter(Color.parseColor(bottomSheetController.selected_payment_method_button
                        .optString("border_color")))
                    viewholder.itemView.radioBox.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.tick))
                }
                else {

                    viewholder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                    viewholder.itemView.radioBox.setColorFilter(Color.parseColor("#50000000"))
                    viewholder.itemView.radioBox.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_baseline_radio_button_unchecked_24))
                }
            }




        bottomSheetController.confirmbtn.setOnClickListener {
            isopened = true

            bottomSheetController.hidebutton.performClick()
            bottomSheetController.progressbar.visibility = View.VISIBLE
            activity.runOnUiThread {
                kotlin.run {
                    val data = data()
                    val supportTokenize = bottomSheetController.supportTokenize
                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                    var lenghtOfAvailBanks:Int = 0
                    if (bottomSheetController.bankIdList.contains("pay_later")){
                        lenghtOfAvailBanks = supportTokenize.size - 1
                    }
                    else{
                        lenghtOfAvailBanks = supportTokenize.size
                    }
                    if (selectedPosition < lenghtOfAvailBanks){
                        if (bottomSheetController.support_deeplink[selectedPosition] == "true"){
                            val client: OkHttpClient = OkHttpClient()

                            var json:String
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                json = """
            {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(bottomSheetController.secretKey,
                                    """
                {"session_id": "${bottomSheetController.sessionId}","client_id": "${bottomSheetController.clientID}","bank_id": "${bottomSheetController.bankIdList[selectedPosition]}","remember_acc": ${bottomSheetController.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()))}"}
        """.trimIndent()
                            }
                            else{
                                json = """
            {"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(bottomSheetController.secretKey,
                                    """
                {"session_id": "${bottomSheetController.sessionId}","client_id": "${bottomSheetController.clientID}","bank_id": "${bottomSheetController.bankIdList[selectedPosition]}","remember_acc": ${bottomSheetController.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()),Base64.DEFAULT)}"}
        """.trimIndent()
                            }
                            json = json.replace("\n","").replace("\r","")
                            var answer = JSONObject(json)

                            Log.d("answerr",answer.toString())

                            val url:String = "$url/payment/init"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                            val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
                            client.newCall(request).enqueue(object : Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("exc","$e")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses = response.body!!.string()

                                    val jsonObject = JSONObject(responses)
                                    if (jsonObject.optJSONObject("result").optString("result_code") != "000"){
                                        activity.runOnUiThread {
                                            if (language == "en"){
                                                Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_en"),
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                            else{
                                                Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_kh"),
                                                    Toast.LENGTH_SHORT).show()
                                            }

                                            bottomSheetController.progressbar.visibility = View.GONE

                                        }

                                        return
                                    }
                                    val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                                    Log.d("dataa","${jsonObject.getJSONObject("data").optString("encrypted_data")}")
                                    try {
                                        secretKey = data.makePbeKey("sdkdev".toCharArray())!!

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
                                        val checkout_data = decryptJson.optString("checkout_data")
                                        val decryptedCheckoutData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            data.cbcDecrypt(secretKey,
                                                java.util.Base64.getDecoder().decode(checkout_data))
                                        } else {
                                            Base64.decode(checkout_data,Base64.DEFAULT)
                                        }
                                        Log.d("Decryptedddd",decryptedCheckoutData.toString())
                                        val deeplinkJson = JSONObject(decryptedCheckoutData.toString())
                                        val deeplink = deeplinkJson.optString("deeplink_data")
                                        try {
                                            val url = deeplink
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            activity.startActivity(intent)
                                        } catch (ex: Exception) {
                                            val intent: Intent = Intent(Intent.ACTION_VIEW).apply {
                                                activity.intent.data = Uri.parse("market://details?id=com.paygo24.ibank")
                                            }
                                            activity.startActivity(intent)
                                        }
                                        activity.finish()
                                    }

                                    catch (e:Exception){

                                    }
                                }

                            })


                        }
                        else{
                            val client: OkHttpClient = OkHttpClient()

                            var json:String
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                json = """
            {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(bottomSheetController.secretKey,
                                    """
                {"session_id": "${bottomSheetController.sessionId}","client_id": "${bottomSheetController.clientID}","bank_id": "${bottomSheetController.bankIdList[selectedPosition]}","remember_acc": ${bottomSheetController.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()))}"}
        """.trimIndent()
                            }
                            else{
                                json = """
{"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(bottomSheetController.secretKey, """
{"session_id": "${bottomSheetController.sessionId}","client_id": "${bottomSheetController.clientID}","bank_id": "${bottomSheetController.bankIdList[selectedPosition]}","remember_acc": ${bottomSheetController.switchbutton.isChecked},"user_agent": "web-mobile"}
""".trimIndent()),Base64.DEFAULT)}"}""".trimIndent()
                            }

                            json = json.replace("\n","").replace("\r","")

                            var answer = JSONObject(json.replace("\n","").replace("\r",""))


                            val url:String = "$url/payment/init"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                            val requestBody = RequestBody.create(mediaTypeJson,json)
                            val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()

                            client.newCall(request).enqueue(object: Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("Exception","${e}")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses = response.body!!.string()
                                    val jsonObject = JSONObject(responses)
                                    if (jsonObject.optJSONObject("result").optString("result_code") != "000"){
                                        activity.runOnUiThread {
                                            kotlin.run {
                                                if (language == "en"){
                                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_en"),
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                                else{
                                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_kh"),
                                                        Toast.LENGTH_SHORT).show()
                                                }

                                                bottomSheetController.progressbar.visibility = View.GONE

                                            }

                                        }
                                        return


                                    }
                                    val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                                    Log.d("dataa","${jsonObject.getJSONObject("data").optString("encrypted_data")}")
                                    try {
                                        secretKey = data.makePbeKey("sdkdev".toCharArray())!!


                                        val decrypted_data = data.cbcDecrypt(
                                                secretKey,
                                                Base64.decode(encrypted_data,Base64.DEFAULT)
                                            )
                                        Log.d("Decryptedd",decrypted_data.toString())
                                        val decryptJson = JSONObject(decrypted_data)
                                        val checkout_data = decryptJson.optString("checkout_data")

                                        val  decryptedCheckoutData = data.cbcDecrypt(
                                            secretKey,
                                            Base64.decode(checkout_data,Base64.DEFAULT)
                                        ).toString().replace("\n","").replace("\r","")


                                        Log.d("Decryptedddd",decryptedCheckoutData.toString())


                                        val bankPaymentController = bankPaymentController(
                                            formdata = decryptedCheckoutData,payment_succeeded = payment_succeeded,
                                            orderID = bottomSheetController.orderID,activity = activity,bottomSheetController = bottomSheetController
                                        ,socketID = bottomSheetController.socketID)

                                        bankPaymentController.show(fragmentManager,"Bank Payment")
                                        bottomSheetController.socket.disconnect()
//                                        fm.addToBackStack("sdk_bottomsheet")
//                                        fm.commit()
                                        bottomSheetController.progressbar.visibility = View.GONE
                                    }
                                    catch (error:Exception){
                                        print(error)
                                    }
                                }

                            })
                        }

                    }
                    else if (selectedPosition == lenghtOfAvailBanks){
                        val intent = Intent(context, paylater!!::class.java)
                        intent.putExtra("order_details",order_details)
                        bottomSheetController.dialog!!.dismiss()
                        context.startActivity(intent)
                    }
                    else{

                        val client:OkHttpClient = OkHttpClient()
                        var json:String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d("tokenizeid",(bottomSheetController.bankIdList[selectedPosition-1].split("$$$"))[1])
                            json = """
                                {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(secretKey,"""
                                    {"session_id": "${bottomSheetController.sessionId}",
                                    "bank_id": "${(bottomSheetController.bankIdList[selectedPosition-1].split("$$$"))[0]}",
                                    "client_id": "${bottomSheetController.clientID}",
                                    "tokenize_id":"${(bottomSheetController.bankIdList[selectedPosition-1].split("$$$"))[1]}"}
                                """.trimIndent()))}"}
                            """.trimIndent()
                        }
                        else{
                            json = """
                                {"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(secretKey,"""
                                    {"session_id": "${bottomSheetController.sessionId}",
                                    "bank_id": "${bottomSheetController.bankIdList[selectedPosition-1].split("$$$")[0]}",
                                    "client_id": "${bottomSheetController.clientID}",
                                    "tokenize_id":"${bottomSheetController.bankIdList[selectedPosition-1].split("$$$")[1]}"}
                                """.trimIndent()),Base64.DEFAULT)}"}
                            """.trimIndent()
                        }
                        var answer = JSONObject(json.replace("\n","").replace("\r",""))
                        val url:String = "$url/tokenize/validate"
                        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                        val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("Failed",e.toString())
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responses = response.body!!.string()
                                val jsonObject = JSONObject(responses)
                                val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                                try{
                                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!

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
                                    val json_object = JSONObject(decrypted_data)

                                    val validatetoken:String = json_object.optString("validate_token")


                                    activity.runOnUiThread {
                                        kotlin.run {
                                            if (jsonObject.optJSONObject("result").optString("result_code") != "000"){
                                                if (bottomSheetController.language == "en") {
                                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_en")
                                                    ,Toast.LENGTH_SHORT).show()
                                                    bottomSheetController.progressbar.visibility = View.GONE
                                                    return@runOnUiThread
                                                }
                                                else{
                                                    Toast.makeText(activity,jsonObject.optJSONObject("result").optString("result_message_kh")
                                                        ,Toast.LENGTH_SHORT).show()
                                                    bottomSheetController.progressbar.visibility = View.GONE

                                                    return@runOnUiThread

                                                }
                                            }
                                            val otp = otp(myItemModel[selectedPosition].imageString, myItemModel[selectedPosition].bankName,myItemModel[selectedPosition].fee,
                                                activity,validatetoken,bottomSheetController.bankIdList[selectedPosition-1].split("$$$")[0]
                                                ,bottomSheetController,payment_succeeded,language,bottomSheetController.paymentConfirmBtn,bottomSheetController.sessionId,
                                                bottomSheetController.clientID,orderID = bottomSheetController.orderID,bottomSheetController.socketID)
                                            otp.show(fragmentManager,"otp")
                                            bottomSheetController.socket.disconnect()
                                            Log.d("decryptedd", decrypted_data)
                                            bottomSheetController.progressbar.visibility = View.GONE
                                        }
                                    }

                                }
                                catch (e: Exception){
                                    Log.d("Exception",e.toString())
                                }
                            }
                        })
                    }
                }
            }


        }
    }


    override fun getItemCount(): Int {
        return myItemModel!!.size
    }
}