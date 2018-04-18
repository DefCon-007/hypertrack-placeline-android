# HyperTrack Placeline
[![Build Status](https://travis-ci.org/hypertrack/hypertrack-live-android.svg?branch=master)](https://travis-ci.org/hypertrack/hypertrack-live-android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger) [![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-HyperTrack%20Live-brightgreen.svg?style=flat)](https://android-arsenal.com/details/3/5754) [![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/licenses/MIT) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Placeline is useful in tracking your daily activity with near-zero battery impact. We automagically use the combination of device sensors - GPS, WiFi, network, accelerometer, pedometer, gyroscope, compass - to deliver accuracy. Placeline is powered by the HyperTrack SDK which collects location and activity data for your users. It includes segments like stop 🛑, walk 🚶‍♀️,run 🏃‍♀️,drive 🏎️ and cycle 🚴. 

Use this open source repo of the [Hypertrack Placeline](https://play.google.com/store/apps/details?id=io.hypertrack.sendeta&hl=en) app to build Placeline within your app within a few hours. The repo uses [HyperTrack](https://www.hypertrack.com/) APIs and SDKs. 

In case you are using iOS, refer to our open source iOS [repository](https://github.com/hypertrack/hypertrack-placeline-ios).

- [Clone the repo](#clone-the-repo)
- [Build placeline within your app](#build-within-your-app)
- [Release to playstore](#release-to-playstore)
- [Dependencies](#dependencies)
- [Documentation](#documentation)
- [Contribute](#contribute)
- [Support](#support)

## Clone the repo

1. Clone this repository
```bash
$ git clone https://github.com/hypertrack/hypertrack-placeline-android.git
```

2. Get your HyperTrack API keys [here](https://www.hypertrack.com/signup?utm_source=github&utm_campaign=ht_placeline_android), and add the publishable key to [key.properties](https://github.com/hypertrack/hypertrack-live-android/blob/master/key.properties) file.
```java
HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
```
3. To track your users through the day, set up a rule that auto-creates an action at the start of the day and auto-completes it at the end of the day. Visit [HyperTrack Dashboard settings](https://dashboard.hypertrack.com/settings) to set up the rule. 

4. Get the [Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup) and add it to [api-keys.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/res/values/api-keys.xml).


## Build within your app

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


## Release to Playstore
Following these steps to release the app on the Play Store.

1. Change the package name in the [AndroidManifest.xml](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/src/main/AndroidManifest.xml#L4) file.

2. Refactor the name of your package. Right click → Refactor → Rename in the tree view. Android Studio will display a window. Select "Rename package" option.

3. Change the application id in [build.gradle](https://github.com/hypertrack/hypertrack-live-android/blob/master/app/build.gradle#L102) file. Once done, clean and rebuild the project.
   - Add `release key store file` in app level folder.
   - Create a `keystore.properties` file in root or project level folder with key-values pair.
    ```properties
        storeFile=<File path of keystore file>
        storePassword=<Key Store Password>
        keyAlias=<Key Alias>
        keyPassword=<Key Password>
   ```

## Dependencies
* [Google v7 appcompat library](https://developer.android.com/topic/libraries/support-library/packages.html#v7-appcompat)
* [Google Design Support Library](https://developer.android.com/topic/libraries/support-library/packages.html#design)
* [Google libphonenumber library](https://github.com/googlei18n/libphonenumber/)
* [Square Retrofit](https://github.com/square/retrofit)
* [Square Picasso](https://github.com/square/picasso)
* [tajchert WaitingDots](https://github.com/tajchert/WaitingDots)
* [Compact Calendar View](https://github.com/SundeepK/CompactCalendarView)
* [Android Ripple Background](https://github.com/skyfishjy/android-ripple-background)
* [Scrolling Image View](https://github.com/Q42/AndroidScrollingImageView)
* [RecylcerView Snap](https://github.com/rubensousa/RecyclerViewSnap)
* [Leak Canary](https://github.com/square/leakcanary)
* [Branch](https://branch.io/)
* [Crashlytics](https://fabric.io/kits/android/crashlytics)

## Documentation
For detailed documentation of the HyperTrack APIs and SDKs, customizations and what all you can build using HyperTrack, please visit the official [docs](https://www.hypertrack.com/docs).

## Contribute
Feel free to clone, use, and contribute back via [pull requests](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [issues tracker](https://github.com/hypertrack/hypertrack-live-android/issues) to raise bug reports and feature requests.

We are excited to see what live location feature you build in your app using this project. Do ping us at help@hypertrack.io once you build one, and we would love to feature your app on our blog!

## Support
Join our [Slack community](http://slack.hypertrack.com) for instant responses. You can also email us at help@hypertrack.com.
