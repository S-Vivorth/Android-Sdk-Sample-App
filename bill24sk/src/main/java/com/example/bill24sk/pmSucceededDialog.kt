package com.example.bill24sk

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.sdk_payment_succeeded.*

class pmSucceededDialog : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        val view: View = inflater!!.inflate(R.layout.sdk_payment_succeeded,null)
        continueShoppingBtn.setOnClickListener {
            Log.d("button", "is clicked.")
        }
        val builder = AlertDialog.Builder(activity)
        builder.setView(view)
        return builder.create()
    }
}