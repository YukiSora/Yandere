package moe.yukisora.yandere;

import android.app.Fragment;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private ArrayList<String> suggestion;
    private ArrayAdapter adapter;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        suggestion = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.suggestion_item_view, suggestion) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView)super.getView(position, convertView, parent);

                textView.setTextColor(Color.parseColor(getResources().getStringArray(R.array.tagColor)[MainActivity.getTags().get(textView.getText().toString())]));

                return textView;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        final SearchView searchView = (SearchView)view.findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.setFocusable(false);
        searchView.clearFocus();
        searchView.setSearchableInfo(MainActivity.getSearchManager().getSearchableInfo(new ComponentName("moe.yukisora.yandere", "moe.yukisora.yandere.SearchActivity")));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.contains(" ")) {
                    searchView.setQuery(newText.replace(' ', '_'), false);
                }
                else {
                    suggestion.clear();
                    if (!newText.equals(""))
                        for (String tag : MainActivity.getTags().keySet())
                            if (tag.startsWith(newText))
                                suggestion.add(tag);

                    adapter.notifyDataSetChanged();
                }

                return true;
            }
        });

        final ListView listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(((TextView)view).getText().toString(), true);
            }
        });

        return view;
    }
}
