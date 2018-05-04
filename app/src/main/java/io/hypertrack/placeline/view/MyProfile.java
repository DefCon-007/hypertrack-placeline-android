package io.hypertrack.placeline.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.hypertrack.placeline.BuildConfig;
import io.hypertrack.placeline.R;
import io.hypertrack.placeline.store.SharedPreferenceManager;
import io.hypertrack.placeline.util.ImageUtils;
import io.hypertrack.placeline.util.PermissionUtils;
import io.hypertrack.placeline.util.images.DefaultCallback;
import io.hypertrack.placeline.util.images.EasyImage;
import io.hypertrack.placeline.util.images.RoundedImageView;

public class MyProfile extends AppCompatActivity {

    public EditText nameView;
    public RoundedImageView mProfileImageView;
    public ProgressBar mProfileImageLoader;
    Switch trackingSwitch;
    TextView appVersion;
    private File profileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        initView();
        setupView();
    }

    private void setupView() {
        if (!HTTextUtils.isEmpty(HyperTrack.getUserId())) {
            HyperTrack.getUser(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    User user = (User) response.getResponseObject();

                    updateProfileImage(user.getPhoto());
                    nameView.setText(user.getName());
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {

                }
            });
        }
    }

    private void updateProfileImage(String photo) {
        if (!HTTextUtils.isEmpty(photo)) {
            mProfileImageLoader.setVisibility(View.VISIBLE);
            int pixel = (int) com.hypertrack.lib.internal.common.util.Utils.convertDpToPixel(
                    getResources().getDimension(R.dimen.profile_image_size), MyProfile.this);
            Picasso.with(this)
                    .load(photo)
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

    private void initView() {
        nameView = findViewById(R.id.profile_name);
        mProfileImageView = findViewById(R.id.profile_image_view);
        mProfileImageLoader = findViewById(R.id.profile_progress_bar);
        trackingSwitch = findViewById(R.id.tracking_switch);
        appVersion = findViewById(R.id.app_version);
        appVersion.setText("Version: " + BuildConfig.VERSION_NAME);
        if (HyperTrack.isTracking()) {
            trackingSwitch.setChecked(true);
            trackingSwitch.setText(R.string.tracking_active);
        }

        trackingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HyperTrack.resumeTracking(new HyperTrackCallback() {
                        @Override
                        public void onSuccess(@NonNull SuccessResponse response) {
                            trackingSwitch.setText(R.string.tracking_active);
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {

                            trackingSwitch.setChecked(false);
                        }
                    });

                } else {
                    HyperTrack.pauseTracking();
                    trackingSwitch.setText(R.string.tracking_pause);
                }
            }
        });

        nameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    io.hypertrack.placeline.util.Utils.hideKeyboard(MyProfile.this, nameView);
                    updateProfile();
                    return true;
                }
                return false;
            }
        });
    }

    public void onProfileImageViewClicked(View view) {
        // Create Image Chooser Intent if READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openChooser(MyProfile.this, "Please select", true);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                Toast.makeText(MyProfile.this, R.string.profile_pic_choose_failed, Toast.LENGTH_SHORT).show();
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
                profileImage = ImageUtils.getScaledFile(MyProfile.this, imageFile);

                int pixel = (int) Utils.convertDpToPixel(getResources().getDimension(
                        R.dimen.profile_image_size), MyProfile.this);
                Picasso.with(MyProfile.this).load(profileImage).centerCrop().resize(pixel, pixel).into(mProfileImageView);
                updateProfile();
            }
        });
    }

    private void updateProfile() {
        String firstName = nameView.getText().toString();
        String name = "";
        if (!HTTextUtils.isEmpty(firstName)) {
            name = firstName;
        }

        UserParams userParams = getUserParams(name, profileImage);
        HyperTrack.updateUser(userParams, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {
                SharedPreferenceManager.setHyperTrackLiveUser(MyProfile.this,
                        (User) successResponse.getResponseObject());
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                Toast.makeText(MyProfile.this, "Profile Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private UserParams getUserParams(final String name, File profileImage) {

        String encodedImage = null;
        // Set user's profile image
        if (profileImage != null && profileImage.length() > 0) {
            byte[] bytes = convertFiletoByteArray(profileImage);
            if (bytes != null && bytes.length > 0)
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        return new UserParams()
                .setName(name)
                .setPhoto(encodedImage);
    }

    private byte[] convertFiletoByteArray(File file) {
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            return b;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
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

    public void onHiringClicked(View view) {
    }

    public void onPrivacyPolicyClicked(View view) {
    }

    public void onAboutHyperTrackClicked(View view) {
    }
}
