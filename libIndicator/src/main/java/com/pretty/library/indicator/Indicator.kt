package com.pretty.library.indicator

import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.Px

/**
 * 可以实现该接口，自定义Indicator
 */
interface Indicator{
    /**
     * Called when data initialization is complete
     */
    fun initIndicatorCount(pagerCount: Int)

    /**
     * return View，and add banner
     */
    fun getView(): View

    /**
     * return RelativeLayout.LayoutParams，Set the position of the banner within the RelativeLayout
     */
    fun params(): RelativeLayout.LayoutParams

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     */
    fun onPageScrolled(position: Int, positionOffset: Float, @Px positionOffsetPixels: Int)

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    fun onPageSelected(position: Int)

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     */
    fun onPageScrollStateChanged(state: Int)
}