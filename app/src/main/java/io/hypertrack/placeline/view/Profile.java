
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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import io.hypertrack.placeline.R;
import io.hypertrack.placeline.model.Country;
import io.hypertrack.placeline.model.CountryMaster;
import io.hypertrack.placeline.model.CountrySpinnerAdapter;
import io.hypertrack.placeline.presenter.IProfilePresenter;
import io.hypertrack.placeline.presenter.ProfilePresenter;
import io.hypertrack.placeline.store.SharedPreferenceManager;
import io.hypertrack.placeline.util.CrashlyticsWrapper;
import io.hypertrack.placeline.util.ImageUtils;
import io.hypertrack.placeline.util.PermissionUtils;
import io.hypertrack.placeline.util.Utils;
import io.hypertrack.placeline.util.images.DefaultCallback;
import io.hypertrack.placeline.util.images.EasyImage;
import io.hypertrack.placeline.util.images.RoundedImageView;

public class Profile extends BaseActivity implements ProfileView {

    private final static String TAG = Profile.class.getSimpleName();
    public EditText nameView;
    public RoundedImageView mProfileImageView;
    public ProgressBar mProfileImageLoader;
    private ProgressDialog mProgressDialog;
    private EditText phoneNumberView;
    private TextView countryCodeTextView, skip;
    private Spinner countryCodeSpinner;
    private CountrySpinnerAdapter adapter;
    private Button register;
    private LinearLayout loadingLayout;
    private String isoCode;
    private File profileImage;
    private IProfilePresenter<ProfileView> presenter = new ProfilePresenter();
    private boolean showSkip = true;
    private String previousPhone = "";
    private boolean fromSplashScreen;
    private CountryMaster cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI Views
        initUIViews();

        // Set Onboarding manager and attach presenter to view
        presenter.attachView(this);

