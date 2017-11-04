package moe.yukisora.yandere.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Filter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.TagData;

public class TagFilter {
    private ArrayList<String> history;
    private Context context;
    private Filter filter;
    private Gson gson;
    private OnFindSuggestionsListener listener;

    @SuppressLint("StaticFieldLeak")
    public TagFilter(final Context context) {
        this.context = context;

        history = new ArrayList<>();
        gson = new Gson();
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignore) {
                }
                String query = charSequence.toString();

                ArrayList<TagData> list;
                if (!query.equals("")) {
                    list = new ArrayList<>();
                    for (String tag : YandereApplication.getTags().keySet()) {
                        if (tag.startsWith(query)) {
                            int color = Color.parseColor(context.getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().get(tag)]);
                            list.add(new TagData(color, tag.replace("_", " ")));
                            if (list.size() == 10) {
                                break;
                            }
                        }
                    }
                }
                else {
                    list = getHistory();
                }

                FilterResults results = new FilterResults();
                results.values = list;
                results.count = list.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listener.onResults((ArrayList<TagData>)filterResults.values);
            }
        };

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try (Reader in = new FileReader(new File(context.getFilesDir(), YandereApplication.SEARCH_HISTORY_FILENAME))) {
                    history = gson.fromJson(in, new TypeToken<ArrayList<String>>() {}.getType());
                } catch (IOException ignore) {
                }

                return null;
            }
        }.execute();
    }

    public void getSuggestions(String query, OnFindSuggestionsListener listener) {
        this.listener = listener;

        filter.filter(query);
    }

    public ArrayList<TagData> getHistory() {
        ArrayList<TagData> list = new ArrayList<>();
        for (String tag : history) {
            int color = Color.parseColor(context.getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().containsKey(tag) ? YandereApplication.getTags().get(tag) : 0]);
            list.add(new TagData(color, tag.replace("_", " "), true));
        }

        return list;
    }

    @SuppressLint("StaticFieldLeak")
    public void addHistory(String tag) {
        if (!history.contains(tag)) {
            history.add(0, tag);
            if (history.size() > 5) {
                history.remove(5);
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try (FileWriter out = new FileWriter(new File(context.getFilesDir(), YandereApplication.SEARCH_HISTORY_FILENAME))) {
                        gson.toJson(history, out);
                    } catch (IOException ignore) {
                    }

                    return null;
                }
            }.execute();
        }
    }

    public interface OnFindSuggestionsListener {
        void onResults(ArrayList<TagData> results);
    }
}
