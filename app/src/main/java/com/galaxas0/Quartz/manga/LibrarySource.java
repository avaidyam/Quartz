package com.galaxas0.Quartz.manga;

import android.net.Uri;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;

public interface LibrarySource {
    class Genre {
        public String name;
        public boolean ascending = false;
        public SortOrder sortOrder = SortOrder.VIEWS;

        public enum SortOrder {
            ALPHABETICAL,
            UPDATES,
            VIEWS
        }

        public Genre(int index) {
            this.name = Library.getGenresList().get(index);
        }
    }

    class Search {
        public static final String SEARCH_MANGA = "rl";
        public static final String SEARCH_MANHWA = "lr";
        public static final String SEARCH_NEITHER = "";

        public static final String SEARCH_CONTAINS = "cw";
        public static final String SEARCH_BEGINSWITH = "bw";
        public static final String SEARCH_ENDSWITH = "ew";

        public static final String SEARCH_BEFORE = "lt";
        public static final String SEARCH_AFTER = "qt";
        public static final String SEARCH_ON = "eq";

        public static final int SEARCH_YES = 1;
        public static final int SEARCH_NO = 2;
        public static final int SEARCH_SAME = 0;

        public String direction = SEARCH_NEITHER;

        public String nameMethod = SEARCH_CONTAINS;
        public String name = "";
        public String authorMethod = SEARCH_CONTAINS;
        public String author = "";
        public String artistMethod = SEARCH_CONTAINS;
        public String artist = "";

        public int[] genres; // genres["genre"]=1

        public String releasedMethod = SEARCH_AFTER;
        public int released = 0;

        public boolean isCompleted = false;
        public final int advOpts = 1;

        public Search() {
            genres = new int[Library.getGenresList().size()];
        }
    }

    String getName();

    String getRegisteredURL();

    List<String> getGenres();

    boolean isGenreMarkedNSFW(String genre);

    List<Manga> getLatestManga(final int page);

    List<Manga> getMangaForGenre(final int page, final LibrarySource.Genre genre);

    List<Manga> getMangaSuggestionsForQuery(final String query);

    List<Manga> getMangaForSearch(final int page, final LibrarySource.Search query);

    Manga getMangaDescription(final Manga manga);

    Manga getMangaInformation(final Manga manga);

    List<Future<Uri>> getPagesForChapter(final Manga manga, final int idx);
}
