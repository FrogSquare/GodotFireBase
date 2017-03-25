
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

		Log.d(TAG, "Firebase Cloud messaging token: " + token);
	}

	public void subscribe(final String topic) {
		if (!isInitialized()) { return; }

		FirebaseMessaging.getInstance().subscribeToTopic(topic);
	}

	public void notifyInMins (final String message, final int mins) {
		int seconds = mins * 60;

		Log.d(TAG, "Setting new Job with message: " + message);

		Bundle bundle = new Bundle();
		bundle.putString("message", message);

		Job myJob = dispatcher.newJobBuilder()
		.setService(NotifyInTime.class)	// the JobService that will be called
		.setTrigger(Trigger.executionWindow(seconds, seconds+60))
		.setTag("firebase-notify-in-time-UID") // uniquely identifies the job
		.setReplaceCurrent(true)
		.setExtras(bundle)
		.build();

		dispatcher.mustSchedule(myJob);
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
			Log.d(TAG, "Notification is not initialized.");
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

	private static final String TAG = "FireBase";
}
