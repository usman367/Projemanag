package com.example.projemanag.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.projemanag.activities.MyProfileActivity

object Constants {

    // Firebase Constants
    // This  is used for the collection name for USERS.
    const val USERS: String = "users"

    // Firebase database field names
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"

    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
    const val READ_STORAGE_PERMISSION_CODE = 1

    // Add a constant for image selection from phone storage)
    const val PICK_IMAGE_REQUEST_CODE = 2

    // Add constant variable for Boards
    // This  is used for the collection name for USERS.
    const val BOARDS: String = "boards"

    // A field name as assignedTo which we are gonna use later on
    const val ASSIGNED_TO: String = "assignedTo"

    // A constant for DocumentId
    const val DOCUMENT_ID: String = "documentId"

    // A new field for TaskList
    const val TASK_LIST: String = "taskList"

    // constant for passing the board details through intent
    const val BOARD_DETAIL: String = "board_detail"

    // A field name as a constant which we will be using for getting the list of user details from the database
    const val ID: String = "id"

    const val EMAIL: String = "email"

    // Add all the required constants for passing the details to CardDetailsActivity through intent
    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"

    //Add a SharedPreferences name and key names.
    const val PROGEMANAG_PREFERENCES: String = "ProjemanagPrefs"
    const val FCM_TOKEN:String = "fcmToken"
    const val FCM_TOKEN_UPDATED:String = "fcmTokenUpdated"

    // Add the base url  and key params for sending firebase notification
    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    // Click on the project, click on the the setting icon next to the project overview, click its "project settings" option
    //Then click on the "Cloud Messaging" option at the top, asn you will fins the server key there
    // Google "fcm sender key" to see how to find it
    const val FCM_SERVER_KEY:String = "AAAArFeK05o:APA91bF4dVPBY8tKR_KzY_l5rTLwYEZ-iglzrfQJmGfkidOUp9cxCOO6i2P-3MPXQ6eYpOcV8gyMt_ExhP7POzmlr8_OrZqSclUGaS-2Wu6oCJUwFTUrBxVFKeXtVU9S6_X-XVRL0jgD"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    /**
     * A function for user profile image selection from phone storage.
     */
    //It will open the gallery so the user can select it
    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    //This is all for uploading the image to the Firebase storage
    /**
     * A function to get the extension of selected image.
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        //MimeTypeMap Allows us to find the type of the URI
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}