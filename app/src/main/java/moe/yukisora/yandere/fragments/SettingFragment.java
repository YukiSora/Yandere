package moe.yukisora.yandere.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.core.ServiceGenerator;
import moe.yukisora.yandere.interfaces.YandereService;
import moe.yukisora.yandere.modles.TagsData;
import retrofit2.Call;
import retrofit2.Response;

public class SettingFragment extends PreferenceFragment {
    private SharedPreferences preferences;
    private Preference updatePreference;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        PreferenceCategory settingCategory = (PreferenceCategory)findPreference("setting");
        Preference isSafePreference = findPreference("isSafe");
        updatePreference = findPreference("update");

        // is safe
        isSafePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                YandereApplication.setSafe((boolean)newValue);

                return true;
            }
        });
        if (!YandereApplication.isEnableRating()) {
            settingCategory.removePreference(isSafePreference);
        }

        // update
        String lastUpdate = preferences.getString("lastUpdate", getString(R.string.last_update_none));
        updatePreference.setSummary(lastUpdate);
        updatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new UpdateTagsTask().execute();

                return true;
            }
        });

        findPreference("license").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.licenses));
                builder.setMessage(Html.fromHtml(getResources().getString(R.string.license_list)));
                AlertDialog alert = builder.create();
                alert.show();
                ((TextView)alert.findViewById(android.R.id.message)).setTextSize(10);
                ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            view.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.bar_height));
        }

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateTagsTask extends AsyncTask<Void, Integer, TagsData<HashMap<String, Integer>>> {
        private final int DOWNLOAD_FAILED = 1;
        private final int UP_TO_DATE = 2;
        private final int START_PARSING = 3;
        private final int START_SAVING = 4;
        private final int SAVE_FAILED = 5;
        private SpotsDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new SpotsDialog(getActivity(), getString(R.string.downloading));
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            dialog.show();
        }

        @Override
        protected TagsData<HashMap<String, Integer>> doInBackground(Void... params) {
            TagsData<HashMap<String, Integer>> tagsData = null;

            // download
            Call<TagsData<String>> call = ServiceGenerator.generate(YandereService.class).getTags();
            TagsData<String> newTagsData = null;
            try {
                Response<TagsData<String>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    newTagsData = response.body();
                }
                else {
                    publishProgress(DOWNLOAD_FAILED);
                }
            } catch (IOException e) {
                publishProgress(DOWNLOAD_FAILED);
            }

            if (newTagsData != null) {
                if (newTagsData.version > YandereApplication.getTagsVersion()) {
                    // parse
                    publishProgress(START_PARSING);
                    tagsData = new TagsData<>();
                    tagsData.version = newTagsData.version;
                    HashMap<String, Integer> data = new HashMap<>();
                    tagsData.data = data;
                    int lastDigit = -1;
                    for (String tag : newTagsData.data.replaceAll("\\s", "").split("`")) {
                        if (!(tag.length() == 1 && tag.charAt(0) >= '0' && tag.charAt(0) <= '9')) {
                            if (lastDigit != -1) {
                                data.put(tag, lastDigit);
                            }
                            else {
                                lastDigit = -1;
                            }
                        }
                        else {
                            lastDigit = tag.charAt(0) - '0';
                        }
                    }

                    // save
                    publishProgress(START_SAVING);
                    try (FileWriter out = new FileWriter(new File(getActivity().getFilesDir(), YandereApplication.TAGS_FILENAME))) {
                        new Gson().toJson(tagsData, out);
                    } catch (IOException e) {
                        publishProgress(SAVE_FAILED);
                    }
                }
                else {
                    publishProgress(UP_TO_DATE);
                }
            }

            return tagsData;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            switch (values[0]) {
                case DOWNLOAD_FAILED:
                    Toast.makeText(SettingFragment.this.getActivity(), getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
                    break;
                case UP_TO_DATE:
                    Toast.makeText(SettingFragment.this.getActivity(), getString(R.string.tags_may_be_able_to_update), Toast.LENGTH_SHORT).show();
                    break;
                case START_PARSING:
                    dialog.setMessage(getString(R.string.parsing));
                    break;
                case START_SAVING:
                    dialog.setMessage(getString(R.string.saving));
                    break;
                case SAVE_FAILED:
                    Toast.makeText(SettingFragment.this.getActivity(), getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected void onPostExecute(TagsData<HashMap<String, Integer>> tagsData) {
            if (tagsData != null) {
                YandereApplication.setTagsData(tagsData);

                String date = SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("lastUpdate", getString(R.string.last_update) + date);
                editor.apply();
                updatePreference.setSummary(getString(R.string.last_update) + date);

                Toast.makeText(SettingFragment.this.getActivity(), getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(SettingFragment.this.getActivity(), getString(R.string.update_cancelled), Toast.LENGTH_SHORT).show();
        }
    }
}
