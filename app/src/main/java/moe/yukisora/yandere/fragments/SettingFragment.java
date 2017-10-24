package moe.yukisora.yandere.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
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

        updatePreference = findPreference("update");

        findPreference("isSafe").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                YandereApplication.setSafe((boolean)newValue);

                return true;
            }
        });


        String lastUpdate = preferences.getString("lastUpdate", "Last update: -");
        updatePreference.setSummary(lastUpdate);
        updatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new UpdateTagsTask().execute();

                return true;
            }
        });
    }

    private class UpdateTagsTask extends AsyncTask<Void, Integer, TagsData<HashMap<String, Integer>>> {
        private final int DOWNLOAD_FAILED = 1;
        private final int UP_TO_DATE = 2;
        private final int START_PARSING = 3;
        private final int START_SAVING = 4;
        private final int SAVE_FAILED = 5;
        private SpotsDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new SpotsDialog(getActivity(), "Downloading...");
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

                    publishProgress(START_SAVING);
                    try (OutputStreamWriter out = new OutputStreamWriter(getActivity().openFileOutput(YandereApplication.TAGS_FILENAME, Context.MODE_PRIVATE))) {
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
                    Toast.makeText(SettingFragment.this.getActivity(), "Download failed", Toast.LENGTH_SHORT).show();
                    break;
                case UP_TO_DATE:
                    Toast.makeText(SettingFragment.this.getActivity(), "Tags are up to date", Toast.LENGTH_SHORT).show();
                    break;
                case START_PARSING:
                    dialog.setMessage("Parsing...");
                    break;
                case START_SAVING:
                    dialog.setMessage("Saving...");
                    break;
                case SAVE_FAILED:
                    Toast.makeText(SettingFragment.this.getActivity(), "Save failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        protected void onPostExecute(TagsData<HashMap<String, Integer>> tagsData) {
            if (tagsData != null) {
                YandereApplication.setTagsData(tagsData);
                String date = SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("lastUpdate", "Last update: " + date);
                editor.apply();
                updatePreference.setSummary("Last update: " + date);

                Toast.makeText(SettingFragment.this.getActivity(), "Update successful", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(SettingFragment.this.getActivity(), "Update cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
