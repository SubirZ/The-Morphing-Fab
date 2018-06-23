package com.ddd.fabtransform.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.ddd.fabtransform.R;


public class FabToBottomNavigationAnim {

    private static final int ANIM_CURVED_PATH_DURATION = 300;
    private static final int ANIM_CIRCULAR_REVEAL_DURATION = 300;
    private static final int ANIM_CIRCULAR_REVEAL_DELAY = 100;
    private static final int ANIM_REVERSE_CIRCULAR_REVEAL_DURATION = 300;
    private static final int ANIM_REVERSE_PATH_DELAY = 100;

    private final FloatingActionButton fabView;
    private final ViewGroup navigationView;
    private final ViewGroup btnView;
    private final SeekBar seekBar;
    private final ImageView ivPause;
    private float fabMargin;

    private AnimatorSet showAnimatorSet;
    private AnimatorSet hideAnimatorSet;

    public FabToBottomNavigationAnim(FloatingActionButton fabView, ViewGroup navigationView, ViewGroup btnView, SeekBar seekBar, ImageView ivPause) {
        this.fabView = fabView;
        this.navigationView = navigationView;
        this.btnView = btnView;
        this.seekBar = seekBar;
        this.ivPause = ivPause;
        this.fabMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                32f, navigationView.getResources().getDisplayMetrics());
    }

    public void showNavigationView() {
        // don't show if it's already visible
        if (navigationView.getVisibility() == View.VISIBLE) {
            return;
        }

        // don't show if it's already gonna show it
        if (showAnimatorSet != null && showAnimatorSet.isStarted()) {
            return;
        }

        Animator curvedAnim = createCurvedPath(navigationView);
        Animator fadeAnim = createFadeOutAnimation();
        Animator scaleBtnAnim = createScaleOutAnimation();
        Animator scaleAnim = createScaleAnimation();
        Animator circularRevealAnim = createCircularReveal(navigationView, ivPause);

        showAnimatorSet = new AnimatorSet();
        showAnimatorSet.playTogether(curvedAnim, fadeAnim, scaleBtnAnim, scaleAnim, circularRevealAnim);
        showAnimatorSet.start();
        showAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                fabView.setImageResource(R.drawable.ic_pause);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fabView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void hideNavigationView() {
        fabView.setVisibility(View.VISIBLE);
        // don't hide if we're already hidden
        if (navigationView.getVisibility() != View.VISIBLE) {
            return;
        }

        // don't hide if we're already hiding it
        if (hideAnimatorSet != null && hideAnimatorSet.isStarted()) {
            return;
        }

        // also, don't hide if we're gonna show it
        if (showAnimatorSet != null && showAnimatorSet.isStarted()) {
            return;
        }

        Animator curvedAnim = createReverseCurvedPath(fabView, navigationView);
        Animator fadeAnim = createFadeInAnimation();
        Animator circularRevealAnim = createReverseCircularReveal(navigationView, ivPause);

        hideAnimatorSet = new AnimatorSet();
        hideAnimatorSet.playTogether(curvedAnim, fadeAnim, circularRevealAnim);
        hideAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigationView.setVisibility(View.INVISIBLE);
                fabView.setVisibility(View.VISIBLE);
                fabView.setAlpha(1f);
                fabView.setTranslationX(0f);
                fabView.setTranslationY(0f);
                fabView.setImageResource(R.drawable.ic_play);
            }
        });
        hideAnimatorSet.start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    private static Animator createCircularReveal(final View navigationView, ImageView ivPause) {

        Animator anim;

        // Check if the runtime version is at least Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = createCircularRevealLollipop(navigationView, ivPause);
        } else {
            anim = ObjectAnimator.ofFloat(navigationView, "alpha", 0f, 1f);
        }

        anim.setDuration(ANIM_CIRCULAR_REVEAL_DURATION);
        anim.setStartDelay(ANIM_CIRCULAR_REVEAL_DELAY);

        // make the view visible and start the animation
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                navigationView.setVisibility(View.VISIBLE);
            }
        });

        return anim;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Animator createCircularRevealLollipop(View myView, ImageView ivPause) {
        Animator anim;// get the center for the clipping circle
        int cx = (int) ivPause.getX() + 48;
        int cy = (int) ivPause.getY() + 48;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        anim = ViewAnimationUtils.createCircularReveal(
                myView, cx, cy, 0f, finalRadius);
        return anim;
    }


    private Animator createCurvedPath(ViewGroup navigationView) {
        float fabDestinationX = -(int) ivPause.getX() + 96;

        AnimatorPath path = new AnimatorPath();
        path.moveTo(0f, 0f);
        path.curveTo(
                0f, fabMargin,
                fabDestinationX / 2f, fabMargin,
                fabDestinationX,
                fabMargin);

        Animator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());

        anim.setInterpolator(AnimUtils.getMaterialInterpolator(navigationView.getContext()));
        anim.setDuration(ANIM_CURVED_PATH_DURATION);

        return anim;
    }

    private ObjectAnimator createFadeOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(fabView, "alpha", 1f, 0f);
        anim.setDuration(ANIM_CURVED_PATH_DURATION);
        return anim;
    }

    private ObjectAnimator createScaleOutAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(btnView, "scaleX", .3f, 1f);
        anim.setDuration(450);
        return anim;
    }

    private ObjectAnimator createScaleAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(seekBar, "scaleX", .3f, 1f);
        anim.setDuration(450);
        return anim;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    private static Animator createReverseCircularReveal(final View navigationView, ImageView ivPause) {

        Animator anim;

        // Check if the runtime version is at least Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = createReverseCircularRevealLollipop(navigationView, ivPause);
        } else {
            anim = ObjectAnimator.ofFloat(navigationView, "alpha", 1f, 0f);
        }

        anim.setDuration(ANIM_REVERSE_CIRCULAR_REVEAL_DURATION);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                navigationView.setVisibility(View.INVISIBLE);
            }
        });
        return anim;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Animator createReverseCircularRevealLollipop(View myView, ImageView ivPause) {
        Animator anim;// get the center for the clipping circle
        int cx = (int) ivPause.getX() + 48;
        int cy = (int) ivPause.getY() + 48;

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, finalRadius, 0f);
        return anim;
    }

    private Animator createReverseCurvedPath(final View fabView, final ViewGroup navigationView) {
        float fabDestinationX = -(int) ivPause.getX() + 96;

        AnimatorPath path = new AnimatorPath();
        path.moveTo(fabDestinationX, fabMargin);
        path.curveTo(
                fabDestinationX / 2f, fabMargin,
                0f, fabMargin,
                0f, 0f);

        Animator anim = ObjectAnimator.ofObject(this, "fabLoc",
                new PathEvaluator(), path.getPoints().toArray());

        anim.setInterpolator(AnimUtils.getMaterialInterpolator(navigationView.getContext()));
        anim.setDuration(ANIM_CURVED_PATH_DURATION);
        anim.setStartDelay(ANIM_REVERSE_PATH_DELAY);

        // make the view visible and start the animation
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabView.setVisibility(View.VISIBLE);
            }
        });


        return anim;
    }

    private ObjectAnimator createFadeInAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(fabView, "alpha", 0f, 1f);
        anim.setDuration(ANIM_CURVED_PATH_DURATION);
        anim.setStartDelay(ANIM_REVERSE_PATH_DELAY);
        return anim;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * We need this setter to translate between the information the animator
     * produces (a new "PathPoint" describing the current animated location)
     * and the information that the button requires (an xy location). The
     * setter will be called by the ObjectAnimator given the 'fabLoc'
     * property string.
     */
    @SuppressWarnings("unused")
    public void setFabLoc(PathPoint newLoc) {
        fabView.setTranslationX(newLoc.mX);
        fabView.setTranslationY(newLoc.mY);
    }

}