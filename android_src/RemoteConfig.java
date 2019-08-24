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
import androidx.annotation.NonNull;;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.godot.game.BuildConfig;
import com.godot.game.R;

import java.util.Map;

public class RemoteConfig {

	public static RemoteConfig getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new RemoteConfig(p_activity);
		}

		return mInstance;
	}

	public RemoteConfig (Activity p_activity) {
		activity = p_activity;
	}

	public void init (FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;

		mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
		FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
		.setDeveloperModeEnabled(BuildConfig.DEBUG)
		.build();

		mFirebaseRemoteConfig.setConfigSettings(configSettings);
		mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

		fetchRemoteConfigs();
	}

	private void fetchRemoteConfigs () {
		Utils.d("GodotFireBase", "Loading Remote Configs");

		long cacheExpiration = 3600;

		if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
			cacheExpiration = 0;
		}

		mFirebaseRemoteConfig.fetch(cacheExpiration)
		.addOnCompleteListener(activity, new OnCompleteListener<Void>() {

			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Utils.d("GodotFireBase", "RemoteConfig, Fetch Successed");

					mFirebaseRemoteConfig.activateFetched();
				} else {
					Utils.d("GodotFireBase", "RemoteConfig, Fetch Failed");
				}

			// Utils.d("GodotFireBase", "Fetched Value: " + getValue("firebase_remoteconfig_test"));
			}
		});
	}

	public void setDefaultsFile (final String filePath) {
		if (!isInitialized()) { return; }

		Utils.d("GodotFireBase", "Loading Defaults from file:" + filePath);

		String data = Utils.readFromFile(filePath, activity.getApplicationContext());
		data = data.replaceAll("\\s+", "");

		setDefaults (data);
	}

	public void setDefaults(final String defaults) {
		if (!isInitialized()) { return; }

		Map<String, Object> defaultsMap = Utils.jsonToMap(defaults);
		Utils.d("GodotFireBase", "RemoteConfig: Setting Default values, " + defaultsMap.toString());

		mFirebaseRemoteConfig.setDefaults(defaultsMap);
	}

	public String getValue (final String key) {
		if (!isInitialized()) { return "NULL"; }

		Utils.d("GodotFireBase", "Getting Remote config value for: " + key);
		return mFirebaseRemoteConfig.getValue(key).asString();
	}

	public String getValue (final String key, final String namespace) {
		if (!isInitialized()) { return "NULL"; }

		Utils.d("GodotFireBase", "Getting Remote config value for { " + key + " : " + namespace + " }");
		return mFirebaseRemoteConfig.getValue(key, namespace).asString();
	}

	private boolean isInitialized() {
		if (mFirebaseApp == null) {
			Utils.d("GodotFireBase", "RemoteConfig is not initialized.");
			return false;
		} else {
			return true;
		}
	}

	private static Activity activity = null;
	private static Context context = null;
	private static RemoteConfig mInstance = null;

	private FirebaseApp mFirebaseApp = null;
	private FirebaseRemoteConfig mFirebaseRemoteConfig = null;
}
