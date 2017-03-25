## GodotFireBase
Godot_FireBase is a firebase integration for godot android;

# Available Features
> Analytics

> Firebase Notification

> RemoteConfig

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
firebase = Globals.get_singleton("FireBase");
```
For Analytics only `firebase.init("");` or to user RemoteConfig or Notifications (subscribing to topic)
```
var config = Dictionary()
config["Notification"] = true;  // Firebase Notification
config["RemoteConfig"] = true;  // Firebase Remote Config
config["Invites"] = true  // Firebase Invites

firebase.init(config.to_json());
```
# Using FireBase Analytics
```
firebase.sendCustom("TestKey", "SomeValue");
firebase.setScreenName("Screen_name");
firebase.sendAchievement("someAchievementId");
```

# AlertDialog aditional;
```
firebase.alert("Message goes here..!");
```

# Firebase Notification API
```
firebase.subscribeToTopic("topic") // Subscribe to particular topic.
firebase.getToken() // Get current client TokenID
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

# Firebase Invites
```
Invite Friends with Email & SMS, DeepLink example: https://play.google.com/store/apps/details?id=[package-id].

firebase.invite("message", "https://example.com/beed/link") // Send Firebase Invites.
firebase.invite("message", "");  // Fallback to use default android share eg: Whatsapp, Twitter and more.
```

# Log FireBase Events
```
adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE
adb shell setprop debug.firebase.analytics.app {org.example.appname}
adb logcat -v time -s FA FA-SV
```
