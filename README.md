## GodotFireBase
Godot_FireBase is a firebase integration for godot android;

# Depends on
> Godot game engine (2.1/2.2-legacy): `git clone https://github.com/godotengine/godot`

> GodotSQL: `git clone https://github.com/FrogSquare/GodotSQL`

# Available Features
> AdMob

> Analytics

> Authentication [W.I.P] Google, Facebook

> Firebase Notification

> RemoteConfig

> Storage

> Invites (Email & SMS)

# Build/Compile module
copy your `google-services.json` file to `[GODOT-ROOT]/platform/android/java/` and edit file modules/FireBase/config.py at line 17
```
env.android_add_default_config("applicationId 'com.your.appid'")
```
replay `com.your.appid` with you android application id.

# Initialize FireBase
Edit engine.cfg and add
```
[android]
modules="org/godotengine/godot/FireBase"
```

RemoteConfigs default parameters `.xml` file is at `[GODOT-ROOT]/modules/FireBase/res/xml/remote_config_defaults.xml`

# GDScript - getting module singleton and initializing;
```
var firebase = Globals.get_singleton("FireBase");
```
For Analytics only `firebase.init("", get_instance_id());` or to user RemoteConfig or Notifications (subscribing to topic)

# GodotFireBase: copy `godot-firebase-config.json` to your projects root directord.
GodotFireBase config file, By default every feature is disabled.
```
{

"AdMob"		 : true,
"Authentication" : true,
"Invites"	 : true,
"RemoteConfig"	 : true,
"Notification"	 : true,
"Storage"	 : true,

"Auth"		 : {
			"Google" : true,

			"Facebook" : true,
			"FacebookAppID" : "1234566789875"
		   },

"Ads"		 : {
			"BannerAd" : true,
			"BannerGravity" : "BOTTOM",
			"BannerAdID" : "",

			"InterstitialAd" : true,
			"InterstitialAdID" : "",

			"RewardedVideoAd" : true,
			"RewardedVideoAdID" : ""
		   }

}
```
And  initialize firebase with file path
```
func _ready():
	if OS.get_name() == "Android":
		firebase.initWithFile("res://godot-firebase-config.json", get_instance_id());

func _receive_message(from, key, data):
	from == "FireBase":
		print("Key: " + key, " Data: ", data)

```
# Using FireBase Analytics
```
firebase.send_events("EventName", Dictionary);
firebase.send_custom("TestKey", "SomeValue");

firebase.setScreenName("Screen_name")
firebase.sendAchievement("someAchievementId")		# unlock achievement
firebase.join_group("clan_name")			# join clan/group
firebase.level_up("character_name", level)		# send character level
firebase.post_score("charcter name", level, score)	# post your score
firebase.earn_currency("currency", amount);		# when play earn some virtual currency gold/Diamond/any
firebase.spend_currency("item_id", "currency", amount)	# when user spend virtual currency

firebase.tutorial_begin()				# tutorial begin
firebase.tutorial_complete()				# tutorial end

Reference: https://support.google.com/firebase/answer/6317494?hl=en
```

# AlertDialog aditional
```
firebase.alert("Message goes here..!");
```

# Authentication

For Facebook edit `res/values/ids.xml` and replace facebook_app_id with your Facebook App Id
```
firebase.authConfig("'Google':true,'Facebook':true") // Configure Auth service

firebase.google_sign_in() // Firebase connect to google.
firebase.facebook_sign_in() // Firebase connect to facebook.

firebase.google_sign_out() // Firebase disconnect from google.
firebase.facebook_sign_out() // Firebase disconnect from facebook.

var gUserDetails = firebase.get_google_user() // returns name, email_id, photo_uri
var fbUserDetails = firebase.get_facebook_user() // returns name, email_id, photo_uri

firebase.google_revoke_access();
firebase.facebook_revoke_access();

firebase.is_google_connected() // bool check for google authentication
firebase.is_facebook_connected() // bool check for facebook authentication
```

More for facebook permissions
```
firebase.facebook_has_permission("publish_actions") // Check for availabe permission

firebase.revoke_facebook_permission("publish_actions") // revoke permission

firebase.ask_facebook_publish_permission("publish_actions"); // asking write permission

firebase.ask_facebook_read_permission("email"); // asking read only permission

firbase.get_facebook_permissions() // getting available permissions
```

Recive message from java

```
func _recive_message(from, key, data):
	from == "FireBase":
		if key == "GoogleLogin" && data == "true": print("User Signed in.");
		if key == "FacebookLogin" && data == "true": print("User Signed in.");
```

# Firebase Notification API
```
firebase.subscribeToTopic("topic") // Subscribe to particular topic.
firebase.getToken() // Get current client TokenID

If recived notifiction has a payload, it will be saved inside SQL Database under key: "firebase_notification_data"

firebase.notifyInMins("message", 60) // Shedule notification in 60 min
```

# RemoteConfig API
```
firebase.getRemoteValue("remote_key") // Return String value
```
# Settings RemoteConfig default values
```
var defs = Dictionary()
defs["some_remoteconfig_key1"] = "remote_config_value1"
defs["some_remoteconfig_key2"] = "remote_config_value2"

firebase.setRemoteDefaults(defs.to_json())
```
OR load from json file
```
firebase.setRemoteDefaultsFile("res://path/to/jsonfile.json")
```

# Firebase Storage
```
Upload Files from sdcard
firebase.upload("images/file", "destFolder") // uploads file from sdcard to firebase

Download Files from Firebase
firebase.download("file", "images"); // Saves file from firebase to sdcard
```

# Firebase Invites
```
Invite Friends with Email & SMS, DeepLink example: https://play.google.com/store/apps/details?id=[package-id].

firebase.invite("message", "https://example.com/beed/link") // Send Firebase Invites.
firebase.invite("message", "");  // Fallback to use default android share eg: Whatsapp, Twitter and more.
```

# Firebase AdMob
```
firebase.show_banner_ad(true)	// Show Banner Ad
firebase.show_banner_ad(false)	// Hide Banner Ad

firebase.show_interstitial_ad() // Show Interstitial Ad

firebase.show_rewarded_video()	// Show Rewarded Video Ad

firebase.request_rewarded_video_status() // Request the rewarded video status
```

Recive message from java

```
func _receive_message(from, key, data):
	if from == "FireBase":
		if key == "AdMobReward":
			# when rewared video play complete
			print("json data with [RewardType & RewardAmount]: ", data);

		elif key == "AdMob_Video":
			# when rewarded video loaded
			# data will be `loaded` or `load_failed and `loaded` or `not_loaded` with `firebase.request_rewarded_video_status()`
			print("AdMob rewarded video status is ", data);

		elif key == "AdMob_Banner":
			# when banner loaded
			# data will be `loaded` or `load_failed`
			print("Banner Status: ", data);

		elif key == "AdMob_Interstitial" and data == "loaded":
			# when Interstitial loaded
			# data will be `loaded` or `load_failed`
			print("Interstitial Status: ", data);
```

# Note
While exporting, don't forget to add `*.json` under Resources tab,
![alt text](http://preview.ibb.co/fTwC8Q/Screenshot_from_2017_06_17_18_44_25.png)

# Log Event
```
adb -d logcat godot:V FireBase:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S
```
