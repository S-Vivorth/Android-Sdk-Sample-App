package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.bill24sk.MainActivity
import com.example.bill24sk.bottomSheetController
import com.example.bill24sk.showtoast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.flutter.embedding.android.FlutterActivity
class MainActivity : AppCompatActivity() {
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        
        button = findViewById(R.id.button)
        button.setOnClickListener {
//            startActivity(
//            FlutterActivity.createDefaultIntent(this)
//        )
            var bottomsheetFrag = bottomSheetController(supportFragmentManager = supportFragmentManager,paylater = pay_later(),
                sessionId = "JOTS2Bwd201SwxrbdxCS68sHRYqco/vrXQVp3pG5qPb7qB/BQI6Pa9G8SxuQJkjUv1nWrptWWQdu0hej0Ib0zfhYeDt0ltTCRlbmpRP98TE=",
                clientID = "W/GkvceL7nCjOF/v+fu5MA+epIQMXMJedMeXvbvEn7I=", activity = this
            ,payment_succeeded = payment_succeeded(),language = "en")

            bottomsheetFrag.show(supportFragmentManager,"bottomsheet")
        showtoast(this)
//        MainActivity().bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


    }

}
