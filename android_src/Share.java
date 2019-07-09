/**
 * Copyright 2019 BunbySoft. All Rights Reserved.
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
import android.content.Intent;
import com.google.firebase.FirebaseApp;

public class Share {	
	public static Share getInstance(Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Share(p_activity);
		}
		return mInstance;
	}
	
	public Share (Activity p_activity) {
		activity = p_activity;
	}
	
	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}
	
	public void share(String text, String subject) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		if (subject != null && !subject.isEmpty()) {
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		sendIntent.setType("text/plain");
		activity.startActivity(sendIntent);
	}

	private static Activity activity = null;
	private static Share mInstance = null;

	private FirebaseApp mFirebaseApp = null;
}
