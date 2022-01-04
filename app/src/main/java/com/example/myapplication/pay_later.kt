package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bill24sk.main
import kotlinx.android.synthetic.main.activity_pay_later.*
import org.json.JSONObject

class pay_later() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val order_details = intent.getStringExtra("order_details")
        val json = JSONObject(order_details)
        var listbank = ""
        setContentView(R.layout.activity_pay_later)
        continueBtn.setOnClickListener {
            startActivity(Intent(this.applicationContext,MainActivity()::class.java))
        }
        orderid.text = "Order #"+json.optString("order_ref")
        customerName.text = json.optJSONObject("customer_info")!!.optString("customer_name")
        itemName.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("item_name")
        quantity.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("quantity")
        amount.text = json.optJSONArray("order_items").optJSONObject(0).optString("price") + ".00 USD"
        subtotal.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("amount") + ".00 USD"
        discount.text = json.optJSONArray("order_items").optJSONObject(0).optString("discount_amount") + ".00 USD"
        total.text = json.optString("total_amount") + ".00 USD"
        for (item in 0..json.optJSONArray("app_or_agency_payment_methods")!!.length()-1) {
            if (item < json.optJSONArray("app_or_agency_payment_methods")!!.length()-1) {
                listbank = listbank+ JSONObject(json.optJSONArray("app_or_agency_payment_methods")!![item].toString())
                    .optString("name_en") + ", "
            }
            else{
                listbank = listbank+ JSONObject(json.optJSONArray("app_or_agency_payment_methods")[item].toString())
                    .optString("name_en")
            }


        }
        banklist.text = listbank
    }
}