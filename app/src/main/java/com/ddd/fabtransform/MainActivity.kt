package com.ddd.fabtransform

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.TranslateAnimation
import com.ddd.fabtransform.util.FabToBottomNavigationAnim
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by S.C. on 21/06/18.
 */

class MainActivity : AppCompatActivity() {

    lateinit var anim: FabToBottomNavigationAnim
    var isClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {

        anim = FabToBottomNavigationAnim(fab, llReveal, llButtons, seekBar, pause)

        fab.setOnClickListener {
            fab.setImageResource(R.drawable.ic_play)

            val handler = Handler()
            val runnable = Runnable {
                animateFabToBottomNav()
                val translateAnimation = TranslateAnimation(0.0f, 0.0f, 0.0f, convertDpToPixel(56f))

                translateAnimation.duration = 350 // 1 second
                translateAnimation.fillAfter = true
                llBot.startAnimation(translateAnimation)
                llTop.startAnimation(translateAnimation)
            }
            handler.postDelayed(runnable, 150)

        }

        llReveal.setOnClickListener {
            animateBottomNavToFab()

            llBottom.animate().scaleX(1f).scaleY(1f).setDuration(350).start()

            val translateAnimation = TranslateAnimation(0.0f, 0.0f, convertDpToPixel(56f), 0.0f)
            translateAnimation.duration = 350 // 1 second
            translateAnimation.fillAfter = true
            llTop.startAnimation(translateAnimation)
            llBot.startAnimation(translateAnimation)
            fab.setImageResource(R.drawable.ic_play)
        }
    }

    private fun animateFabToBottomNav() {
        anim.showNavigationView()
    }

    private fun animateBottomNavToFab() {
        anim.hideNavigationView()
    }

    override fun onBackPressed() {
        if (fab.visibility == View.GONE) {
            animateBottomNavToFab()

            val translateAnimation = TranslateAnimation(0.0f, 0.0f, convertDpToPixel(56f), 0.0f)
            translateAnimation.duration = 350 // 1 second
            translateAnimation.fillAfter = true
            llTop.startAnimation(translateAnimation)
            llBot.startAnimation(translateAnimation)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float): Float {
        val resources = resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
