package com.galaxas0.Quartz.manga;

import android.net.Uri;

import org.json.JSONObject;

import java.net.URL;
import java.util.Date;
import java.util.Objects;

public class Chapter {
    String title;
    String shareableTitle;
    Double chapter;
    Boolean updated;
    Date release;
    Integer index;
    Uri link;

    public Chapter(final String title, final String shareableTitle, final double chapter,
                   final boolean updated, final Date release, final int index, final Uri link) {
        this.title = title;
        this.shareableTitle = shareableTitle;
        this.index = index;
        this.updated = updated;
        this.release = release;
        this.link = link;
        this.chapter = chapter;
    }

    public String title() {
        return this.title;
    }

    public String shareableTitle() {
        return this.shareableTitle;
    }

    public Double chapter() {
        return this.chapter;
    }

    public Boolean updated() {
        return this.updated;
    }

    public Date release() {
        return this.release;
    }

    public Integer index() {
        return this.index;
    }

    public Uri link() {
        return this.link;
    }

    public static Chapter fromJSONObject(JSONObject jsonObject) {
        try {
            return new Chapter(jsonObject.getString("title"),
                    jsonObject.getString("shareableTitle"),
                    jsonObject.getDouble("chapter"),
                    jsonObject.getBoolean("updated"),
                    new Date(jsonObject.getString("release")),
                    jsonObject.getInt("index"),
                    Uri.parse(jsonObject.getString("link")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", this.title);
            jsonObject.put("shareableTitle", this.shareableTitle);
            jsonObject.put("chapter", this.chapter);
            jsonObject.put("updated", this.updated);
            jsonObject.put("release", this.release.toString());
            jsonObject.put("index", this.index);
            jsonObject.put("link", this.link.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Chapter fromJSONString(String json) {
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
        return link.toString();
    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that != null && (this == that || this.getClass() == that.getClass() && Objects.equals(link, ((Chapter) that).link));
    }
}
