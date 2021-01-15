package com.pretty.library.indicator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import kotlin.math.max
import kotlin.math.min

class LineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Indicator {

    private var pagerCount = 0
    private var roundRadius = 0f
    private var space = 0f
    private var color = Color.GRAY
    private var colorSelect = Color.RED
    private var lineWidth = 0f
    private var lineWidthSelect = 0f
    private var lineHeight = 0f
    private val centerPointF = ArrayList<PointF>()

    private var selectLineCenterX = 0f
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var followScroll = true
    private val animatorInterpolator = LinearInterpolator()
    private val marginRect = RectF(0f, 0f, 0f, 0f)
    private var layoutParamRule = intArrayOf(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.CENTER_HORIZONTAL)

    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.LineIndicatorView)
        setRoundRadius(attr.getDimension(R.styleable.LineIndicatorView_indicator_roundRadius, 10f))
        setColor(attr.getColor(R.styleable.LineIndicatorView_indicator_color, Color.GRAY))
        setColorSelect(attr.getColor(R.styleable.LineIndicatorView_indicator_colorSelect, Color.RED))
        setWidth(attr.getDimension(R.styleable.LineIndicatorView_indicator_lineWidth, 90f))
        setWidthSelect(attr.getDimension(R.styleable.LineIndicatorView_indicator_lineWidthSelect, lineWidth))
        setHeight(attr.getDimension(R.styleable.LineIndicatorView_indicator_lineHeight, 20f))
        setFollowScroll(attr.getBoolean(R.styleable.LineIndicatorView_indicator_followScroll, true))
        setSpace(attr.getDimension(R.styleable.LineIndicatorView_indicator_space, 0f))
        setMargin(attr.getDimension(R.styleable.LineIndicatorView_indicator_margin, 0f))
        setMarginLeft(attr.getDimension(R.styleable.LineIndicatorView_indicator_marginLeft, marginRect.left))
        setMarginTop(attr.getDimension(R.styleable.LineIndicatorView_indicator_marginTop, marginRect.top))
        setMarginRight(attr.getDimension(R.styleable.LineIndicatorView_indicator_marginRight, marginRect.right))
        setMarginBottom(attr.getDimension(R.styleable.LineIndicatorView_indicator_marginBottom, marginRect.bottom))
        attr.recycle()
    }

    override fun getView() = this

    override fun initIndicatorCount(pagerCount: Int) {
        this.centerPointF.clear()
        this.pagerCount = pagerCount
        this.visibility = if (pagerCount > 1) {
            val centerY = lineHeight / 2 + paddingTop
            var startX = max(lineWidth, lineWidthSelect) / 2 + paddingLeft
            val centerSpace = max(lineWidth, lineWidthSelect) + space
            for (i in 0 until pagerCount) {
                centerPointF.add(PointF(startX, centerY))
                startX += centerSpace
            }
            this.selectLineCenterX = centerPointF[0].x
            VISIBLE
        } else
            GONE
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas?) {
        if (centerPointF.size <= 0)
            return
        mPaint.style = Paint.Style.FILL
        canvas?.let {
            drawLines(it)
            drawIndicator(it)
        }
    }

    override fun params(): LayoutParams {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParamRule.forEach {
            params.addRule(it)
        }
        marginRect.run {
            params.leftMargin = left.toInt()
            params.topMargin = top.toInt()
            params.rightMargin = right.toInt()
            params.bottomMargin = bottom.toInt()
        }
        return params
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (centerPointF.isEmpty())
            return
        if (followScroll) {
            val size = centerPointF.size
            val currentPosition = min(size - 1, position)
            val nextPosition = min(size - 1, (position + 1) % pagerCount)
            val current = centerPointF[currentPosition]
            val next = centerPointF[nextPosition]
            selectLineCenterX = current.x + (next.x - current.x) * animatorInterpolator.getInterpolation(
                positionOffset
            )
            invalidate()
        }
    }

    override fun onPageSelected(position: Int) {
        if (!followScroll) {
            selectLineCenterX = centerPointF[position].x
            invalidate()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> width
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                val maxWidth = max(lineWidth, lineWidthSelect).toInt()
                val spaceSize = (pagerCount - 1) * space.toInt()
                paddingLeft + pagerCount * maxWidth + spaceSize + paddingRight
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
                paddingTop + paddingBottom + lineHeight.toInt()
            else -> 0
        }
    }

    private fun drawLines(canvas: Canvas) {
        mPaint.color = Color.GRAY
        centerPointF.forEach {
            val rectF = RectF(it.x - lineWidth / 2, it.y - lineHeight / 2, it.x + lineWidth / 2, it.y + lineHeight / 2)
            canvas.drawRoundRect(rectF, roundRadius, roundRadius, mPaint)
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        mPaint.color = Color.RED
        val rectF =
            RectF(
                selectLineCenterX - lineWidthSelect / 2,
                paddingTop.toFloat(),
                selectLineCenterX + lineWidthSelect / 2,
                paddingTop + lineHeight
            )
        canvas.drawRoundRect(rectF, roundRadius, roundRadius, mPaint)
    }

    /**
     * @author Arvin.xun
     */
    fun setRoundRadius(radius: Float) = apply {
        this.roundRadius = radius
    }

    /**
     * @author Arvin.xun
     */
    fun setColor(color: Int) = apply {
        this.color = color
    }

    /**
     * @author Arvin.xun
     */
    fun setColorSelect(color: Int) = apply {
        this.colorSelect = color
    }

    /**
     * @author Arvin.xun
     */
    fun setWidth(width: Float) = apply {
        this.lineWidth = width
    }

    /**
     * @author Arvin.xun
     */
    fun setWidthSelect(width: Float) = apply {
        this.lineWidthSelect = width
    }

    /**
     * @author Arvin.xun
     */
    fun setHeight(height: Float) = apply {
        this.lineHeight = height
    }

    /**
     * @author Arvin.xun
     */
    fun setFollowScroll(enable: Boolean) = apply {
        this.followScroll = enable
    }

    /**
     * @author Arvin.xun
     */
    fun setMargin(margin: Float) = apply {
        this.marginRect.set(margin, margin, margin, margin)
    }

    /**
     * @author Arvin.xun
     * 设置Point的间距
     */
    fun setSpace(space: Float) = apply {
        this.space = space
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginLeft(left: Float) = apply {
        this.marginRect.left = left
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginTop(top: Float) = apply {
        this.marginRect.top = top
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginRight(right: Float) = apply {
        this.marginRect.right = right
    }

    /**
     * @author Arvin.xun
     */
    fun setMarginBottom(bottom: Float) = apply {
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