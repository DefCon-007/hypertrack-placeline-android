# Build Placeline within your own app
Following this tutorial to track your users through the day, and show a placeline view within the app. The tutorial is divided into three sections.
1. In the first section, we will do a basic setup of Hypertrack SDK. 
2. In the second section, we will set up rules to track users throught he day. 
3. In the last section, we will build a Placeline view within your app.

Let's get started 😊. Strap yourself in and get ready for an exciting ride 🚀.

- [Basic setup](#basic-setup)
  - [Get API keys](#step-1-get-api-keys)
  - [Install SDK](#Step-2-Install-SDK)
  - [Enable communication](#Step-3-Enable-communication)
  - [Set permissions](#Step-4-Set-permissions)
  - [Create HyperTrack user](#Step-5-Create-HyperTrack-user)
  - [Crashlytics Setup (optional)](#step-6-crashlytics-setup-optional)
- [Track users through the day](#track-users-through-the-day)
- [Build Placeline view](#build-placeline-view)
  - [Understand Placeline format](#understand-placeline-format)
  - [Get Placeline data in your app](#get-placeline-data-in-your-app)
  - [Get Placeline view in your Android app](#get-placeline-view-in-your-android-app)
  
## Basic Setup
### Step 1. Get API keys
Get your HyperTrack API keys [here](https://www.hypertrack.com/signup?utm_source=github&utm_campaign=ht_placeline_android).

### Step 2. Install SDK
1. Import our SDK into your app
```java
repositories {
    maven { url 'http://hypertrack-android-sdk.s3-website-us-west-2.amazonaws.com/' }
}

dependencies {
    implementation('com.hypertrack:android:0.7.7@aar') {
        transitive = true;
    }
}
```

2. Initialize the SDK with your [publishable keys](https://dashboard.hypertrack.com/settings)
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HyperTrack.initialize(this, YOUR_PUBLISHABLE_KEY_HERE);
    }
}
```

### Step 3. Enable communication
1. Enable bidirectional communication between server and SDK using FCM notifications
```java
public class MyFirebaseMessagingService extends HyperTrackFirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            String sdkNotification = remoteMessage.getData().get(Constants.HT_SDK_NOTIFICATION_KEY);
            if (sdkNotification != null && sdkNotification.equalsIgnoreCase("true")) {
                /**
                 * HyperTrack notifications are received here
                 * Dont handle these notifications. This might end up in a crash
                 */
                return;
            }
        }
        // Handle your notifications here.
    }
}
```

```java
 <service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
2. Head over to [FCM Console](https://console.firebase.google.com/project/), select your project, and then visit Settings > Cloud Messaging to get your FCM server key. Copy the key and it to your [HyperTrack dashboard settings](https://dashboard.hypertrack.com/settings).

### Step 4. Set permissions
Ask user permission to access location
```java
private void checkForLocationSettings() {
     // Check for Location permission
    if (!HyperTrack.checkLocationPermission(this)) {
        HyperTrack.requestPermissions(this);
        return;
    }
    // Check for Location settings
    if (!HyperTrack.checkLocationServices(this)) {
        HyperTrack.requestLocationServices(this);
    }
    // Location Permissions and Settings have been enabled
    // Proceed with your app logic here i.e User Login in this case
    attemptUserLogin();
}

@Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
 
 @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, R.string.enable_location_settings,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

```

### Step 5. Create HyperTrack user
Create a HyperTrack user to identify the mobile device. You can do so when your user logs-in into your app.

```java
UserParams userParams = new UserParams()
    .setName(name)
    .setPhone(phoneNumber)
    .setPhoto(encodedImage)
    .setLookupId(phoneNumber);
                
HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse successResponse) {
        // Handle success on getOrCreate user
        Toast.makeText(this, "Yay! User is created successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle error on getOrCreate user
        Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

### Step 6. Crashlytics Setup (Optional)
You can **optionally** enable the crashlytics crash reporting tool. 
1. Get your Crashlytics key from the **Add Your API Key** section [here](https://fabric.io/kits/android/crashlytics/install).
2. Paste the key to [fabric.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/fabric.properties). Create a new fabric.properties file, if it doesn't exist already.

## Track users through the day
To track your users through the day, set up a rule that auto-creates an action at the start of the day and auto-completes it at the end of the day. Visit [HyperTrack Dashboard settings](https://dashboard.hypertrack.com/settings) to set up the rule. 

## Build Placeline view
### Understand Placeline format
Placeline object is a list of activity segments, where each segment is defined by a start and an end timestamp, and has relevant location, activity and health data as properties. The Placeline object includes segments like **stop**, **walk**, and **drive**. See an example JSON representation [here](https://docs.hypertrack.com/api/entities/action.html#placeline).

### Get Placeline data in your app
Once the `track-through-the-day` action has been created for your users (via a rule), implement the following function [placelineManager.getPlacelineData()](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/store/PlacelineManager.java#L43) and get [PlacelineData](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/model/PlacelineData.java). You are all set to use the rich activity data in your app.

```java
Date date = new Date();
HyperTrack.getPlaceline(date, new HyperTrackCallback() {
    @Override
    public void onSuccess(@NonNull SuccessResponse response) {
        // Handle getPlaceline success here
        if (response != null) {
            PlacelineData = (PlacelineData) response.getResponseObject();
        }
    }

    @Override
    public void onError(@NonNull ErrorResponse errorResponse) {
        // Handle getPlaceline error here
        Log.d("Placeline", "onError: " + errorResponse.getErrorMessage());
    }
});
```

### Get Placeline view in your Android app

#### Step 1: Setup Activity

* **Firstly**, add the following xml snippet in your view layout to enable `PlacelineFragment`.

```xml
<fragment
    android:id="@+id/placeline_fragment"
    android:name="com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:layout="@layout/placeline_fragment" />
```

* **Secondly**, instantiate `PlacelineFragment` in the onCreate method of the activity in which placeline fragment has been included.

```java
PlacelineFragment placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
```

Once all of the above is done, the code snippet would look like as below.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  ...

  // Initialize Placeline Fragment added in Activity Layout
  PlacelineFragment placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment); 

  ...
}
```

#### Step 2: Disable ActionBar

In case your AppTheme adds an ActionBar by default, disable the default Action Bar for the activity containing PlacelineFragment by adding the following under your Activity's theme style-tag in styles.xml file. Refer to Android documentation on [Setting up the AppBar](https://developer.android.com/training/appbar/setting-up.html).

```xml
<!-- Change Placeline activity's theme to remove default ActionBar. -->
<style name="PlacelineActivityTheme" parent="Theme.AppCompat.Light.NoActionBar">
    ...
    <!-- We will be using the toolbar so no need to show ActionBar -->
    <item name="windowActionBar">false</item>
    <item name="windowNoTitle">true</item>
    <item name="colorAccent">@color/colorAccent</item>
	  ...
</style>
```

Add the `android:theme` attribute to the `<activity>` tag in `AndroidManifest.xml` file.

```xml
<!-- Placeline Activity Tag -->
 <activity android:name=".PlacelineActivity"
    android:theme="@style/PlacelineActivityTheme"/>
```


#### Step 3: Start Placeline Activity

Placeline Activity can be the `Launcher activity` or it can start from some other activity as normal activity.

**Launcher Activity**

Add the following xml snippet in your `AndroidManifest.xml` file to make `PlacelineActivity` as a launcher activity.


```xml
<activity
    android:name=".PlacelineActivity"
    android:theme="@style/PlacelineActivityTheme">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

**Normal Activity**

Add the following snippet in your activity from which you want to start `PlacelineActivity`.


```java
public void startPlaceline(View view) {
    startActivity(new Intent(this, PlacelineActivity.class));
}
```

If you have any problems or suggestions for the tutorial, do not hesitate to buzz 🐝 us on [Slack](http://slack.hypertrack.com) or email us at help@hypertrack.com.
