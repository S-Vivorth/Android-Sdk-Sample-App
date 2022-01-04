package com.example.bill24sk

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bank_cell.view.*
import kotlinx.android.synthetic.main.otp.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.crypto.SecretKey
import kotlin.math.floor


class otp(paylater:Activity,bankImage:String,bankName:String,accNo:String,activity:Activity,
validate_token:String,bankID:String,bottomSheetController: bottomSheetController,
          payment_succeeded:Activity,language:String,verifybtncolor:String,sessionId:String,clientId:String): BottomSheetDialogFragment() {
    var paylater:Activity = paylater
    var bankImage = bankImage
    var bankName = bankName
    var accNo = accNo
    var activity = activity
    var validate_token = validate_token
    var bankID = bankID
    var bottomSheetController = bottomSheetController

    val url:String = "https://sdkapi-demo.bill24.net"
    val payment_succeeded = payment_succeeded
    var language:String = language
    var verifybtncolor:String = verifybtncolor
    var sessionId = sessionId
    var clientId = clientId
    var custom_font = ResourcesCompat.getFont(activity,R.font.kh9)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.otp,container,false)
        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }
    fun timer(){
        resendBtn.typeface = custom_font
        val timer = object : CountDownTimer(8000,1000){
            override fun onTick(millisUntilFinished: Long) {
                if ((millisUntilFinished/1000).toInt() < 10) {
                    resendBtn.text = "0:0"+(millisUntilFinished/1000).toInt().toString()
                }
                else{
                    resendBtn.text = "0:"+(millisUntilFinished/1000).toInt().toString()
                }
            }

            override fun onFinish() {
                resendBtn.setOnClickListener {
                    Log.d("das","das")
                    val client = OkHttpClient()
                    val json:String
                    val data = data()
                    val secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        json = """
                            {"encrypted_data" : "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(
                            secretKey,"""
                                {"session_id" : "$sessionId", "bank_id" : "$bankID", "client_id" : "$clientId",
                                "validate_token" : "$validate_token" }
                            """.trimIndent()
                            ))}"}
                        """.trimIndent()
                    }
                    else{
                        json = """
                            {"encrypted_data" : "${Base64.encodeToString(data.cbcEncrypt(
                            secretKey,"""
                                {"session_id" : "$sessionId", "bank_id" : "$bankID", "client_id" : "$clientId",
                                "validate_token" : "$validate_token"}
                            """.trimIndent()
                        ),Base64.DEFAULT)}"}
                        """.trimIndent()
                    }
                    var answer = JSONObject(json)
                    val url:String = "$url/tokenize/resend-otp"
                    val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder().url(url).post(answer.toString().toRequestBody(mediaTypeJson)).build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("Execption",e.toString())
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responses = response.body!!.string()
                            val jsonObject = JSONObject(responses)
                            if (language == "en"){
                                activity.runOnUiThread {
                                    kotlin.run {
                                        Toast.makeText(activity,jsonObject
                                            .optJSONObject("result").optString("result_message_en"),
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            else{
                                activity.runOnUiThread{
                                    kotlin.run {
                                        Toast.makeText(activity,jsonObject
                                            .optJSONObject("result").optString("result_message_kh"),
                                        Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            Log.d("resultt",jsonObject.toString())
                            val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")

                        }

                    })
                    timer()
                }
                if (language == "en") {
                    resendBtn.text = "Resend"
                }
                else{
                    resendBtn.text = "ស្នើម្ដងទៀត"
                }
            }

        }
        timer.start()

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        otpverification.typeface = custom_font
        enterOtpText.typeface = custom_font
        dontreceive.typeface = custom_font
        if (language != "en"){
            otpverification.text = "ការបញ្ជាក់ OTP"
            enterOtpText.text = "បញ្ចូល OTP ដែលបានផ្ញើទៅកាន់លេខទូរស័ព្ទរបស់លោកអ្នក"
            dontreceive.text = "មិនបានទទួល OTP?"
        }
        verify_btn.setTextSize(20F)
        verify_btn.setOnClickListener {
            verifyBtnTapped()
        }
        if (language != "en"){
            verify_btn.text = "បញ្ជូន"
        }
        verify_btn.setBackgroundColor(Color.parseColor(verifybtncolor))
        otpbankName.typeface = custom_font
        otpAccNo.typeface = custom_font
        otpbankName.text = bankName
        otpAccNo.text = accNo
        activity.runOnUiThread {
            kotlin.run {
                context.let { Glide.with(it!!).load(bankImage).centerCrop().into(otpbankimage) }
            }
        }

        otpEditText.requestFocus()

        val otp = otpEditText.text
        resendBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG or resendBtn.paintFlags
        timer()


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels) * 1).toInt()

        }
    }
    fun verifyBtnTapped(){
        otpProgressBar.visibility = View.VISIBLE

        val data = data()
        val client = OkHttpClient()
        val secretKey:SecretKey = data.makePbeKey("sdkdev".toCharArray())!!
        val json:String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            json = """
            {"encrypted_data": "${java.util.Base64.getEncoder().encodeToString(data.cbcEncrypt(
                secretKey, """
                    {"session_id": "${bottomSheetController.sessionId}", "validate_token": "${validate_token}",
                    "otp": "${otpEditText.text}", "client_id":"${bottomSheetController.clientID}",
                    "bank_id": "${bankID}"}
                """.trimIndent()))}"}
        """.trimIndent()
        }
        else{
            json = """
            {"encrypted_data": "${
                Base64.encodeToString(data.cbcEncrypt(
                secretKey, """
                    {"session_id": "${bottomSheetController.sessionId}", "validate_token": "${validate_token}",
                    "otp": "${otpEditText.text}", "client_id":"${bottomSheetController.clientID}",
                    "bank_id": "${bankID}"}
                """.trimIndent()
            ),Base64.DEFAULT)}"}
        """.trimIndent()
        }
        var answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder().url(url+"/tokenize/confirm")
            .post(answer.toString().toRequestBody(mediaTypeJson)).build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("$call","$e")
                otpProgressBar.visibility = View.GONE

            }

            override fun onResponse(call: Call, response: Response) {
                val responses = response.body!!.string()
                Log.d("Responsebody","$responses")
                val jsonObject = JSONObject(responses)
                if (jsonObject.optJSONObject("result").optString("result_code") == "000"){
                    val encrypted_data:String = jsonObject.getJSONObject("data").optString("encrypted_data")
                    try {
                        val secretKey = data.makePbeKey("sdkdev".toCharArray())!!

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
                        val intent = Intent(activity,payment_succeeded::class.java)

                        val data_to_pass = arrayOf<String>(JSONObject(decrypted_data).optJSONObject("tran_data")!!
                            .optString("trans_id"),
                            JSONObject(decrypted_data).optJSONObject("tran_data")!!
                                .optString("bank_name_en"),
                            JSONObject(decrypted_data).optJSONObject("tran_data")!!
                                .optString("bank_ref"),
                            JSONObject(decrypted_data).optJSONObject("tran_data")!!
                                .optString("order_ref"))
                        intent.putExtra("tran_data",data_to_pass)
                        startActivity(intent)
                        otpProgressBar.visibility = View.GONE

                    }
                    catch (e:Exception){
                        Log.d("Exception","$e")
                    }
                }
                else{
                    activity.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(activity,"${jsonObject.optJSONObject("result")
                                .optString("result_message_kh")}",Toast.LENGTH_LONG).show()
                            otpProgressBar.visibility = View.GONE

                        }
                    }
                }

            }

        })
    }
}