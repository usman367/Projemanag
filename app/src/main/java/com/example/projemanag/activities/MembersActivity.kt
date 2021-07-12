package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.MemberListItemsAdapter
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board

    // A global variable for Assigned Members List.
    //Initialized in setupMembersList() func
    private lateinit var mAssignedMembersList:ArrayList<User>

    // A variable for notifying any changes done or not in the assigned members list.
    private var anyChangesDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        // Get the Board Details through intent and assign it to the global variable
        //We get it using its name which we passed through TaskListActivity
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            //<Board> It will be type Board
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        // Call the setup action bar function
        setupActionBar()

        // Get the members list details from the database
        // Show the progress dialog.
        //We pass in the assignedTo of Board, which is an ArrayList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )

    }


    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        val toolbar_members_activity = findViewById<Toolbar>(R.id.toolbar_members_activity)
        setSupportActionBar(toolbar_members_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            //We create the back button
            actionBar.setDisplayHomeAsUpEnabled(true)
            //we use this pic for the back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        //Go back when the back button is pressed
        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }


    /**
     * A function to setup assigned members list into recyclerview.
     */
    // Func is called inside getAssignedMembersListDetails() in FirestoreClass
    fun setupMembersList(list: ArrayList<User>) {
        val rv_members_list = findViewById<RecyclerView>(R.id.rv_members_list)

        // Initialize the Assigned Members List
        mAssignedMembersList = list

        hideProgressDialog()

        //Giving the RecyclerView the layoutManager
        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)

        //Creating an object of the adapter
        val adapter = MemberListItemsAdapter(this@MembersActivity, list)
        //Assigning it to the RecyclerView
        rv_members_list.adapter = adapter
    }


    // Inflate the menu file for adding the member
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        //menu_add_member We want to use menu_add_member as the menu
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            //When the action_add_member item from the menu is pressed
            R.id.action_add_member -> {

                //Call the dialogSearchMember function here
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Initialize the dialog for searching member from Database
    /**
     * Method is used to show the Custom Dialog.
     */
    private fun dialogSearchMember() {
        //Create a dialog
        val dialog = Dialog(this)
        /*Set the screen content from a layout resource.
    The resource will be inflated, adding all top-level views to the screen.*/
        //dialog_search_member We want to use this design for the dialog
        dialog.setContentView(R.layout.dialog_search_member)

        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener(View.OnClickListener {
            //When the add button is clicked on the dialog
            //Get the email the user has entered in the EditText
            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()
            //val email = dialog.et_email_search_member.text.toString()

            if (email.isNotEmpty()) {
                //If its not empty, close the dialog
                dialog.dismiss()

                // Show the progress dialog while we get the data from the database
                showProgressDialog(resources.getString(R.string.please_wait))
                //Call this func from the FirestoreClass which will get the user with that email if it exists
                FirestoreClass().getMemberDetails(this@MembersActivity, email)

            } else {
                //Otherwise, tell them they need to enter something
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener(View.OnClickListener {
            //When the cancel button is pressed, close it
            dialog.dismiss()
        })

        //Start the dialog and display it on screen.
        dialog.show()
    }


    // Here we will get the result of the member if it is found in the database
    // Func is called in the getMemberDetails() func in the FirestoreClass
    fun memberDetails(user: User) {

        //Here add the user id to the existing assigned members list of the board
        // Therefore, once we have found the member the user wants to add
        //Add their user ID to the assignedTo
        mBoardDetails.assignedTo.add(user.id)

        // Finally assign the member to the board.
        //Calls this function in the FirestoreClass which will update the database with the new member
        FirestoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
    }


    //Initialize the dialog for searching member from Database
    /**
     * A function to get the result of assigning the members.
     */
    fun memberAssignSuccess(user: User) {

        hideProgressDialog()

        //Add the user to the list
        //We update the list with the new member
        mAssignedMembersList.add(user)

        //Here the list is updated so change the global variable which we have declared for notifying changes
        anyChangesDone = true

        //Call the setupUp method which will create the list again with the updated version
        //It updates the UI with the new member
        setupMembersList(mAssignedMembersList)


        // Call the AsyncTask class when the board is assigned to the user and based on the users detail send them the notification using the FCM token
        //We pass in the users name and their token
        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }


    //When the back button is pressed
    // Send the result to the base activity onBackPressed
    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }



    // Create a AsyncTask class for sending the notification to user based on the FCM Token
    /**
     * “A nested class marked as inner can access the members of its outer class.
     * Inner classes carry a reference to an object of an outer class:”
     * source: https://kotlinlang.org/docs/reference/nested-classes.html
     *
     * This is the background class is used to execute background task.
     *
     * For Background we have used the AsyncTask
     *
     * Asynctask : Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {

        /**
         * This function is for the task which we wants to perform before background execution.
         * Here we have shown the progress dialog to user that UI is not freeze but executing something in background.
         */
        override fun onPreExecute() {
            super.onPreExecute()

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        /**
         * This function will be used to perform background execution.
         */
        override fun doInBackground(vararg params: Any): String {
            var result: String

            /**
             * https://developer.android.com/reference/java/net/HttpURLConnection
             *
             * You can use the above url for Detail understanding of HttpURLConnection class
             */
            var connection: HttpURLConnection? = null

            try {
                //Use the constant we have prepared to set the URL
                val url = URL(Constants.FCM_BASE_URL) // Base Url
                //Use our connection to open the URL
                connection = url.openConnection() as HttpURLConnection

                /**
                 * A URL connection can be used for input and/or output.  Set the DoOutput
                 * flag to true if you intend to use the URL connection for output,
                 * false if not.  The default is false.
                 */
                connection.doOutput = true
                connection.doInput = true

                /**
                 * Sets whether HTTP redirects should be automatically followed by this instance.
                 * The default value comes from followRedirects, which defaults to true.
                 */
                connection.instanceFollowRedirects = false

                /**
                 * Set the method for the URL request, one of:
                 *  POST
                 */
                //We want to use a POST request to send the data
                connection.requestMethod = "POST"

                /**
                 * Sets the general request property. If a property with the key already
                 * exists, overwrite its value with the new value.
                 */
                //Setting the request properties of the connection
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")


                // Add the firebase Server Key
                // In order to find your Server Key or authorization key, follow the below steps:
                // 1. Goto Firebase Console.
                // 2. Select your project.
                // 3. Firebase Project Setting
                // 4. Cloud Messaging
                // 5. Finally, the ServerKey.
                // For Detail understanding visit the link: https://android.jlelse.eu/android-push-notification-using-firebase-and-advanced-rest-client-3858daff2f50
                //We set the request property as FCM_AUTHORIZATION which is just "authorization"
                // For its key we send .FCM_KEY which is just "key", and CM_SERVER_KEY which is our actual key
                //Basically saying key = the actual key
                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )


                /**
                 * Some protocols do caching of documents.  Occasionally, it is important
                 * to be able to "tunnel through" and ignore the caches (e.g., the
                 * "reload" button in a browser).  If the UseCaches flag on a connection
                 * is true, the connection is allowed to use whatever caches it can.
                 *  If false, caches are to be ignored.
                 *  The default value comes from DefaultUseCaches, which defaults to
                 * true.
                 */
                connection.useCaches = false


                /**
                 * Creates a new data output stream to write data to the specified
                 * underlying output stream. The counter written is set to zero.
                 */
                //We want to use our connections output stream
                val wr = DataOutputStream(connection.outputStream)

                // TODO Create a notification data payload
                // Create JSONObject Request
                val jsonRequest = JSONObject()

                // Create a data object, which is also a JSON object
                val dataObject = JSONObject()

                // Here you can pass the title as per requirement as here we have added some text and board name.
                //The title of the notification, we pass in the boardName
                //We get the boardName from the inner class above as a parameter
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")

                // Here you can pass the message as per requirement as here we have added some text and appended the name of the Board Admin.
                //The main message, we pass in the name of the user who added you to the card - the creator of the card (who is the first person in the list)
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mAssignedMembersList[0].name}"
                )

                // Here add the data object and the user's token in the jsonRequest object.
                //Add the data object that we've just prepared to the jsonRequest object we created above using v as the key
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                //Also put the token into it using FCM_KEY_TO as the key
                jsonRequest.put(Constants.FCM_KEY_TO, token)


                /**
                 * Writes out the string to the underlying output stream as a
                 * sequence of bytes. Each character in the string is written out, in
                 * sequence, by discarding its high eight bits. If no exception is
                 * thrown, the counter written is incremented by the
                 * length of s.
                 */
                wr.writeBytes(jsonRequest.toString())

                wr.flush() // Flushes this data output stream.
                wr.close() // Closes this output stream and releases any system resources associated with the stream

                //Get the http result code
                val httpResult: Int =
                    connection.responseCode // Gets the status code from an HTTP response message.

                //If its equal to the HTTP_Ok (Which is 200)
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    //If we got a result

                    /**
                     * Returns an input stream that reads from this open connection.
                     */
                    val inputStream = connection.inputStream

                    /**
                     * Creates a buffering character-input stream that uses a default-sized input buffer.
                     */
                    //Create a buffer reader which will read the inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    //Creating a stringBuilder
                    val sb = StringBuilder()
                    var line: String?

                    try {
                        /**
                         * Reads a line of text.  A line is considered to be terminated by any one
                         * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
                         * followed immediately by a linefeed.
                         */
                        //While there is a line to read,
                        while (reader.readLine().also { line = it } != null) {
                            //Add it to the StringBuilder
                            sb.append(line + "\n")
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            /**
                             * Closes this input stream and releases any system resources associated
                             * with the stream.
                             */
                            //Close the inputStream
                            inputStream.close()

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    //Set our result variable to the stringBuilder we created
                    result = sb.toString()

                } else {
                    //If we didn't get a result
                    /**
                     * Gets the HTTP response message, if any, returned along with the
                     * response code from a server.
                     */
                    //Set it to the response message
                    result = connection.responseMessage
                }


            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                //Check the connection, if it exists, disconnect it
                connection?.disconnect()
            }

            // You can notify with your result to onPostExecute.
            return result
        }

        /**
         * This function will be executed after the background execution is completed.
         */
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            hideProgressDialog()

            // JSON result is printed in the log.
            Log.e("JSON Response Result", result)
        }

    }


}