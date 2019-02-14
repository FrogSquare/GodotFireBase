
import re

FILES_LIST		= \
{
"AdMob"		: ["AdMob.java"],
"Analytics"	: ["Analytics.java"],
"Auth"		: ["AnonymousAuth.java", "Auth.java", "EmailAndPassword.java"],
"Base"		: ["FireBase.java", "AndroidPermissionsChunk.xml"],
"Invites"	: ["Invites.java"],
"Notification"	: ["MessagingService.java", "Notification.java", \
                   "NotifyInTime.java", "InstanceIDService.java"],
"RemoteConfig"	: ["RemoteConfig.java"],
"Storage"	: ["storage/"],
"Firestore"	: ["Firestore.java"],

"AuthGoogle"    : ["GoogleSignIn.java"],
"AuthFacebook"  : ["FacebookSignIn.java"],
"AuthTwitter"   : ["TwitterSignIn.java"],
}

directory = "android"
empty_line = re.compile(r'^\s*$')
