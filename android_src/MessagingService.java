/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
		Utils.d("Message From: " + remoteMessage.getFrom());
		Utils.d("Message From: " + remoteMessage.toString());

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Map<String, String> data = remoteMessage.getData();

			Utils.d("Message data payload: " + data.toString());

			KeyValueStorage.set_context(this);
			handleData(data);
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Utils.d(
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
		} catch (JSONException e) { Utils.d("JSONException: parsing, " + e.toString()); }

		if (jobject.length() > 0) {
			KeyValueStorage.setValue("firebase_notification_data", jobject.toString());
		}
	}

	public static void sendNotification(Bundle bundle, Context context) {
		Intent intent = new Intent(context, org.godotengine.godot.Godot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(
		context, Utils.FIREBASE_NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri =
		RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_stat_ic_notification)
		.setContentTitle(bundle.getString("title"))
		.setStyle(new NotificationCompat.BigPictureStyle()
				.bigPicture(Utils.getBitmapFromAsset(context, bundle.getString("image"))))
		.setAutoCancel(true)
		.setSound(defaultSoundUri)
		.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
		(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(7002, nBuilder.build());
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
}
