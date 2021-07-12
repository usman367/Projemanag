package com.example.projemanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import java.io.IOException

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener  {

    // a global variable for user name
    private lateinit var mUserName: String

    // A global variable for SharedPreferences
    //For the notifications part, vid no. 282
    private lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Call the setup action bar function here
        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        val nav_view = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
        nav_view.setNavigationItemSelectedListener(this)

        //  Call a function to get the current logged in user details
        FirestoreClass().loadUserData(this@MainActivity, true)

        // Launch the Create Board screen on a fab button click
        val fab_create_board = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_create_board)
        fab_create_board.setOnClickListener {
            // Pass the user name through intent to CreateBoardScreen
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            // Here now pass the unique code for StartActivityForResult
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)

            //startActivity(Intent(this@MainActivity, CreateBoardActivity::class.java)) //We don't need this anymore
        }


        // Initialize the mSharedPreferences variable
        mSharedPreferences = this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)


        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        // Here if the token is already updated than we don't need to update it every time.
        if (tokenUpdated) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            // Get the current logged in user details.
            FirestoreClass().loadUserData(this@MainActivity, true)
        } else {
            //Otherwise, get the updated token
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) {
                    instanceIdResult ->
                //We call the updateFCMToken() from below to update the token
                    updateFCMToken(instanceIdResult.token)
                }
        }

    }

    //A function to setup action bar
    private fun setupActionBar() {
        val toolbar_main_activity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar_main_activity)

        //We want to use the menu icon as our icon
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        //Add click event for the menu we created above
        toolbar_main_activity.setNavigationOnClickListener {
            //This allows us to close and open the drawer
            toggleDrawer()
        }

    }

    // A function for opening and closing the Navigation Drawer
    private fun toggleDrawer() {
        //Gets the drawer_layout from activity_main
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)

        //If its open, the close it
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            //Otherwise open it
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    //Add a onBackPressed function, which closes the drawer when the back button is pressed
    override fun onBackPressed() {
        //Gets the drawer_layout from activity_main
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is called from Base Activity.
            doubleBackToExit()
        }
    }


    // Implement members of NavigationView.OnNavigationItemSelectedListener
    //For when any of the items in the menu is pressed
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        //Gets the drawer_layout from activity_main
        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)

        //Add the click events of navigation menu items
        when (item.itemId) {
            //When the item pressed has the ID nav_my_profile
            R.id.nav_my_profile -> {
                //Take them to the profile activity
                //We pass in the constant MY_PROFILE_REQUEST_CODE so we can update the profile in the menu
                // once the user has updated their details it in the MyProfile Activity
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java)
                        , MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                //When the sign-out item is pressed

                // Here sign outs the user from firebase in this device.
                FirebaseAuth.getInstance().signOut()

                //  Clear the shared preferences when the user signOut
                //For the notifications part
                mSharedPreferences.edit().clear().apply()

                // Send the user to the intro screen of the application.
                val intent = Intent(this, IntroActivity::class.java)
                //Adding a flag to the intent which closes all the other activities
                // (Hold Ctrl + click on the flags to view more info about them)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                //Close this activity, so when back is pressed it doesn't open
                finish()
            }
        }
        //Close the drawer
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }


    // A function to update the user details in the navigation view
    //We use a library called Glide to get the data from firebase
    // https://github.com/bumptech/glide
    //Added a parameter to check whether to read the boards list or not
    fun updateNavigationUserDetails(user: User, isToReadBoardsList: Boolean) {
        hideProgressDialog()

        //Initialize the UserName variable
        mUserName = user.name

        val nav_view = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)

        // The instance of the header view of the navigation view.
        val headerView = nav_view.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this@MainActivity)//We want to use it in this activity
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder (f the image we want is not loaded, use this)
            .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name to the username we go
        navUsername.text = user.name


        // Here if the isToReadBoardList is TRUE then get the list of boards
        if (isToReadBoardsList) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }

    }


    // Add the onActivityResult function and check the result of the activity for which we expect the result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //We pass in MY_PROFILE_REQUEST_CODE in the onNavigationItemSelected() func
        if (resultCode == Activity.RESULT_OK
                && requestCode == MY_PROFILE_REQUEST_CODE) {
            // Get the user updated details.
            FirestoreClass().loadUserData(this@MainActivity)

        }else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE) {
            //We set it to OK in the CreateBoardActivity in boardCreatedSuccessfully() func
            //And we pass in the request code when the fab_create_board button is clicked above, through the intent
            // Get the latest boards list.
            FirestoreClass().getBoardsList(this@MainActivity)

        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }


    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        val rv_boards_list = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_boards_list)
        val tv_no_boards_available = findViewById<TextView>(R.id.tv_no_boards_available)

        hideProgressDialog()

        if (boardsList.size > 0) {
            //If there are items in the ArrayList

            rv_boards_list.visibility = View.VISIBLE //We make the recyclerView Visible
            tv_no_boards_available.visibility = View.GONE //We make the TextView Gone

            //We want to use a linear layout manager for the RecyclerView
            rv_boards_list.layoutManager = LinearLayoutManager(this@MainActivity)
            //So our boards have a fixed size
            rv_boards_list.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)

            // Attach the adapter to the recyclerView
            rv_boards_list.adapter = adapter

            // Add click event for boards item and launch the TaskListActivity
            //We implement the OnClickListener interface from the BoardItemsAdapter
            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                //We override the onClick() func from interface
                override fun onClick(position: Int, model: Board) {
                    //When one of the adapter elements is clicked, start the TaskListActivity
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    // Pass the documentId (which we get from constants) of a board through intent
                    //We'll get the DocumentID in TaskListActivity
                    //We'll use it to load the document in the TaskListActivity
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            //If the list is empty, don't show the RecyclerView
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }


    //For the notifications part
    /**
     * A function to notify the token is updated successfully in the database.
     */
    fun tokenUpdateSuccess() {

        hideProgressDialog()

        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.

        //Creating an editor of our SharedPreferences
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        //Put the token into it
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        // The change will then be applied to our SharedPreferences
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        //Call this func from FirestoreClass to get the current data
        FirestoreClass().loadUserData(this@MainActivity, true)
    }

    // A function to update the user's FCM token into the database
    private fun updateFCMToken(token: String) {
        //Create a HashMap to update the database
        val userHashMap = HashMap<String, Any>()
        //Add the token to it using our the constant we created as the key
        userHashMap[Constants.FCM_TOKEN] = token

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }


    //A companion object and a constant variable for My profile Screen result.)
    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        // A unique code for starting the create board activity for result
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }



}