# Build Placeline within your own app

Following this tutorial to track your users through the day, and show a placeline view within the app.

 - [Understand Placeline format](#understand-placeline-format)
 - [Setup HyperTrack SDK](#setup-hypertrack-sdk)
 - [Create Hypertrack user](#create-hypertrack-user)
 - [Start tracking](#start-tracking)
 - [Get Placeline in your app](#get-placeline-data-in-your-app)

#### Understand Placeline format
Placeline object contains detailed information about the activity like the start time, end time, location, steps and more.
An example JSON representation is given [here](https://docs.hypertrack.com/gettingstarted/activities.html#placeline).

#### Setup HyperTrack SDK
Set up the HyperTrack SDK by following these [instructions](https://dashboard.hypertrack.com/setup).

#### Create HyperTrack user
The next thing that you need to do is create a HyperTrack User. This would tag the location/activity data to the user and help you get useful filtered data in the form of Placeline. More details about the function [here](https://dashboard.hypertrack.com/setup). 

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
                    HyperTrack.startTracking();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    // Handle error on getOrCreate user
                    Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
```

#### Start tracking
Start tracking for the created user by calling the following method
```java
HyperTrack.startTracking();
```

#### Get Placeline Data in your app
Once tracking has started, implement the following function [placelineManager.getPlacelineData()](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/store/PlacelineManager.java#L43) and get [PlacelineData](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/java/io/hypertrack/sendeta/model/PlacelineData.java). You are all set to use the rich activity data in your app.

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

#### Get Placeline View in your Android App

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

We hope that you got a good taste of Placeline. If you have any problems or suggestions for the tutorial, do not hesitate to buzz 🐝 us [here](#support).
