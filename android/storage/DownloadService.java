
package org.godotengine.godot.storage;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.godot.game.BuildConfig;
import com.godot.game.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.godotengine.godot.Utils;

public class DownloadService extends BaseTaskService {

	/** Actions **/
	public static final String ACTION_DOWNLOAD = "action_download";
	public static final String DOWNLOAD_COMPLETED = "download_completed";
	public static final String DOWNLOAD_ERROR = "download_error";

	/** Extras **/
	public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
	public static final String EXTRA_DOWNLOAD_TO = "extra_download_to";
	public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "SD:DownloadTask:Created");

		mStorageRef = FirebaseStorage.getInstance().getReference();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "SD:OnStartCommand: {" + intent + ":" + startId + "}");

		if (ACTION_DOWNLOAD.equals(intent.getAction())) {
			String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
			String downloadTo = intent.getStringExtra(EXTRA_DOWNLOAD_TO);
			//downloadFromPath(downloadPath);
			downloadToFile(downloadPath, downloadTo);
		}

		return START_REDELIVER_INTENT;
	}

	private void downloadToFile(final String downloadPath, final String downloadTo) {
		if (Utils.isExternalStorageWritable()) {
			Log.d(TAG, "SD:CanWrite");
		} else { Log.d(TAG, "SD:CannotWrite"); }

		File rootPath = new File(
		Environment.getExternalStorageDirectory(), downloadTo);

		if (!rootPath.exists()) { rootPath.mkdirs(); }

		Uri fileUri = Uri.parse(downloadPath);
		File localFile = new File(rootPath, fileUri.getLastPathSegment());

		taskStarted();
		showProgressNotification("Progress downloading", 0, 0);

		mStorageRef.child(downloadPath).getFile(localFile)
		.addOnProgressListener(new  OnProgressListener<FileDownloadTask.TaskSnapshot>() {
			@Override
			public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
				long totalBytes = taskSnapshot.getTotalByteCount();
				long bytesDownloaded = taskSnapshot.getBytesTransferred();

				onMainProgress(downloadPath, bytesDownloaded, totalBytes);
			}
		})
		.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
				onMainSuccess(downloadPath, taskSnapshot.getTotalByteCount());
			}
		})
		.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception exception) {
				onMainFailure(downloadPath, exception);
			}
		});
	}

	private void downloadFromPath(final String downloadPath) {
		Log.d(TAG, "SD:DownloadFromPath:" + downloadPath);

		taskStarted();
//		showProgressNotification(getString(R.string.progress_downloading), 0, 0);
		showProgressNotification("Progress downloading", 0, 0);

		mStorageRef.child(downloadPath).getStream(new StreamDownloadTask.StreamProcessor() {
			@Override
			public void doInBackground(StreamDownloadTask.TaskSnapshot taskSnapshot,
							InputStream inputStream) throws IOException {

				long totalBytes = taskSnapshot.getTotalByteCount();
				long bytesDownloaded = 0;

				byte[] buffer = new byte[1024];
				int size;

				while ((size = inputStream.read(buffer)) != -1) {
					bytesDownloaded += size;
					onMainProgress(downloadPath, bytesDownloaded, totalBytes);
				}

				// Close the stream at the end of the Task
				inputStream.close();
			}
		})
		.addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
				onMainSuccess(downloadPath, taskSnapshot.getTotalByteCount());
			}
		})
		.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception exception) {
				onMainFailure(downloadPath, exception);
			}
		});
	}

	private void onMainProgress(String path, long currentByteCount, long totalByteCount) {
		showProgressNotification("Progress downloading", currentByteCount, totalByteCount);
	}

	private void onMainSuccess(String path, long totalByteCount) {
		Log.d(TAG, "SD:Download:SUCCESS");

		// Send success broadcast with number of bytes downloaded
		broadcastDownloadFinished(path, totalByteCount);
		showDownloadFinishedNotification(path, (int) totalByteCount);

		// Mark task completed
		taskCompleted();
	}

	private void onMainFailure(String path, @NonNull Exception exception) {
		Log.w(TAG, "SD:Download:FAILURE", exception);

		// Send failure broadcast
		broadcastDownloadFinished(path, -1);
		showDownloadFinishedNotification(path, -1);

		// Mark task completed
		taskCompleted();
	}

	/**
	 * Broadcast finished download (success or failure).
	 * @return true if a running receiver received the broadcast.
	 */
	private boolean broadcastDownloadFinished(String downloadPath, long bytesDownloaded) {
		boolean success = bytesDownloaded != -1;
		String action = success ? DOWNLOAD_COMPLETED : DOWNLOAD_ERROR;

		Intent broadcast = new Intent(action)
		.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
		.putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded);

		return
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
	}

	/**
	 * Show a notification for a finished download.
	 */
	private void showDownloadFinishedNotification(String downloadPath, int bytesDownloaded) {
		// Hide the progress notification
		dismissProgressNotification();

		// Make Intent to MainActivity
		Intent intent = new Intent(this, org.godotengine.godot.Godot.class)
		.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
		.putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
		.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		boolean success = bytesDownloaded != -1;
		String caption =
		success ? "Download Success" : "Download failure";
//		success ? getString(R.string.download_success) : getString(R.string.download_failure);

		showFinishedNotification(caption, intent, true);
	}

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_COMPLETED);
		filter.addAction(DOWNLOAD_ERROR);

		return filter;
	}

	private StorageReference mStorageRef = null;
	private static final String TAG = "FireBase";
}
