
def can_build(plat):
	return (plat == "android")

def configure(env):
	if env["platform"] == "android":
		env.android_add_maven_repository("url 'https://maven.fabric.io/public'")
		env.android_add_maven_repository("url 'https://maven.google.com'")
		env.android_add_maven_repository(\
		"url 'https://oss.sonatype.org/content/repositories/snapshots'")

		env.android_add_gradle_classpath("com.google.gms:google-services:3.1.1")
		env.android_add_gradle_plugin("com.google.gms.google-services")

		env.android_add_dependency("compile 'com.android.support:support-annotations:25.0.1'")
                env.android_add_dependency("compile 'com.google.firebase:firebase-core:11.4.2'")

		##Auth++

		env.android_add_dependency("compile 'com.google.firebase:firebase-auth:11.4.2'")

		##AuthGoogle++
		env.android_add_dependency("compile 'com.google.android.gms:play-services-auth:11.4.2'")
		##AuthGoogle--

		##AuthFacebook++
		env.android_add_dependency("compile 'com.facebook.android:facebook-android-sdk:4.18.0'")
		##AuthFacebook--

		##AuthTwitter++
		env.android_add_dependency(\
		"compile('com.twitter.sdk.android:twitter-core:1.6.6@aar') { transitive = true }")
		env.android_add_dependency(\
		"compile('com.twitter.sdk.android:twitter:1.13.1@aar') { transitive = true }")
		##AuthTwitter--

		##Auth--

		##AdMob++
		env.android_add_dependency("compile 'com.google.firebase:firebase-ads:11.4.2'")
		##AdMob--

		##RemoteConfig++
		env.android_add_dependency("compile 'com.google.firebase:firebase-config:11.4.2'")
		##RemoteConfig--

		##Notification++
                env.android_add_dependency("compile 'com.google.firebase:firebase-messaging:11.4.2'")
		env.android_add_dependency("compile 'com.firebase:firebase-jobdispatcher:0.5.2'")
		##Notification--

		##Invites++
		env.android_add_dependency("compile 'com.google.firebase:firebase-invites:11.4.2'")
		##Invites--

		##Storage++
		env.android_add_dependency("compile 'com.google.firebase:firebase-storage:11.4.2'")
		##Storage--

		##Firestore++
		env.android_add_dependency("compile 'com.google.firebase:firebase-firestore:11.4.2'")
		##Firestore--

		env.android_add_dependency("compile 'commons-codec:commons-codec:1.10'")

		env.android_add_java_dir("android");
		env.android_add_res_dir("res");
		env.android_add_to_manifest("android/AndroidManifestChunk.xml");
		env.android_add_to_permissions("android/AndroidPermissionsChunk.xml");
		env.android_add_default_config("minSdkVersion 15")
		env.android_add_default_config("applicationId 'com.froglogics.dotsndots'")
		env.disable_module()
