package moe.yukisora.yandere;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment {
    public static SettingFragment newInstance() {
        Bundle args = new Bundle();
        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }
}
