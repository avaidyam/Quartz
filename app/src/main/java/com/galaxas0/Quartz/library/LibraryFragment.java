package com.galaxas0.Quartz.library;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.galaxas0.Quartz.R;
import com.galaxas0.Quartz.activity.DetailActivity;
import com.galaxas0.Quartz.manga.Manga;
import com.galaxas0.Quartz.manga.ReadingSession;
import com.galaxas0.Quartz.utils.AnimationUtils;
import com.galaxas0.Quartz.utils.ThemeUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class LibraryFragment extends Fragment {
    SwipeRefreshLayout refresh;
    RecyclerView mRecyclerView;

    OnSharedPreferenceChangeListener listener;
    LibraryAdapter adapter;

    public LibraryFragment() {}
    public static LibraryFragment newInstance(Class adapterClass, int identifier) {
        LibraryFragment fragment = new LibraryFragment();
        final Bundle args = new Bundle(2);
        args.putString("adapterClass", adapterClass.getName());
        args.putInt("identifier", identifier);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            Class<?> _class = Class.forName(getArguments().getString("adapterClass"));
            Constructor<?> _constructor = _class.getConstructor(Context.class, int.class);
            adapter = (LibraryAdapter)_constructor.newInstance(getActivity(), getArguments().getInt("identifier"));
        } catch(Exception e) { e.printStackTrace(); }
        super.onCreate(savedInstanceState);
    }

    @Override
    @SuppressWarnings("ResourceAsColor")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int[] colors = ThemeUtils.getThemeColors(getActivity());
        refresh.setProgressBackgroundColorSchemeResource(R.color.grey_900);
        refresh.setColorSchemeColors(colors[0], colors[1]);
        refresh.setOnRefreshListener(adapter::refresh);
        refresh.setRefreshing(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setHasOptionsMenu(true);

        final View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        refresh = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);

        int columns = expandedColumnCount(ThemeUtils.preferences(this).getBoolean("expandColumns", false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns, LinearLayout.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller)rootView.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(mRecyclerView);
        mRecyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView v, int state) {
                refresh.setEnabled(v.getLayoutManager().findViewByPosition(0) != null);
            }

            @Override
            public void onScrolled(RecyclerView v, int dx, int dy) {
            }
        });

        adapter.setOnClickListener((View view) -> {
            final Manga item = adapter.getItemFromView(mRecyclerView, view);
            Intent outgoing = new Intent(getActivity(), DetailActivity.class);
            outgoing.putExtra("manga", item.toJSONString());
            startActivity(outgoing, AnimationUtils.scaleUp(view).toBundle());
        });

        adapter.setOnLongClickListener((View view) -> {
            final View items = view.findViewById(R.id.items);
            items.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                items.setAlpha(1.0f);
                int cx = (items.getLeft() + items.getRight()) / 2;
                int cy = (items.getTop() + items.getBottom()) / 2;
                int cr = items.getWidth();
                ViewAnimationUtils.createCircularReveal(items, cx, cy, 0, cr).start();
            } else {
                items.setTranslationY(-items.getHeight());
                items.animate().setDuration(150L).translationY(0).alpha(1.0f).start();
            }

            final Manga item = adapter.getItemFromView(mRecyclerView, view);
            ((ImageButton)view.findViewById(R.id.items_star)).setImageDrawable(ThemeUtils.getBitmapResourceDrawables(getActivity(), R.drawable.ic_star_outline_white_18dp, R.drawable.ic_star_white_18dp));
            ((ImageButton)view.findViewById(R.id.items_save)).setImageDrawable(ThemeUtils.getBitmapResourceDrawables(getActivity(), R.drawable.ic_bookmark_outline_white_18dp, R.drawable.ic_bookmark_white_18dp));

            if(ReadingSession.isStarred(item))
                    ThemeUtils.toggleTransitionDrawable((ImageView) view.findViewById(R.id.items_star), true);
            if(ReadingSession.isSaved(item))
                    ThemeUtils.toggleTransitionDrawable((ImageView) view.findViewById(R.id.items_save), true);

            new Handler().postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int cx = (items.getLeft() + items.getRight()) / 2;
                    int cy = (items.getTop() + items.getBottom()) / 2;
                    int cr = items.getWidth();
                    Animator anim = ViewAnimationUtils.createCircularReveal(items, cx, cy, cr, 0);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            items.setAlpha(0.0f);
                            items.setVisibility(View.INVISIBLE);
                        }
                    });
                    anim.start();
                } else {
                    items.animate().setDuration(150L).translationY(-items.getHeight()).alpha(0.0f).withEndAction(() -> {
                        items.setTranslationY(0);
                        items.setVisibility(View.INVISIBLE);
                    }).start();
                }
            }, 5 * 1000L);
            return true;
        });

        adapter.setListener(() -> refresh.setRefreshing(false));
        adapter.setContextClickListener((View view, int resId) -> {
            ReadingSession.open(getActivity());
            final Manga item = adapter.getItemFromView(mRecyclerView, view);
            if(resId == R.id.items_star) {
                ThemeUtils.toggleTransitionDrawable((ImageButton) view.findViewById(R.id.items_star),
                        ReadingSession.toggleStarred(item));

                String toast = ReadingSession.isStarred(item) ? "Starred!" : "Removed from starred.";
                Snackbar.make(refresh, toast, Snackbar.LENGTH_SHORT).setAction("Undo", v -> {
                    ReadingSession.toggleStarred(item);
                }).show();
            } else if(resId == R.id.items_save) {
                ThemeUtils.toggleTransitionDrawable((ImageButton) view.findViewById(R.id.items_save),
                        ReadingSession.toggleSaved(item));

                String toast = ReadingSession.isStarred(item) ? "Bookmarked!" : "Removed from bookmarks.";
                Snackbar.make(refresh, toast, Snackbar.LENGTH_SHORT).setAction("Undo", v -> {
                    ReadingSession.toggleSaved(item);
                }).show();
            }
            ReadingSession.save();
        });

        listener = (SharedPreferences sharedPreferences, String s) -> {
            if(!s.equals("expandColumns"))
                return;

            int _columns = expandedColumnCount(sharedPreferences.getBoolean("expandColumns", false));
            ((GridLayoutManager)mRecyclerView.getLayoutManager()).setSpanCount(_columns);
            mRecyclerView.requestLayout();
        };
        ThemeUtils.preferences(this).registerOnSharedPreferenceChangeListener(listener);
        return rootView;
    }

    public LibraryAdapter getAdapter() {
        return adapter;
    }

    private int expandedColumnCount(boolean expanded) {
        if(getActivity() == null) return 1;
        int t = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        boolean wide = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        boolean large = (t == Configuration.SCREENLAYOUT_SIZE_LARGE || t == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        return (expanded ? (wide || large ? 5 : 3) : (wide || large ? 3 : 2));
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int columns = expandedColumnCount(ThemeUtils.preferences(this).getBoolean("expandColumns", false));
        ((GridLayoutManager)mRecyclerView.getLayoutManager()).setSpanCount(columns);
        mRecyclerView.requestLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.adapter.createOptionsMenu(inflater, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.adapter.optionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animator =  super.onCreateAnimator(transit, enter, nextAnim);
        if (animator == null && nextAnim != 0)
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);

        if (animator != null) {
            getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator anim) {
                    getView().setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
        }
        return animator;
    }

    public abstract static class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
        protected Context context = null;
        protected List<Manga> data = new ArrayList<>();
        protected int identifier = 0;
        protected Runnable listener;
        protected boolean populated = false;

        public interface OnClickListener {
            void onClick(View view, int resId);
        }

        private View.OnClickListener clickListener;
        private View.OnLongClickListener longClickListener;
        private LibraryAdapter.OnClickListener contextClickListener;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;

            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView)v.findViewById(R.id.txt);
                mImageView = (ImageView)v.findViewById(R.id.img);
            }
        }

        public void setListener(Runnable l) {
            listener = l;
            if(populated && listener != null) listener.run();
        }

        protected void setPopulated() {
            populated = true;
            if(listener != null) listener.run();
        }

        public LibraryAdapter(Context context, int identifier) {
            this.context = context;
            this.identifier = identifier;
            this.populate();
        }

        @SuppressWarnings("unused")
        public static LibraryAdapter newInstance(Context context, int identifier) {
            return null; //new LibraryAdapter(context, identifier)
        }

        public void setOnClickListener(View.OnClickListener listener) {
            this.clickListener = listener;
        }
        public void setOnLongClickListener(View.OnLongClickListener listener) {
            this.longClickListener = listener;
        }
        public void setContextClickListener(LibraryAdapter.OnClickListener listener) {
            this.contextClickListener = listener;
        }

        public Manga getItemFromView(RecyclerView r, View v) {
            return ((LibraryAdapter)r.getAdapter()).data.get(r.getChildPosition(v));
        }

        @Override
        public LibraryAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.titlecard_layout, parent, false);
            v.setOnClickListener(this.clickListener);
            v.setOnLongClickListener(this.longClickListener);

            v.findViewById(R.id.items_star).setOnClickListener((View view) -> contextClickListener.onClick(v, R.id.items_star));
            v.findViewById(R.id.items_save).setOnClickListener((View view) -> contextClickListener.onClick(v, R.id.items_save));

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(data.get(position).title());
            Glide.with(context)
                    .load(data.get(position).image())
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.library_placeholder))
                    .override(200, 300)
                    .centerCrop()
                    .crossFade(250)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.mImageView);

            if(position == data.size() - 1)
                this.append();
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        protected abstract void populate();
        protected abstract void append();
        protected abstract void refresh();

        protected abstract void createOptionsMenu(MenuInflater inflater, Menu menu);
        protected abstract void optionsItemSelected(MenuItem item);
    }
}