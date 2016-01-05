package com.galaxas0.Quartz.activity;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.databinding.ActivityDetailBinding;
import com.galaxas0.Quartz.manga.Chapter;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.ReadingSession;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.AnimationUtils;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import java8.util.Optional;

public class DetailActivity extends QuartzActivity {
    private ActivityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Populate Manga information.
        Manga item = Manga.fromJSONString(getIntent().getStringExtra("manga"));
        Library.getMangaInformation(item, this::generateChapters);

        // Setup toolbar and sharing Intent.
        binding.photo.setOnClickListener(v -> binding.scroll.smoothScrollTo(0, 0));
        binding.toolbar.inflateMenu(R.menu.detail);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.toolbar.setOnMenuItemClickListener(item1 -> {

            // Share as a link.
            if (item1.getItemId() == R.id.action_share) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.title());
                shareIntent.putExtra(Intent.EXTRA_TEXT, item.link().toString());
                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, "Share via"));
                return true;
            }
            return false;
        });

        if (item.level() > Manga.MangaDetailLevel.Descriptor)
            generatePalette(item);
        generateMenu(item);
    }

    public void generatePalette(final Manga item) {
        binding.setManga(item);
        binding.photo.setTag(R.id.content, true);

        DrawableRequestBuilder b = Glide.with(this).
                load(item.image()).
                placeholder(ContextCompat.getDrawable(this, R.drawable.library_placeholder)).
                override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).
                centerCrop().
                listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri m, Target<GlideDrawable> t, boolean b) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable r, Uri m, Target<GlideDrawable> t, boolean c, boolean f) {

                        // Style the Task header if possible.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            setTaskDescription(new ActivityManager.TaskDescription(item.title(),
                                    ((GlideBitmapDrawable) r).getBitmap(), Color.WHITE));

                        Palette.from(((GlideBitmapDrawable) r).getBitmap()).generate(p -> {
                            Optional.ofNullable(p.getDarkMutedSwatch())
                                    .map(Palette.Swatch::getRgb)
                                    .ifPresent(binding.scroll::setBackgroundColor);

                            Optional.ofNullable(p.getDarkVibrantSwatch())
                                    .map(Palette.Swatch::getRgb).ifPresent(s -> {
                                binding.titleBackground.setCardBackgroundColor(s);
                                binding.listBackground.setCardBackgroundColor(s);
                                binding.metaBackground.setCardBackgroundColor(s);

                                // Style the Task header if possible.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    setTaskDescription(new ActivityManager.TaskDescription(item.title(),
                                            ((GlideBitmapDrawable) r).getBitmap(), s));
                            });
                        });
                        return false;
                    }
                });

        // Execute any requests on the UI thread.
        runOnUiThread(() -> b.into(binding.photo));
    }

    public void generateMenu(final Manga item) {

        // Assign the starred item value.
        MenuItem star = binding.toolbar.getMenu().findItem(R.id.action_star);
        ReadingSession.open(this);
        if (ReadingSession.isStarred(item))
            star.setIcon(R.drawable.ic_star_white_18dp);
        else star.setIcon(R.drawable.ic_star_outline_white_18dp);

        // Handle toggling the starred state.
        star.setOnMenuItemClickListener((menuItem) -> {
            //ReadingSession.toggleStarred(item);
            if (ReadingSession.isStarred(item))
                star.setIcon(R.drawable.ic_star_white_18dp);
            else star.setIcon(R.drawable.ic_star_outline_white_18dp);
            ReadingSession.save();
            return true;
        });

        // Assign the bookmarked item value.
        MenuItem bookmark = binding.toolbar.getMenu().findItem(R.id.action_bookmark);
        ReadingSession.open(this);
        if (ReadingSession.isSaved(item))
            bookmark.setIcon(R.drawable.ic_bookmark_white_18dp);
        else bookmark.setIcon(R.drawable.ic_bookmark_outline_white_18dp);

        // Handle toggling the bookmarked state.
        bookmark.setOnMenuItemClickListener((menuItem) -> {
            //ReadingSession.toggleSaved(item);
            if (ReadingSession.isSaved(item))
                bookmark.setIcon(R.drawable.ic_bookmark_white_18dp);
            else bookmark.setIcon(R.drawable.ic_bookmark_outline_white_18dp);
            ReadingSession.save();
            return true;
        });
    }

    public void generateChapters(final Manga item) {
        // If we haven't set the palette completion bit, go back and do that.
        if (binding.photo.getTag(R.id.content) == null)
            generatePalette(item);
        else binding.setManga(item);

        // Create the Intent to launch the Reader.
        final Intent outgoing = new Intent(DetailActivity.this, ReaderActivity.class);
        outgoing.putExtra("manga", item.toJSONString());
        if (ThemeUtils.preferences(this).getBoolean("startNewTask", false)) {
            outgoing.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            outgoing.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }

        // Create in reverse order in background thread.
        ArrayList<View> views = new ArrayList<>(item.chapters().size());
        for (int position = 0; position < item.chapters().size(); position++) {
            final View rootView = LayoutInflater.from(this).inflate(R.layout.detail_list_item, null);
            Chapter c = item.chapters().get(position);

            // Register listeners to trigger the intent on either type of press.
            rootView.setOnClickListener((v) -> {
                outgoing.putExtra("index", c.index());
                startActivity(outgoing, AnimationUtils.scaleUp(v).toBundle());
            });
            rootView.setOnLongClickListener((v) -> {
                outgoing.putExtra("index", c.index());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startActivity(outgoing, ActivityOptions.makeTaskLaunchBehind().toBundle());
                else startActivity(outgoing, AnimationUtils.scaleUp(v).toBundle());
                return true;
            });

            // Configure the Chapter template.
            if (c != null) {
                TextView header = (TextView) rootView.findViewById(R.id.list_header);
                TextView main = (TextView) rootView.findViewById(R.id.list_text);

                String date;
                if (DateUtils.isToday(c.release().getTime()))
                    date = "Today";
                else date = (String) DateUtils.getRelativeTimeSpanString(c.release().getTime(),
                        new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_RELATIVE);

                header.setText(date + " " + (c.updated() ? "[new]" : ""));
                main.setText("" + new DecimalFormat("####.#").format(c.chapter()) + ": " + c.title());
            }

            // Add the view to the generated list.
            views.add(rootView);
        }

        runOnUiThread(() -> {
            for (int i = 0; i < views.size(); i++)
                binding.list.addView(views.get(i), i);
        });
    }
}
