package com.galaxas0.Quartz.manga;

import android.content.Context;

import com.galaxas0.Quartz.BuildConfig;
import com.galaxas0.Quartz.utils.SerializablePair;

import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReadingSession {
    private static final String SESSION_NAME = "session-v" + BuildConfig.VERSION_CODE;
    private static Context ctx = null;

    public static boolean isOpened() {
        return (ctx != null);
    }

    private static HashMap<String, String> starred;
    private static HashMap<String, String> saved;
    private static LinkedHashMap<String, SerializablePair<String, Integer>> history;

    private static List<WeakReference<ReadingSessionListener>> listeners;

    public interface ReadingSessionListener {
        public void onSessionChanged();
    }

    public static void registerOnSessionChangedListener(ReadingSessionListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(new WeakReference<>(listener));
    }

    public static void unregisterOnSessionChangedListener(ReadingSessionListener listener) {
        if (listeners == null)
            return;
        for (WeakReference<ReadingSessionListener> item : listeners)
            if (item.get() != null && item.get().equals(listener)) {
                listeners.remove(item);
                break;
            }
    }

    private static void triggerOnSessionChangedListeners() {
        for (WeakReference<ReadingSessionListener> item : listeners)
            if (item.get() != null)
                item.get().onSessionChanged();
    }

    @SuppressWarnings("unchecked")
    public static void open(Context _ctx) {
        if (ctx != null) return;
        Library.infoExecutor.execute(() -> {
            try {
                ObjectInputStream ois = new ObjectInputStream(_ctx.openFileInput(SESSION_NAME));
                starred = (HashMap<String, String>) ois.readObject();
                saved = (HashMap<String, String>) ois.readObject();
                history = (LinkedHashMap<String, SerializablePair<String, Integer>>) ois.readObject();
                ois.close();
            } catch (FileNotFoundException e) {
                starred = new HashMap<>();
                saved = new HashMap<>();
                history = new LinkedHashMap<>();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ctx = _ctx;
            }
        });
    }

    public static void save() {
        if (ctx == null) return;
        Library.infoExecutor.execute(() -> {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(ctx.openFileOutput(SESSION_NAME, Context.MODE_PRIVATE));
                oos.writeObject(starred);
                oos.writeObject(saved);
                oos.writeObject(history);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void close() {
        save();
        ctx = null;
    }

    public static void setStarred(Manga m, boolean _starred) {
        if (_starred)
            starred.put(m.descriptor(), m.toJSONString());
        else starred.remove(m.descriptor());
        triggerOnSessionChangedListeners();
    }

    public static boolean toggleStarred(Manga m) {
        boolean contain = !starred.keySet().contains(m.descriptor());
        if (contain) starred.put(m.descriptor(), m.toJSONString());
        else starred.remove(m.descriptor());
        triggerOnSessionChangedListeners();
        return !contain;
    }

    public static boolean isStarred(Manga m) {
        return starred.keySet().contains(m.descriptor());
    }

    public static void setSaved(Manga m, boolean _saved) {
        if (_saved)
            saved.put(m.descriptor(), m.toJSONString());
        else saved.remove(m.descriptor());
        triggerOnSessionChangedListeners();
    }

    public static boolean toggleSaved(Manga m) {
        boolean contain = !saved.keySet().contains(m.descriptor());
        if (contain) saved.put(m.descriptor(), m.toJSONString());
        else saved.remove(m.descriptor());
        triggerOnSessionChangedListeners();
        return !contain;
    }

    public static boolean isSaved(Manga m) {
        return saved.keySet().contains(m.descriptor());
    }

    public static void appendHistory(Manga m, Integer i) {
        history.put(m.descriptor(), new SerializablePair<>(m.toJSONString(), i));
        triggerOnSessionChangedListeners();
    }

    public static void removeHistory(Manga m) {
        history.remove(m.descriptor());
        triggerOnSessionChangedListeners();
    }

    public static void clearHistory() {
        history.clear();
        triggerOnSessionChangedListeners();
    }

    public static List<Manga> starred() {
        List<Manga> _starred = new ArrayList<>();
        if (starred != null) for (Map.Entry<String, String> entry : starred.entrySet())
            _starred.add(0, Manga.fromJSONString(entry.getValue()));
        return _starred;
    }

    public static List<Manga> saved() {
        List<Manga> _saved = new ArrayList<>();
        if (saved != null) for (Map.Entry<String, String> entry : saved.entrySet())
            _saved.add(0, Manga.fromJSONString(entry.getValue()));
        return _saved;
    }

    public static List<Manga> history() {
        List<Manga> _history = new ArrayList<>();
        if (history != null)
            for (Map.Entry<String, SerializablePair<String, Integer>> entry : history.entrySet())
                _history.add(0, Manga.fromJSONString(entry.getValue().first));
        return _history;
    }
}
