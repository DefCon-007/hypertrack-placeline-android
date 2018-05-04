
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

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.placeline.PlacelineActivitySummaryView;
import com.hypertrack.lib.tracking.MapProvider.HyperTrackMapFragment;
import com.hypertrack.lib.tracking.MapProvider.MapFragmentView;

import io.hypertrack.placeline.R;
import io.hypertrack.placeline.util.PermissionUtils;

public class Home extends AppCompatActivity {

    private static final String TAG = Home.class.getSimpleName();

    HyperTrackMapFragment mHyperTrackMapFragment;
    private HomeMapAdapter mMapAdapter;
    private GoogleMap mMap;

    private ProgressDialog mProgressDialog;

    PlacelineActivitySummaryView mPlacelineActivitySummaryView;

    public MapFragmentCallback callback = new MapFragmentCallback() {

        @Override
        public void onMapReadyCallback(Context context, GoogleMap map) {
            mMap = map;
            if (checkForLocationSettings())
                mMap.setMyLocationEnabled(true);
            super.onMapReadyCallback(context, map);
        }

        @Override
        public boolean onBackButtonPressed() {
            if (mHyperTrackMapFragment == null)
                return false;

            if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.PLACELINE) {
                setPlacelineSummaryViewUseCase();
                return true;
            }
            return false;
        }

        @Override
        public void onBottomBaseViewCreated(@MapFragmentView.Type int useCaseType) {
            switch (useCaseType) {
                case MapFragmentView.Type.PLACELINE_SUMMARY:
                    break;
            }
        }

        @Override
        public boolean onPlacelineViewClosed() {
            if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.PLACELINE) {
                setPlacelineSummaryViewUseCase();
                return true;
            }
            return false;
        }

        @Override
        public void onSettingButtonClicked() {
            startActivity(new Intent(Home.this, MyProfile.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);

        //Initialize Map Fragment added in Activity Layout to getMapAsync
        mHyperTrackMapFragment = (HyperTrackMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.htMapfragment);

        mMapAdapter = new HomeMapAdapter(this);
        mHyperTrackMapFragment.setMapAdapter(mMapAdapter);
        mHyperTrackMapFragment.setMapCallback(callback);

        // Check & Prompt User if Internet is Not Connected
        if (!HyperTrackUtils.isInternetConnected(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }
        setPlacelineSummaryViewUseCase();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean checkForLocationSettings() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestLocationServices(this);
            return false;
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
            return false;
        }

        return true;
    }

    private void setPlacelineSummaryViewUseCase() {
        mHyperTrackMapFragment.hideBackButton();
        if (mPlacelineActivitySummaryView == null) {
            mPlacelineActivitySummaryView = (PlacelineActivitySummaryView) mHyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.PLACELINE_SUMMARY);
        } else {
            mHyperTrackMapFragment.setUseCase(mPlacelineActivitySummaryView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForLocationSettings();

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.location_permission_never_allow));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                checkForLocationSettings();
            } else {
                // Handle Location services request denied error
                Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                        Snackbar.LENGTH_INDEFINITE).setAction("Enable Location", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkForLocationSettings();
                    }
                }).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}