package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    // A global variable for User name
    private lateinit var mUserName: String

    // A global variable for a board image URL
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        //  Get the username from the MainActivity's intent
        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        //A click event for iv_board_image
        val iv_board_image = findViewById<ImageView>(R.id.iv_board_image)
        iv_board_image.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                //If we already have the permission, call the showImageChooser() func from constants
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                //Otherwise, ask for he permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }


        // A click event for btn_create
        val btn_create = findViewById<Button>(R.id.btn_create)
        btn_create.setOnClickListener {

            if (mSelectedImageFileUri != null) {
                //If we have selected an image, then upload the board image
                //(Inside uploadBoardImage() we also create the board by calling the createBoard() func)
                uploadBoardImage()
            } else {
                //Otherwise, show the progress dialog
                showProgressDialog(resources.getString(R.string.please_wait))

                // And call a function to update create a board.
                createBoard()
            }
        }


    }


    // A function to setup action bar
    private fun setupActionBar() {
        val toolbar_create_board_activity = findViewById<Toolbar>(R.id.toolbar_create_board_activity)
        //Creating the toolbar
        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            //Create the back button
            actionBar.setDisplayHomeAsUpEnabled(true)
            //Use this image for the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            //Set its title, using a string we created from resources
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        //Go back when the back button is pressed
        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }


    // Check the result of runtime permission after the user allows or deny based on the unique code
    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //If the request code is equal to READ_STORAGE_PERMISSION_CODE (Which we into it above, when we asked for the permission)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Call the image chooser function
                Constants.showImageChooser(this)

            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //Get the result of the image selection based on the constant code
    //Once the user has selected an image, get its URI, and set it using Glider
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {

            val iv_board_image = findViewById<ImageView>(R.id.iv_board_image)

            // Get uri of selection image from phone storage.
            mSelectedImageFileUri = data.data

            try {
                // Load the user image in the ImageView.
                Glide
                    .with(this)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_board_place_holder) // A default place holder
                    .into(iv_board_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    // A function which will notify the success of board creation
    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        //Set the result as OK
        setResult(Activity.RESULT_OK)

        finish()
    }

    // A function to create the board
    /**
     * A function to make an entry of a board in the database.
     */
    private fun createBoard() {
        val et_board_name = findViewById<EditText>(R.id.et_board_name)

        //  A list is created to add the assigned members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()

        assignedUsersArrayList.add(getCurrentUserID()) // adding the current user id.

        // Creating the instance of the Board and adding the values as per parameters.
        val board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        //We call this function from the firebase class which creates the board in the database
        FirestoreClass().createBoard(this@CreateBoardActivity, board)
    }

    // Creating the function to upload the Board Image to storage and getting the downloadable URL of the image
    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )

        //adding the image file's URI to storage reference
        sRef.putFile(mSelectedImageFileUri!!)

            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl

                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // assign the image url to the variable.
                        mBoardImageURL = uri.toString()

                        // Call a function to create the board.
                        createBoard()
                    }

            }

            .addOnFailureListener { exception ->
                //If it doesn't work
                Toast.makeText(
                    this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
    }

}