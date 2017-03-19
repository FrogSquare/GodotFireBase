<<<<<<< HEAD

Godot_FireBase is a firebase integration for godot android;

Build/Compile module
Edit file modules/FireBase/config.py Line 17 with
env.android_add_default_config("applicationId 'com.your.appid'")

Initialize FireBase

edit engine.cfg and add
[android]

modules="org/godotengine/godot/FireBase"

and in GDScript;
firebase = Globals.get_singleton("FireBase");
firebase.init();


firebase.sendCustom("TestKey", "SomeValue");
firebase.setScreenName("Screen_name");
firebase.sendAchivement("someAchivementId");

AlertDialog aditional;
firebase.alert("Message goes here..!");

Log FireBase Events

adb shell setprop log.tag.FA VERBOSE
adb shell setprop log.tag.FA-SVC VERBOSE
adb shell setprop debug.firebase.analytics.app {org.example.appname}
adb logcat -v time -s FA FA-SV

=======
# GodotFireBase
FireBase for godot
>>>>>>> 8c4629208eaea1ef09fc3e79a4fd1ff34aa967b4
