# Placeline
[![Build Status](https://travis-ci.org/hypertrack/hypertrack-live-android.svg?branch=master)](https://travis-ci.org/hypertrack/hypertrack-live-android) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/4fad0c93fd3749d690571a7a728ce047)](https://www.codacy.com/app/piyushguptaece/hypertrack-live-android?utm_source=github.com&utm_medium=referral&utm_content=hypertrack/hypertrack-live-android&utm_campaign=badger) [![Slack Status](http://slack.hypertrack.com/badge.svg)](http://slack.hypertrack.com) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-HyperTrack%20Live-brightgreen.svg?style=flat)](https://android-arsenal.com/details/3/5754) [![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/licenses/MIT) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Placeline is useful in tracking your daily activity. We automagically use the combination of device sensors - GPS, WiFi, network, accelerometer, pedometer, gyroscope, compass - to deliver accurate movement data (location + activity) with near-zero battery usage. Placeline is powered by the HyperTrack SDK which collects location and activity data for your users. It includes segments like stop 🛑, walk 🚶‍♀️,run 🏃‍♀️,drive 🏎️ and cycle 🚴. 

Developers also use Placeline to record & visualize movement of their sales team, delivery & service fleet, and on-ground operations team. It helps them provide visibility when their workforce is out in the field.

<p align="center">
<kbd>
<img src="http://res.cloudinary.com/hypertrack/image/upload/v1524554794/HT_Placeline.gif" alt="Live Location Sharing" width="300">
</kbd>
</p>

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

2. [Signup](https://www.hypertrack.com/signup?utm_source=github&utm_campaign=ht_placeline_android) to get your [HyperTrack API keys](https://dashboard.hypertrack.com/settings)(Publishable key).

    Create two new files with name *key.properties* one at `app/src/release/java/io/hypertrack/placeline/` and second at `app/src/debug/java/io/hypertrack/placeline/`.
     
    Add this content to above created file.
    ```properties
        HYPERTRACK_PUBLISHABLE_KEY="ADD_YOUR_PUBLISHABLE_KEY_HERE"
    ```
   
    Add the **publishable key** to [release key.properties](app/src/release/java/io/hypertrack/placeline/key.properties) and [debug key.properties](app/src/debug/java/io/hypertrack/placeline/key.properties) file.
```java
HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
```
3. To track your users through the day, set up a rule that auto-creates an action at the start of the day and auto-completes it at the end of the day. Visit [HyperTrack Dashboard settings](https://dashboard.hypertrack.com/settings) to set up the rule. 

4. Get the [Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup) and add it to [api-keys.xml](app/src/main/res/values/api-keys.xml).


## Build within your app
[Follow this step-by-step tutorial](TUTORIAL.md) to track your users through the day, and show a placeline view within your app.

## Release to Playstore
Following these steps to release the app on the Play Store.

1. Change the package name in the [AndroidManifest.xml](app/src/main/AndroidManifest.xml#L4) file.

2. Refactor the name of your package. Right click → Refactor → Rename in the tree view. Android Studio will display a window. Select "Rename package" option.

3. Change the application id in [build.gradle](app/build.gradle#L102) file. Once done, clean and rebuild the project.
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
* [Android Ripple Background](https://github.com/skyfishjy/android-ripple-background)
* [Scrolling Image View](https://github.com/Q42/AndroidScrollingImageView)
* [RecylcerView Snap](https://github.com/rubensousa/RecyclerViewSnap)
* [Leak Canary](https://github.com/square/leakcanary)
* [Crashlytics](https://fabric.io/kits/android/crashlytics)

## Documentation
For detailed documentation of the HyperTrack APIs and SDKs, customizations and what all you can build using HyperTrack, please visit the official [docs](https://www.hypertrack.com/docs).

## Contribute
Feel free to clone, use, and contribute back via [pull requests](https://help.github.com/articles/about-pull-requests/). We'd love to see your pull requests - send them in! Please use the [issues tracker](https://github.com/hypertrack/hypertrack-live-android/issues) to raise bug reports and feature requests.

We are excited to see what live location feature you build in your app using this project. Do ping us at help@hypertrack.com once you build one, and we would love to feature your app on our blog!

## Support
Join our [Slack community](http://slack.hypertrack.com) for instant responses. You can also email us at help@hypertrack.com.
