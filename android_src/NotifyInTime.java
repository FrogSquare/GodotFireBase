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
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class NotifyInTime extends JobService {

	@Override
	public boolean onStartJob(JobParameters job) {
		// Do some work here
		Utils.d("GodotFireBase", "Job Started.");

		Bundle bundle = job.getExtras();
		Utils.d("GodotFireBase", "Message: " + bundle.getString("message"));

        if (bundle.getString("type").equals("image")) {
    		MessagingService.sendNotification(bundle, this);
        } else {
    		MessagingService.sendNotification(bundle.getString("message"), this);
        }

		return false; // Answers the question: "Is there still work going on?"
	}

	@Override
	public boolean onStopJob(JobParameters job) {
		Utils.d("GodotFireBase", "Job Stopped.");

		return false; // Answers the question: "Should this job be retried?"
	}
}
