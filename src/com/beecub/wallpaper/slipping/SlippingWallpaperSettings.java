package com.beecub.wallpaper.slipping;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SlippingWallpaperSettings extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener {

@Override
protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    getPreferenceManager().setSharedPreferencesName(
            SlippingWallpaper.SHARED_PREFS_NAME);
    addPreferencesFromResource(R.xml.console_settings);
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
            this);
}

@Override
protected void onResume() {
    super.onResume();
}

@Override
protected void onDestroy() {
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
            this);
    super.onDestroy();
}

public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        String key) {
}
}
