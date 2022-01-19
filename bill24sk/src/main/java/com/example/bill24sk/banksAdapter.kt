package com.example.bill24sk

import android.app.Activity
import android.app.Dialog
import android.app.job.JobInfo
import android.content.Intent
import android.content.res.Resources
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
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import okhttp3.internal.http.promisesBody
import javax.crypto.SecretKey
import kotlin.collections.ArrayList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.opengl.Visibility
import android.util.Base64
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle

import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.bank_cell.view.*
import kotlinx.android.synthetic.main.bottomsheet.*
import kotlinx.android.synthetic.main.save_acc_cell.view.*

import java.net.URI
import java.util.*
import java.io.Serializable

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
    var isopened:Boolean = false
    val url = "http://203.217.169.102:60096"
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
            viewholder.itemView.bankImage.setPadding(bottomSheetController.paymentMethodBtn.optString("icon_border_size").split("p")[0].toInt()*3)
            viewholder.itemView.bankImage.setBackgroundColor(Color.parseColor(bottomSheetController.paymentMethodBtn.optString("icon_border_color")))

            viewholder.itemView.bankImage.adjustViewBounds = true
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
            Log.d("isopenn",isopened.toString())

            bottomSheetController.hidebutton.performClick()
            Log.d("bankPaymentIsOpened",bottomSheetController.bankPaymentIsOpened.toString())
            bottomSheetController.progressbar.visibility = View.VISIBLE
            activity.runOnUiThread {
                kotlin.run {
                    val data = data()
                    val supportTokenize = bottomSheetController.supportTokenize
                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                    Log.d("deeplinkk",bottomSheetController.support_deeplink.toString())
                    if (selectedPosition < supportTokenize.size-1){
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
                            Log.d("jsonn before replace",json)

                            json = json.replace("\n","").replace("\r","")
                            Log.d("after replaced",json)

                            var answer = JSONObject(json.replace("\n","").replace("\r",""))

                            Log.d("answerr",answer.toString())

                            val url:String = "$url/payment/init"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                            val requestBody = RequestBody.create(mediaTypeJson,json)
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

//                                        val decryptedCheckoutData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                            data.cbcDecrypt(secretKey,
//                                                java.util.Base64.getDecoder().decode(checkout_data))
//                                        } else {
//                                            Base64.decode(checkout_data,Base64.DEFAULT)
//                                        }
                                        Log.d("Decryptedddd",decryptedCheckoutData.toString())
//                                        try {
//                                            var intent = Intent(activity,bankController::class.java)
//                                            var arrayList = ArrayList<Any>()
//
//                                        arrayList.add(decryptedCheckoutData.toString())
//                                        arrayList.add(payment_succeeded.toString())
//                                        arrayList.add(bottomSheetController.orderID.toString())
//                                        arrayList.add(bottomSheetController.socketID)
////                                            val a = Class.forName("dasd").asSubclass(Activity::class.java)
//                                            intent.putExtra("data",arrayList)
////                                            intent.putExtra("data","com.example.myapplication.show")
//                                            activity.startActivity(intent)
//
//                                        }
//                                        catch (ex:Exception) {
//                                            Log.d("exceptionnn","$ex")
//                                        }

                                        val bankPaymentController = bankPaymentController(
                                            formdata = decryptedCheckoutData,payment_succeeded = payment_succeeded,
                                            orderID = bottomSheetController.orderID,activity = activity,bottomSheetController = bottomSheetController
                                        ,socketID = bottomSheetController.socketID)

                                        bankPaymentController.show(fragmentManager,"Bank Payment")
                                        bottomSheetController.socket.disconnect()
//                                        fm.addToBackStack("bottomsheet")
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
                    else if (selectedPosition == supportTokenize.size-1){
                        Log.d("laterr","selected")
                        val intent = Intent(context, paylater::class.java)
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
                                    val json_object = JSONObject(decrypted_data)
                                    val validatetoken:String = json_object.optString("validate_token")
                                    Log.d("json_ob", jsonObject.toString())
                                    Log.d("token",validatetoken)
                                    Log.d("selected position",selectedPosition.toString())
                                    Log.d("items",myItemModel!!.size.toString())
                                    activity.runOnUiThread {
                                        kotlin.run {

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