        // Retrieve deeplink parameters from intent
        if (getIntent() != null && getIntent().hasExtra("class_from")) {
            if (getIntent().getStringExtra("class_from").
                    equalsIgnoreCase(SplashScreen.class.getSimpleName()))
                fromSplashScreen = true;
        }
    }

    private void initUIViews() {
        // Initialize UI Views before Attaching View Presenter
        nameView = (EditText) findViewById(R.id.profile_name);
        mProfileImageView = (RoundedImageView) findViewById(R.id.profile_image_view);
        mProfileImageLoader = (ProgressBar) findViewById(R.id.profile_image_loader);
        register = (Button) findViewById(R.id.register_profile);
        skip = (TextView) findViewById(R.id.register_skip);
        phoneNumberView = (EditText) findViewById(R.id.register_phone_number);
        countryCodeTextView = (TextView) findViewById(R.id.register_country_code);
        countryCodeSpinner = (Spinner) findViewById(R.id.register_country_codes_spinner);
        LinearLayout countryCodeLayout = (LinearLayout) findViewById(R.id.register_country_code_layout);
        countryCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCodeSpinner.performClick();
            }
        });

        // Initialize UI Action Listeners
        nameView.setOnEditorActionListener(mNameEditorActionListener);
        phoneNumberView.setOnEditorActionListener(mEditorActionListener);
        nameView.addTextChangedListener(mTextWatcher);
        phoneNumberView.addTextChangedListener(mTextWatcher);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInButtonClicked();
            }
        });
        loadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
        initCountryFlagSpinner();
    }

    private void initCountryFlagSpinner() {
        cm = new CountryMaster(this);
        final ArrayList<Country> countries = cm.getCountries();

        adapter = new CountrySpinnerAdapter(this, R.layout.view_country_list_item, countries);
        countryCodeSpinner.setAdapter(adapter);

        final String isoCountryCode = Utils.getCountryRegionFromPhone(this);
        Log.v(TAG, "Region ISO: " + isoCountryCode);

        if (!HTTextUtils.isEmpty(isoCountryCode)) {
            for (Country c : countries) {
                if (c.mCountryIso.equalsIgnoreCase(isoCountryCode)) {
                    countryCodeSpinner.setSelection(adapter.getPosition(c));
                    break;
                }
            }
        }

        countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isoCode = countries.get(position).mCountryIso;

                countryCodeTextView.setText("+ " + countries.get(position).mDialPrefix);
                countryCodeTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private TextView.OnEditorActionListener mNameEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (v.getId() == nameView.getId() && actionId == EditorInfo.IME_ACTION_NEXT) {
                phoneNumberView.requestFocus();
                return true;
            }

            return false;
        }
    };

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(Profile.this, phoneNumberView);
                return true;
            }

            return false;
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (HTTextUtils.isEmpty(nameView.getText().toString())
                    && HTTextUtils.isEmpty(phoneNumberView.getText().toString())) {
                showSkip = true;
                toggleRegisterButton();
            } else if (showSkip && !HTTextUtils.isEmpty(s.toString())) {
                showSkip = false;
                toggleRegisterButton();
            }
        }
    };

    public void onProfileImageViewClicked(View view) {
        // Create Image Chooser Intent if READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openChooser(Profile.this, "Please select", true);

        } else {
            // Show Rationale & Request for READ_EXTERNAL_STORAGE permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionUtils.showRationaleMessageAsDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.read_external_storage_permission_msg));
            } else {
                PermissionUtils.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    public void onSkipButtonClicked(View view) {
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            if (fromSplashScreen) {
                showProgress(true);
                onProfileUpdateSuccess();
                return;
            }
            finish();
            return;
        }
        onSignInButtonClicked();
    }

    private void onSignInButtonClicked() {
        showProgress(true);
        String firstName = nameView.getText().toString();
        String name = "";
        if (!HTTextUtils.isEmpty(firstName)) {
            name = firstName;
        }
        String number = phoneNumberView.getText().toString();

        Utils.hideKeyboard(Profile.this, register);
        boolean verifyPhone = false;
        if (!previousPhone.equalsIgnoreCase(number)) {
            verifyPhone = true;
        }
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            presenter.updateProfile(name, number, isoCode, profileImage, verifyPhone, this);
        } else
            presenter.attemptLogin(name, number, isoCode, profileImage, verifyPhone, this);
    }

    @Override
    public void updateViews(User user,  String ISOCode, String phone) {
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            skip.setText(R.string.cancel);
        }
        String nameFromAccount = getName();

        if (user.getName() != null) {
            nameView.setText(nameFromAccount);
            showSkip = false;
            toggleRegisterButton();

        }
        if (!HTTextUtils.isEmpty(user.getName())) {
            nameView.setText(user.getName());
        }

        if (!HTTextUtils.isEmpty(ISOCode)) {
            final ArrayList<Country> countries = cm.getCountries();
            for (Country c : countries) {
                if (c.mCountryIso.equalsIgnoreCase(ISOCode)) {
                    countryCodeSpinner.setSelection(adapter.getPosition(c));
                    break;
                }
            }
        }

        if (!HTTextUtils.isEmpty(phone)) {
            phone = phone.replaceAll("\\s", "");
            phoneNumberView.setText(phone);
            previousPhone = phone;
        }

        if (!HTTextUtils.isEmpty(user.getPhoto())) {
            mProfileImageLoader.setVisibility(View.VISIBLE);
            int pixel = (int) convertDpToPixel(getResources().getDimension(R.dimen.profile_image_size), Profile.this);
            Picasso.with(this)
                    .load(user.getPhoto())
                    .placeholder(R.drawable.default_profile_pic)
                    .error(R.drawable.default_profile_pic)
                    .centerCrop()
                    .resize(pixel, pixel)
                    .into(mProfileImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mProfileImageLoader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProfileImageLoader.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private String getName() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Account[] list = manager.getAccounts();

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                return account.name;
            }
        }
        return null;
    }

    @Override
    public void onProfileUpdateSuccess() {
        startHomeScreen();
    }

    private void startHomeScreen() {
        CrashlyticsWrapper.setCrashlyticsKeys(this);
        showProgress(false);

        // Clear Existing running trip on Registration Successful
        SharedPreferenceManager.deleteAction(this);
        SharedPreferenceManager.deletePlace(this);

        HyperLog.i(TAG, "User Registration successful: Clearing Active Trip, if any");
        Intent intent = new Intent(Profile.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder.create(Profile.this)
                .addNextIntentWithParentStack(intent)
                .startActivities();
        finish();
    }

    @Override
    public void navigateToVerifyCodeScreen() {
        showProgress(false);
        SharedPreferenceManager.deleteAction(this);
        SharedPreferenceManager.deletePlace(this);
        Intent intent = new Intent(Profile.this, Verify.class);
        intent.putExtra("branch_params", getIntent().getStringExtra("branch_params"));
        startActivity(intent);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        showProgress(false);
        Toast.makeText(Profile.this, "Error Occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void showProgress(boolean show) {
        if (show) {
            String progressDialogMessage = HTTextUtils.isEmpty(HyperTrack.getUserId()) ?
                    getString(R.string.create_profile_message) : getString(R.string.update_profile_message);
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(progressDialogMessage);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        } else if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showProfileLoading(boolean show) {
        if (show) {
            loadingLayout.setVisibility(View.VISIBLE);
        } else
            loadingLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                Toast.makeText(Profile.this, R.string.profile_pic_choose_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                try {
                    if (imageFile == null || !imageFile.canRead() || !imageFile.exists()) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                mProfileImageLoader.setVisibility(View.GONE);
                profileImage = ImageUtils.getScaledFile(Profile.this, imageFile);

                int pixel = (int) convertDpToPixel(getResources().getDimension(R.dimen.profile_image_size), Profile.this);
                Picasso.with(Profile.this).load(profileImage).centerCrop().resize(pixel, pixel).into(mProfileImageView);
            }
        });
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Read external storage permission successfully granted response
                onProfileImageViewClicked(null);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Handle Read external storage permission request denied error
                PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.read_external_storage_permission_never_allow));
            }
        }
    }

    private void toggleRegisterButton() {
        register.setEnabled(!showSkip);
    }

    @Override
    protected void onDestroy() {
        // Detach View Presenter from View
        presenter.detachView();
        super.onDestroy();
    }
}