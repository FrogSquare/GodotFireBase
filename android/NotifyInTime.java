/**
 * Copyright 2017 FrogLogics. All Rights Reserved.
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
