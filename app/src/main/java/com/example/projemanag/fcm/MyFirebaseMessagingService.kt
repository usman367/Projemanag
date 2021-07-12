package com.example.projemanag.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projemanag.R
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.SignInActivity
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//  Add the firebase Messaging Service class
// Here this class is provide on the firebase github repository: https://github.com/firebase/quickstart-android/tree/master/messaging
// We will change the remaining things later on as per requirement.
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Checking where the message comes from
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        //If the messages data is not empty
        remoteMessage.data.isNotEmpty().let {
            // The notification data payload is printed in the log.
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // The Title and Message are assigned to the local variables
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

            // Finally sent them to build a notification.
            sendNotification(title, message)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }



    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        //Logging the new token
        Log.e(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //Send the registration to the server with the new token
        sendRegistrationToServer(token)
    }



    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // Implement this method to send token to your app server.
        //Log.e(TAG, "Token : $token")

//        val sharedPreferences =
//            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)
//        val editor: SharedPreferences.Editor = sharedPreferences.edit()
//        editor.putString(Constants.FCM_TOKEN, token)
//        editor.apply()
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message FCM message body received.
     */
    //Function used to send a notification to the user
    //Called in onMessageReceived() above
    private fun sendNotification(title: String, message: String) {
        //  Now once the notification is received and visible in the notification tray than we can navigate them into the app as per requirement
        // As here we will navigate them to the main screen if user is already logged in or to the login screen.
        val intent: Intent = if (FirestoreClass().getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            //Ig they're not signed-in then send them to the sign-in activity
            Intent(this, SignInActivity::class.java)
        }

        // Before launching the screen add some flags to avoid duplication of activities.
        //We use flag to make sure a specific activity is set to a specific position inside the stack of activities
        //This will make sure the activities are not overlapping each other, i.e. the same activity is not open more than once
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TOP)



        //For when the user is in another application and they click on the token, we use this
        //As we cannot just send them from another application to the main activity
        //FLAG_ONE_SHOT Means this activity or intent should only be used once
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        //Get the string from strings
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        //We want to use the users NOTIFICATION sound for the notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        //Create the actual notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification) //The icon
            .setContentTitle(title) // The title of the notification, from the parameter
            .setContentText(message) //The main message, from the parameter
            .setAutoCancel(true) //Cancels the notification when the user clicks on it
            .setSound(defaultSoundUri) //The sound we want to use for it
            .setContentIntent(pendingIntent) //If the user clicks on it use the pendingIntent, which opens the MainActivity

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel Projemanag title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        //Use the notificationManager we created to create the notification
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        //Creating our own TAG
        private const val TAG = "MyFirebaseMsgService"
    }

}