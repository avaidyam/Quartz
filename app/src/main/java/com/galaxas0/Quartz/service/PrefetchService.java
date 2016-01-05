package com.galaxas0.Quartz.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.activity.DetailActivity;
import com.galaxas0.Quartz.activity.LibraryActivity;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.ReadingSession;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobScheduler;
import me.tatarka.support.job.JobService;
import me.tatarka.support.os.PersistableBundle;

public class PrefetchService extends JobService {
    public static final int NOTIFICATION_ID = 0x10101010;
    public static final int OTHER_NOTIFICATION_ID = 0x01010101;
    public static final int PREFETCH_SERVICE_JOB_ID = 0x0111100;

    public static void scheduleJob(Context context, PersistableBundle bundle) {
        JobInfo job = new JobInfo.Builder(PREFETCH_SERVICE_JOB_ID, new ComponentName(context, PrefetchService.class))
                .setExtras(bundle == null ? PersistableBundle.EMPTY : bundle)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(3 * 60 * 10 * 1000) /* 3 hours */
                .setPersisted(true)
                .build();

        JobScheduler scheduler = JobScheduler.getInstance(context);
        scheduler.schedule(job);

        Log.d("TEST", "Job Scheduled.");
    }

    public static void removeNotification(Context context) {
        NotificationManager n = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        n.cancel(PrefetchService.NOTIFICATION_ID);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        //if(LibraryActivity.isFront)
        //   return false;

        Log.d("TEST", "Starting job.");

        Library.getLatestManga(1, (List<Manga> manga) -> {
            if(manga == null) return;

            ReadingSession.open(getApplicationContext());
            List<Manga> filtered = new ArrayList<Manga>();
            for(Manga item : manga)
                    if(ReadingSession.isStarred(item))
                    filtered.add(item);
            if(filtered.size() == 0) return;

            Log.d("TEST", "Making notification.");

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            Notification.Builder noteBuilder = new Notification.Builder(getApplicationContext());
            Notification.Style style = new Notification.BigTextStyle();

            if(filtered.size() == 1) {
                stackBuilder.addParentStack(DetailActivity.class);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("manga", filtered.get(0).toJSONString());
                stackBuilder.addNextIntent(intent);

                style = new Notification.BigPictureStyle().
                        setSummaryText(Html.fromHtml("<b>" + " Chapter " + "X" + "</b>" + " updated"));

                try {
                    Bitmap bmp = BitmapFactory.decodeStream(new URL(filtered.get(0).image().toString()).openStream());
                    ((Notification.BigPictureStyle)style).bigPicture(bmp);
                } catch(Exception e) { e.printStackTrace(); }

                noteBuilder.setContentTitle(filtered.get(0).title())
                        .setContentText(Html.fromHtml("<b>" + " Chapter " + "X" + "</b>" + " updated"))
                        .setTicker(filtered.get(0).title() + " updated");
            } else if(filtered.size() > 1) {
                stackBuilder.addParentStack(LibraryActivity.class);
                stackBuilder.addNextIntent(new Intent(getApplicationContext(), LibraryActivity.class));

                style = new Notification.InboxStyle().setBigContentTitle(filtered.size() + " new manga updates");
                if(filtered.size() - 5 > 0)
                    ((Notification.InboxStyle)style).setSummaryText("+" + (filtered.size() - 5) + " more");

                for(int i = 0; i < 5 && i < filtered.size(); i++) {
                    Spanned str = Html.fromHtml("<b>" + filtered.get(i).title() + "</b>" + " Chapter " + "X");
                    ((Notification.InboxStyle)style).addLine(str);
                }

                noteBuilder.setContentTitle(filtered.size() + " new manga updates")
                        .setContentText(Html.fromHtml("<b>" + filtered.get(0).title() + "</b>" + " and more..."))
                        .setTicker(filtered.size() + " new manga updates");
            }

            PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            noteBuilder.setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .setStyle(style)
                    .addAction(R.drawable.ic_action_star, "Read", pending)
                    .addAction(R.drawable.ic_add_filled, "Save", pending);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, noteBuilder.build());
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("TEST", "Stopping job.");
        return false;
    }
}
