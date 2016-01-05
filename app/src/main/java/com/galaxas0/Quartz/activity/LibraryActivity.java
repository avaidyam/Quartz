package com.galaxas0.Quartz.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.databinding.ActivityLibraryBinding;
import com.galaxas0.Quartz.library.ExploreLibraryAdapter.ExplorePagerAdapter;
import com.galaxas0.Quartz.library.ReadNowLibraryAdapter.ReadNowPagerAdapter;
import com.galaxas0.Quartz.manga.Chapter;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.MangaHere;
import com.galaxas0.Quartz.service.PrefetchService;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.SerializablePair;
import com.galaxas0.Quartz.utils.StringUtils;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.util.List;

//
// FIXME - Reader pages aspect ratio is wrong
// FIXME - PageSession clipping does not work correctly
// FIXME - PageSession wrapping does not have proper chapter/page info
// FIXME - Verify keylines, font sizes
// FIXME - Add Save image Intent to Reader
// FIXME - Manga constructor + link property handling
// FIXME - Multiple sources support as a clicked link
// FIXME - Detail view as dialog in large width
//
// TODO - Saved + History Sync
// TODO - Search Library
// TODO - Download + Cache Manga
//

public class LibraryActivity extends QuartzActivity {
    int section = 0, page = 0;

    private ActivityLibraryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Library.setCurrentSource(MangaHere.class);

        PrefetchService.removeNotification(this);
        PrefetchService.scheduleJob(this, null);
        handleIntent();

        setTheme(ThemeUtils.getThemeResource(this, ThemeUtils.preferences(this).getInt("themeColor", Color.parseColor("#212121"))));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_library);

        if (!ThemeUtils.preferences(this).getBoolean("setup_complete", false))
            startActivity(new Intent(this, WelcomeActivity.class));

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        binding.toolbar.setNavigationOnClickListener((view) -> binding.drawerLayout.openDrawer(Gravity.LEFT));

        binding.navView.setNavigationItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                binding.drawerLayout.closeDrawers();
                return false;
            }  else if(menuItem.getItemId() == R.id.action_readnow) {
                selectItem(0);
                binding.drawerLayout.closeDrawers();
                return false;
            } else if(menuItem.getItemId() == R.id.action_explore) {
                selectItem(1);
                binding.drawerLayout.closeDrawers();
                return false;
            }
            return true;
        });

        binding.fab.setOnClickListener(v -> {
            Log.d("Library", "Search activated.");
        });

        binding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.pagerTabs));
        binding.pagerTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.pagerTabs.setTabGravity(TabLayout.GRAVITY_CENTER);

        selectItem(section);
        binding.pager.setCurrentItem(page);

        addPreferenceListener("themeColor", pref -> runWhenVisible(this::recreate));
    }

    public void selectItem(int position) {
        section = position;
        if (position == 0 && binding.pager != null) {
            //spinner.setSelection(0);
            binding.toolbar.setTitle("Read Now");
            binding.pager.setAdapter(new ReadNowPagerAdapter(getFragmentManager()));
            binding.pager.setOffscreenPageLimit(10);
            binding.pagerTabs.setupWithViewPager(binding.pager);
        } else if (position == 1 && binding.pager != null) {
            //spinner.setSelection(1);
            binding.toolbar.setTitle("Explore");
            binding.pager.setAdapter(new ExplorePagerAdapter(getFragmentManager()));
            binding.pager.setOffscreenPageLimit(2);
            binding.pagerTabs.setupWithViewPager(binding.pager);
        }
    }

    private void handleIntent() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getScheme() != null) {
            final String forwarding = StringUtils.removeStart(getIntent().getDataString(), "http://www.mangahere.co/");
            final String components[] = StringUtils.split(forwarding, '/');

            if (components[0].equals("directory") || components[0].equals("mangalist"))
                components[0] = "all";
            else components[0] = components[0].replace('_', ' ');

            if (components[0].equals("manga") && components.length == 2) {
                Intent outgoing = new Intent(this, DetailActivity.class);
                Manga m = new Manga(components[1]);
                outgoing.putExtra("manga", m.toJSONString());
                startActivity(outgoing);
                finish();
            } else if (components[0].equals("manga") && components.length > 2) {
                if (components[components.length - 1].startsWith("c")) {
                    final double c = Double.valueOf(components[components.length - 1].substring(1));
                    Library.getMangaInformation(new Manga(components[1]), manga -> {
                        int position = 0;
                        List<Chapter> list = manga.chapters();
                        for (Chapter chap : list)
                            if (chap.chapter() == c) {
                                position = list.indexOf(chap);
                                break;
                            }
                        final int pos = position;
                        runOnUiThread(() -> {
                            Intent outgoing = new Intent(LibraryActivity.this, ReaderActivity.class);
                            outgoing.putExtra("manga", manga.toJSONString());
                            outgoing.putExtra("index", pos);
                            if (preferences.getBoolean("startNewTask", false))
                                outgoing.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            startActivity(outgoing);
                        });
                    });
                    finish();
                }
            } else if (Library.getGenresList().contains(components[0])) {
                section = 1;
                page = Library.getGenresList().indexOf(components[0]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Create, configure, and add the SearchView.
        final SearchView searchView = new SearchView(this);
        MenuItem item = menu.add(android.R.string.search_go);
        item.setIcon(R.drawable.ic_search_white_18dp);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        item.setActionView(searchView);

        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(LibraryActivity.this,
                R.layout.search_suggestion,
                null, new String[]{"text"}, new int[]{R.id.list_text}, 0));

        final SerializablePair.ObjectHolder selection = new SerializablePair.ObjectHolder();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent search = new Intent(LibraryActivity.this, SearchActivity.class);
                search.setAction(Intent.ACTION_SEARCH);
                search.putExtra(SearchManager.QUERY, s);
                startActivity(search);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if ("".equals(s.trim())) return true;
                Library.getMangaSuggestionsForQuery(s, manga -> {
                    selection.object = manga;
                    final MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "text"});
                    for (int i = 0; i < manga.size(); i++) {
                        final int r = i;
                        cursor.addRow(new String[]{r + "", manga.get(i).title()});
                    }
                    runOnUiThread(() -> searchView.getSuggestionsAdapter().swapCursor(cursor));
                });
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return true;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean onSuggestionClick(int i) {
                Intent outgoing = new Intent(LibraryActivity.this, DetailActivity.class);
                outgoing.putExtra("manga", ((List<Manga>) selection.object).get(i).toJSONString());
                startActivity(outgoing);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
