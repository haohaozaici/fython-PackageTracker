package info.papdt.express.helper.ui.fragment.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.BoolRes;

import info.papdt.express.helper.R;
import info.papdt.express.helper.support.Settings;

public class SettingsUi extends AbsPrefFragment implements Preference.OnPreferenceChangeListener {

    private SwitchPreference mPrefNavigationTint;
    private SwitchPreference mPrefNavigationUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_ui);

        /** findPreference */
        mPrefNavigationTint = (SwitchPreference) findPreference("navigation_tint");
        mPrefNavigationUI = (SwitchPreference) findPreference("navigation_UI");

        /** Default value */
        mPrefNavigationTint.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        mPrefNavigationTint.setChecked(getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true));
        mPrefNavigationUI.setChecked(getSettings().getBoolean(Settings.KEY_NAVIGATION_UI, false));

        /** Set callback */
        mPrefNavigationTint.setOnPreferenceChangeListener(this);
        mPrefNavigationUI.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object o) {
        if (pref == mPrefNavigationTint) {
            Boolean b = (Boolean) o;
            getSettings().putBoolean(Settings.KEY_NAVIGATION_TINT, b);
            makeRestartTips();
            return true;
        } else if (pref == mPrefNavigationUI) {
            Boolean b = (Boolean) o;
            getSettings().putBoolean(Settings.KEY_NAVIGATION_UI, b);
            makeRestartTipsClear();
        }
        return false;
    }

}
