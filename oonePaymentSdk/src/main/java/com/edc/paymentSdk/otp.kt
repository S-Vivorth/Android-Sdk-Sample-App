package com.oone.paymentSdk

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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import androidx.core.content.res.ResourcesCompat
import com.oone.paymentSdk.R
import com.marozzi.roundbutton.RoundButton
import kotlinx.android.synthetic.main.sdk_otp.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.crypto.SecretKey


class otp(
    bankImage: String,
    bankName: String,
    accNo: String,
    activity: Activity,
    validate_token: String,
    bankID: String,
    paymentSdk: paymentSdk,
    payment_succeeded: Activity,
    language: String,
    verifyBtnStyle: JSONObject,
    sessionId: String,
    clientId: String,
    orderID: String,
    socketID: String,
    function: (callback: Any) -> Unit
) : BottomSheetDialogFragment() {
    var bankImage = bankImage
    var bankName = bankName
    var accNo = accNo
    var activity = activity
    var validate_token = validate_token
    var bankID = bankID
    var paymentSdk = paymentSdk

    lateinit var url: String
    val payment_succeeded = payment_succeeded
    var language: String = language
    var verifyBtnStyle: JSONObject = verifyBtnStyle
    var sessionId = sessionId
    var clientId = clientId
    var custom_font = ResourcesCompat.getFont(activity, R.font.kh9)
    var orderID = orderID
    var socketID = socketID
    val function = function
    var isFinishPayment = false
    lateinit var t: CountDownTimer
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.sdk_otp, container, false)
        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }


    fun timer() {
        resendBtn.typeface = custom_font
        t = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                if ((millisUntilFinished / 1000).toInt() < 10) {
                    resendBtn.text = "0:0" + (millisUntilFinished / 1000).toInt().toString()
                } else {
                    resendBtn.text = "0:" + (millisUntilFinished / 1000).toInt().toString()
                }
            }

            override fun onFinish() {
                resendBtn.setOnClickListener {
                    val client = OkHttpClient()
                    val json: String
                    val data = data()
                    val secretKey = data.makePbeKey("sdkdev".toCharArray())!!
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        json = """
                            {"encrypted_data" : "${
                            java.util.Base64.getEncoder().encodeToString(
                                data.cbcEncrypt(
                                    secretKey, """
                                {"session_id" : "$sessionId", "bank_id" : "$bankID", "client_id" : "$clientId",
                                "validate_token" : "$validate_token" }
                            """.trimIndent()
                                )
                            )
                        }"}
                        """.trimIndent()
                    } else {
                        json = """
                            {"encrypted_data" : "${
                            Base64.encodeToString(
                                data.cbcEncrypt(
                                    secretKey, """
                                {"session_id" : "$sessionId", "bank_id" : "$bankID", "client_id" : "$clientId",
                                "validate_token" : "$validate_token"}
                            """.trimIndent()
                                ), Base64.DEFAULT
                            )
                        }"}
                        """.trimIndent()
                    }
                    var answer = JSONObject(json)
                    val url: String = "$url/tokenize/resend-otp"
                    val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder().url(url)
                        .post(answer.toString().toRequestBody(mediaTypeJson)).build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("Execption", e.toString())
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responses = response.body!!.string()
                            val jsonObject = JSONObject(responses)
                            if (language == "en") {
                                activity.runOnUiThread {
                                    kotlin.run {
                                        Toast.makeText(
                                            activity, jsonObject
                                                .optJSONObject("result")
                                                .optString("result_message_en"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                activity.runOnUiThread {
                                    kotlin.run {
                                        Toast.makeText(
                                            activity, jsonObject
                                                .optJSONObject("result")
                                                .optString("result_message_kh"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            Log.d("resultt", jsonObject.toString())
                            val encrypted_data: String =
                                jsonObject.getJSONObject("data").optString("encrypted_data")

                        }

                    })
                    timer()
                }
                if (language == "en") {
                    resendBtn.text = "Resend"
                } else {
                    resendBtn.text = "ស្នើម្ដងទៀត"
                }
            }

        }
        t.start()

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        t.cancel()
        try {
            paymentSdk.socket.connect()
        } catch (ex: java.lang.Exception) {
            Log.d("exception", ex.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        url = paymentSdk.url
        sendProcessing()
        paymentSdk.socket.disconnect()
        otpverification.typeface = custom_font
        enterOtpText.typeface = custom_font
        dontreceive.typeface = custom_font
        if (language != "en") {
            otpverification.text = "ការបញ្ជាក់ OTP"
            enterOtpText.text = "បញ្ចូល OTP ដែលបានផ្ញើទៅកាន់លេខទូរស័ព្ទរបស់លោកអ្នក"
            dontreceive.text = "មិនបានទទួល OTP?"
        }
        if (paymentSdk.theme_mode == "dark") {
            otp_indicator.setBackgroundColor(Color.parseColor("#ffffff"))
            otp_linear.setBackgroundColor(Color.parseColor("#454545"))
            otp_relative.setBackgroundColor(Color.parseColor("#454545"))
            otpverification.setTextColor(Color.parseColor("#ffffff"))
            otpbankName.setTextColor(Color.parseColor("#ffffff"))
            otpAccNo.setTextColor(Color.parseColor("#ffffff"))
            enterOtpText.setTextColor(Color.parseColor("#ffffff"))
            dontreceive.setTextColor(Color.parseColor("#ffffff"))
            resendBtn.setTextColor(Color.parseColor("#ffffff"))


        }
        verify_btn.setTextSize(20F)
        verify_btn.setOnClickListener {
            verifyBtnTapped()
        }
        if (language != "en") {
            verify_btn.text = "បញ្ជូន"
        }
        val builder = RoundButton.newBuilder()
        builder.withBackgroundColor(Color.parseColor(verifyBtnStyle.optString("background_color")))
            .withTextColor(
                Color.parseColor(
                    verifyBtnStyle.optString("text_color")
                )
            )
            .withCornerColor(
                Color.parseColor(
                    verifyBtnStyle.optString("border_color")
                )
            )
            .withCornerWidth(
                verifyBtnStyle.optString("border_size").split("p")[0].toInt() * 2
            )
            .withCornerRadius(verifyBtnStyle.optString("radius").split("p")[0].toInt() * 2)
        verify_btn.setCustomizations(builder)
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
        if (paymentSdk.theme_mode == "dark") {
            otpEditText.backgroundTintList =
                AppCompatResources.getColorStateList(activity, R.color.white)
        }
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        val otp = otpEditText.text
        resendBtn.paintFlags = Paint.UNDERLINE_TEXT_FLAG or resendBtn.paintFlags
        timer()
        view.setOnClickListener {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = ((Resources.getSystem().displayMetrics.heightPixels) * 1).toInt()

        }
    }

    fun verifyBtnTapped() {
        otpProgressBar.visibility = View.VISIBLE
        otpProgressBar.bringToFront()
        val data = data()
        val client = OkHttpClient()
        val secretKey: SecretKey = data.makePbeKey("sdkdev".toCharArray())!!
        val json: String

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            json = """
            {"encrypted_data": "${
                java.util.Base64.getEncoder().encodeToString(
                    data.cbcEncrypt(
                        secretKey, """
                    {"session_id": "${paymentSdk.sessionId}", "validate_token": "${validate_token}",
                    "otp": "${otpEditText.text}", "client_id":"${paymentSdk.clientID}",
                    "bank_id": "${bankID}"}
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
                    {"session_id": "${paymentSdk.sessionId}", "validate_token": "${validate_token}",
                    "otp": "${otpEditText.text}", "client_id":"${paymentSdk.clientID}",
                    "bank_id": "${bankID}"}
                """.trimIndent()
                    ), Base64.NO_WRAP
                )
            }"}
        """.trimIndent()
        }
        var answer = JSONObject(json)
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder().url(url + "/tokenize/confirm")
            .post(answer.toString().toRequestBody(mediaTypeJson)).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    kotlin.run {
                        otpProgressBar.visibility = View.GONE
                    }
                }

            }

            override fun onResponse(call: Call, response: Response) {
                val response_copy = response
                val responses = response_copy.body!!.string()
                Log.d("Responsebody", "${response_copy.code}")
                if (response_copy.code.toString() != "200") {
                    activity.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(activity, "${responses}", Toast.LENGTH_SHORT).show()
                            otpProgressBar.visibility = View.GONE
                        }
                    }
                    return
                }
                val jsonObject = JSONObject(responses)
                if (jsonObject.optJSONObject("result").optString("result_code") == "000") {
                    val encrypted_data: String =
                        jsonObject.getJSONObject("data").optString("encrypted_data")
                    try {
                        val secretKey = data.makePbeKey("sdkdev".toCharArray())!!

                        var decrypted_data: String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            decrypted_data = data.cbcDecrypt(
                                secretKey,
                                java.util.Base64.getDecoder().decode(encrypted_data)
                            ).toString()
                        } else {
                            decrypted_data = data.cbcDecrypt(
                                secretKey,
                                Base64.decode(encrypted_data, Base64.NO_WRAP)
                            ).toString()
                        }
                        Log.d("Decryptedd", decrypted_data.toString())
                        try {
                            val response_data = Array<Any>(1) { decrypted_data }
                            function(response_data)

                            activity.runOnUiThread {
                                kotlin.run {
                                    isFinishPayment = true
                                    dismiss()
                                    paymentSdk.dismiss()
                                    otpProgressBar.visibility = View.GONE
                                }
                            }
                        } catch (ex: Exception) {
                            activity.runOnUiThread {
                                kotlin.run {
                                    Toast.makeText(activity, ex.toString(), Toast.LENGTH_LONG)
                                        .show()
                                    otpProgressBar.visibility = View.GONE

                            }
                        }
                        return
                    }


                } catch (e: Exception) {
                    Log.d("Exception", "$e")
                }
            } else
            {
                if (language == "kh") {
                    activity.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(
                                activity, "${
                                    jsonObject.optJSONObject("result")
                                        .optString("result_message_kh")
                                }", Toast.LENGTH_LONG
                            ).show()
                            otpProgressBar.visibility = View.GONE

                        }
                    }
                } else {
                    activity.runOnUiThread {
                        kotlin.run {
                            Toast.makeText(
                                activity, jsonObject.optJSONObject("result")
                                    .optString("result_message_en"), Toast.LENGTH_LONG
                            ).show()
                            otpProgressBar.visibility = View.GONE

                        }
                    }
                }

            }

        }

    })
}

