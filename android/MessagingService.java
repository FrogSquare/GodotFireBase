
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

import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.KeyValueStorage;

public class MessagingService extends FirebaseMessagingService {

	private static int NOTIFICATION_REQUEST_ID	= 9003;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Log.d(TAG, "Message From: " + remoteMessage.getFrom());
		Log.d(TAG, "Message From: " + remoteMessage.toString());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Map<String, String> data = remoteMessage.getData();

			Log.d(TAG, "Message data payload: " + data.toString());

			KeyValueStorage.set_context(this);
			handleData(data);
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG,
			"Notification Body: " + remoteMessage.getNotification().getBody());

			sendNotification(remoteMessage.getNotification().getBody(), this);
		}
	}

	private void handleData (Map<String, String> data) {
		// TODO: Perform some action now..!
		// ...

		JSONObject jobject = new JSONObject();

		try {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				jobject.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) { Log.d(TAG, "JSONException: parsing, " + e.toString()); }

		if (jobject.length() > 0) {
			KeyValueStorage.setValue("firebase_notification_data", jobject.toString());
		}
	}

	public static void sendNotification(String messageBody, Context context) {
		Intent intent = new Intent(context, org.godotengine.godot.Godot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(
		context, Utils.FIREBASE_NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri =
		RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_stat_ic_notification)
		.setContentTitle("FCM Message")
		.setContentText(messageBody)
		.setAutoCancel(true)
		.setSound(defaultSoundUri)
		.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
		(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(7002, nBuilder.build());
	}

	private static final String TAG = "FireBase";
}
