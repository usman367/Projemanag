package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.projemanag.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //For the font
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        val tv_app_name_intro = findViewById<TextView>(R.id.tv_app_name_intro)
        tv_app_name_intro.typeface = typeface


        // Add a click event for Sign In btn and launch the Sign In Screen.
        val btn_sign_in_intro = findViewById<Button>(R.id.btn_sign_in_intro)
        btn_sign_in_intro.setOnClickListener {
            // Launch the sign in screen.
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        // Add a click event for Sign Up btn and launch the Sign Up Screen.
        val btn_sign_up_intro = findViewById<Button>(R.id.btn_sign_up_intro)
        btn_sign_up_intro.setOnClickListener {

            // Launch the sign up screen.
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }
}