fun sendProcessing() {
    var json: String
    val data = data()
    val secretKey = data.makePbeKey("admin".toCharArray())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        json = """
                {"data" : "${
            java.util.Base64.getEncoder().encodeToString(
                data.cbcEncrypt(
                    secretKey!!, """
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()
                )
            )
        }"}
            """.trimIndent()
    } else {
        json = """
                {"data" : "${
            Base64.encodeToString(
                data.cbcEncrypt(
                    secretKey!!, """
                    {"event":"payment_processing", "message":"$socketID"}
                """.trimIndent()
                ), Base64.DEFAULT
            )
        }"}
            """.trimIndent()
    }
    val client = OkHttpClient()
    val answer = JSONObject(json)
    val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
    var request: Request
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        request = Request.Builder().header(
            "token", java.util.Base64.getEncoder().encodeToString(
                data.cbcEncrypt(
                    secretKey, """
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent()
                )
            ).toString()
        )
            .header("Accept", "application/json")
            .url("http://203.217.169.102:50212/socket/send")
            .post(answer.toString().toRequestBody(mediaTypeJson))
            .build()
    } else {
        request = Request.Builder().header(
            "token", Base64.encodeToString(
                data.cbcEncrypt(
                    secretKey, """
            {"app_id":"sdk","room_name":"${orderID}"}
        """.trimIndent()
                ), Base64.NO_WRAP
            ).toString()
        )
            .header("Accept", "application/json")
            .url("http://203.217.169.102:50212/socket/send")
            .post(answer.toString().toRequestBody(mediaTypeJson))
            .build()
    }
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {

        }

        override fun onResponse(call: Call, response: Response) {
            Log.d("response", response.body!!.string())
        }

    })
}

}