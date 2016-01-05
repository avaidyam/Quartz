package com.galaxas0.Quartz.manga;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Manga {
     String descriptor;
     int level;
     String title;
     Uri image;
     Uri link;
     String description;
     boolean complete;
     Set<String> authors;
     Set<String> artists;
     Set<String> genres;
     List<Chapter> chapters;

    public static class MangaDetailLevel {
        private MangaDetailLevel() {}

        public static final int Descriptor = 0;
        public static final int Summary = 1;
        public static final int Complete = 2;
    }

    public Manga(final String descriptor) {
        this.descriptor = descriptor;
        this.link = Uri.parse("http://www.mangahere.co/manga/" + descriptor + "/");
        this.level = Manga.MangaDetailLevel.Descriptor;
    }

    public String descriptor() {
        return descriptor;
    }

    public int level() {
        return level;
    }

    public String title() {
        return this.title;
    }

    public Uri image() {
        try {
            Library.getMangaDescription(this).get();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return this.image;
    }

    public Uri link() {
        return link;
    }

    public Set<String> genres() {
        try {
            Library.getMangaDescription(this).get();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return this.genres;
    }

    public Set<String> authors() {
        return authors;
    }

    public String description() {
        return description;
    }

    public Set<String> artists() {
        return artists;
    }

    public boolean complete() {
        return complete;
    }

    public List<Chapter> chapters() {
        return chapters;
    }

    public static Manga fromJSONObject(JSONObject jsonObject) {
        try {
            Manga manga = new Manga(jsonObject.getString("descriptor"));
            manga.title = jsonObject.optString("title");
            manga.level = jsonObject.getInt("level");

            String _image = jsonObject.optString("image");
            if (_image != null && !_image.isEmpty())
                manga.image = Uri.parse(_image);
            String _link = jsonObject.optString("link");
            if (_link != null && !_image.isEmpty())
                manga.link = Uri.parse(_link);

            manga.description = jsonObject.optString("description");
            manga.complete = jsonObject.optBoolean("complete");

            manga.authors = new HashSet<String>();
            JSONArray _authors = jsonObject.optJSONArray("authors");
            if (_authors != null) {
                for (int i = 0; i < _authors.length(); i++)
                    manga.authors.add(_authors.get(i).toString());
            }

            manga.artists = new HashSet<>();
            JSONArray _artists = jsonObject.optJSONArray("artists");
            if (_artists != null) {
                for (int i = 0; i < _artists.length(); i++)
                    manga.artists.add(_artists.get(i).toString());
            }

            manga.genres = new HashSet<>();
            JSONArray _genres = jsonObject.optJSONArray("genres");
            if (_genres != null) {
                for (int i = 0; i < _genres.length(); i++)
                    manga.genres.add(_genres.get(i).toString());
            }

            manga.chapters = new ArrayList<Chapter>();
            JSONArray _chapters = jsonObject.optJSONArray("chapters");
            if (_chapters != null) {
                for (int i = 0; i < _chapters.length(); i++)
                    manga.chapters.add(0, Chapter.fromJSONString((String) _chapters.get(i)));
            }
            return manga;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        if (description == null) description = "";
        try {
            jsonObject.put("descriptor", this.descriptor);
            jsonObject.put("level", this.level);

            jsonObject.put("title", this.title);
            jsonObject.put("image", this.image != null ? this.image.toString() : null);
            jsonObject.put("link", this.link != null ? this.link.toString() : null);

            jsonObject.put("description", this.description);
            jsonObject.put("complete", this.complete);

            if (authors != null) {
                JSONArray _authors = new JSONArray();
                for (String str : authors)
                    _authors.put(str);
                jsonObject.put("authors", _authors);
            }

            if (artists != null) {
                JSONArray _artists = new JSONArray();
                for (String str : artists)
                    _artists.put(str);
                jsonObject.put("artists", _artists);
            }

            if (genres != null) {
                JSONArray _genres = new JSONArray();
                for (String str : genres)
                    _genres.put(str);
                jsonObject.put("genres", _genres);
            }

            if (chapters != null) {
                JSONArray _chapters = new JSONArray();
                for (int i = 0; i < chapters.size(); i++)
                    _chapters.put(i, chapters.get(i).toJSONString());
                jsonObject.put("chapters", _chapters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Manga fromJSONString(String json) {
        try {
            return fromJSONObject(new JSONObject(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    @Override
    public String toString() {
        return this.descriptor;
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that != null && (this == that || this.getClass() == that.getClass() && Objects.equals(descriptor, ((Manga) that).descriptor));
    }
}
