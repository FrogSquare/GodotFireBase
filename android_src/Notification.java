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

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import com.godot.game.R;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Notification {

	public static Notification getInstance (Activity p_activity) {
		if (mInstance == null) {
			synchronized (Notification.class) {
				mInstance = new Notification(p_activity);
			}
		}

		return mInstance;
	}

	public Notification (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
		String token = getFirebaseMessagingToken();

		dispatcher =
		new FirebaseJobDispatcher(new GooglePlayDriver(activity.getApplicationContext()));
		dispatcher.cancel("firebase-notify-in-time-UID");

		Utils.d("Firebase Cloud messaging token: " + token);

		// Perform task here..!
		if (KeyValueStorage.getValue("notification_complete_task") != "0") {
			try {
				JSONObject obj =
				new JSONObject(KeyValueStorage.getValue("notification_task_data"));

				Dictionary data = new Dictionary();
				Iterator<String> iterator = obj.keys();

				while (iterator.hasNext()) {
					String key = iterator.next();
					Object value = obj.opt(key);

					if (value != null) {
						data.put(key, value);
					}
				}

				Utils.callScriptCallback(
				KeyValueStorage.getValue("notification_complete_task"),
				"Notification", "TaskComplete", data);

			} catch (JSONException e) {

			}

			KeyValueStorage.setValue("notification_complete_task", "0");
		}
	}

	public void subscribe(final String topic) {
		if (!isInitialized()) { return; }

		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

	public void notifyOnComplete(Dictionary data) {
		Utils.d("Setting new Job with message: " + data.toString());

		Bundle bundle = new Bundle();
		bundle.putString("message", (String) data.get("message"));

        String title = (String) data.get("title");
        if (title == null) {
            title = activity.getString(R.string.godot_project_name_string);
        }

		bundle.putString("title", title);

		bundle.putString("image_uri", (String) data.get("image")); // Image uri of the 
		bundle.putString("type", (String) data.get("type")); // text or image

		int seconds = (int) data.get("secs");

		Job myJob = dispatcher.newJobBuilder()
		.setService(NotifyInTime.class)	// the JobService that will be called
		.setTrigger(Trigger.executionWindow(seconds, seconds+60))
		.setTag("firebase-notify-in-time-UID") // uniquely identifies the job
		.setReplaceCurrent(true)
		.setExtras(bundle)
		.build();

		dispatcher.mustSchedule(myJob);
	}

	public void notifyInSecs(final String message, final int seconds) {
		Utils.d("Setting new Job with message: " + message);

		Bundle bundle = new Bundle();
		bundle.putString("title", activity.getString(R.string.godot_project_name_string));
		bundle.putString("message", message);
		bundle.putString("type", "text");

		Job myJob = dispatcher.newJobBuilder()
		.setService(NotifyInTime.class)	// the JobService that will be called
		.setTrigger(Trigger.executionWindow(seconds, seconds+60))
		.setTag("firebase-notify-in-time-UID") // uniquely identifies the job
		.setReplaceCurrent(true)
		.setExtras(bundle)
		.build();

		dispatcher.mustSchedule(myJob);
	}

	public void notifyInMins (final String message, final int mins) {
		int seconds = mins * 60;
		notifyInSecs(message, seconds);
	}

	public void sendMessage (final String data) {
		FirebaseMessaging fm = FirebaseMessaging.getInstance();

		String token = FirebaseInstanceId.getInstance().getToken();
		String msgID = DigestUtils.sha1Hex(token + System.currentTimeMillis());
		String SENDER_ID = "someID";

		RemoteMessage.Builder RMBuilder =
		new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com");
		RMBuilder.setMessageId(msgID);

		Map<String, Object> mapData = Utils.jsonToMap(data);

		for (Map.Entry<String, Object> entry : mapData.entrySet()) {
			RMBuilder.addData(entry.getKey(), entry.getValue().toString());
		}

		fm.send(RMBuilder.build());
	}

	public String getFirebaseMessagingToken() {
		if (!isInitialized()) { return "NULL"; }

		return FirebaseInstanceId.getInstance().getToken();
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Utils.d("Notification is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Context context;
	private static Activity activity;

	private FirebaseApp mFirebaseApp = null;
	private FirebaseJobDispatcher dispatcher = null;

	private static Notification mInstance = null;
}
