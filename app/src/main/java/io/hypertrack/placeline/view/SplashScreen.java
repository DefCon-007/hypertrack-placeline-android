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
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import io.hypertrack.placeline.R;
import io.hypertrack.placeline.util.CrashlyticsWrapper;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();

    private ProgressBar progressBar;

    Button enableLocation;

    TextView mTextView;

    ConstraintLayout locationPermissionLayout, splashLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize UI Views
        initUI();

        // Check for location settings and request in case not available
        if (HyperTrack.checkLocationPermission(this) &&
                HyperTrack.checkLocationServices(this)) {
            splashLayout.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    proceedToNextScreen();
                }
            }, 1000);
        } else {
            if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
                locationPermissionLayout.setVisibility(View.VISIBLE);
            } else {
                splashLayout.setVisibility(View.VISIBLE);
                requestForLocationSettings();
            }
        }

    }

    public void initUI() {
        // Initialize UI Views
        locationPermissionLayout = findViewById(R.id.location_permission_layout);
        splashLayout = findViewById(R.id.splash_layout);
        enableLocation = findViewById(R.id.enable_location);
        progressBar = findViewById(R.id.progress_bar);
        mTextView = findViewById(R.id.app_name);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, Mode.SRC_ATOP);
        // Initialize button click listeners
        enableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForLocationSettings();
            }
        });
        mTextView.setText(getString(R.string.app_name));

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Location permission successfully granted response
                requestForLocationSettings();

            } else {
                // Handle Location permission request denied error
                locationPermissionLayout.setVisibility(View.VISIBLE);
                splashLayout.setVisibility(View.GONE);
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
                locationPermissionLayout.setVisibility(View.VISIBLE);
                splashLayout.setVisibility(View.GONE);
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
}