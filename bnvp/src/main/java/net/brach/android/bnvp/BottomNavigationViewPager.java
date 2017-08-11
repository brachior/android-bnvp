package net.brach.android.bnvp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("TryWithIdenticalCatches")
public class BottomNavigationViewPager extends RelativeLayout {
    // item modes
    private static final int ITEM_MODE_ALL = 0;
    private static final int ITEM_MODE_ICON = 1;
    private static final int ITEM_MODE_LABEL = 2;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private final ViewPager pager;
    private final BottomNavigationView navigation;

    private boolean textVisible;
    private boolean iconVisible;

    private BottomNavigationMenuView mMenuView;
    private BottomNavigationItemView[] mButtons;
    private boolean visibilityTextSizeRecord;
    private boolean visibilityHeightRecord;
    private float mLargeLabelSize;
    private float mSmallLabelSize;
    private int mItemHeight;
    private int navHeight;

    public BottomNavigationViewPager(Context context) {
        this(context, null);
    }

    public BottomNavigationViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationViewPager, 0, 0);
        boolean swipeable = a.getBoolean(R.styleable.BottomNavigationViewPager_bnvp_pager_swipeable, true);
        int pagerBG = a.getColor(R.styleable.BottomNavigationViewPager_bnvp_pager_background, Color.TRANSPARENT);

        int menu = a.getResourceId(R.styleable.BottomNavigationViewPager_bnvp_nav_menu, -1);
        int navBG = a.getColor(R.styleable.BottomNavigationViewPager_bnvp_nav_background, Color.WHITE);
        final boolean shiftable = a.getBoolean(R.styleable.BottomNavigationViewPager_bnvp_nav_shiftMode, true);
        ColorStateList itemIconTint = a.getColorStateList(R.styleable.BottomNavigationViewPager_bnvp_nav_itemIconTint);
        textVisible = true;
        iconVisible = true;
        switch (a.getInt(R.styleable.BottomNavigationViewPager_bnvp_nav_itemMode, ITEM_MODE_ALL)) {
            case ITEM_MODE_ICON:
                textVisible = false;
                break;
            case ITEM_MODE_LABEL:
                iconVisible = false;
                break;
        }
        a.recycle();

        navigation = new BottomNavigationView(context);
        LayoutParams navParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        navParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        navigation.setLayoutParams(navParams);
        navigation.setId(generateViewId());
        navigation.setBackgroundColor(navBG);
        navigation.setItemTextColor(itemIconTint);
        navigation.setItemIconTintList(itemIconTint);
        addView(navigation);

        addOnGlobalLayoutListener(navigation, new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeOnGlobalLayoutListener(navigation, this);

                navHeight = navigation.getHeight();
                LayoutParams navParams = new LayoutParams(LayoutParams.MATCH_PARENT, navHeight);
                navParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                navigation.setLayoutParams(navParams);

                if (!shiftable) {
                    BottomNavigationMenuView mMenuView = getMenuView();
                    if (mMenuView != null) {
                        setField(BottomNavigationMenuView.class, mMenuView, "mShiftingMode", false);

                        BottomNavigationItemView[] mButtons = getItemViews();
                        if (mButtons != null) {
                            for (BottomNavigationItemView button : mButtons) {
                                setField(BottomNavigationItemView.class, button, "mShiftingMode", false);
                            }
                        }

                        updateMenuView();
                    }
                }

                setTextVisibility(textVisible);
                setIconVisibility(iconVisible);
            }
        });

        pager = swipeable
                ? new ViewPager(context)
                : new NonSwipeableViewPager(context);
        LayoutParams pagerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pagerParams.addRule(ALIGN_PARENT_TOP, TRUE);
        pagerParams.addRule(ABOVE, navigation.getId());
        pager.setLayoutParams(pagerParams);
        pager.setId(generateViewId());
        pager.setBackgroundColor(pagerBG);
        addView(pager);

        final HashMap<MenuItem, Integer> positions = new HashMap<>();
        if (menu != -1) {
            navigation.inflateMenu(menu);
            Menu m = navigation.getMenu();
            for (int i = 0; i < m.size(); i++) {
                positions.put(m.getItem(i), i);
            }
        }

        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        pager.setCurrentItem(positions.get(item));
                        return true;
                    }
                });
    }

    /** {@link BottomNavigationView} **/

    public View getItemView(int position) {
        return getItemViews()[position];
    }

    /** {@link ViewPager} **/

    public void setAdapter(PagerAdapter adapter) {
        pager.setAdapter(adapter);
    }

    public int getCurrentItem() {
        return pager.getCurrentItem();
    }

    public void setCurrentItem(int item) {
        BottomNavigationMenuView mMenuView = getMenuView();
        OnClickListener mOnClickListener = getField(BottomNavigationMenuView.class, mMenuView, "mOnClickListener");
        if (mOnClickListener != null) {
            mOnClickListener.onClick(getItemView(item));
        }
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        pager.addOnPageChangeListener(listener);
    }

    public void removeOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        pager.removeOnPageChangeListener(listener);
    }

    public void clearOnPageChangeListeners() {
        pager.clearOnPageChangeListeners();
    }

    /** private **/

    @SuppressLint("NewApi")
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    private BottomNavigationMenuView getMenuView() {
        if (null == mMenuView) {
            mMenuView = getField(BottomNavigationView.class, navigation, "mMenuView");
        }
        return mMenuView;
    }

    private BottomNavigationItemView[] getItemViews() {
        if (null != mButtons) {
            return mButtons;
        }
        BottomNavigationMenuView mMenuView = getMenuView();
        mButtons = getField(BottomNavigationMenuView.class, mMenuView, "mButtons");
        return mButtons;
    }

    private void setTextVisibility(boolean visibility) {
        BottomNavigationItemView[] mButtons = getItemViews();

        for (BottomNavigationItemView button : mButtons) {
            TextView mLargeLabel = getField(BottomNavigationItemView.class, button, "mLargeLabel");
            TextView mSmallLabel = getField(BottomNavigationItemView.class, button, "mSmallLabel");
            if (mLargeLabel == null || mSmallLabel == null) {
                return;
            }

            if (!visibility) {
                if (!visibilityTextSizeRecord) {
                    visibilityTextSizeRecord = true;
                    mLargeLabelSize = mLargeLabel.getTextSize();
                    mSmallLabelSize = mSmallLabel.getTextSize();
                }

                mLargeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0);
                mSmallLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 0);
            } else {
                if (!visibilityTextSizeRecord) {
                    break;
                }

                mLargeLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLargeLabelSize);
                mSmallLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSmallLabelSize);
            }
        }

        if (!visibility) {
            if (!visibilityHeightRecord) {
                visibilityHeightRecord = true;
                mItemHeight = getItemHeight();
            }
            setItemHeight(mItemHeight - getFontHeight(mSmallLabelSize));
        } else {
            if (!visibilityHeightRecord) {
                return;
            }
            setItemHeight(mItemHeight);
        }

        updateMenuView();
    }

    private void setIconVisibility(boolean visibility) {
        BottomNavigationItemView[] mButtons = getItemViews();

        for (BottomNavigationItemView button : mButtons) {
            ImageView mIcon = getField(button.getClass(), button, "mIcon");
            if (mIcon == null) {
                return;
            }

            mIcon.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
        }

        if (!visibility) {
            if (!visibilityHeightRecord) {
                visibilityHeightRecord = true;
                mItemHeight = getItemHeight();
            }

            BottomNavigationItemView button = mButtons[0];
            if (null != button) {
                final ImageView mIcon = getField(button.getClass(), button, "mIcon");
                if (null != mIcon) {
                    mIcon.post(new Runnable() {
                        @Override
                        public void run() {
                            setItemHeight(mItemHeight - mIcon.getMeasuredHeight());
                        }
                    });
                }
            }
        } else {
            if (!visibilityHeightRecord) {
                return;
            }
            setItemHeight(mItemHeight);

            if (!textVisible) {
                for (BottomNavigationItemView item: getItemViews()) {
                    setField(BottomNavigationItemView.class, item, "mDefaultMargin", (int) (mSmallLabelSize * 1.2));
                }
            }
        }

        updateMenuView();
    }

    private static int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    private void setItemHeight(int height) {
        BottomNavigationMenuView mMenuView = getMenuView();
        setField(mMenuView.getClass(), mMenuView, "mItemHeight", height);

        updateMenuView();
    }

    private int getItemHeight() {
        BottomNavigationMenuView mMenuView = getMenuView();
        Integer mItemHeight = getField(mMenuView.getClass(), mMenuView, "mItemHeight");
        return mItemHeight == null
                ? 0
                : mItemHeight;
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Class targetClass, Object instance, String fieldName) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    private void setField(Class targetClass, Object instance, String fieldName, Object value) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
    }

    private static void addOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    @SuppressWarnings("deprecation")
    private static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }

    @SuppressWarnings("RestrictedApi")
    private void updateMenuView() {
        getMenuView().updateMenuView();
    }
}
