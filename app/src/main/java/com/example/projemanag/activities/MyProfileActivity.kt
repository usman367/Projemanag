package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    // A global variable for user details.
    private lateinit var mUserDetails: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        // Call a function to get the current logged in user details
        FirestoreClass().loadUserData(this@MyProfileActivity)


        //Add a click event for iv_profile_user_image, so we can ask for permissions and change it
        val iv_profile_user_image = findViewById<ImageView>(R.id.iv_profile_user_image)
        iv_profile_user_image.setOnClickListener {

            //If we have the permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                //Call the image chooser function
                Constants.showImageChooser(this)

            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                //We pass in the READ_STORAGE_PERMISSION_CODE which we will use in the onRequestPermissionsResult() func below
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }


        // Add a click event for updating the user profile data to the database
        val btn_update = findViewById<Button>(R.id.btn_update)
        btn_update.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadUserImage()
            }else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }


    }

    // Create a function to setup action bar.
    private fun setupActionBar() {
        val toolbar_my_profile_activity = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            //Then create the back button
            actionBar.setDisplayHomeAsUpEnabled(true)
            //Use the following icon for it
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            //Set its title, using a string we created from resources
            actionBar.title = resources.getString(R.string.my_profile)
        }
        //When its pressed go back
        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }


    // A function to set the existing data in UI.
    //We use a library called Glide to get the data from firebase
    // https://github.com/bumptech/glide
    fun setUserDataInUI(user: User) {
        val iv_user_image = findViewById<ImageView>(R.id.iv_profile_user_image)
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_email = findViewById<EditText>(R.id.et_email)
        val et_mobile = findViewById<EditText>(R.id.et_mobile)


        // Initialize the user details variable
        mUserDetails = user

        //Using glide to load the image and place it into the image view
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image)

        //Setting the name and the email
        et_name.setText(user.name)
        et_email.setText(user.email)
        //If the mobile number is not 0, then set it
        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
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

            val iv_profile_user_image = findViewById<ImageView>(R.id.iv_profile_user_image)

            // Get uri of selection image from phone storage.
            mSelectedImageFileUri = data.data

            try {
                // Load the user image in the ImageView.
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_profile_user_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }



    // Before start with database we need to perform some steps in Firebase Console and after adding a dependency in Gradle file.
    // Follow the Steps:
    // Step 1: Go to the "Storage" tab in the Firebase Console in your project details in the navigation bar under "Develop".
    // Step 2: In the Storage Page click on the Get Started. Click on Next
    // Step 3: As we have already selected the storage location while creating the database so now click the Done button.
    // Step 4: Now the storage bucket is created.
    // Step 5: For more details visit the link: https://firebase.google.com/docs/storage/android/start
    // Step 6: Now add the code to upload image.
    /**
     * A function to upload the selected user image to firebase cloud storage.
     */
    private fun uploadUserImage() {
        // Call the showProgressDialog() func from the BaseActivity
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference from Firebase
            //We give the URI we get from the image we used on our device
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this, mSelectedImageFileUri
                )
            )

            //Adding the file to reference
            //We want to put the image from mSelectedImageFileUri on the Firebase storage
            sRef.putFile(mSelectedImageFileUri!!)
                    //If it was successful
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success

                    //Log it
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            hideProgressDialog()

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()

                            // Call a function to update user details in the database.
                            updateUserProfileData()
                        }
                }
                    //If we were unsuccessful
                .addOnFailureListener { exception ->
                    //Write a toast for now
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }


    //All for uploading the image to the Firebase database
    // A function to notify the user profile is updated successfully
    fun profileUpdateSuccess() {

        hideProgressDialog()

        // Send the success result to the Base Activity, this is so we can update the users details in the menu
        //Once the user has updated them in th emyProfile Activity (Check Main Activity for details)
        setResult(Activity.RESULT_OK)

        finish() //Close the activity so when the user presses back, it doesn't open this activity
    }

    // Update the user profile details into the database
    //Made it private so we don't mix the name with the func in Firestore class (As it has the same name)
    private fun updateUserProfileData() {
        val et_name = findViewById<EditText>(R.id.et_name)
        val et_mobile = findViewById<EditText>(R.id.et_mobile)

        //Creating a HashMap
        val userHashMap = HashMap<String, Any>()

        //If the image URL is not empty, and if the URL is not the same as the image we already use as the profile pic
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            //Then add it to the HashMap
            //We use the Image variable from constants as the key
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_mobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        // Update the data in the database.
        //Calls the function updateUserProfileData() from Firestore Class to update the result in the database
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }


//    // Create a companion object and add a constant for Read Storage runtime permission
//    //We use these constants to identify the different permissions
//    companion object {
//        //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
//        private const val READ_STORAGE_PERMISSION_CODE = 1
//
//        // Add a constant for image selection from phone storage)
//        private const val PICK_IMAGE_REQUEST_CODE = 2
//
//    }

}