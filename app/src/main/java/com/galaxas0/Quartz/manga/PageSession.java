package com.galaxas0.Quartz.manga;

import android.graphics.Bitmap;
import android.net.Uri;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class PageSession {
    public int bookmark = -1;
    public PageSessionListener listener;

    private PageSessionMode mode = PageSessionMode.Wrapping; //Clipping
    private Manga manga = null;
    private ArrayList<Bitmap> preloadCache = null;
    private ArrayList<List<Future<Uri>>> pageGroups = null;

    public enum PageSessionMode {
        Clipping,
        Wrapping
    }

    public interface PageSessionListener {
        void update(int idx);
    }

    public PageSession(Manga manga, int cache) {
        this.manga = manga;
        this.preloadCache = new ArrayList<>(cache);
        this.pageGroups = new ArrayList<>();
    }

    public Manga manga() {
        return manga;
    }

    public PageSessionMode mode() {
        return mode;
    }

    public void mode(PageSessionMode mode) {
        this.mode = PageSessionMode.Wrapping /* mode*/;
    }

    public boolean isEmpty() {
        return pageGroups.isEmpty();
    }

    public void clear() {
        pageGroups.clear();
    }

    public int size() {
        return pageGroups.size();
    }

    public int size(int i) {
        if (mode == PageSessionMode.Clipping) {
            return pageGroups.get(i - 1) != null ? pageGroups.get(i - 1).size() : 0;
        } else {
            int count = 0;
            for (List<?> list : pageGroups) {
                count += list != null ? list.size() : 0;
            }
            return count;
        }
    }

    public void add(final int i) {
        int size = manga.chapters().size();
        if (i > size || i < 0) return;

        pageGroups.ensureCapacity(size);
        while (pageGroups.size() < size)
            pageGroups.add(null);

        Library.getPagesForChapter(manga, i, pages -> {
            pageGroups.set(i, pages);
            if (listener != null) listener.update(i);
        });
    }

    public Uri get(int index, int page) {
        if (mode == PageSessionMode.Wrapping) {
            for (int i = 0; i < pageGroups.size(); i++) {
                int size = pageGroups.get(i) != null ? pageGroups.get(i).size() : 0;
                if (page - size < 0) {
                    index = i;
                    break;
                } else page -= size;
            }
        }

        try {
            return pageGroups.get(index).get(page).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public Chapter getPreviousChapter() {
        if(this.index + 1 > manga.getChapters().size() - 1)
            return null;
        return manga.getChapters().get(this.index + 1);
    }

    public Chapter getNextChapter() {
        if(this.index - 1 < 0)
            return null;
        return manga.getChapters().get(this.index - 1);
    }//*/

    /*
    chapter.openPageSession()
    chapter.closePageSession()
    ps.get(page) --> Bitmap
    ps.get(15) --> on demand load if not in preload cache
     */
}
