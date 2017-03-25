
package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.support.annotation.NonNull;;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class NotifyInTime extends JobService {

	@Override
	public boolean onStartJob(JobParameters job) {
		// Do some work here
		Log.d(TAG, "Job Started.");

		Bundle bundle = job.getExtras();
		Log.d(TAG, "Message: " + bundle.getString("message"));

		MessagingService.sendNotification(bundle.getString("message"), this);
		return true; // Answers the question: "Is there still work going on?"
	}

	@Override
	public boolean onStopJob(JobParameters job) {
		Log.d(TAG, "Job Stopped.");

		return true; // Answers the question: "Should this job be retried?"
	}

	private static final String TAG = "FireBase";
}
