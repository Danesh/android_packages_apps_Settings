package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.bluetooth.DockEventReceiver;

public class HeadsetSettings extends SettingsPreferenceFragment implements
        OnDismissListener, DialogInterface.OnClickListener {

    private static final String KEY_HEADSET_CONNECT_PLAYER = "headset_connect_player";
    private static final String KEY_SAFE_HEADSET_VOLUME = "safe_headset_volume";

    private CheckBoxPreference mHeadsetConnectPlayer;
    private CheckBoxPreference mSafeHeadsetVolume;

    // To track whether a confirmation dialog was clicked.
    private boolean mDialogClicked;
    private Dialog mWaiverDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.headset_settings);

        ContentResolver resolver = getContentResolver();

        mHeadsetConnectPlayer = (CheckBoxPreference) findPreference(KEY_HEADSET_CONNECT_PLAYER);
        mHeadsetConnectPlayer.setChecked(Settings.System.getInt(resolver,
                Settings.System.HEADSET_CONNECT_PLAYER, 0) != 0);

        mSafeHeadsetVolume = (CheckBoxPreference) findPreference(KEY_SAFE_HEADSET_VOLUME);
        mSafeHeadsetVolume.setPersistent(false);
        boolean safeMediaVolumeEnabled = getResources().getBoolean(
                com.android.internal.R.bool.config_safe_media_volume_enabled);
        mSafeHeadsetVolume.setChecked(Settings.System.getInt(resolver,
                Settings.System.SAFE_HEADSET_VOLUME, safeMediaVolumeEnabled ? 1 : 0) != 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHeadsetConnectPlayer) {
            Settings.System.putInt(getContentResolver(), Settings.System.HEADSET_CONNECT_PLAYER,
                    mHeadsetConnectPlayer.isChecked() ? 1 : 0);
        } else if (preference == mSafeHeadsetVolume) {
            if (!mSafeHeadsetVolume.isChecked()) {
                // User is trying to disable the feature, display the waiver
                mDialogClicked = false;
                if (mWaiverDialog != null) {
                    dismissDialog();
                }
                mWaiverDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.cyanogenmod_waiver_body)
                        .setTitle(R.string.cyanogenmod_waiver_title)
                        .setPositiveButton(R.string.ok, this)
                        .setNegativeButton(R.string.cancel, this)
                        .show();
                mWaiverDialog.setOnDismissListener(this);
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.SAFE_HEADSET_VOLUME, 1);
            }
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWaiverDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mDialogClicked = true;
                Settings.System.putInt(getContentResolver(), Settings.System.SAFE_HEADSET_VOLUME, 0);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (dialog == mWaiverDialog) {
            if (!mDialogClicked) {
                mSafeHeadsetVolume.setChecked(true);
            }
            mWaiverDialog = null;
        }
    }

    private void dismissDialog() {
        if (mWaiverDialog != null) {
            mWaiverDialog.dismiss();
            mWaiverDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }
}
