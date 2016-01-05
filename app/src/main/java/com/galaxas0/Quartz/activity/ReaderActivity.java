package com.galaxas0.Quartz.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.databinding.ActivityReaderBinding;
import com.galaxas0.Quartz.manga.Library;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.PageSession;
import com.galaxas0.Quartz.manga.ReadingSession;
import com.galaxas0.Quartz.ui.QuartzActivity;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.text.DecimalFormat;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

public class ReaderActivity extends QuartzActivity {
    private ActivityReaderBinding binding;

    private PageSession ps;
    private ReaderAdapter da;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reader);

        Manga item = Manga.fromJSONString(getIntent().getStringExtra("manga"));
        int index = getIntent().getIntExtra("index", 0);

        ReadingSession.open(getApplicationContext());

        binding.fastScroller.setRecyclerView(binding.recyclerView);
        binding.recyclerView.addOnScrollListener(binding.fastScroller.getOnScrollListener());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Glide.with(this).load(item.image()).centerCrop().into(new SimpleTarget<GlideDrawable>(200, 200) {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    Bitmap bmp = ThemeUtils.drawableToBitmap(resource);
                    ActivityManager.TaskDescription task = new ActivityManager.TaskDescription(item.title(), bmp, Color.DKGRAY);
                    setTaskDescription(task);
                }
            });
        }

        binding.recyclerView.setAdapter((da = new ReaderAdapter(this, ps)));
        if (ThemeUtils.preferences(this).getBoolean("zoomMinimap", true))
            binding.zoomLayout.setMiniMapEnabled(true);

        //nextView
        binding.previousChapter.setOnClickListener(view -> {
            if (ps.bookmark == 0) {
                if (ps.mode() != PageSession.PageSessionMode.Wrapping) {
                    finish();
                } else {
                    Snackbar.make(view, "No previous chapters.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                ps.add(--ps.bookmark);
                //da.notifyDataSetChanged();
                ReadingSession.appendHistory(ps.manga(), ps.bookmark);
                ReadingSession.save();
            }
        });

        binding.nextChapter.setOnClickListener(view -> {
            if (ps.bookmark == ps.manga().chapters().size() - 1) {
                if (ps.mode() != PageSession.PageSessionMode.Wrapping) {
                    finish();
                } else {
                    Snackbar.make(view, "No further chapters.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                ps.add(++ps.bookmark);
                //da.notifyDataSetChanged();
                ReadingSession.appendHistory(ps.manga(), ps.bookmark);
                ReadingSession.save();
            }
        });

        da.setOnClickListener(view -> {
            int idx = binding.recyclerView.getLayoutManager().getPosition(view) + 1;
            String marker = "Chapter " + new DecimalFormat("####.#").format(ps.manga().chapters().get(ps.bookmark).chapter()) + " | Page " + idx + "/" + ps.size(ps.bookmark);
            Snackbar.make(view, marker, Snackbar.LENGTH_SHORT).show();
        });

        da.setOnLongClickListener(view -> {
            int idx = binding.recyclerView.getLayoutManager().getPosition(view) + 1;
            String name = ps.manga().chapters().get(ps.bookmark).shareableTitle() + " Page " + idx;
            String url = ps.manga().chapters().get(ps.bookmark).link().toString() + idx + ".html";

            // Create the actual send Intent.
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, name);
            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
            sendIntent.setType("text/plain");

            // Create an IntentChooser with an added Intent.
            // This way we can add the ability to save an image.
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INTENT, sendIntent);
            chooser.putExtra(Intent.EXTRA_TITLE, "Share via");

            //Intent[] intentArray =  {addIntent};
            //chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivity(chooser);
            return true;
        });

        binding.recyclerView.setLayoutManager(reorient(new LinearLayoutManager(this), da));
        //final Manga manga = Manga.fromJSONString(getArguments().getString("manga"));
        Library.getMangaInformation(item, item1 -> {
            ReadingSession.open(getApplicationContext());
            ps = Library.openPageSession(item1, index);
            if (ThemeUtils.preferences(ReaderActivity.this).getBoolean("continuousPages", false))
                ps.mode(PageSession.PageSessionMode.Wrapping);
            da.setPageSession(ps);
            ReadingSession.appendHistory(ps.manga(), ps.bookmark);
            ReadingSession.save();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final View decorView = getWindow().getDecorView();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        enterImmersive(decorView);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                enterImmersive(decorView);
        });

        if (ThemeUtils.preferences(this).getBoolean("nightMode", false)) {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.screenBrightness = 0.01f;
            attr.dimAmount = 0.99f;
            getWindow().setAttributes(attr);
        }
    }

    private void enterImmersive(final View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reorient((LinearLayoutManager) binding.recyclerView.getLayoutManager(), (ReaderAdapter) binding.recyclerView.getAdapter());
    }

    private LinearLayoutManager reorient(LinearLayoutManager l, ReaderAdapter d) {
        SharedPreferences pref = ThemeUtils.preferences(this);
        if (!pref.getBoolean("orientationMatch", false)) {
            if (pref.getBoolean("reverseOrientation", false)) {
                l.setOrientation(LinearLayoutManager.HORIZONTAL);
                d.swapwrap = true;

                if (pref.getBoolean("reverseLayout", false))
                    l.setReverseLayout(true);
            }
            return l;
        }

        boolean wide = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        l.setOrientation(wide ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        d.swapwrap = wide;
        if (wide && pref.getBoolean("reverseLayout", false))
            l.setReverseLayout(true);
        binding.recyclerView.requestLayout();
        return l;
    }

    public static class ReaderAdapter extends RecyclerView.Adapter<ReaderAdapter.ViewHolder> {
        private PageSession ps;
        private Context mContext;

        View.OnClickListener onClickListener;
        View.OnLongClickListener onLongClickListener;

        public boolean swapwrap = false;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView mImageView;

            public ViewHolder(View v) {
                super(v);
                mImageView = (ImageView) v.findViewById(R.id.page);
            }
        }

        public void setOnClickListener(View.OnClickListener listener) {
            onClickListener = listener;
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            onLongClickListener = listener;
        }

        public ReaderAdapter(final Activity context, PageSession _ps) {
            mContext = context;
            setPageSession(_ps);
        }

        public void setPageSession(PageSession _ps) {
            ps = _ps;
            if (ps == null) return;
            ps.listener = idx -> new Handler(mContext.getMainLooper()).post(this::notifyDataSetChanged);
            notifyDataSetChanged();
        }

        @Override
        public ReaderAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_layout, parent, false);
            v.setOnClickListener(onClickListener);
            v.setOnLongClickListener(onLongClickListener);

            if (swapwrap) {
                v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mImageView.setAdjustViewBounds(true);
            Glide.with(mContext).
                    load(ps.get(ps.bookmark, position).toString()).
                    placeholder(R.drawable.reader_placeholder).
                    crossFade(250).
                    diskCacheStrategy(DiskCacheStrategy.SOURCE).
                    override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).
                    skipMemoryCache(true).
                    bitmapTransform(new BitmapTransformation(mContext) {
                        @Override
                        protected Bitmap transform(BitmapPool bitmapPool, Bitmap toTransform, int width, int height) {
                            Bitmap b = toTransform;
                            if(width > 4096 || height > 4096) {
                                double ratio = 4096.0 / Math.max(width, height);
                                height *= ratio;
                                width *= ratio;

                                b = Bitmap.createScaledBitmap(toTransform, width, height, false);
                                bitmapPool.put(b);
                            }
                            return b;
                        }

                        @Override
                        public String getId() {
                            return "Inline.Quartz";
                        }
                    }).
                    into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return ps != null ? ps.size(ps.bookmark) : 0;
        }
    }

}
