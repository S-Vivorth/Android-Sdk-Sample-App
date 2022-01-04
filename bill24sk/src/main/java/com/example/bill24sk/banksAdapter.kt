package com.example.bill24sk

import android.app.Activity
import android.app.Dialog
import android.app.job.JobInfo
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bank_cell.view.*

import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.bottomsheet.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import android.util.*
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.save_acc_cell.view.*
import okhttp3.internal.http.promisesBody
import javax.crypto.SecretKey
import kotlin.collections.ArrayList
import android.graphics.Typeface





class banksAdapter(supportFragmentManager: FragmentManager,paylater:Activity,
                   itemModelList: ArrayList<itemModel>,activity:Activity,bottomSheetController: bottomSheetController,
                   payment_succeeded:Activity,order_details:String,language:String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = activity
    lateinit var fragmentManager:FragmentManager
    lateinit var paylater: Activity
    lateinit var myItemModel: ArrayList<itemModel>
    var selectedPosition = -1
    var count = 0
    var bottomSheetController = bottomSheetController
    lateinit var secretKey: SecretKey
    lateinit var payment_succeeded:Activity
    lateinit var activity: Activity
    var order_details:String
    val staging = "http://192.168.197.6:60096"
    val url = "https://sdkapi-demo.bill24.net"
    var language:String
    var bankPaymentIsOpened = false
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

        bottomSheetController.confirmbtn.setOnClickListener {
            Log.d("Hellooo","asda")
        }
        val view = LayoutInflater.from(activity).inflate(R.layout.bank_cell,parent,false)
        val view1 = LayoutInflater.from(activity).inflate(R.layout.save_acc_cell,parent,false)
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
            viewholder.itemView.bankName.setTypeface(ResourcesCompat.getFont(activity,R.font.kh9))

            if (position == bottomSheetController.supportTokenize.size-1){
                viewholder.itemView.fee.text = ""
            }
            else{
                viewholder.itemView.fee.text = current_item_position.fee
                viewholder.itemView.fee.typeface = custom_font
            }
//        if (current_item_position.bankName == "Saved Accounts"){
//            holder.itemView.bankName.text = current_item_position.bankName
//            holder.itemView.fee.text = current_item_position.fee
//            holder.itemView.bankName.textSize = 25F
//            Log.d("Tagg",count.toString())
//
//            holder.itemView.bankName.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
//            holder.itemView.bankName.gravity = Gravity.CENTER
//            holder.itemView.bankImage.layoutParams = LinearLayout.LayoutParams(0,0)
//            holder.itemView.radioBox.layoutParams = RelativeLayout.LayoutParams(0,0)
//        }

            viewholder.itemView.setOnClickListener {
                bottomSheetController.confirmbtn.isEnabled = true
                bottomSheetController.confirmbtn.alpha = 1F
                bottomSheetController.print()
                if (position< bottomSheetController.supportTokenize.size){
                    if(bottomSheetController.supportTokenize[position] == "true"){
                        Log.d("tokenizeee","true")
                        bottomSheetController.switchbutton.isEnabled = true
                    }
                    else{
                        Log.d("tokenizee","false")
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
                selectedPosition = viewholder.getAdapterPosition()

                notifyItemChanged(selectedPosition)

            }
            context.runOnUiThread {
                kotlin.run {
                    context.let { Glide.with(it).load(current_item_position.imageString).centerCrop().into(viewholder.itemView.bankImage) }
                }
            }

            viewholder.itemView.bankName.setTextColor(Color.parseColor(bottomSheetController.selected_payment_method_button
                .optString("text_color")))
            viewholder.itemView.fee.setTextColor(Color.parseColor(bottomSheetController.selected_payment_method_button
                .optString("text_color")))
            if (selectedPosition == position) {
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
            bottomSheetController.bankPaymentIsOpened = true

            bottomSheetController.progressbar.visibility = View.VISIBLE

            AsyncTask.execute {
                kotlin.run {
                    val data = data()
                    val supportTokenize = bottomSheetController.supportTokenize
                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!

                    if (selectedPosition < supportTokenize.size-1){
                        if (bottomSheetController.support_deeplink[selectedPosition] == "1"){
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
                                    println("responded: ${responses}")

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
                                        }
                                    catch (e:Exception){

                                    }
                                }

                            })

//                            try {
//                                val url = "${ABA_SCHEME}://${ABA_DOMAIN}?type=payway&qrcode=${qrString}"
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                                activity.startActivity(intent)
//                            } catch (ex: Exception) {
//                                val intent: Intent = Intent(Intent.ACTION_VIEW).apply {
//                                    activity.intent.data = Uri.parse("market://details?id=com.paygo24.ibank")
//                                }
//                                activity.startActivity(intent)
//                            }
//                            activity.finish()
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
            {"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(bottomSheetController.secretKey,
                                    """
                {"session_id": "${bottomSheetController.sessionId}","client_id": "${bottomSheetController.clientID}","bank_id": "${bottomSheetController.bankIdList[selectedPosition]}","remember_acc": ${bottomSheetController.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()),Base64.DEFAULT)}"}
        """.trimIndent()
                            }

                            Log.d("jsonn",json)
                            var answer = JSONObject(json)

                            Log.d("answerr",answer.toString())

                            val url:String = "$url/payment/init"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                            val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
                            client.newCall(request).enqueue(object: Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("Failedd","${e}")
                                    println("Faileddd: ${e}")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses = response.body!!.string()
                                    println("responded: ${responses}")
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
                                        Log.d("Decrypteddd",decryptedCheckoutData.toString())
                                        val bankPaymentController = bankPaymentController(fragmentManager = fragmentManager,paylater,
                                            formdata = decryptedCheckoutData.toString(),payment_succeeded = payment_succeeded,
                                            orderID = bottomSheetController.orderID,activity = activity,bottomSheetController)
                                        bankPaymentController.show(fragmentManager,"Bank Payment")

                                        bottomSheetController.progressbar.visibility = View.GONE
                                    }
                                    catch (error:Exception){
                                        print(error)
                                    }
                                }

                            })
                        }

                    }
                    else if (selectedPosition == supportTokenize.size-1){
                        Log.d("laterr","selected")
                        val intent = Intent(context, paylater::class.java)
                        intent.putExtra("order_details",order_details)
                        context.startActivity(intent)
                    }
                    else{

                        val client:OkHttpClient = OkHttpClient()
                        var json:String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                                    "bank_id": "${bottomSheetController.bankIdList[selectedPosition].split("$$$")[0]}",
                                    "client_id": "${bottomSheetController.clientID}",
                                    "tokenize_id":"${bottomSheetController.bankIdList[selectedPosition].split("$$$")[1]}"}
                                """.trimIndent()),Base64.DEFAULT)}"}
                            """.trimIndent()
                        }
                        var answer = JSONObject(json)
                        val url:String = "$url/tokenize/validate"
                        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                        val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("Failedd",e.toString())
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
                                    val jsonObject = JSONObject(decrypted_data)
                                    val validate_token = jsonObject.optString("validate_token")
                                    val otp = otp(paylater = paylater,bankImage = myItemModel[selectedPosition].imageString,
                                        myItemModel[selectedPosition].bankName,myItemModel[selectedPosition].fee,
                                        activity,validate_token,bankID = bottomSheetController.bankIdList[selectedPosition].split("$$$")[0]
                                        ,bottomSheetController,payment_succeeded,language,bottomSheetController.confirmbtnColor,sessionId = bottomSheetController.sessionId,
                                    clientId = bottomSheetController.clientID)
                                    otp.show(fragmentManager,"OTP")
                                    Log.d("Decryptedd",decrypted_data.toString())
                                    bottomSheetController.progressbar.visibility = View.GONE
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