package moe.yukisora.yandere.core;

import android.content.Context;
import android.graphics.Color;
import android.widget.Filter;

import java.util.ArrayList;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.modles.TagData;

public class TagFilter {
    private Filter filter;
    private OnFindSuggestionsListener listener;

    public TagFilter(final Context context) {
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignore) {
                }
                String query = charSequence.toString();

                ArrayList<TagData> list = new ArrayList<>();
                if (!query.equals("")) {
                    for (String tag : YandereApplication.getTags().keySet()) {
                        if (tag.startsWith(query)) {
                            list.add(new TagData(Color.parseColor(context.getResources().getStringArray(R.array.tagColor)[YandereApplication.getTags().get(tag)]), tag.replace("_", " ")));
                            if (list.size() == 10) {
                                break;
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = list;
                results.count = list.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listener.onResults(filterResults.values);
            }
        };
    }

    public void filter(String query, OnFindSuggestionsListener listener) {
        this.listener = listener;
        filter.filter(query);
    }

    public interface OnFindSuggestionsListener {
        void onResults(Object results);
    }
}
