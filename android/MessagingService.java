
package org.godotengine.godot;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.godot.game.R;

public class MessagingService extends FirebaseMessagingService {

	private static int NOTIFICATION_REQUEST_ID	= 9003;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Log.d(TAG, "on Message received");

		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG,
			"Message Notification Body: " + remoteMessage.getNotification().getBody());

			sendNotification(remoteMessage.getNotification().getBody());
		}

	}

	public void sendNotification(String messageBody) {
		Intent intent = new Intent(this, org.godotengine.godot.Godot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(
		this, NOTIFICATION_REQUEST_ID, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_stat_ic_notification)
		.setContentTitle("FCM Message")
		.setContentText(messageBody)
		.setAutoCancel(true)
		.setSound(defaultSoundUri)
		.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
		(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}

	private static String TAG = "FireBase";
}
