package com.galaxas0.Quartz.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.library.LibraryFragment;
import com.galaxas0.Quartz.library.SearchLibraryAdapter;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.ThemeUtils;

public class SearchActivity extends QuartzActivity {
    LibraryFragment frag;
    SearchView searchView;
    String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeUtils.getThemeResource(this, preferences.getInt("themeColor", Color.parseColor("#212121"))));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        onNewIntent(getIntent());

        getFragmentManager().
                beginTransaction().
                replace(R.id.container_search, (frag = LibraryFragment.newInstance(SearchLibraryAdapter.class, 0))).
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_ENTER_MASK).
                commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!Intent.ACTION_SEARCH.equals(intent.getAction()))
            return;
        query = intent.getStringExtra(SearchManager.QUERY);
        if (searchView != null) searchView.setQuery(query, false);
        beginSearchQuery(query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                beginSearchQuery(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setIconified(false);
        searchView.setQuery(query, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void beginSearchQuery(String query) {
        Toast.makeText(this, "Searching for \"" + query + "\"", Toast.LENGTH_SHORT).show();
        if (frag != null)
            ((SearchLibraryAdapter) frag.getAdapter()).setQuery(query);
    }
}
