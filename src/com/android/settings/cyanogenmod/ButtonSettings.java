package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.view.IWindowManager;

public class ButtonSettings extends SettingsPreferenceFragment {

    private static final String KEY_POWER_MENU = "power_menu";
    private static final String KEY_HOME_WAKE = "pref_home_wake";
    private static final String KEY_VOLUME_WAKE = "pref_volume_wake";
    private static final String KEY_HARDWARE_KEYS = "hardware_keys";
    private static final String KEY_VOLBTN_MUSIC_CTRL = "volbtn_music_controls";

    private CheckBoxPreference mHomeWake;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mVolBtnMusicCtrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_settings);
        Resources res = getResources();
        ContentResolver resolver = getActivity().getContentResolver();

        // Home button wake
        mHomeWake = (CheckBoxPreference) findPreference(KEY_HOME_WAKE);
        if (mHomeWake != null) {
            if (!res.getBoolean(R.bool.config_show_homeWake)) {
                removePreference(KEY_HOME_WAKE);
            } else {
                mHomeWake.setChecked(Settings.System.getInt(resolver,
                        Settings.System.HOME_WAKE_SCREEN, 1) == 1);
            }
        }

        // Volume rocker wake
        mVolumeWake = (CheckBoxPreference) findPreference(KEY_VOLUME_WAKE);
        if (mVolumeWake != null) {
            if (!res.getBoolean(R.bool.config_show_volumeRockerWake)
                    || !Utils.hasVolumeRocker(getActivity())) {
                removePreference(KEY_VOLUME_WAKE);
            } else {
                mVolumeWake.setChecked(Settings.System.getInt(resolver,
                        Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);
            }
        }

        IWindowManager windowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        try {
            if (windowManager.hasNavigationBar()) {
                removePreference(KEY_HARDWARE_KEYS);
            }
        } catch (RemoteException e) {
            // Do nothing
        }

        mVolBtnMusicCtrl = (CheckBoxPreference) findPreference(KEY_VOLBTN_MUSIC_CTRL);
        if (mVolBtnMusicCtrl != null) {
            if (!Utils.hasVolumeRocker(getActivity())) {
                getPreferenceScreen().removePreference(mVolBtnMusicCtrl);
            } else {
                mVolBtnMusicCtrl.setChecked(Settings.System.getInt(resolver,
                        Settings.System.VOLBTN_MUSIC_CONTROLS, 1) != 0);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHomeWake) {
            Settings.System.putInt(getContentResolver(), Settings.System.HOME_WAKE_SCREEN,
                    mHomeWake.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mVolumeWake) {
            Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                    mVolumeWake.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mVolBtnMusicCtrl) {
            Settings.System.putInt(getContentResolver(), Settings.System.VOLBTN_MUSIC_CONTROLS,
                    mVolBtnMusicCtrl.isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
