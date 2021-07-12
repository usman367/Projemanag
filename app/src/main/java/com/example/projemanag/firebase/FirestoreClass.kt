package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

//Create a class where we will add the operation performed for the firestore database.
//This stores the data into the database whereas the SignUpActivity stores it in the Authentication
class FirestoreClass {

    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()


    // Create a function to make an entry of the registered user in the firestore database.
    //Func is called in the SignUpActivity in the registerUser() method
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        //Creating a new collection (We pass in its name, which we have stored in constants)
        mFireStore.collection(Constants.USERS)

            // Creating a document ID for users fields. Here the document it is the User ID (which we get from the func below)
            .document(getCurrentUserID())

            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
                //userInfo is the parameter of type User, passed to the func above
            .set(userInfo, SetOptions.merge())

                //To check if it worked successfully
            .addOnSuccessListener {
                // Here call a function of SignUpActivity activity for transferring the result to it.
                //We call the userRegisteredSuccess() func from SignUpActivity
                activity.userRegisteredSuccess()
            }

                //If it doesn't work, log an error
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }


    // Create a function to SignIn using firebase and get the user details from Firestore Database.
    //Func is called in the Firestore Class in SignInUser()
    //Added a parameter to check whether to read the boards list or not (Set to false at the start so we don't have to pass it to everything)
    fun loadUserData(activity: Activity, isToReadBoardsList: Boolean = false ) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)

            // The document id to get the Fields of user.
            .document(getCurrentUserID())

                //We get it instead of setting it like we did in registerUser() above
            .get()

                //For when it has worked successfully
            .addOnSuccessListener { document ->

                // Pass the result to base activity.
                // Here we have received the document (which we create above) snapshot which is converted into the User Data model object.
                //We want to create an object of the User class
                val loggedInUser = document.toObject(User::class.java)!!

                // Modify the parameter and check the instance of activity and send the success result to it
                // Here call a function of base activity for transferring the result to it
                when(activity) {
                    //If the SignInActivity is passed in
                    is SignInActivity -> {
                        // Then here call a function of SignInActivity activity for transferring the result to it.
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        //If its the main activity then call its updateNavigationUserDetails()
                        activity.updateNavigationUserDetails(loggedInUser, isToReadBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }

                }

                Log.e(
                    activity.javaClass.simpleName, document.toString()
                )

            }
                //If it doesn't work, log an error
            .addOnFailureListener { e ->


                // Hide the progress dialog in failure function based on instance of activity.
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }


    // A function to update the user profile data into the database
    //In Java you can pass in Object for the HashMap parameter type, in Kotlin you use Any
    //Func is called inside the updateUserProfileData() func in the MyProfileActivity
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFireStore.collection(Constants.USERS) // Collection Name
                .document(getCurrentUserID()) // Document ID
                .update(userHashMap) // A HashMap of fields which are to be updated.

                .addOnSuccessListener {
                    // Profile data is updated successfully.
                    Log.e(activity.javaClass.simpleName, "Profile Data updated successfully!")

                    Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                    when (activity) {
                        //Added for vid no. 282, Adding the notifications part
                        is MainActivity -> {
                            activity.tokenUpdateSuccess()
                        }
                        is MyProfileActivity -> {
                            // Notify the success result.
                            //Call the profileUpdateSuccess() func from the MyProfileActivity
                            activity.profileUpdateSuccess()
                        }
                    }

                }

                .addOnFailureListener { e ->

                    when (activity) {
                        is MainActivity -> {
                            //If it fails, hide the progress dialog
                            activity.hideProgressDialog()
                        }
                        is MyProfileActivity -> {
                            activity.hideProgressDialog()
                        }
                    }

                    //Log the error
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating a board.",
                            e
                    )
                }
    }


    // A function for creating a board and making an entry in the database
    fun createBoard(activity: CreateBoardActivity, board: Board) {

        mFireStore.collection(Constants.BOARDS) //Create its collection (We get its name from Constants)
            .document() //Adding a document
            .set(board, SetOptions.merge()) //Merge it with the board data (board is passed in a as a parameter to our function)

            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully.")

                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()
                //Call this function from the CreateBoardActivity (Which hides the progress dialog, and finishes the activity
                activity.boardCreatedSuccessfully()
            }

            .addOnFailureListener { e ->
                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }


    // A function to get the list of created boards from the database
    fun getBoardsList(activity: MainActivity) {

        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            // We're checking where the value for assignedTo is equal to the current user ID (On the Firebase database)
            // We use the Constant we created called ASSIGNED_TO, and the fun getCurrentUserID() (Which we created below)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get() // Will get the documents snapshots.

            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                // Here we have created a new instance for Boards ArrayList (Rn its empty)
                val boardsList: ArrayList<Board> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                //Go through the documents
                for (i in document.documents) {
                    //We create the board
                    val board = i.toObject(Board::class.java)!!

                    //Get the documentID and add it to the board
                    board.documentId = i.id

                    //Add it to the ArrayList
                    boardsList.add(board)
                }

                // Here pass the result to the func in the MainActivity, which will add the boards to the RecyclerView
                activity.populateBoardsListToUI(boardsList)
            }

            .addOnFailureListener { e ->
                //When it does work, log an error
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    /**
     * A function to get the Board Details.
     */
    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId) //We pass in the documentID we want to get
            .get()

            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                //We convert the document into an Object, which should be of the type Board
                val board = document.toObject(Board::class.java)!!
                // Assign the board document id to the Board Detail object
                board.documentId = document.id

                // Send the result of board to the base activity.
                activity.boardDetails(board)

                // Send the result to the boardDetails() function in TaskListActivity.
                //We convert the document into an Object, which should be of the type Board
                //activity.boardDetails(document.toObject(Board::class.java)!!)
            }

            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    /**
     * A function to create a task list in the board detail.
     */
    fun addUpdateTaskList(activity: Activity, board: Board) {
        //A HashMap
        val taskListHashMap = HashMap<String, Any>()
        //We want to store the ArrayList which contains the tasks
        //We use the variable from constants as the key
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId) //We want to get the board withe the ID we pass in
                .update(taskListHashMap) //We want to update the boards TaskList

                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                    if (activity is TaskListActivity) {
                        //Call TaskListActivity addUpdateTaskListSuccess() func
                        activity.addUpdateTaskListSuccess()

                    } else if (activity is CardDetailsActivity2) {
                        activity.addUpdateTaskListSuccess()
                    }

                }

                .addOnFailureListener { e ->

                    if (activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    } else if (activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    }

                    Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                }
    }


    /**
     * A function to get the list of user details which is assigned to the board.
     */
    //We want to get the members thar are in assignedTo
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {

        mFireStore.collection(Constants.USERS) // Collection Name
                //We want the users where the ID is equal to assignedTo in the firebase database
                // (each user has an id in the database, and we want to get the assigned to from it)
            .whereIn(Constants.ID, assignedTo) // Here the database field name and the id's of the members.
            .get()

            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                //Creating an arrayList of type User
                val usersList: ArrayList<User> = ArrayList()

                //For every entry in the documents
                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    //Add them to the ArrayList
                    usersList.add(user)
                }

                if(activity is MembersActivity) {
                    //Call this func from MembersActivity, which creates the list of members in the RecyclerView
                    activity.setupMembersList(usersList)
                }else if(activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }

            }

            .addOnFailureListener { e ->

                if(activity is MembersActivity) {
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity){
                    activity.hideProgressDialog()
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }



    /**
     * A function to get the user details from Firestore Database using the email address.
     */
    // Func is called in dialogSearchMember() in the MembersActivity
    // (Once the user wants to add a member, this func is called)
    fun getMemberDetails(activity: MembersActivity, email: String) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
                //We want to get the email
            .whereEqualTo(Constants.EMAIL, email)
            .get()

            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                //If there are any entries in the database with that email
                if (document.documents.size > 0) {
                    //We want to convert the document with that email into an object of type User
                    // (So we can add it show it on the membersActivity later)
                    //documents[0] Because there will only be one account with that email
                    val user = document.documents[0].toObject(User::class.java)!!
                    // Here call a function of MembersActivity for transferring the result to it.
                    activity.memberDetails(user)

                } else {
                    //Otherwise, hide the dialog and show an error
                    activity.hideProgressDialog()
                    //This func is in the BaseActivity
                    activity.showErrorSnackBar("No such member found.")
                }

            }

            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    /**
     * A function to assign a updated members list to board, after the user has changed the members email address
     */
    //Updates the members list in the database when the user has added a member to the list
    // Func is called in memberDetails() func in MembersActivity
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        //Creating a HashMap for making updates
        val assignedToHashMap = HashMap<String, Any>()

        //We want to use the constant ASSIGNED_TO as a key
        //We want to to add the value of assignedTo to it
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId) //We want to update the board with this id
            .update(assignedToHashMap) //Using the HasMap above (It will override the assignedTo in the database, because that's what we use as the key)

            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                //Call the memberAssignSuccess from the MembersActivity, which will update the UI with the new member
                activity.memberAssignSuccess(user)
            }

            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        //Return the user id if he is already logged in before or else it will be blank.

        // Gets an Instance of the currentUser using FirebaseAuth
        //If it returns null then there is no current user ( they haven't signed in)
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""

        if (currentUser != null) {
            //Assign it the ID
            currentUserID = currentUser.uid
        }

        return currentUserID

    }

}