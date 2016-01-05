package com.galaxas0.Quartz.library;

import android.content.Context;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.LibrarySource;

public class SearchLibraryAdapter extends LibraryFragment.LibraryAdapter {
    private int page = 1;
    private LibrarySource.Search query;

    public SearchLibraryAdapter(Context _context, int _identifier) {
        super(_context, _identifier);
    }

    public static SearchLibraryAdapter newInstance(Context context, int identifier) {
        return new SearchLibraryAdapter(context, identifier);
    }

    public void setQuery(String query) {
        page = 1;
        this.query = new LibrarySource.Search();
        this.query.name = query;
        this.populate();
    }

    protected void populate() {
        new Thread() {
            @Override
            public void run() {
                Library.getMangaForGenre/*getMangaForSearch*/(page, /*query*/ new LibrarySource.Genre(0), manga -> {
                    data = manga;

                    new Handler(context.getMainLooper()).post(() -> {
                        notifyItemRangeInserted(0, data.size());
                        setPopulated();
                    });
                });
            }
        }.start();
    }

    protected void append() {
    }

    protected void refresh() {
    }

    protected void createOptionsMenu(MenuInflater inflater, Menu menu) {
    }

    protected void optionsItemSelected(MenuItem item) {
    }
}
