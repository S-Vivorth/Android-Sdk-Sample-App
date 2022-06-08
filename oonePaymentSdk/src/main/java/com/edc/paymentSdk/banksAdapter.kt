package com.oone.paymentSdk

import amk.sdk.deeplink.AMKDeeplink
import amk.sdk.deeplink.entity.model.AMKDLMerchant
import amk.sdk.deeplink.entity.model.SourceInfo
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.annotation.RequiresApi
import com.oone.paymentSdk.R

import kotlinx.android.synthetic.main.sdk_bank_cell.view.*
import kotlinx.android.synthetic.main.sdk_bottomsheet.*
import kotlinx.android.synthetic.main.sdk_bottomsheet.view.*
import kotlinx.android.synthetic.main.sdk_save_acc_cell.view.*

import java.util.*

class banksAdapter(
    supportFragmentManager: FragmentManager,
    paylater: Activity?,
    itemModelList: ArrayList<itemModel>,
    activity: Activity,
    paymentSdk: paymentSdk,
    payment_succeeded: Activity,
    order_details: String,
    language: String,
    function: (callback: Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = activity
    lateinit var fragmentManager: FragmentManager
    var paylater: Activity?
    lateinit var myItemModel: ArrayList<itemModel>
    var selectedPosition = -1
    var paymentSdk = paymentSdk
    lateinit var secretKey: SecretKey
    lateinit var payment_succeeded: Activity
    lateinit var activity: Activity
    var order_details: String
    var isopened: Boolean = false
    lateinit var url: String
    var language: String
    var custom_font = ResourcesCompat.getFont(activity, R.font.kh9)
    var user_agent: String = "android"
    lateinit var function: (callback: Any) -> Unit

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {

        }

    }

    class ViewHolder1(view: View) : RecyclerView.ViewHolder(view) {

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
        this.function = function
    }

    companion object {
        const val ABA_SCHEME = "abamobilebank"
        const val ABA_DOMAIN = "ababank.com"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        val view = LayoutInflater.from(activity).inflate(R.layout.sdk_bank_cell, parent, false)
        val view1 = LayoutInflater.from(activity).inflate(R.layout.sdk_save_acc_cell, parent, false)
        if (viewType == 0) {
            return ViewHolder(view)
        } else {
            return ViewHolder1(view1)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if (myItemModel[position].imageString == "") {
            return 1
        } else {
            return 0
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        paymentSdk.progressbar.visibility = View.GONE
        Log.d("deeppp", paymentSdk.support_deeplink.toString())
        Log.d("favourite", paymentSdk.isFavouriteList.toString())
        url = paymentSdk.url
        if (myItemModel[position].imageString == "") {
            val viewholder1: ViewHolder1 = holder as ViewHolder1
            viewholder1.itemView.savedAccText.text = myItemModel[position].bankName
            viewholder1.itemView.savedAccText.typeface = custom_font
            if (paymentSdk.theme_mode == "dark") {
                viewholder1.itemView.savedAccText.setTextColor(Color.parseColor("#ffffff"))
                viewholder1.itemView.save_acc_layout.setBackgroundColor(Color.parseColor("#454545"))

            } else {
                viewholder1.itemView.savedAccText.setTextColor(Color.parseColor(paymentSdk.savedAccLabelColor))

            }


        } else {
            val viewholder: ViewHolder = holder as ViewHolder

            val current_item_position: itemModel = myItemModel!!.get(position)

            viewholder.itemView.bankName.text = current_item_position.bankName
            viewholder.itemView.bankName.setTextColor(
                Color.parseColor(
                    paymentSdk.paymentMethodBtn.optString(
                        "text_color"
                    )
                )
            )
            viewholder.itemView.fee.setTextColor(
                Color.parseColor(
                    paymentSdk.paymentMethodBtn.optString(
                        "text_color"
                    )
                )
            )
            if (paymentSdk.paymentMethodBtn.optBoolean("convenience_fee_visible") == false && (position < paymentSdk.supportTokenize.size) && (paymentSdk.bankCodeList[position] != "pay_later")) {
                viewholder.itemView.fee.visibility = View.GONE
            } else {
                viewholder.itemView.fee.visibility = View.VISIBLE
            }
            if (position < paymentSdk.supportTokenize.size) {
                viewholder.itemView.favourite.visibility = View.VISIBLE
                var isFavourite: Boolean

                if (paymentSdk.isFavouriteList[position] == "true") {
                    isFavourite = true
                    viewholder.itemView.favourite.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.favourite_active
                        )
                    )
                    viewholder.itemView.favourite.setColorFilter(
                        Color.parseColor(
                            paymentSdk.favouriteStyle.optString(
                                "selected_color"
                            )
                        )
                    )

                } else {
                    isFavourite = false
                    viewholder.itemView.favourite.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.favourite
                        )
                    )
                    viewholder.itemView.favourite.setColorFilter(
                        Color.parseColor(
                            paymentSdk.favouriteStyle.optString(
                                "color"
                            )
                        )
                    )
                }


                viewholder.itemView.favourite.setOnClickListener {
                    activity.runOnUiThread {
                        kotlin.run {
                            val data_tobe_encrypted = """
                                {"session_id": "${paymentSdk.sessionId}",
                                "bank_id" : "${paymentSdk.bankIdList[position]}",
                                "client_id": "${paymentSdk.clientID}",
                                "favorite": ${!isFavourite}}
                            """.trimIndent()
                            val payload = """
                                {"encrypted_data" : "${encryption(data_tobe_encrypted)}"}
                            """.trimIndent()
                            val client = OkHttpClient()
                            var answer = JSONObject(payload)


                            val url: String = "$url/bank/favorite"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                            val request = Request.Builder().url(url)
                                .post(answer.toString().toRequestBody(mediaTypeJson)).build()
                            client.newCall(request).enqueue(object : Callback{
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d(call.toString(), e.toString())
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses_data = response
                                    val responses = responses_data.body!!.string()
                                    if (responses_data.code != 200) {
                                        context.runOnUiThread {
                                            kotlin.run {
                                                Toast.makeText(
                                                    context,
                                                    responses,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                        return
                                    }
                                    val jsonObject = JSONObject(responses)
                                    if (jsonObject.optJSONObject("result")
                                            .optString("result_code") == "000"
                                    ){
                                        activity.runOnUiThread {
                                            kotlin.run {
                                                if (isFavourite == true) {

                                                    viewholder.itemView.favourite.setImageDrawable(
                                                        ContextCompat.getDrawable(
                                                            context,
                                                            R.drawable.favourite
                                                        )
                                                    )
                                                    viewholder.itemView.favourite.setColorFilter(
                                                        Color.parseColor(
                                                            paymentSdk.favouriteStyle.optString(
                                                                "color"
                                                            )
                                                        )
                                                    )

                                                } else {
                                                    viewholder.itemView.favourite.setImageDrawable(
                                                        ContextCompat.getDrawable(
                                                            context,
                                                            R.drawable.favourite_active
                                                        )
                                                    )

                                                    viewholder.itemView.favourite.setColorFilter(
                                                        Color.parseColor(
                                                            paymentSdk.favouriteStyle.optString(
                                                                "selected_color"
                                                            )
                                                        )
                                                    )
                                                }
                                                isFavourite = !isFavourite
                                            }
                                        }

                                    }
                                    else{
                                        if (paymentSdk.language == "en") {
                                            exc_handling(jsonObject.optJSONObject("result")
                                                .optString("result_message_en"))
                                        }
                                        else{
                                            exc_handling(jsonObject.optJSONObject("result")
                                                .optString("result_message_kh"))
                                        }

                                    }
                                }

                            })


                        }
                    }
                }
            } else {
                viewholder.itemView.favourite.visibility = View.GONE

            }


            val shape = GradientDrawable()
            shape.cornerRadius = 0F
            viewholder.itemView.card.radius =
                paymentSdk.paymentMethodBtn.optString("icon_radius").split("p")[0].toFloat()
            val padding =
                paymentSdk.paymentMethodBtn.optString("icon_border_size").split("p")[0].toInt() * 3
            viewholder.itemView.bankImage.setPadding(padding, padding, padding, padding)
            viewholder.itemView.bankImage.setBackgroundColor(
                Color.parseColor(
                    paymentSdk.paymentMethodBtn.optString(
                        "icon_border_color"
                    )
                )
            )

            viewholder.itemView.bankImage.adjustViewBounds = true
            viewholder.itemView.bankName.setTypeface(ResourcesCompat.getFont(activity, R.font.kh9))

            viewholder.itemView.fee.text = current_item_position.fee
            viewholder.itemView.fee.typeface = custom_font



            viewholder.itemView.setOnClickListener {

                paymentSdk.confirmbtn.isEnabled = true
                paymentSdk.confirmbtn.alpha = 1F
                if (position < paymentSdk.supportTokenize.size) {
                    if (paymentSdk.supportTokenize[position] == "true") {
                        paymentSdk.switchbutton.isEnabled = true
                    } else {
                        paymentSdk.switchbutton.isChecked = false
                        paymentSdk.switchbutton.isEnabled = false

                    }
                } else {
                    paymentSdk.switchbutton.isChecked = false
                    paymentSdk.switchbutton.isEnabled = false
                }

                if (selectedPosition >= 0) {
                    notifyItemChanged(selectedPosition)
                }
                selectedPosition = position

                notifyItemChanged(selectedPosition)

            }
            context.runOnUiThread {
                kotlin.run {
                    context.let {
                        Glide.with(it).load(current_item_position.imageString).centerCrop()
                            .into(viewholder.itemView.bankImage)
                    }
                }
            }



            if (selectedPosition == position) {

                viewholder.itemView.bankName.setTextColor(
                    Color.parseColor(
                        paymentSdk.selected_payment_method_button
                            .optString("text_color")
                    )
                )
                viewholder.itemView.fee.setTextColor(
                    Color.parseColor(
                        paymentSdk.selected_payment_method_button
                            .optString("text_color")
                    )
                )
                viewholder.itemView.setBackgroundColor(
                    Color.parseColor(
                        paymentSdk.selected_payment_method_button
                            .optString("background_color")
                    )
                )
                viewholder.itemView.radioBox.setColorFilter(
                    Color.parseColor(
                        paymentSdk.selected_payment_method_button
                            .optString("border_color")
                    )
                )
                viewholder.itemView.radioBox.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.tick
                    )
                )
            } else {
                if (paymentSdk.theme_mode == "dark") {
                    viewholder.itemView.setBackgroundColor(Color.parseColor("#454545"))
                    viewholder.itemView.radioBox.setColorFilter(Color.parseColor("#50000000"))
                    viewholder.itemView.bankName.setTextColor(Color.parseColor("#ffffff"))
                    viewholder.itemView.fee.setTextColor(Color.parseColor("#ffffff"))
                    viewholder.itemView.radioBox.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_radio_button_unchecked_24
                        )
                    )
                } else {
                    viewholder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                    viewholder.itemView.radioBox.setColorFilter(Color.parseColor("#50000000"))
                    viewholder.itemView.radioBox.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_radio_button_unchecked_24
                        )
                    )
                }

            }
        }




        paymentSdk.confirmbtn.setOnClickListener {
            isopened = true

            paymentSdk.hidebutton.performClick()
            paymentSdk.progressbar.visibility = View.VISIBLE
            activity.runOnUiThread {
                kotlin.run {
                    val data = data()
                    val supportTokenize = paymentSdk.supportTokenize
                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                    var lenghtOfAvailBanks: Int = 0
                    if (paymentSdk.bankCodeList.contains("pay_later")) {
                        lenghtOfAvailBanks = supportTokenize.size - 1
                    } else {
                        lenghtOfAvailBanks = supportTokenize.size
                    }

                    if (selectedPosition < lenghtOfAvailBanks) {
                        if (paymentSdk.support_deeplink[selectedPosition] == "true") {
                            val client: OkHttpClient = OkHttpClient()
                            var json: String
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                json = """
//            {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(paymentSdk.secretKey,
//                                    """
//                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
//            """.trimIndent()))}"}
//        """.trimIndent()
//                            }
//                            else{
//                                json = """
//            {"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(paymentSdk.secretKey,
//                                    """
//                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
//            """.trimIndent()),Base64.DEFAULT)}"}
//        """.trimIndent()
//                            }
                            json = init_payment_payload(agent = user_agent)
                            json = json.replace("\n", "").replace("\r", "")
                            var answer = JSONObject(json)

                            Log.d("answerr", answer.toString())

                            val url: String = "$url/payment/init"
                            val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                            val request = Request.Builder().url(url)
                                .post(answer.toString().toRequestBody(mediaTypeJson)).build()
                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("exc", "$e")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    val responses_data = response
                                    val responses = responses_data.body!!.string()
                                    if (responses_data.code != 200) {
                                        context.runOnUiThread {
                                            kotlin.run {
                                                Toast.makeText(
                                                    context,
                                                    responses,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                        return
                                    }
                                    val jsonObject = JSONObject(responses)
                                    if (jsonObject.optJSONObject("result")
                                            .optString("result_code") != "000"
                                    ) {
                                        activity.runOnUiThread {
                                            if (language == "kh") {
                                                Toast.makeText(
                                                    activity,
                                                    jsonObject.optJSONObject("result")
                                                        .optString("result_message_kh"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    activity,
                                                    jsonObject.optJSONObject("result")
                                                        .optString("result_message_en"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            paymentSdk.progressbar.visibility = View.GONE
                                            return@runOnUiThread
                                        }

                                        return
                                    }
                                    val encrypted_data: String =
                                        jsonObject.getJSONObject("data").optString("encrypted_data")

                                    try {
                                        secretKey = data.makePbeKey("sdkdev".toCharArray())!!

                                        var decrypted_data: String
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            decrypted_data = data.cbcDecrypt(
                                                secretKey,
                                                java.util.Base64.getDecoder().decode(encrypted_data)
                                            ).toString()
                                        } else {
                                            decrypted_data = data.cbcDecrypt(
                                                secretKey,
                                                Base64.decode(encrypted_data, Base64.DEFAULT)
                                            ).toString()
                                        }
                                        Log.d("Decryptedd", decrypted_data.toString())
                                        val decryptJson = JSONObject(decrypted_data)
                                        val checkout_data = decryptJson.optString("checkout_data")
                                        val decryptooneheckoutData =
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                data.cbcDecrypt(
                                                    secretKey,
                                                    java.util.Base64.getDecoder()
                                                        .decode(checkout_data)
                                                )
                                            } else {
                                                Base64.decode(checkout_data, Base64.DEFAULT)
                                            }
                                        Log.d("Decryptedddd", decryptooneheckoutData.toString())
                                        val formdata =
                                            decryptooneheckoutData.toString().replace("\n", "")
                                                .replace("\r", "")
                                        val deeplinkJson =
                                            JSONObject(decryptooneheckoutData.toString())
                                        val deeplink = deeplinkJson.optString("deeplink_data")
                                        try {
                                            val url = deeplink
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            activity.startActivity(intent)

                                        } catch (ex: Exception) {
//                                            if (paymentSdk.bankCodeList[selectedPosition] == "ACLEDA") {
//                                                Log.d("failed", "Failed to open AC deeplink")
//                                                init_web_payment()
//                                            } else {
                                                val package_name =
                                                    deeplinkJson.optString("play_store")
                                                        .split("id=")[1]
                                                try {
                                                    activity.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("market://details?id=${package_name}")
                                                        )
                                                    )
                                                } catch (e: ActivityNotFoundException) {
                                                    activity.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://play.google.com/store/apps/details?id=${package_name}")
                                                        )
                                                    )

                                            }

                                        }
                                        paymentSdk.progressbar.visibility = View.GONE
                                    } catch (e: Exception) {

                                    }
                                }

                            })


                        } else {
                            init_web_payment()
                        }

                    } else if (selectedPosition == lenghtOfAvailBanks) {
                        val intent = Intent(context, paylater!!::class.java)
                        intent.putExtra("order_details", order_details)
                        paymentSdk.dialog!!.dismiss()
                        context.startActivity(intent)
                    } else {

                        val client: OkHttpClient = OkHttpClient()
                        var json: String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d(
                                "tokenizeid",
                                (paymentSdk.bankCodeList[selectedPosition - 1].split("$$$"))[1]
                            )
                            json = """
                                {"encrypted_data": "${
                                java.util.Base64.getEncoder().encodeToString(
                                    data.cbcEncrypt(
                                        secretKey, """
                                    {"session_id": "${paymentSdk.sessionId}",
                                    "bank_id": "${
                                            (paymentSdk.bankIdList[selectedPosition - 1].split(
                                                "$$$"
                                            ))[0]
                                        }",
                                    "client_id": "${paymentSdk.clientID}",
                                    "tokenize_id":"${
                                            (paymentSdk.bankCodeList[selectedPosition - 1].split(
                                                "$$$"
                                            ))[1]
                                        }"}
                                """.trimIndent()
                                    )
                                )
                            }"}
                            """.trimIndent()
                        } else {
                            json = """
                                {"encrypted_data": "${
                                Base64.encodeToString(
                                    data.cbcEncrypt(
                                        secretKey, """
                                    {"session_id": "${paymentSdk.sessionId}",
                                    "bank_id": "${
                                            paymentSdk.bankIdList[selectedPosition - 1].split(
                                                "$$$"
                                            )[0]
                                        }",
                                    "client_id": "${paymentSdk.clientID}",
                                    "tokenize_id":"${
                                            paymentSdk.bankCodeList[selectedPosition - 1].split(
                                                "$$$"
                                            )[1]
                                        }"}
                                """.trimIndent()
                                    ), Base64.DEFAULT
                                )
                            }"}
                            """.trimIndent()
                        }
                        val answer = JSONObject(json.replace("\n", "").replace("\r", ""))
                        val url: String = "$url/tokenize/validate"
                        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                        val request = Request.Builder().url(url)
                            .post(answer.toString().toRequestBody(mediaTypeJson)).build()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("Failed", e.toString())
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responses_data = response
                                val responses = responses_data.body!!.string()
                                if (responses_data.code != 200) {
                                    context.runOnUiThread {
                                        kotlin.run {
                                            Toast.makeText(context, responses, Toast.LENGTH_SHORT)
                                                .show()

                                        }
                                    }
                                    return
                                }
                                val jsonObject = JSONObject(responses)
                                activity.runOnUiThread {
                                    kotlin.run {
                                        if (jsonObject.optJSONObject("result")
                                                .optString("result_code") != "000"
                                        ) {
                                            if (paymentSdk.language == "en") {
                                                Toast.makeText(
                                                    activity,
                                                    jsonObject.optJSONObject("result")
                                                        .optString("result_message_en"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                paymentSdk.progressbar.visibility = View.GONE
                                            } else {
                                                Toast.makeText(
                                                    activity,
                                                    jsonObject.optJSONObject("result")
                                                        .optString("result_message_kh"),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                paymentSdk.progressbar.visibility = View.GONE
                                            }
                                            return@runOnUiThread
                                        }
                                    }
                                }

                                val encrypted_data: String =
                                    jsonObject.getJSONObject("data").optString("encrypted_data")
                                try {
                                    secretKey = data.makePbeKey("sdkdev".toCharArray())!!

                                    var decrypted_data: String
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        decrypted_data = data.cbcDecrypt(
                                            secretKey,
                                            java.util.Base64.getDecoder().decode(encrypted_data)
                                        ).toString()
                                    } else {
                                        decrypted_data = data.cbcDecrypt(
                                            secretKey,
                                            Base64.decode(encrypted_data, Base64.DEFAULT)
                                        ).toString()
                                    }
                                    val json_object = JSONObject(decrypted_data)

                                    val validatetoken: String =
                                        json_object.optString("validate_token")


                                    activity.runOnUiThread {
                                        kotlin.run {
                                            if (jsonObject.optJSONObject("result")
                                                    .optString("result_code") != "000"
                                            ) {
                                                if (paymentSdk.language == "en") {
                                                    Toast.makeText(
                                                        activity,
                                                        jsonObject.optJSONObject("result")
                                                            .optString("result_message_en"),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    paymentSdk.progressbar.visibility = View.GONE
                                                    return@runOnUiThread
                                                } else {
                                                    Toast.makeText(
                                                        activity,
                                                        jsonObject.optJSONObject("result")
                                                            .optString("result_message_kh"),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    paymentSdk.progressbar.visibility = View.GONE

                                                    return@runOnUiThread

                                                }
                                            }
                                            val otp = otp(
                                                myItemModel[selectedPosition].imageString,
                                                myItemModel[selectedPosition].bankName,
                                                myItemModel[selectedPosition].fee,
                                                activity,
                                                validatetoken,
                                                paymentSdk.bankIdList[selectedPosition - 1].split("$$$")[0],
                                                paymentSdk,
                                                payment_succeeded,
                                                language,
                                                paymentSdk.paymentConfirmBtn,
                                                paymentSdk.sessionId,
                                                paymentSdk.clientID,
                                                orderID = paymentSdk.orderID,
                                                paymentSdk.socketID
                                            ) {
                                                function(it)
                                            }
                                            otp.show(fragmentManager, "otp")
                                            paymentSdk.socket.disconnect()
                                            Log.d("decryptedd", decrypted_data)
                                            paymentSdk.progressbar.visibility = View.GONE
                                        }
                                    }

                                } catch (e: Exception) {
                                    Log.d("Exception", e.toString())
                                }
                            }
                        })
                    }
                }
            }


        }
    }

    fun openAmkDeeplink(
        secret_key: String, client_id: String,
        audience: String, app_icon_url: String, app_name: String,
        app_deep_link_callback: String, merchant_acc: String, merchant_name: String,
        receiver_name: String, currency: String, amount: Double, merchant_city: String,
        bill_number: String, mobile_number: String, store_label: String, terminal_label: String,
        reference_id: String, number_of_bills: Int
    ) {

        AMKDeeplink.init(
            client_id,
            secret_key, audience
        )
        val sourceInfo = SourceInfo()
        sourceInfo.appIconUrl = app_icon_url
        sourceInfo.appName = app_name
        sourceInfo.appDeeplinkCallback = app_deep_link_callback
        val amkdlMerchant: AMKDLMerchant = AMKDLMerchant()
        amkdlMerchant.merchantAccount = merchant_acc
        amkdlMerchant.merchantName = merchant_name
        amkdlMerchant.receiverName = receiver_name
        amkdlMerchant.currency = currency
        amkdlMerchant.amount = amount
        amkdlMerchant.merchantCity = merchant_city
        amkdlMerchant.billNumber = bill_number
        amkdlMerchant.mobileNumber = mobile_number
        amkdlMerchant.storeLabel = store_label
        amkdlMerchant.terminalLabel = terminal_label
        amkdlMerchant.referenceId = reference_id
        amkdlMerchant.sourceInfo = sourceInfo
        AMKDeeplink.getInstance().generateMerchantDeeplink(
            amkdlMerchant
        ) {
            if (it.status.code == 0) {
                val shortLink: String = it.getData().getShortLink()
                val referenceId: String = it.getData()
                    .getReferenceId()
                Log.d("SortLink", shortLink)
                Log.d("ReferenceId", referenceId)
                try {
                    val url = shortLink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    activity.startActivity(intent)
                    paymentSdk.progressbar.visibility = View.GONE
                } catch (ex: Exception) {
                    Log.d("Failed open Deeplink", ex.toString())
                    paymentSdk.progressbar.visibility = View.GONE
                }
            } else {

                Log.d("exception", it.status.message)
                exc_handling(it.status.message)
            }

        }
    }

    fun init_payment_payload(agent: String): String {
        val json: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            json = """
            {"encrypted_data": "${
                java.util.Base64.getEncoder().encodeToString(
                    data().cbcEncrypt(
                        paymentSdk.secretKey,
                        """
                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "${agent}"}
            """.trimIndent()
                    )
                )
            }"}
        """.trimIndent()
        } else {
            json = """
            {"encrypted_data": "${
                Base64.encodeToString(
                    data().cbcEncrypt(
                        paymentSdk.secretKey,
                        """
                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()
                    ), Base64.DEFAULT
                )
            }"}
        """.trimIndent()
        }
        Log.d(
            "answerr", """
                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
            """.trimIndent()
        )
        return json
    }

    fun init_web_payment() {
        val client: OkHttpClient = OkHttpClient()

        var json: String
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                json = """
//            {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(paymentSdk.secretKey,
//                                    """
//                {"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
//            """.trimIndent()))}"}
//        """.trimIndent()
//                            }
//                            else{
//                                json = """
//{"encrypted_data": "${Base64.encodeToString(data.cbcEncrypt(paymentSdk.secretKey, """
//{"session_id": "${paymentSdk.sessionId}","client_id": "${paymentSdk.clientID}","bank_id": "${paymentSdk.bankIdList[selectedPosition]}","remember_acc": ${paymentSdk.switchbutton.isChecked},"user_agent": "web-mobile"}
//""".trimIndent()),Base64.DEFAULT)}"}""".trimIndent()
//                            }
        json = init_payment_payload(agent = "web-mobile")
        json = json.replace("\n", "").replace("\r", "")

        var answer = JSONObject(json.replace("\n", "").replace("\r", ""))

        val url: String = "$url/payment/init"
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        val requestBody = RequestBody.create(mediaTypeJson, json)
        val request =
            Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Exception", "${e}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responses_data = response
                val responses = responses_data.body!!.string()
                if (responses_data.code != 200) {
                    context.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(context, responses, Toast.LENGTH_SHORT).show()
                            paymentSdk.progressbar.visibility = View.GONE
                        }
                    }
                    return
                }

                val jsonObject = JSONObject(responses)
                if (jsonObject.optJSONObject("result").optString("result_code") != "000") {
                    activity.runOnUiThread {
                        kotlin.run {
                            if (language == "kh") {
                                Toast.makeText(
                                    activity,
                                    jsonObject.optJSONObject("result")
                                        .optString("result_message_kh"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    activity,
                                    jsonObject.optJSONObject("result")
                                        .optString("result_message_en"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            paymentSdk.progressbar.visibility = View.GONE
                            return@runOnUiThread
                        }

                    }


                }
                val encrypted_data: String =
                    jsonObject.getJSONObject("data").optString("encrypted_data")

                try {
                    secretKey = data().makePbeKey("sdkdev".toCharArray())!!


                    val decrypted_data = data().cbcDecrypt(
                        secretKey,
                        Base64.decode(encrypted_data, Base64.DEFAULT)
                    )
                    Log.d("Decryptedd", decrypted_data.toString())
                    val decryptJson = JSONObject(decrypted_data)
                    val checkout_data = decryptJson.optString("checkout_data")

                    val decryptooneheckoutData = data().cbcDecrypt(
                        secretKey,
                        Base64.decode(checkout_data, Base64.DEFAULT)
                    ).toString().replace("\n", "").replace("\r", "")


                    Log.d("Decryptedddd", decryptooneheckoutData.toString())

                    if (paymentSdk.in_app[selectedPosition] == "true") {

                        if (paymentSdk.bankCodeList[selectedPosition] == "AMKPLC") {

                            val submit_data = JSONObject(decryptooneheckoutData)

                            val formdata = submit_data.optJSONObject("form_data")
                            val action_url = submit_data.optString("action_url")
                            val app_deep_link_callback = action_url

                            val secret_key = formdata.optString("secret_key")
                            val client_id = formdata.optString("client_id")
                            val audience = action_url
                            val app_icon_url = formdata.optString("app_icon_url")
                            val app_name = formdata.optString("app_name")
                            val merchant_acc = formdata.optString("merchant_acc")
                            val merchant_name = formdata.optString("merchant_name")
                            val receiver_name = formdata.optString("receiver_name")
                            val currency = formdata.optString("currency")
                            val amount = formdata.optDouble("amount")

                            val merchant_city = formdata.optString("merchant_city")
                            val bill_number = formdata.optString("transaction_id")
                            val mobile_number = formdata.optString("mobile_number")
                            val store_label = formdata.optString("store_label")
                            val terminal_label = formdata.optString("terminal_label")
                            val reference_id = formdata.optString("transaction_id")
                            val number_of_bills = formdata.optInt("number_of_bills")
                            Log.d("errr", audience)

                            openAmkDeeplink(
                                secret_key, client_id, audience,
                                app_icon_url, app_name, app_deep_link_callback,
                                merchant_acc, merchant_name, receiver_name,
                                currency, amount, merchant_city, bill_number, mobile_number,
                                store_label, terminal_label, reference_id, number_of_bills
                            )
                        }
                    } else {
                        val bankPaymentController = bankPaymentController(
                            formdata = decryptooneheckoutData,
                            payment_succeeded = payment_succeeded,
                            orderID = paymentSdk.orderID,
                            activity = activity,
                            paymentSdk = paymentSdk,
                            socketID = paymentSdk.socketID
                        ) {
                            function(it)
                        }
                        bankPaymentController.show(fragmentManager, "Bank Payment")
                        paymentSdk.socket.disconnect()
                    }


                } catch (error: Exception) {
                    print(error)
                }
            }

        })
    }
    fun encryption(data_tobe_encrypted:String ): String{
        val data = data()
        val encrypted_data:String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encrypted_data = java.util.Base64.getEncoder().encodeToString(
                data.cbcEncrypt(
                    paymentSdk.secretKey,
                    data_tobe_encrypted
                )
            )
        } else {
            encrypted_data = Base64.encodeToString(
                data.cbcEncrypt(
                    paymentSdk.secretKey,
                    data_tobe_encrypted
                ), Base64.DEFAULT
            )

        }
        return encrypted_data
    }
    fun exc_handling(message: String) {
        activity.runOnUiThread {
            kotlin.run {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                paymentSdk.progressbar.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return myItemModel!!.size
    }
}