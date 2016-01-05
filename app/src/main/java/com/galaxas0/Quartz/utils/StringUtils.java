package com.galaxas0.Quartz.utils;

import android.util.DisplayMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public final class StringUtils {
    private StringUtils() {}

    public static String truncateAt(String string, int length) {
        return string.length() > length ? string.substring(0, length) : string;
    }

    public static String getDensityString(DisplayMetrics displayMetrics) {
        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW: return "LDPI";
            case DisplayMetrics.DENSITY_MEDIUM: return "MDPI";
            case DisplayMetrics.DENSITY_HIGH: return "HDPI";
            case DisplayMetrics.DENSITY_XHIGH: return "XHDPI";
            case DisplayMetrics.DENSITY_XXHIGH: return "XXHDPI";
            case DisplayMetrics.DENSITY_XXXHIGH: return "XXXHDPI";
            case DisplayMetrics.DENSITY_TV: return "TVDPI";
            default: return "?DPI";
        }
    }

    public static String getSizeString(long bytes) {
        String[] units = new String[] { "B", "KB", "MB", "GB" };
        int unit = 0;
        while (bytes >= 1024) {
            bytes /= 1024;
            unit += 1;
        }
        return bytes + units[unit];
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time/*, Context context*/) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = new Date().getTime();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return 0 + "m";//context.getResources().getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return 1 + "m";//context.getResources().getString(R.string.a_min_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";//(context.getResources().getString(R.string.minutes_ago)).replace("%s", diff / MINUTE_MILLIS + "");
        } else if (diff < 90 * MINUTE_MILLIS) {
            return 1 + "h";//context.getResources().getString(R.string.an_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            if (diff / HOUR_MILLIS == 1)
                return 1 + "h";//context.getResources().getString(R.string.an_hour_ago);
            else
                return diff / HOUR_MILLIS + "h";//(context.getResources().getString(R.string.hours_ago)).replace("%s", diff / HOUR_MILLIS + "");
        } else if (diff < 48 * HOUR_MILLIS) {
            return 1 + "d";//context.getResources().getString(R.string.yesterday);
        } else {
            return diff / DAY_MILLIS + "d";//(context.getResources().getString(R.string.days_ago)).replace("%s", diff / DAY_MILLIS + "");
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0)
            return true;
        for (int i = 0; i < strLen; i++)
            if ((!Character.isWhitespace(str.charAt(i))))
                return false;
        return true;
    }

    public static String removeStart(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }

    public static String removeStartIgnoreCase(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (startsWithIgnoreCase(str, remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    public static String removeEnd(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static String removeEndIgnoreCase(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (endsWithIgnoreCase(str, remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static boolean startsWith(String str, String prefix) {
        return startsWith(str, prefix, false);
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return startsWith(str, prefix, true);
    }

    private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
        if (str == null || prefix == null) {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
    }

    public static boolean endsWith(String str, String suffix) {
        return endsWith(str, suffix, false);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return endsWith(str, suffix, true);
    }

    private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }

    public static String join(Collection collection, char separator) {
        return join(collection.iterator(), "" + separator);
    }

    public static String join(Iterator iterator, char separator) {
        return join(iterator, "" + separator);
    }

    public static String join(Object[] array, char separator) {
        return join(array, "" + separator, 0, array.length);
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        return join(array, "" + separator, startIndex, endIndex);
    }

    public static String join(Object[] array, String separator) {
        return join(array, separator, 0, array.length);
    }

    public static String join(Collection collection, String separator) {
        if (collection == null)
            return null;
        return join(collection.iterator(), separator);
    }

    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null)
            return null;
        if (separator == null)
            separator = "";

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0)
            return "";

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
                + separator.length());

        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex)
                buf.append(separator);
            if (array[i] != null)
                buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(Iterator iterator, String separator) {
        if (iterator == null)
            return null;
        if (!iterator.hasNext())
            return "";

        Object first = iterator.next();
        if (!iterator.hasNext())
            return first.toString();

        // two or more elements
        StringBuilder buf = new StringBuilder(256); // default=16, too small
        if (first != null)
            buf.append(first);

        while (iterator.hasNext()) {
            if (separator != null)
                buf.append(separator);
            Object obj = iterator.next();
            if (obj != null)
                buf.append(obj);
        }
        return buf.toString();
    }

    public static String[] split(String str, char separatorChar) {
        return splitWorker(str, separatorChar, false);
    }

    private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
        if (str == null)
            return null;
        int len = str.length();
        if (len == 0)
            return new String[] {};

        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;

        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }

        if (match || (preserveAllTokens && lastMatch))
            list.add(str.substring(start, i));
        return list.toArray(new String[list.size()]);
    }

    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null)
            return str;
        if (separator.length() == 0)
            return "";

        int pos = str.indexOf(separator);
        if (pos == -1)
            return str;
        return str.substring(0, pos);
    }

    public static String substringAfter(String str, String separator) {
        if (isEmpty(str))
            return str;
        if (separator == null)
            return "";

        int pos = str.indexOf(separator);
        if (pos == -1)
            return "";
        return str.substring(pos + separator.length());
    }

    public static String substringBeforeLast(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator))
            return str;

        int pos = str.lastIndexOf(separator);
        if (pos == -1)
            return str;
        return str.substring(0, pos);
    }

    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str))
            return str;
        if (isEmpty(separator))
            return "";

        int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == (str.length() - separator.length()))
            return "";
        return str.substring(pos + separator.length());
    }

    public static String substringBetween(String str, String tag) {
        return substringBetween(str, tag, tag);
    }

    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null)
            return null;

        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1)
                return str.substring(start + open.length(), end);
        }
        return null;
    }
    public static String[] substringsBetween(String str, String open, String close) {
        if (str == null || isEmpty(open) || isEmpty(close))
            return null;

        int strLen = str.length();
        if (strLen == 0)
            return new String[] {};

        int closeLen = close.length();
        int openLen = open.length();
        List<String> list = new ArrayList<>();

        int pos = 0;
        while (pos < (strLen - closeLen)) {
            int start = str.indexOf(open, pos);
            if (start < 0)
                break;

            start += openLen;
            int end = str.indexOf(close, start);
            if (end < 0)
                break;

            list.add(str.substring(start, end));
            pos = end + closeLen;
        }

        if (list.isEmpty())
            return null;
        return list.toArray(new String [list.size()]);
    }
}
