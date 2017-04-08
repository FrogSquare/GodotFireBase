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
import android.support.annotation.NonNull;;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import org.json.JSONObject;
import org.json.JSONException;

public class Authentication {

	public static Authentication getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Authentication(p_activity);
		}

		return mInstance;
	}

	public Authentication (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;
	}

	private boolean isInitialized () {
		if (mFirebaseApp == null) {
			Log.d(TAG, "Authentication is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Activity activity = null;
	private static Context context = null;
	private static Authentication mInstance = null;

	private FirebaseApp mFirebaseApp = null;

	private static final String TAG = "FireBase";
}
