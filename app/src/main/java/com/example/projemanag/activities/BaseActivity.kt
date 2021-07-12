package com.example.projemanag.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.projemanag.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

//Here we have created a BaseActivity Class in which we have added the progress dialog and SnackBar.
// Now all the activity will extend the BaseActivity instead of AppCompatActivity.
//We made it open so other classes can inherit from it
open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    /**
     * This is a progress dialog instance which we will initialize later on.
     */
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }


    /**
     * This function is used to show the progress dialog with the title and message to user.
     */
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }


    //Using Firebase to return the current users ID
    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }


    //function to check if the user has pressed the back button twice
    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        //Tell the user they can press it again to exit the app
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        //Using a handler, to change it to false, if the user doesn't press it again  within 2 secs
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }


    //Func to show errors
    fun showErrorSnackBar(message: String) {
        //SnackBar is basically a toast but you can swipe it away
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)

        //We create a snackBar view to design it e.g., background color
        val snackBarView = snackBar.view

        //We change it's background color
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )

        snackBar.show()
    }
}