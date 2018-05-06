/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.placeline.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import io.hypertrack.placeline.R;
import io.hypertrack.placeline.model.AppDeepLink;
import io.hypertrack.placeline.util.AnimationUtils;
import io.hypertrack.placeline.util.CrashlyticsWrapper;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();

    private AppDeepLink appDeepLink;
    private ProgressBar progressBar;
    private boolean autoAccept;
    private String userID, accountID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize UI Views
        initUI();

        // Check for location settings and request in case not available
        requestForLocationSettings();
    }

    public void initUI() {
        // Initialize UI Views
        Button enableLocation = (Button) findViewById(R.id.enable_location);
        TextView permissionText = (TextView) findViewById(R.id.permission_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Initialize button click listeners
        enableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForLocationSettings();
            }
        });

        // Ask for enabling location permissions and location services to proceed further
        if (!HyperTrack.checkLocationPermission(this) || !HyperTrack.checkLocationServices(this)) {
            progressBar.setVisibility(View.INVISIBLE);
            permissionText.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.VISIBLE);
        }

        // Start animation for ripple view
        final ImageView locationRippleView = (ImageView) findViewById(R.id.location_ripple);
        AnimationUtils.ripple(locationRippleView);
    }

    private void proceedToNextScreen() {
        CrashlyticsWrapper.setCrashlyticsKeys(SplashScreen.this);

        // Check if user has signed up
        boolean isHyperTrackUserConfigured = !HTTextUtils.isEmpty(HyperTrack.getUserId());
        if (isHyperTrackUserConfigured) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(new Intent(this, Home.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    .startActivities();
        } else {
            final Intent registerIntent = new Intent(SplashScreen.this, Profile.class);
            registerIntent.putExtra("class_from", SplashScreen.class.getSimpleName());
            registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(registerIntent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Location permission successfully granted response
                requestForLocationSettings();

            } else {
                // Handle Location permission request denied error
                showSnackBar();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle Location services successfully enabled response
                requestForLocationSettings();

            } else {
                // Handle Location services request denied error
                showSnackBar();
            }
        }
    }

    private void requestForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
            return;
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here
        proceedToNextScreen();
    }

    private void showSnackBar() {
        if (!HyperTrack.checkLocationPermission(this)) {
            // Handle Location permission request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_permission_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Allow", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestForLocationSettings();
                }
            }).show();

        } else if (HyperTrack.checkLocationServices(this)) {
            // Handle Location services request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Enable", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestForLocationSettings();
                }
            }).show();
        }
    }
}