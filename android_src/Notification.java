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
import android.util.Log;

import androidx.core.app.NotificationCompat;

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
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;

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
        dispatcher.cancelAll();

        /**
		cancel_notification(activity.getString(R.string.godot_firebase_default_tag));

        if (!Utils.get_db_value("shedule_tag").equals("0")) {
            cancel_notification(Utils.get_db_value("shedule_tag"));
            Utils.set_db_value("shedule_tag", "0");
        }
        **/

		Utils.d("GodotFireBase", "Firebase Cloud messaging token: " + token);

		// Perform task here..!
		if (Utils.get_db_value("notification_complete_task") != "0") {
			try {

		        Dictionary data = new Dictionary();
                if (!Utils.get_db_value("notification_task_data").equals("0")) {

    				JSONObject obj =
	    			new JSONObject(Utils.get_db_value("notification_task_data"));

			    	Iterator<String> iterator = obj.keys();
     				while (iterator.hasNext()) {
	       	    		String key = iterator.next();
		    			Object value = obj.opt(key);

				    	if (value != null) {
					    	data.put(key, value);
    					}
	    			}
                }

				Utils.callScriptCallback(
			    Utils.get_db_value("notification_complete_task"), "Notification", "TaskComplete", data);

			} catch (JSONException e) {

			}

			KeyValueStorage.setValue("notification_complete_task", "0");
			KeyValueStorage.setValue("notification_task_data", "0");
		}
	}

    public void cancel_notification(final String tag) {
        dispatcher.cancel(tag);
    }

	public void subscribe(final String topic) {
		if (!isInitialized()) { return; }

		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

    public Bundle get_bundle(Dictionary data) {
        JSONObject dict = new JSONObject(data);

		Bundle bundle = new Bundle();
		bundle.putString("message", (String) data.get("message"));
		bundle.putString("image_uri", (String) data.get("image_uri"));

		bundle.putString("type", dict.optString("type", "text"));
        bundle.putString("title", 
                dict.optString("title", activity.getString(R.string.godot_project_name_string)));
        bundle.putString("tag", 
                dict.optString("tag", activity.getString(R.string.godot_firebase_default_tag)));

        return bundle;
    }

    public void shedule(Dictionary data, int seconds) {
		Bundle bundle = get_bundle(data);
		Utils.d("GodotFireBase", 
        "Setting new Notification: " + bundle.toString() + ", Seconds: " + String.valueOf(seconds));

		Job myJob = dispatcher.newJobBuilder()
		.setService(NotifyInTime.class)	// the JobService that will be called
		.setTag(bundle.getString("tag")) // uniquely identifies the job
        .setRecurring(true)
        .setLifetime(Lifetime.FOREVER)
		.setTrigger(Trigger.executionWindow(seconds, seconds+60))
		.setReplaceCurrent(true)
        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
		.setExtras(bundle)
		.build();

		dispatcher.mustSchedule(myJob);
    }

	public void notifyOnComplete(Dictionary data, int seconds) {
        if (data.get("image_uri") == null && data.get("type") == "image") {
            Utils.d("GodotFireBase", 
            "Notification: using Image in content need `image_uri` (i.e, \"res://image.png\")");

            return;
        }

        dispatch_single_job(get_bundle(data), seconds);
	}

	public void notifyInSecs(final String message, final int seconds) {
		Bundle bundle = new Bundle();
		bundle.putString("tag", activity.getString(R.string.godot_firebase_default_tag));
		bundle.putString("title", activity.getString(R.string.godot_project_name_string));
		bundle.putString("message", message);
		bundle.putString("type", "text");

        dispatch_single_job(bundle, seconds);
	}

    private void dispatch_single_job(Bundle bundle, int seconds) {
		Utils.d("GodotFireBase", 
        "Setting new Notification: " + bundle.toString() + ", Seconds: " + String.valueOf(seconds));

		Job myJob = dispatcher.newJobBuilder()
		.setService(NotifyInTime.class)	// the JobService that will be called
		.setTag(bundle.getString("tag")) // uniquely identifies the job
        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
		.setTrigger(Trigger.executionWindow(seconds, seconds+60))
		.setReplaceCurrent(true)
        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
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
			Utils.d("GodotFireBase", "Notification is not initialized.");
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
