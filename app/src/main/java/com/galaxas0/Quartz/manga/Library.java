package com.galaxas0.Quartz.manga;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Future;
import java8.util.concurrent.ForkJoinPool;
import java8.util.function.Consumer;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class Library {

    // r18 censor can be set here.
    public static boolean r18 = true;

    private static LibrarySource currentSource;
    private static final ArrayList<LibrarySource> sources = new ArrayList<>();
    private static final ArrayList<WeakReference<Runnable>> sourcesAdded = new ArrayList<>();
    private static final ArrayList<WeakReference<Runnable>> sourcesRemoved = new ArrayList<>();

    private static int count = Runtime.getRuntime().availableProcessors() * 2;
    public static ForkJoinPool defaultExecutor = new ForkJoinPool(count);
    public static ForkJoinPool infoExecutor = defaultExecutor; //compat
    public static ForkJoinPool pageExecutor = defaultExecutor; //compat

    static Future<Manga> getMangaDescription(final Manga manga) {
        return defaultExecutor.submit(() -> currentSource.getMangaDescription(manga));
    }

    public static List<String> getGenresList() {
        if(r18) return currentSource.getGenres();
        return StreamSupport.stream(currentSource.getGenres())
                .filter(m -> !currentSource.isGenreMarkedNSFW(m))
                .collect(Collectors.toList());
    }

    public static void getLatestManga(final int page, final Consumer<List<Manga>> r) {
        final Consumer<List<Manga>> receipt = r != null ? r : a -> {};
        defaultExecutor.submit(() -> receipt.accept(currentSource.getLatestManga(page)));
    }

    public static void getMangaForGenre(final int page, final LibrarySource.Genre genre, final Consumer<List<Manga>> r) {
        final Consumer<List<Manga>> receipt = r != null ? r : a -> {};
        if (!currentSource.getGenres().contains(genre.name.toLowerCase()) || page <= 0) {
            receipt.accept(new ArrayList<>());
            return;
        }

        defaultExecutor.submit(() -> receipt.accept(currentSource.getMangaForGenre(page, genre)));
    }

    public static void getMangaSuggestionsForQuery(final String query, final Consumer<List<Manga>> r) {
        final Consumer<List<Manga>> receipt = r != null ? r : a -> {};
        defaultExecutor.submit(() -> receipt.accept(currentSource.getMangaSuggestionsForQuery(query)));
    }

    public static void getMangaForSearch(final int page, final LibrarySource.Search query, final Consumer<List<Manga>> r) {
        final Consumer<List<Manga>> receipt = r != null ? r : a -> {};
        defaultExecutor.submit(() -> receipt.accept(currentSource.getMangaForSearch(page, query)));
    }

    public static void getMangaInformation(final Manga manga, final Consumer<Manga> r) {
        final Consumer<Manga> receipt = r != null ? r : a -> {};
        if (manga == null) return;
        if (manga.level() == Manga.MangaDetailLevel.Complete) {
            infoExecutor.execute(() -> receipt.accept(manga));
            return;
        }
        infoExecutor.submit(() -> receipt.accept(currentSource.getMangaInformation(manga)));
    }

    public static void getPagesForChapter(final Manga manga, final int idx, final Consumer<List<Future<Uri>>> r) {
        final Consumer<List<Future<Uri>>> receipt = r != null ? r : a -> {};
        pageExecutor.submit(() -> receipt.accept(currentSource.getPagesForChapter(manga, idx)));
    }

    // Multiple Sources

    public static void registerSource(LibrarySource source) {
        sources.add(source);
        for(WeakReference<Runnable> listener : sourcesAdded) {
            if(listener.get() != null)
                listener.get().run();
        }
    }

    public static void unregisterSource(LibrarySource source) {
        sources.remove(source);
        for(WeakReference<Runnable> listener : sourcesRemoved) {
            if(listener.get() != null)
                listener.get().run();
        }
    }

    public static List<LibrarySource> getSources() {
        return sources;
    }

    public static LibrarySource getCurrentSource() {
        return currentSource;
    }

    public static void setCurrentSource(Class<? extends LibrarySource> sourceClass) {
        try {
            currentSource = sourceClass.newInstance();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addSourceAddedListener(Runnable sourceListener) {
        sourcesAdded.add(new WeakReference<>(sourceListener));
    }

    public static void addSourceRemovedListener(Runnable sourceListener) {
        sourcesRemoved.add(new WeakReference<>(sourceListener));
    }

    public static void removeSourceAddedListener(Runnable sourceListener) {
        sourcesAdded.remove(sourceListener);
    }

    public static void removeSourceRemovedListener(Runnable sourceListener) {
        sourcesRemoved.remove(sourceListener);
    }

    // NSFW Facilities

    public static boolean isMangaMarkedNSFW(Manga manga) {
        return StreamSupport.stream(manga.genres())
                .filter(currentSource::isGenreMarkedNSFW)
                .count() > 0;
    }

    public static boolean isGenreMarkedNSFW(String genre) {
        return currentSource.isGenreMarkedNSFW(genre);
    }

    public static List<Manga> optionalSanitize(List<Manga> manga) {
        if(r18) return manga;
        List a = StreamSupport.stream(manga)
                .filter(Library::isMangaMarkedNSFW)
                .collect(Collectors.toList());
        manga.removeAll(a);
        return manga;
    }

    // -----

    @Deprecated
    public static class _MangaGuardHolder {
        Future<?> _token = null;

        @Deprecated
        public void _wait() {
            if (_token == null)
                return;

            try {
                _token.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //
    // LATEST:
    // manga: div.manga_updates > dl
    // links: div.manga_updates > dl > dt > a.manga_info
    // updates: div.manga_updates > dl > dd
    // times: div.manga_updates > dl > dt > span.time
    //
    private static List<Manga> _latest = new ArrayList<>();
    private static long _cacheTime = 0, _lastPage = 0;

    private static boolean _cacheStale() {
        return ((System.nanoTime() - _cacheTime) / 1e9) >= (30 * 60);
    }
    // FIXME: Cache Stale

    public static PageSession openPageSession(Manga manga, int chapterIndex) {
        PageSession ps = new PageSession(manga, 50);
        ps.add(chapterIndex);
        ps.bookmark = chapterIndex;
        return ps;
    }

    public static Document _documentForURL(final URL link, final int maxTries) {
        return _documentForURL(link.toString(), maxTries);
    }

    public static Document _documentForURL(final String link, final int maxTries) {
        Document doc = null;
        for (int count = 0; count <= maxTries; count++) {
            boolean success = true;
            try {
                doc = Jsoup.connect(link).get();
            } catch (IOException e) {
                System.err.println(e.getClass().toString() + ": " + e.getMessage());
                success = false;
                if (count == maxTries) break; // no further processing
            }
            if (success) break; // continue processing
        }
        return doc;
    }

/*
http://www.mangahere.co/ajax/search.php?
query=abc	    		<—— Quick Search Query

http://www.mangahere.co/search.php?
direction=rl&			<—— rl = Manga, lr = Manhwa, blank = Either
name_method=cw&			<—— cw = Contains
name=[name]&			<—— Title String
author_method=bw&		<—— bw = Begins With
author=[name]&			<—— Author String
artist_method=ew&		<—— ew = Ends With
artist=[name]&			<—— Artist String

genres[Action]=0&		<—— Genre Matching
genres[Adventure]=1&	<—— 1 = YES
genres[Comedy]=2&		<—— 2 = NO
genres[Doujinshi]=0&	<—— 0 = SAME
genres[Drama]=0&
genres[Ecchi]=0&
genres[Fantasy]=0&
genres[Gender+Bender]=0&
genres[Harem]=0&
genres[Historical]=0&
genres[Horror]=0&
genres[Josei]=0&
genres[Martial+Arts]=0&
genres[Mature]=1&
genres[Mecha]=0&
genres[Mystery]=0&
genres[One+Shot]=0&
genres[Psychological]=0&
genres[Romance]=0&
genres[School+Life]=0&
genres[Sci-fi]=0&
genres[Seinen]=0&
genres[Shoujo]=0&
genres[Shoujo+Ai]=2&
genres[Shounen]=0&
genres[Shounen+Ai]=0&
genres[Slice+of+Life]=0&
genres[Sports]=0&
genres[Supernatural]=0&
genres[Tragedy]=0&

released_method=gt&		<—— lt = Before, qt = After, eq = On
released=1900&			<—— Release Date
is_completed=1&			<—— Title Completed
advopts=1			    <—— Mandatory Flag
*/

}
