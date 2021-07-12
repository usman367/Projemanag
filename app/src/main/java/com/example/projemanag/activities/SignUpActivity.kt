package com.example.projemanag.activities

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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//Inheriting from Base activity instead of the default AppCompatActivity
class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Create the action bar
        setupActionBar()

        // Add a click event to the Sign-Up button and call the registerUser function.
        val btn_sign_up = findViewById<android.widget.Button>(R.id.btn_sign_up)
        btn_sign_up.setOnClickListener{
            registerUser()
        }

    }

    //A function for setting up the actionBar.
    private fun setupActionBar() {
        val toolbar_sign_up_activity = findViewById<Toolbar>(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            //For the back button
            actionBar.setDisplayHomeAsUpEnabled(true)
            //For the design of the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        //Setting an onClickListener on the button
        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    // A function to validate the entries of a new user.
    private fun validateForm(name: String, email: String, password: String): Boolean {
        //Returns false when the name email or passwords are empty
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter name.")
                false
            }
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

    //function to register a new user to the app.
    /**
     * A function to register a user to our app using the Firebase.
     * For more details visit: https://firebase.google.com/docs/auth/android/custom-auth
     */
    //This saved the user in Authentication on Firebase
    private fun registerUser(){
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_email = findViewById<EditText>(R.id.et_email)
        val et_password = findViewById<EditText>(R.id.et_password)

        //Trim the empty spaces
        val name: String = et_name.text.toString().trim { it <= ' ' }
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        //Use the validateForm() func to validate these details
        if (validateForm(name, email, password)) {
            // Show the progress dialog (Which we created in the BaseActivity)
            showProgressDialog(resources.getString(R.string.please_wait))

            //Using firebase to create the user with email and password, we pass in the email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        // Hide the progress dialog
                        //hideProgressDialog(), We don't need it anymore as we do it in the userRegisteredSuccess() func below

                        // If the registration is successfully done
                        if (task.isSuccessful) {

                            // Firebase registered user
                            //We create a new Firebase user
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            // Get the registered Email
                            val registeredEmail = firebaseUser.email!!

                            //We create a new user object, using their ID, name and email
                            val user = User(
                                firebaseUser.uid, name, registeredEmail
                            )

                            // call the registerUser function of FirestoreClass to make an entry into the database.
                            FirestoreClass().registerUser(this@SignUpActivity, user)


                        } else {
                            ///If the task was not successful, just show an error message
                            Toast.makeText(
                                this@SignUpActivity,
                                task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }
    }


    //Called in the FirestoreClass, in SignInUser()
    /**
     * A function to be called the user is registered successfully and entry is made in the firestore database.
     */
    fun userRegisteredSuccess() {

        //Let the user know they've successfully registered
        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        // Hide the progress dialog
        hideProgressDialog()

        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()

        // Finish the Sign-Up Screen
        finish()
    }

}