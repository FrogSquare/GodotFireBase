
def can_build(plat):
	#False;
	return (plat == "android")

def configure(env):
	if env["platform"] == "android":
		env.android_add_maven_repository("url 'https://oss.sonatype.org/content/repositories/snapshots'")

		env.android_add_gradle_classpath("com.google.gms:google-services:3.0.0")
		env.android_add_gradle_plugin("com.google.gms.google-services")

		env.android_add_dependency("compile 'com.android.support:support-annotations:25.0.1'")

		env.android_add_dependency("compile 'com.google.android.gms:play-services-auth:10.0.1'")
		env.android_add_dependency("compile 'com.facebook.android:facebook-android-sdk:4.18.0'")

		env.android_add_dependency("compile 'com.google.firebase:firebase-ads:10.0.1'")
		env.android_add_dependency("compile 'com.google.firebase:firebase-auth:10.0.1'")
                env.android_add_dependency("compile 'com.google.firebase:firebase-core:10.0.1'")
		env.android_add_dependency("compile 'com.google.firebase:firebase-config:10.0.1'")
                env.android_add_dependency("compile 'com.google.firebase:firebase-messaging:10.0.1'")
		env.android_add_dependency("compile 'com.google.firebase:firebase-invites:10.0.1'")
		env.android_add_dependency("compile 'com.google.firebase:firebase-storage:10.0.1'")

		env.android_add_dependency("compile 'com.firebase:firebase-jobdispatcher:0.5.2'")

		env.android_add_dependency("compile 'commons-codec:commons-codec:1.10'")

		env.android_add_java_dir("android");
		env.android_add_res_dir("res");
		env.android_add_to_manifest("android/AndroidManifestChunk.xml");
		env.android_add_to_permissions("android/AndroidPermissionsChunk.xml");
		env.android_add_default_config("minSdkVersion 15")
		env.android_add_default_config("applicationId 'com.froglogics.dotsndots'")
		env.disable_module()
