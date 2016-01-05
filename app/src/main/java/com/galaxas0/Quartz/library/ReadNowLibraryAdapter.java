package com.galaxas0.Quartz.library;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.ReadingSession;

import java.util.List;
import java.util.Objects;

public class ReadNowLibraryAdapter extends LibraryFragment.LibraryAdapter implements ReadingSession.ReadingSessionListener {

    public static String READ_NOW_TYPES[] = {"Latest", "Starred", "Saved", "History"};

    private Activity ctx = ((Activity)context);

    public static class ReadNowType {
        public static final int LATEST = 0;
        public static final int STARRED = 1;
        public static final int SAVED = 2;
        public static final int HISTORY = 3;
    }

    public static class ReadNowPagerAdapter extends FragmentStatePagerAdapter {
        public ReadNowPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return LibraryFragment.newInstance(ReadNowLibraryAdapter.class, i);
        }

        @Override
        public int getCount() {
            return ReadNowLibraryAdapter.READ_NOW_TYPES.length;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return ReadNowLibraryAdapter.READ_NOW_TYPES[i];
        }
    }

    private int latestPage = 1;

    public ReadNowLibraryAdapter(Context context, int identifier) {
        super(context, identifier);
        ReadingSession.open(context.getApplicationContext());
        ReadingSession.registerOnSessionChangedListener(this);
    }

    public static ReadNowLibraryAdapter newInstance(Context context, int identifier) {
        return new ReadNowLibraryAdapter(context, identifier);
    }

    protected void populate() {
        Objects.requireNonNull(data);
        if (identifier == ReadNowType.LATEST) {
            Library.getLatestManga(latestPage, (List<Manga> manga) -> {
                Library.optionalSanitize(manga);

                final List<Manga> set = manga;
                set.removeAll(data);
                data.addAll(0, set);

                ctx.runOnUiThread(() -> {
                    Log.i("here", "found all of " + data.size() + " -> " + set.size());
                    notifyItemRangeInserted(0, set.size());
                    setPopulated();
                });
            });
        } else if (identifier > ReadNowType.LATEST) {
            if (identifier == ReadNowType.STARRED)
                data = ReadingSession.starred();
            if (identifier == ReadNowType.SAVED)
                data = ReadingSession.saved();
            else if (identifier == ReadNowType.HISTORY)
                data = ReadingSession.history();

            setPopulated();
            notifyItemRangeInserted(0, data.size());
        }
    }

    protected void append() {
        Objects.requireNonNull(data);
        if (identifier != ReadNowType.LATEST)
            return;

        final int origin = data.size();
        Library.getLatestManga(++latestPage, (List<Manga> manga) -> {
            Library.optionalSanitize(manga);

            data.addAll(manga);

            ctx.runOnUiThread(() -> notifyItemRangeInserted(origin, data.size()));
        });
    }

    protected void refresh() {
        Objects.requireNonNull(data);
        if (identifier == ReadNowType.LATEST) {
            Library.getLatestManga(1, (List<Manga> manga) -> {
                Library.optionalSanitize(manga);

                final List<Manga> set = manga;
                set.removeAll(data);
                data.addAll(0, set);
                ctx.runOnUiThread(() -> notifyItemRangeInserted(0, set.size()));
            });
        } else if (identifier > ReadNowType.LATEST) {
            if (identifier == ReadNowType.STARRED)
                data = ReadingSession.starred();
            if (identifier == ReadNowType.SAVED)
                data = ReadingSession.saved();
            else if (identifier == ReadNowType.HISTORY)
                data = ReadingSession.history();

            ctx.runOnUiThread(() -> {
                setPopulated();
                if (data != null) notifyItemRangeInserted(0, data.size());
            });
        }
    }

    @Override
    public void onSessionChanged() {
        this.refresh();
    }

    protected void createOptionsMenu(MenuInflater inflater, Menu menu) {
        inflater.inflate(R.menu.sort, menu);
        menu.findItem(R.id.action_sort).setVisible(false);
    }

    protected void optionsItemSelected(MenuItem item) {
    }
}
