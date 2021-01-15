package com.pretty.library.indicator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout.*
import kotlin.math.min

class PointIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Indicator {

    private var spacing = 0f
    private var pagerCount = 0
    private var currentIndex = 0
    private var radius = 0f
    private var color = Color.GRAY
    private var stroke = 0f
    private var strokeColor = Color.GRAY
    private var selectColor = Color.RED
    private var selectStroke = 0f
    private var selectStrokeColor = Color.RED
    private var selectPointX = 0f
    private val marginRect = Rect(0, 0, 0, 0)
    private val circlePoints = ArrayList<PointF>()
    private val animatorInterpolator = LinearInterpolator()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var layoutParamRule = intArrayOf(ALIGN_PARENT_BOTTOM, CENTER_HORIZONTAL)
    private var clickEnable = false
    private var followScroll = true
    private var positionClickListener: OnPositionClickListener? = null

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.PointIndicatorView)
        seRadius(attr.getDimension(R.styleable.PointIndicatorView_indicator_radius, 10f))
        setFullColor(attr.getColor(R.styleable.PointIndicatorView_indicator_color, Color.TRANSPARENT))
        setFullSelectColor(attr.getColor(R.styleable.PointIndicatorView_indicator_colorSelect, Color.RED))
        setStroke(attr.getDimension(R.styleable.PointIndicatorView_indicator_stroke, 0f))
        setSelectStroke(attr.getDimension(R.styleable.PointIndicatorView_indicator_strokeSelect, 0f))
        setStrokeColor(attr.getColor(R.styleable.PointIndicatorView_indicator_strokeColor, color))
        setSelectStrokeColor(attr.getColor(R.styleable.PointIndicatorView_indicator_strokeColorSelect, selectColor))

        setFollowScroll(attr.getBoolean(R.styleable.PointIndicatorView_indicator_followScroll, true))
        setMargin(attr.getDimension(R.styleable.PointIndicatorView_indicator_margin, 0f).toInt())
        setMarginLeft(attr.getDimension(R.styleable.PointIndicatorView_indicator_marginLeft, 0f).toInt())
        setMarginTop(attr.getDimension(R.styleable.PointIndicatorView_indicator_marginTop, 0f).toInt())
        setMarginRight(attr.getDimension(R.styleable.PointIndicatorView_indicator_marginRight, 0f).toInt())
        setMarginBottom(attr.getDimension(R.styleable.PointIndicatorView_indicator_marginBottom, 0f).toInt())
        setSpace(attr.getDimension(R.styleable.PointIndicatorView_indicator_space, radius))
        setClickEnable(attr.getBoolean(R.styleable.PointIndicatorView_indicator_clickEnable, false))
        attr.recycle()
    }

    override fun getView() = this

    override fun initIndicatorCount(pagerCount: Int) {
        this.circlePoints.clear()
        this.pagerCount = pagerCount
        this.visibility = if (pagerCount > 1) {
            val y = radius + paddingTop
            val pointCenterSpace = radius * 2 + spacing
            var startX = radius + paddingLeft
            for (i in 0 until pagerCount) {
                val pointF = PointF(startX, y)
                circlePoints.add(pointF)
                startX += pointCenterSpace
            }
            selectPointX = circlePoints[currentIndex].x
            VISIBLE
        } else
            GONE
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawCircles(it)
            drawIndicator(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (clickEnable) {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> true
                MotionEvent.ACTION_UP -> {
                    positionClickListener?.let { listener ->
                        circlePoints.forEachIndexed { index, pointF ->
                            if (event.x > pointF.x - radius && event.x < radius + pointF.x)
                                listener.positionClick(index)
                        }
                    }
                    super.onTouchEvent(event)
                }
                else ->
                    super.onTouchEvent(event)
            }
        } else
            super.onTouchEvent(event)

    }

    override fun params(): LayoutParams {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParamRule.forEach {
            params.addRule(it)
        }
        marginRect.let {
            params.leftMargin = left
            params.topMargin = top
            params.rightMargin = right
            params.bottomMargin = bottom
        }
        return params
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (circlePoints.isEmpty())
            return
        if (followScroll) {
            val currentPosition = min(circlePoints.size - 1, position)
            val nextPosition = min(circlePoints.size - 1, (position + 1) % pagerCount)
            val current: PointF = circlePoints[currentPosition]
            val next: PointF = circlePoints[nextPosition]
            selectPointX = current.x + (next.x - current.x) * animatorInterpolator.getInterpolation(
                positionOffset
            )
            invalidate()
        }
    }

    override fun onPageSelected(position: Int) {
        if (!followScroll) {
            currentIndex = position
            selectPointX = circlePoints[currentIndex].x
            invalidate()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> width
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                val positionWidth = pagerCount * radius.toInt() * 2
                val spaceWidth = (pagerCount - 1) * spacing.toInt()
                paddingLeft + positionWidth + +spaceWidth + paddingRight
            }
            else -> 0
        }
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> height
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED ->
                (radius * 2 + paddingTop + paddingBottom).toInt()
            else -> 0
        }
    }

    private fun drawCircles(canvas: Canvas) {
        mPaint.style = Paint.Style.FILL
        circlePoints.forEach { pointF ->
            if (stroke > 0) {
                mPaint.color = strokeColor
                canvas.drawCircle(pointF.x, pointF.y, radius, mPaint)
            }
            mPaint.color = color
            canvas.drawCircle(pointF.x, pointF.y, radius - stroke, mPaint)
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        if (circlePoints.size > 0) {
            val indicatorPointY = radius + paddingTop
            mPaint.style = Paint.Style.FILL
            if (selectStroke > 0) {
                mPaint.color = selectStrokeColor
                canvas.drawCircle(selectPointX, indicatorPointY, radius, mPaint)
            }
            mPaint.color = selectColor
            canvas.drawCircle(selectPointX, indicatorPointY, radius - selectStroke, mPaint)
        }
    }

    /*-------------------------下面是对外的函数----------------------------------- */

    /**
     * 设置Point的半径
     * @author Arvin.xun
     */
    fun seRadius(radius: Float) = apply {
        this.radius = radius
    }

    /**
     * 设置Point的颜色
     * @author Arvin.xun
     */
    fun setFullColor(color: Int) = apply {
        this.color = color
    }

    /**
     * 设置Point的内边框
     * @author Arvin.xun
     */
    fun setStroke(stroke: Float) = apply {
        this.stroke = stroke
    }

    /**
     * 设置Point内边框的颜色
     * @author Arvin.xun
     */
    fun setStrokeColor(color: Int) = apply {
        this.strokeColor = color
    }

    /**
     * 设置指示器颜色
     * @author Arvin.xun
     */
    fun setFullSelectColor(color: Int) = apply {
        this.selectColor = color
    }

    /**
     * 设置示器内边
     * @author Arvin.xun
     */
    fun setSelectStroke(stroke: Float) = apply {
        this.selectStroke = stroke
    }

    /**
     * 设置指示器边框颜色
     * @author Arvin.xun
     */
    fun setSelectStrokeColor(color: Int) = apply {
        this.selectStrokeColor = color
    }

    /**
     * @author Arvin.xun
     * 设置Point的间距
     */
    fun setSpace(space: Float) = apply {
        this.spacing = space
    }

    /**
     * 设置指示器是否跟随滑动
     * @author Arvin.xun
     */
    fun setFollowScroll(followScroll: Boolean) = apply {
        this.followScroll = followScroll
    }

    /**
     * 设置是否可点击
     * @author Arvin.xun
     */
    fun setClickEnable(enable: Boolean) = apply {
        this.clickEnable = enable
    }

    /**
     * 设置位置点击事件
     *  @author Arvin.xun
     */
    fun setPositionClickListener(listener: OnPositionClickListener) = apply {
        this.clickEnable = true
        this.positionClickListener = listener
    }

    /**
     * @author Arvin.xun
     */
    fun setMargin(margin: Int) = apply {
        this.marginRect.let {
            left = margin
            top = margin
            right = margin
            bottom = margin
        }
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginLeft(left: Int) = apply {
        this.marginRect.left = left
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginTop(top: Int) = apply {
        this.marginRect.top = top
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginRight(right: Int) = apply {
        this.marginRect.right = right
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginBottom(bottom: Int) = apply {
        this.marginRect.bottom = bottom
    }

    /**
     * 添加布局限制条件
     * @author Arvin.xun
     */
    fun addLayoutParamRule(vararg rules: Int) = apply {
        this.layoutParamRule = rules
    }

}