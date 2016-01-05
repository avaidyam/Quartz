package com.galaxas0.Quartz.library;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Handler;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.LibrarySource;
import com.galaxas0.Quartz.manga.Manga;

import java.util.List;
import java.util.Objects;

public class ExploreLibraryAdapter extends LibraryFragment.LibraryAdapter {
    private int page = 1;
    private LibrarySource.Genre genre;

    public static class ExplorePagerAdapter extends FragmentStatePagerAdapter {
        List<String> genres = Library.getGenresList();

        public ExplorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return LibraryFragment.newInstance(ExploreLibraryAdapter.class, i);
        }

        @Override
        public int getCount() {
            return genres.size();
        }

        @Override
        public CharSequence getPageTitle(int i) {
            return genres.get(i);
        }
    }

    public ExploreLibraryAdapter(Context _context, int _identifier) {
        super(_context, _identifier);
        genre = new LibrarySource.Genre(identifier);
    }

    public static ExploreLibraryAdapter newInstance(Context context, int identifier) {
        return new ExploreLibraryAdapter(context, identifier);
    }

    public void setSort(LibrarySource.Genre.SortOrder sortOrder, boolean ascending) {
        page = 1;
        genre.sortOrder = sortOrder;
        genre.ascending = ascending;
        this.populate();
    }

    protected void populate() {
        Objects.requireNonNull(data);
        if (genre == null)
            genre = new LibrarySource.Genre(identifier);

        Library.getMangaForGenre(page, genre, (List<Manga> manga) -> {
            Library.optionalSanitize(manga);

            final List<Manga> set = manga;
            set.removeAll(data);
            data.addAll(0, set);

            setPopulated();
            new Handler(context.getMainLooper()).post(() -> notifyItemRangeInserted(0, set.size()));
        });
    }

    protected void append() {
        Objects.requireNonNull(data);
        Library.getMangaForGenre(++page, genre, (List<Manga> manga) -> {
            Library.optionalSanitize(manga);

            final List<Manga> set = manga;
            final int origin = data.size() - 1;
            data.addAll(set);

            new Handler(context.getMainLooper()).post(() -> notifyItemRangeInserted(origin, set.size()));
        });
    }

    protected void refresh() {
        Objects.requireNonNull(data);
        Library.getMangaForGenre(1, genre, (List<Manga> manga) -> {
            Library.optionalSanitize(manga);

            final List<Manga> set = manga;
            set.removeAll(data);
            data.addAll(0, set);

            new Handler(context.getMainLooper()).post(() -> notifyItemRangeInserted(0, set.size()));
        });
    }

    protected void createOptionsMenu(MenuInflater inflater, Menu menu) {
        inflater.inflate(R.menu.sort, menu);
        if (menu.findItem(R.id.menu_sort_alphabetical) != null) {
            if (genre.sortOrder == LibrarySource.Genre.SortOrder.ALPHABETICAL)
                menu.findItem(R.id.menu_sort_alphabetical).setChecked(true);
            else if (genre.sortOrder == LibrarySource.Genre.SortOrder.VIEWS)
                menu.findItem(R.id.menu_sort_views).setChecked(true);
            else if (genre.sortOrder == LibrarySource.Genre.SortOrder.UPDATES)
                menu.findItem(R.id.menu_sort_updates).setChecked(true);
            menu.findItem(R.id.menu_sort_type).setChecked(!genre.ascending);
        }
    }

    protected void optionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_alphabetical) {
            this.setSort(LibrarySource.Genre.SortOrder.ALPHABETICAL, genre.ascending);
            item.setChecked(true);
        } else if (item.getItemId() == R.id.menu_sort_views) {
            this.setSort(LibrarySource.Genre.SortOrder.VIEWS, genre.ascending);
            item.setChecked(true);
        } else if (item.getItemId() == R.id.menu_sort_updates) {
            this.setSort(LibrarySource.Genre.SortOrder.UPDATES, genre.ascending);
            item.setChecked(true);
        } else if (item.getItemId() == R.id.menu_sort_type) {
            this.setSort(genre.sortOrder, item.isChecked());
            item.setChecked(!item.isChecked());
        }
    }
}
