package com.galaxas0.Quartz.manga;

import android.net.Uri;
import android.util.Log;

import com.galaxas0.Quartz.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.galaxas0.Quartz.manga.Library._documentForURL;

public class MangaHere implements LibrarySource {

    private static List<String> genreList = null;
    private static Pattern chapterPattern = Pattern.compile(".*/([^/?]+).*");

    static {
        Library.registerSource(new MangaHere());
    }

    @Override
    public String getName() {
        return "MangaHere";
    }

    @Override
    public String getRegisteredURL() {
        return "http://www.mangahere.co/";
    }

    @Override
    public List<String> getGenres() {
        if (genreList != null) return genreList;
        final String listing[] = {"all", "action", "adventure", "comedy", "doujinshi", "drama",
                "ecchi", "fantasy", "gender bender", "harem", "historical", "horror", "josei",
                "martial arts", "mature", "mecha", "mystery", "one shot", "psychological", "romance",
                "school life", "sci-fi", "seinen", "shoujo", "shoujo ai", "shounen", "shounen ai",
                "slice of life", "sports", "supernatural", "tragedy", "yaoi", "yuri",};
        return (genreList = Arrays.asList(listing));
    }

    @Override
    public boolean isGenreMarkedNSFW(String genre) {
        List<String> nsfw = Arrays.asList("doujinshi", "ecchi", "mature",
                            "shoujo ai", "shounen ai", "yaoi", "yuri");
        return nsfw.contains(genre.toLowerCase().trim());
    }

    @Override
    public List<Manga> getLatestManga(int page) {
        final Document doc = _documentForURL("http://www.mangahere.co/" + "latest" + "/" + page + "/", 3);
        Log.d("mangahere", "got document!");
        final Elements list = doc.select("div.manga_updates > dl > dt > a.manga_info");

        List<Manga> latest = new ArrayList<>();
        for (Element item : list) {
            String title = item.attr("rel");
            String href = item.attr("abs:href");
            href = StringUtils.removeStart(href, "http://www.mangahere.co/manga/");
            href = StringUtils.removeEnd(href, "/");

            Manga m = new Manga(href);
            m.title = title;
            m.link = Uri.parse("http://www.mangahere.co/manga/" + href + "/");
            latest.add(m);
            Library.defaultExecutor.submit(() -> getMangaDescription(m));
        }
        return latest;
    }

    @Override
    public List<Manga> getMangaForGenre(int page, Genre genre) {
        String _sortString = "";
        if (genre.sortOrder == LibrarySource.Genre.SortOrder.ALPHABETICAL)
            _sortString = "name." + (genre.ascending ? "az" : "za");
        else if (genre.sortOrder == LibrarySource.Genre.SortOrder.UPDATES)
            _sortString = "last_chapter_time." + (genre.ascending ? "az" : "za");
        else if (genre.sortOrder == LibrarySource.Genre.SortOrder.VIEWS)
            _sortString = "views." + (genre.ascending ? "az" : "za");
        final String sortString = _sortString;
        String lookupGenre = (genre.name.toLowerCase().equals("all") ? "directory" : genre.name.toLowerCase().replace(' ', '_'));

        final Document doc = _documentForURL("http://www.mangahere.co/" + lookupGenre + "/" + page + ".htm?" + sortString, 3);
        final Elements list = doc.select("div.manga_text > div.title > a[href]");
        final List<Manga> items = new ArrayList<>();

        for (Element item : list) {
            String title = item.attr("rel");
            String href = item.attr("abs:href");
            href = StringUtils.removeStart(href, "http://www.mangahere.co/" + "manga" + "/");
            href = StringUtils.removeEnd(href, "/");

            Manga m = new Manga(href);
            m.title = title;
            m.link = Uri.parse("http://www.mangahere.co/manga/" + href + "/");
            items.add(m);
            Library.defaultExecutor.submit(() -> getMangaDescription(m));
        }
        return items;
    }

