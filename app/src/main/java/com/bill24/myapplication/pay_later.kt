package com.bill24.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityPayLaterBinding
import org.json.JSONObject

class pay_later() : AppCompatActivity() {
    lateinit var binding:ActivityPayLaterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayLaterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val order_details = intent.getStringExtra("order_details")
        val json = JSONObject(order_details)
        var listbank = ""
        binding.continueBtn.setOnClickListener {
            startActivity(Intent(this.applicationContext, homescreen()::class.java))
            finish()
        }
        binding.orderid.text = "Order #"+json.optString("order_ref")
        binding.customerName.text = json.optJSONObject("customer_info")!!.optString("customer_name")
        binding.itemName.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("item_name")
        binding.quantity.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("quantity")
        binding.amount.text = json.optJSONArray("order_items").optJSONObject(0).optString("price") + ".00 USD"
        binding.subtotal.text = json.optJSONArray("order_items")!!.optJSONObject(0).optString("amount") + ".00 USD"
        binding.discount.text = json.optJSONArray("order_items").optJSONObject(0).optString("discount_amount") + ".00 USD"
        binding.total.text = json.optString("total_amount") + ".00 USD"
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
        binding.banklist.text = listbank
    }
}