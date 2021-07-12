package com.example.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth

//Inheriting from Base activity instead of the default AppCompatActivity
class SignInActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Create the action bar
        setupActionBar()

        //Add click event for sign-in button and call the function to sign in.
        val btn_sign_in = findViewById<Button>(R.id.btn_sign_in)
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    //A function for setting up the actionBar.
    private fun setupActionBar() {
        val toolbar_sign_in_activity = findViewById<Toolbar>(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            //For the back button
            actionBar.setDisplayHomeAsUpEnabled(true)
            //For the design of the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        //Setting an onClickListener on the button
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }


    // A function to validate the entries of a new user.
    private fun validateForm(email: String, password: String): Boolean {
        //Returns false when the name email or passwords are empty
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            //Otherwise return true
            else -> {
                true
            }
        }
    }

    //A function for Sign-In using the registered user using the email and password.
    private fun signInRegisteredUser() {
        val et_email = findViewById<EditText>(R.id.et_email_sign_in)
        val et_password = findViewById<EditText>(R.id.et_password_sign_in)

        // Here we get the text from editText and trim the space
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        //Validate their details
        if (validateForm(email, password)) {
            // Show the progress dialog (which we created in the Base Activity)
            showProgressDialog(resources.getString(R.string.please_wait))

            // Sign-In using FirebaseAuth, using their email and password
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    //Hide the progress dialog
                    hideProgressDialog()

                    //If the task was successful
                    if (task.isSuccessful) {

                        //We removed the toast message and call the FirestoreClass signInUser function to get the data of user from database. And also move the code of hiding Progress Dialog and Launching MainActivity to Success function.
                        // Calling the FirestoreClass signInUser function to get the data of user from database.
                        FirestoreClass().loadUserData(this@SignInActivity)

                    } else {
                        //If the task was unsuccessful, let the user know
                        Toast.makeText(
                            this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
        }

    }


    //Create a function to get the user details from the firestore database after authentication.
    //Called from the Firestore class in the signInUser() func
    fun signInSuccess(user: User) {
        //Hide the progress dialog (func is in BaseActivity)
        hideProgressDialog()

        //Ove to the main activity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))

        //Finish this activity (so when we press back we don't come back to this activity)
        finish()
    }

}