    @Override
    public List<Manga> getMangaSuggestionsForQuery(String query) {
        try {
            final List<Manga> mangaList = new ArrayList<>();
            URL url = new URL("http://www.mangahere.co/ajax/search.php?query=" + URLEncoder.encode(query, "utf-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                String line, response = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null)
                    response += line;

                JSONObject obj = new JSONObject(response);
                for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
                    String rel = obj.getJSONArray("suggestions").get(i).toString(); // title
                    String href = obj.getJSONArray("data").get(i).toString();
                    href = StringUtils.removeStart(href, "http://www.mangahere.co/" + "manga" + "/");
                    href = StringUtils.removeEnd(href, "/");

                    Manga m = new Manga(href);
                    m.title = rel;
                    m.link = Uri.parse("http://www.mangahere.co/manga/" + href + "/");
                    mangaList.add(m);
                }
            }
            return mangaList;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Manga> getMangaForSearch(int page, Search query) {
        return new ArrayList<>();
    }

    @Override
    public Manga getMangaDescription(final Manga manga) {
        if(manga.level() != Manga.MangaDetailLevel.Descriptor)
            return manga;

        try {
            URL url = new URL("http://www.mangahere.co/ajax/series.php");
            String item = "name=" + URLEncoder.encode(manga.title, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setFixedLengthStreamingMode(item.getBytes("UTF-8").length);
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            writer.write(item);
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line, response = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null)
                    response += line;

                JSONArray obj = new JSONArray(response);
                manga.image = Uri.parse(obj.get(1).toString().replace("thumb_cover", "cover"));
                manga.genres = new HashSet<>(Arrays.asList(obj.get(4).toString().split("\\s*,\\s*")));
                //manga.authors = new HashSet<>(Arrays.asList(obj.get(5).toString().split("\\s*,\\s*")));
                manga.level = Manga.MangaDetailLevel.Summary;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return manga;
    }

    @Override
    public Manga getMangaInformation(Manga manga) {
        Log.d("mangahere", "manga: " + manga.toJSONString() + "\nlevel " + manga.level());
        if(manga.level() == Manga.MangaDetailLevel.Complete)
            return manga;

        final Document document = _documentForURL(manga.link.toString(), 3);

        Elements image = document.select("div.manga_detail > div.manga_detail_top > img[src]");
        manga.image = Uri.parse(image.get(0).attr("abs:src"));

        Elements description = document.select("div.manga_detail > div.manga_detail_top > ul.detail_topText > li > label");
        for (Element e : description) {
            if (e.text().equals("Genre(s):")) {
                String value = StringUtils.removeStart(e.parent().text(), "Genre(s):");
                List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
                manga.genres = new HashSet<>(items);
            } else if (e.text().equals("Author(s):")) {
                String value = StringUtils.removeStart(e.parent().text(), "Author(s):");
                List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
                manga.authors = new HashSet<>(items);
            } else if (e.text().equals("Artist(s):")) {
                String value = StringUtils.removeStart(e.parent().text(), "Artist(s):");
                List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
                manga.artists = new HashSet<>(items);
            } else if (e.text().equals("Status:")) {
                String value = StringUtils.removeStart(e.parent().text(), "Status:");
                manga.complete = value.equals("Completed");
            } else if (e.text().contains("Manga Summary")) {
                Element desc = e.parent().select("p#show").first();
                manga.description = StringUtils.removeEnd(desc.text(), "Show less");
                manga.title = StringUtils.removeEnd(e.getElementsByTag("h2").first().text(), " Manga");
            }
        }

        Elements chapters = document.select("div.detail_list > ul > li > span.left");
        ArrayList<Chapter> _chapters = new ArrayList<>();
        for (int i = 0; i < chapters.size(); i++) {
            String _title = chapters.get(i).ownText();
            String _date = chapters.get(i).parent().select("span.right").get(0).ownText();
            boolean _new = chapters.get(i).parent().select("i.new").size() > 0;
            Uri _link = Uri.parse(chapters.get(i).select("a[href]").attr("abs:href"));

            Date date;
            if ("Today".equals(_date)) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                date = cal.getTime();
            } else if ("Yesterday".equals(_date)) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                date = cal.getTime();
            } else {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                try {
                    date = format.parse(_date);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            String chap = chapterPattern.matcher(_link.toString()).replaceFirst("$1");
            double c = Double.valueOf(chap.substring(1));
            final String name = manga.title + " " + new DecimalFormat("####.#").format(c) + ": " + _title;
            _chapters.add(i, new Chapter(_title, name, c, _new, date, chapters.size() - 1 - i, _link));
        }

        manga.chapters = _chapters;
        manga.level = Manga.MangaDetailLevel.Complete;
        return manga;
    }

    @Override
    public List<Future<Uri>> getPagesForChapter(Manga manga, int idx) {
        final Document chap = _documentForURL(manga.chapters().get(idx).link().toString(), 3);
        final Elements _ip = chap.select("section.readpage_top > div.go_page.clearfix > span.right > select.wid60 > option");

        ArrayList<Future<Uri>> set = new ArrayList<>(_ip.size());
        for (int _idx = 0; _idx < _ip.size(); _idx++) {
            final int idx1 = _idx;
            set.add(idx1, Library.pageExecutor.submit(() -> {
                final Document page = _documentForURL(_ip.get(idx1).attr("abs:value"), 3);
                return Uri.parse(page.select("section.read_img > a > img[src]").first().attr("abs:src"));
            }));
        }
        return set;
    }
}
