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

package org.godotengine.godot.storage;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StreamDownloadTask;
import com.godot.game.BuildConfig;
import com.godot.game.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.godotengine.godot.Utils;

public class UploadService extends BaseTaskService {

	/** Intent Actions **/
	public static final String ACTION_UPLOAD = "action_upload";
	public static final String UPLOAD_COMPLETED = "upload_completed";
	public static final String UPLOAD_ERROR = "upload_error";

	/** Intent Extras **/
	public static final String EXTRA_FILE_URI = "extra_file_uri";
	public static final String EXTRA_FILE_CHILD = "extra_file_child";
	public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

	@Override
	public void onCreate() {
		super.onCreate();

		mStorageRef = FirebaseStorage.getInstance().getReference();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils.d("GodotFireBase", "SD:OnStartCommand: {" + intent + ":" + startId + "}");

		if (ACTION_UPLOAD.equals(intent.getAction())) {
			Utils.d("GodotFireBase", "Intent here: " + intent.getExtras().toString());

			Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
			String child = intent.getStringExtra(EXTRA_FILE_CHILD);
			uploadFromUri(fileUri, child);
		}

		return START_REDELIVER_INTENT;
	}

	private void uploadFromUri(final Uri fileUri, final String child) {
		uploadFromUri(fileUri, child, "");
	}

	private void uploadFromUri(final Uri fileUri, final String folder, final String meta) {
		Utils.d("GodotFireBase", "SD:UploadFromUri:src:" + fileUri.toString());

		taskStarted();
		showProgressNotification("progress_uploading", 0, 0);

		// Get a reference to store file at photos/<FILENAME>.jpg
		final StorageReference photoRef;

		if (folder.equals("")) { photoRef = mStorageRef.child(fileUri.getLastPathSegment()); }
		else { photoRef = mStorageRef.child(folder).child(fileUri.getLastPathSegment()); }

		// Upload file to Firebase Storage
		Utils.d("GodotFireBase", "SD:UploadFromUri:dist:" + photoRef.getPath());

		UploadTask task;

		if (meta.equals("")) { task = photoRef.putFile(fileUri); }
		else {
			StorageMetadata metadata = new StorageMetadata.Builder()
			.setContentType(meta)
			.build();

			task = photoRef.putFile(fileUri, metadata);
		}

		task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
				showProgressNotification("progress_uploading",
				taskSnapshot.getBytesTransferred(),
				taskSnapshot.getTotalByteCount());
			}
		}).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				// Upload succeeded
				Utils.d("GodotFireBase", "SD:UploadFromUri:onSuccess");

				// Get the public download URL
				Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

				broadcastUploadFinished(downloadUri, fileUri);
				showUploadFinishedNotification(downloadUri, fileUri);
				taskCompleted();
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception exception) {
				// Upload failed
				Utils.w("GodotFireBase", "SD:UploadFromUri:onFailure:" + exception.toString());

				broadcastUploadFinished(null, fileUri);
				showUploadFinishedNotification(null, fileUri);

				taskCompleted();
			}
		});
	}

	private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
		boolean success = downloadUrl != null;

		String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

		Intent broadcast = new Intent(action)
		.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
		.putExtra(EXTRA_FILE_URI, fileUri);

		return
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
	}

	private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
		// Hide the progress notification
		dismissProgressNotification();

		// Make Intent to MainActivity
		Intent intent = new Intent(this, org.godotengine.godot.Godot.class)
		.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
		.putExtra(EXTRA_FILE_URI, fileUri)
		.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		boolean success = downloadUrl != null;
		String caption = success ? "upload_success" : "upload_failure";

		showFinishedNotification(caption, intent, success);
	}

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPLOAD_COMPLETED);
		filter.addAction(UPLOAD_ERROR);

		return filter;
	}

	@Override
	public void onDestroy() {

	}

	/**
	// Pause the upload
	uploadTask.pause();

	// Resume the upload
	uploadTask.resume();

	// Cancel the upload
	uploadTask.cancel();
	**/

	private StorageReference mStorageRef = null;
}
