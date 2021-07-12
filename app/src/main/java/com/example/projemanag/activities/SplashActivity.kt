package com.example.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TextView
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_splash)

        // Add the full screen flags here.
        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Add the file in the custom font file to the assets folder. And add the below line of code to apply it to the title TextView.
        // Steps for adding the assets folder are :
        // Right click on the "app" package and GO TO ==> New ==> Folder ==> Assets Folder ==> Finish.
        // This is used to get the file from the assets folder and set it to the title textView.
        //Creating a typeface object, then we create the asset by giving the name of the folder, and the file name
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        val tv_app_name = findViewById<TextView>(R.id.tv_app_name)
        //Apply the typeface to the text view
        tv_app_name.typeface = typeface


        //To change switch to a different activity after a couple of seconds
        // As using handler the splash screen will disappear after what we give to the handler.
        // Adding the handler to after the a task after some delay.
        Handler().postDelayed({

            //Check if the current user id is not blank then send the user to MainActivity as they've already signed-in
            // before or else send him to Intro Screen as earlier (Because they will need to sign-in)
            // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
            // we will redirect him to MainScreen or else to the Intro Screen as it was before.

            // Get the current user id from the FirestoreClass
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                // If the ID is not empty, start the Main Activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // Otherwise, start the Intro Activity
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish() // Close this activity

        }, 2500) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.

    }

}