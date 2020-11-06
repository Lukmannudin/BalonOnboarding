package com.oleg.balononboarding.showcaseview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.oleg.balononboarding.showcaseview.config.AlignType
import com.oleg.balononboarding.showcaseview.config.DismissType
import com.oleg.balononboarding.showcaseview.listener.GuideListener

@SuppressLint("ViewConstructor")
class GuideView private constructor(context: Context, view: View?) : FrameLayout(context) {
    private val selfPaint = Paint()
    private val paintLine = Paint()
    private val paintCircle = Paint()
    private val paintCircleInner = Paint()
    private val paintTriangleIndicator = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val X_FER_MODE_CLEAR: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val target: View?
    private var targetRect: RectF
    private val selfRect = Rect()
    private val density: Float
    private var stopY = 0f
    private var isTop = false
    var isShowing = false
        private set
    private var yMessageView = 0
    private var startYLineAndCircle = 0f
    private var circleIndicatorSize = 0f
    private var circleIndicatorSizeFinal = 0f
    private var circleInnerIndicatorSize = 0f
    private var lineIndicatorWidthSize = 0f
    private var messageViewPadding = 0
    private var marginGuide = 0f
    private var strokeCircleWidth = 0f
    private var indicatorHeight = 0f
    private val isPerformedAnimationSize = false
    private var mGuideListener: GuideListener? = null
    private var mAlignType: AlignType? = null
    private var dismissType: DismissType? = null
    var mMessageView: GuideMessageView

    private fun init() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density
        marginGuide = MARGIN_INDICATOR * density
        indicatorHeight = INDICATOR_HEIGHT * density
        messageViewPadding = (MESSAGE_VIEW_PADDING * density).toInt()
        strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density
    }

    private val navigationBarSize: Int
        get() {
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }
    private val isLandscape: Boolean
        get() {
            val display_mode = resources.configuration.orientation
            return display_mode != Configuration.ORIENTATION_PORTRAIT
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (target != null) {
            selfPaint.color = BACKGROUND_COLOR
            selfPaint.style = Paint.Style.FILL
            selfPaint.isAntiAlias = true
            canvas.drawRect(selfRect, selfPaint)
            paintLine.style = Paint.Style.FILL
            paintLine.color = LINE_INDICATOR_COLOR
            paintLine.strokeWidth = lineIndicatorWidthSize
            paintLine.isAntiAlias = true
            paintCircle.style = Paint.Style.STROKE
            paintCircle.color = CIRCLE_INDICATOR_COLOR
            paintCircle.strokeCap = Paint.Cap.ROUND
            paintCircle.strokeWidth = strokeCircleWidth
            paintCircle.isAntiAlias = true
            paintCircleInner.style = Paint.Style.FILL
            paintCircleInner.color = CIRCLE_INNER_INDICATOR_COLOR
            paintCircleInner.isAntiAlias = true
            val x = targetRect.left / 2 + targetRect.right / 2
            drawTriangle(
                canvas,
                paintTriangleIndicator,
                x.toInt(),
                startYLineAndCircle.toInt() - 40,
                50
            )
            targetPaint.xfermode = X_FER_MODE_CLEAR
            targetPaint.isAntiAlias = true
            canvas.drawRoundRect(
                targetRect,
                RADIUS_SIZE_TARGET_RECT.toFloat(),
                RADIUS_SIZE_TARGET_RECT.toFloat(),
                targetPaint
            )
        }
    }

    fun drawTriangle(canvas: Canvas, paint: Paint, x: Int, y: Int, width: Int) {
        val halfWidth = width / 2
        val path = Path()
        path.moveTo(x.toFloat(), y + halfWidth.toFloat()) // Top
        path.lineTo(x - halfWidth.toFloat(), y - halfWidth.toFloat()) // Bottom left
        path.lineTo(x + halfWidth.toFloat(), y - halfWidth.toFloat()) // Bottom right
        path.lineTo(x.toFloat(), y + halfWidth.toFloat()) // Back to Top
        path.close()
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)
    }

    @JvmOverloads
    fun dismiss(view: View? = target) {
        ((context as Activity).window.decorView as ViewGroup).removeView(this)
        isShowing = false
        if (mGuideListener != null) {
            mGuideListener!!.onDismiss(view)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        if (dismissType == null) {
            return false
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (dismissType) {
                DismissType.outside -> if (!isViewContains(mMessageView, x, y)) {
                    dismiss()
                }
                DismissType.anywhere -> dismiss()
                DismissType.targetView -> if (targetRect.contains(x, y)) {
                    target!!.performClick()
                    dismiss()
                }
                DismissType.button -> mMessageView.childContentButton.setOnClickListener { view: View? -> dismiss() }
            }
            return true
        }
        return false
    }

    private fun isViewContains(view: View, rx: Float, ry: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    private fun setMessageLocation(p: Point) {
        mMessageView.x = p.x.toFloat()
        mMessageView.y = p.y.toFloat()
        postInvalidate()
    }

    fun updateGuideViewLocation() {
        requestLayout()
    }

    private fun resolveMessageViewLocation(): Point {
        var xMessageView = 0
        xMessageView = if (mAlignType == AlignType.center) {
            (targetRect.left - mMessageView.width / 2 + target!!.width / 2).toInt()
        } else {
            targetRect.right.toInt() - mMessageView.width
        }
        if (isLandscape) {
            xMessageView -= navigationBarSize
        }
        if (xMessageView + mMessageView.width > width) {
            xMessageView = width - mMessageView.width
        }
        if (xMessageView < 0) {
            xMessageView = 0
        }

        //set message view bottom
        if (targetRect.top + indicatorHeight > height / 2) {
            isTop = false
            yMessageView = (targetRect.top - mMessageView.height - indicatorHeight).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect.top + target!!.height + indicatorHeight).toInt()
        }
        if (yMessageView < 0) {
            yMessageView = 0
        }
        return Point(xMessageView, yMessageView)
    }

    fun show() {
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.isClickable = false
        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        startAnimation(startAnimation)
        isShowing = true
    }

    fun setTitle(str: String?) {
        mMessageView.setTitle(str)
    }

    fun setContentText(str: String?) {
        mMessageView.setContentText(str)
    }

    fun setTitleGravity(setGravity: Int) {
        mMessageView.mTitleTextView.gravity = setGravity
    }

    fun setContentGravity(setGravity: Int) {
        mMessageView.mContentTextView.gravity = setGravity
    }

    fun setButtonGravity(setGravity: Int) {
        mMessageView.childContentButton.gravity = setGravity
    }

    fun setButtonText(buttonText: String?) {
        mMessageView.okButton.text = buttonText
    }

    fun setButtonBackground(drawable: Drawable?) {
        mMessageView.okButton.background = drawable
    }

    fun setButtonTextColor(@ColorInt color: Int) {
        mMessageView.okButton.setTextColor(color)
    }

    fun setTitlePadding(paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
        val mPaddingLeft = mMessageView.padding + paddingLeft
        val mPaddingRight = mMessageView.padding + paddingRight
        val mPaddingBottom = mMessageView.paddingBetween + paddingBottom
        val mPaddingTop = mMessageView.paddingBetween + paddingTop

        mMessageView.mTitleTextView.setPadding(
            mPaddingLeft,
            mPaddingTop,
            mPaddingRight,
            mPaddingBottom
        )
    }

    fun setMessagePadding(
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        val mPaddingLeft = mMessageView.padding + paddingLeft
        val mPaddingRight = mMessageView.padding + paddingRight
        val mPaddingBottom = mMessageView.paddingBetween + paddingBottom
        val mPaddingTop = mMessageView.paddingBetween + paddingTop
        mMessageView.mContentTextView.setPadding(
            mPaddingLeft,
            mPaddingTop,
            mPaddingRight,
            mPaddingBottom
        )
    }

    fun setButtonPadding(paddingLeft: Int, paddingTop: Int, paddingRight: Int, paddingBottom: Int) {
        val mPaddingLeft = mMessageView.padding + paddingLeft
        val mPaddingRight = mMessageView.padding + paddingRight
        val mPaddingBottom = mMessageView.paddingBetween + paddingBottom
        val mPaddingTop = mMessageView.paddingBetween + paddingTop
        mMessageView.childContentButton.setPadding(
            mPaddingLeft,
            mPaddingTop,
            mPaddingRight,
            mPaddingBottom
        )
    }

    fun setContentSpan(span: Spannable?) {
        mMessageView.setContentSpan(span)
    }

    fun setTitleTypeFace(typeFace: Typeface?) {
        mMessageView.setTitleTypeFace(typeFace)
    }

    fun setContentTypeFace(typeFace: Typeface?) {
        mMessageView.setContentTypeFace(typeFace)
    }

    fun setTitleTextSize(size: Int) {
        mMessageView.setTitleTextSize(size)
    }

    fun setContentTextSize(size: Int) {
        mMessageView.setContentTextSize(size)
    }

    class Builder(private val context: Context) {
        private var targetView: View? = null
        private var title: String? = null
        private var contentText: String? = null
        private var alignType: AlignType? = null
        private var dismissType: DismissType? = null
        private var contentSpan: Spannable? = null
        private var titleTypeFace: Typeface? = null
        private var contentTypeFace: Typeface? = null
        private var guideListener: GuideListener? = null
        private var titleTextSize = 0
        private var contentTextSize = 0
        private var lineIndicatorHeight = 0f
        private var lineIndicatorWidthSize = 0f
        private var circleIndicatorSize = 0f
        private var circleInnerIndicatorSize = 0f
        private var strokeCircleWidth = 0f
        private var titleGravity = 0
        private var contentGravity = 0
        private var buttonGravity = 0
        private var buttonText: String? = null
        private var buttonBackground: Drawable? = null
        private var buttonTextColor: Int? = null
        private var paddingLeftTitle = 0
        private var paddingRightTitle = 0
        private var paddingTopTitle = 0
        private var paddingBottomTitle = 0
        private var paddingLeftMessage = 0
        private var paddingRightMessage = 0
        private var paddingBottomMessage = 0
        private var paddingTopMessage = 0
        private var paddingLeftButton = 0
        private var paddingRightButton = 0
        private var paddingTopButton = 0
        private var paddingBottomButton = 0
        fun setTargetView(view: View?): Builder {
            targetView = view
            return this
        }

        /**
         * alignType GuideView
         *
         * @param alignType it should be one type of AlignType enum.
         */
        fun setViewAlign(alignType: AlignType?): Builder {
            this.alignType = alignType
            return this
        }

        /**
         * defining a title
         *
         * @param title a title. for example: submit button.
         */
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        /**
         * defining a description for the target view
         *
         * @param contentText a description. for example: this button can for submit your information..
         */
        fun setContentText(contentText: String?): Builder {
            this.contentText = contentText
            return this
        }

        /**
         * @param paddingLeftTitle
         * @param paddingTopTitle
         * @param paddingRightTitle
         * @param paddingBottomTitle
         * @return
         */
        fun setPaddingTitle(
            paddingLeftTitle: Int, paddingTopTitle: Int, paddingRightTitle: Int,
            paddingBottomTitle: Int
        ): Builder {
            this.paddingLeftTitle = paddingLeftTitle
            this.paddingTopTitle = paddingTopTitle
            this.paddingRightTitle = paddingRightTitle
            this.paddingBottomTitle = paddingBottomTitle
            return this
        }

        /**
         * @param paddingLeftMessage
         * @param paddingTopTitle
         * @param paddingRightTitle
         * @param paddingBottomTitle
         * @return
         */
        fun setPaddingMessage(
            paddingLeftMessage: Int, paddingTopTitle: Int, paddingRightTitle: Int,
            paddingBottomTitle: Int
        ): Builder {
            this.paddingLeftMessage = paddingLeftMessage
            paddingTopMessage = paddingTopTitle
            paddingRightMessage = paddingRightTitle
            paddingBottomMessage = paddingBottomTitle
            return this
        }

        fun setPaddingButton(
            paddingLeftButton: Int, paddingTopButton: Int, paddingRightButton: Int,
            paddingBottomButton: Int
        ): Builder {
            this.paddingLeftButton = paddingLeftButton
            this.paddingTopButton = paddingTopButton
            this.paddingRightButton = paddingRightButton
            this.paddingBottomButton = paddingBottomButton
            return this
        }

        /**
         * setting spannable type
         *
         * @param span a instance of spannable
         */
        fun setContentSpan(span: Spannable?): Builder {
            contentSpan = span
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setContentTypeFace(typeFace: Typeface?): Builder {
            contentTypeFace = typeFace
            return this
        }

        /**
         * adding a listener on show case view
         *
         * @param guideListener a listener for events
         */
        fun setGuideListener(guideListener: GuideListener?): Builder {
            this.guideListener = guideListener
            return this
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         */
        fun setTitleTypeFace(typeFace: Typeface?): Builder {
            titleTypeFace = typeFace
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setContentTextSize(size: Int): Builder {
            contentTextSize = size
            return this
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        fun setTitleTextSize(size: Int): Builder {
            titleTextSize = size
            return this
        }

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        fun setDismissType(dismissType: DismissType?): Builder {
            this.dismissType = dismissType
            return this
        }

        /**
         * changing line height indicator
         *
         * @param height you can change height indicator (Converting to Dp)
         */
        fun setIndicatorHeight(height: Float): Builder {
            lineIndicatorHeight = height
            return this
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        fun setIndicatorWidthSize(width: Float): Builder {
            lineIndicatorWidthSize = width
            return this
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        fun setCircleIndicatorSize(size: Float): Builder {
            circleIndicatorSize = size
            return this
        }

        /**
         * set title gravity. you can use Gravity.CENTER, Gravity.LEFT, or Gravity.RIGHT
         *
         * @param titleGravity int
         * @return builder
         */
        fun setTitleGravity(titleGravity: Int): Builder {
            this.titleGravity = titleGravity
            return this
        }

        /**
         * set messages gravity, you can use Gravity.CENTER, Gravity.LEFT, or Gravity.RIGHT
         *
         * @param contentGravity int
         * @return builder
         */
        fun setContentGravity(contentGravity: Int): Builder {
            this.contentGravity = contentGravity
            return this
        }

        /**
         * set button gravity, you can use Gravity.CENTER, Gravity.LEFT, or Gravity.RIGHT
         *
         * @param buttonGravity int
         * @return builder
         */
        fun setButtonGravity(buttonGravity: Int): Builder {
            this.buttonGravity = buttonGravity
            return this
        }

        fun setButtonText(buttonText: String?): Builder {
            this.buttonText = buttonText
            return this
        }

        fun setButtonBackground(drawable: Drawable?): Builder {
            buttonBackground = drawable
            return this
        }

        fun setButtonTextColor(@ColorInt color: Int): Builder {
            buttonTextColor = color
            return this
        }

        /**
         * changing inner circle size indicator
         *
         * @param size you can change inner circle indicator size
         */
        fun setCircleInnerIndicatorSize(size: Float): Builder {
            circleInnerIndicatorSize = size
            return this
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        fun setCircleStrokeIndicatorSize(size: Float): Builder {
            strokeCircleWidth = size
            return this
        }

        fun build(): GuideView {
            val guideView = GuideView(context, targetView)
            guideView.mAlignType = if (alignType != null) alignType else AlignType.auto
            guideView.dismissType = dismissType
            val density = context.resources.displayMetrics.density
            guideView.setTitle(title)
            if (contentText != null) {
                guideView.setContentText(contentText)
            }
            if (titleTextSize != 0) {
                guideView.setTitleTextSize(titleTextSize)
            }
            if (contentTextSize != 0) {
                guideView.setContentTextSize(contentTextSize)
            }
            if (contentSpan != null) {
                guideView.setContentSpan(contentSpan)
            }
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace)
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace)
            }
            if (guideListener != null) {
                guideView.mGuideListener = guideListener
            }
            if (lineIndicatorHeight != 0f) {
                guideView.indicatorHeight = lineIndicatorHeight * density
            }
            if (lineIndicatorWidthSize != 0f) {
                guideView.lineIndicatorWidthSize = lineIndicatorWidthSize * density
            }
            if (circleIndicatorSize != 0f) {
                guideView.circleIndicatorSize = circleIndicatorSize * density
            }
            if (circleInnerIndicatorSize != 0f) {
                guideView.circleInnerIndicatorSize = circleInnerIndicatorSize * density
            }
            if (strokeCircleWidth != 0f) {
                guideView.strokeCircleWidth = strokeCircleWidth * density
            }
            if (titleGravity != 0) {
                guideView.setTitleGravity(titleGravity)
            }
            if (contentGravity != 0) {
                guideView.setContentGravity(contentGravity)
            }
            if (buttonGravity != 0) {
                guideView.setButtonGravity(buttonGravity)
            }
            if (buttonText != null) {
                guideView.setButtonText(buttonText)
            }
            if (buttonBackground != null) {
                guideView.setButtonBackground(buttonBackground)
            }
            if (buttonTextColor != null) {
                guideView.setButtonTextColor(buttonTextColor!!)
            }
            if (paddingLeftTitle != 0 || paddingTopTitle != 0 || paddingRightTitle != 0 || paddingBottomTitle != 0) {
                guideView.setTitlePadding(
                    paddingLeftTitle,
                    paddingTopTitle,
                    paddingRightTitle,
                    paddingBottomTitle
                )
            }
            if (paddingLeftMessage != 0 || paddingTopMessage != 0 || paddingRightMessage != 0 || paddingBottomMessage != 0) {
                guideView.setMessagePadding(
                    paddingLeftMessage, paddingTopMessage, paddingRightMessage,
                    paddingBottomMessage
                )
            }
            if (paddingLeftButton != 0 || paddingRightButton != 0 || paddingBottomButton != 0 || paddingTopButton != 0) {
                guideView
                    .setButtonPadding(
                        paddingLeftButton,
                        paddingTopButton,
                        paddingRightButton,
                        paddingBottomButton
                    )
            }
            return guideView
        }
    }

    companion object {
        const val TAG = "GuideView"
        private const val INDICATOR_HEIGHT = 15
        private const val MESSAGE_VIEW_PADDING = 5
        private const val SIZE_ANIMATION_DURATION = 700
        private const val APPEARING_ANIMATION_DURATION = 400
        private const val CIRCLE_INDICATOR_SIZE = 0
        private const val LINE_INDICATOR_WIDTH_SIZE = 0
        private const val STROKE_CIRCLE_INDICATOR_SIZE = 0
        private const val RADIUS_SIZE_TARGET_RECT = 8
        private const val MARGIN_INDICATOR = 0
        private const val BACKGROUND_COLOR = -0x67000000
        private const val CIRCLE_INNER_INDICATOR_COLOR = -0x333334
        private const val CIRCLE_INDICATOR_COLOR = Color.WHITE
        private const val LINE_INDICATOR_COLOR = Color.WHITE
    }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        target = view
        density = context.resources.displayMetrics.density
        init()
        val locationTarget = IntArray(2)
        target!!.getLocationOnScreen(locationTarget)
        targetRect = RectF(
            locationTarget[0].toFloat(),
            locationTarget[1].toFloat(),
            (locationTarget[0] + target.width).toFloat(),
            (locationTarget[1] + target.height).toFloat()
        )
        mMessageView = GuideMessageView(getContext())
        mMessageView.setPadding(
            messageViewPadding,
            messageViewPadding,
            messageViewPadding,
            messageViewPadding
        )
        mMessageView.setColor(Color.WHITE)
        val messageViewParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        messageViewParams.setMargins(20, 0, 20, 0)
        addView(mMessageView, messageViewParams)
        setMessageLocation(resolveMessageViewLocation())
        val layoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                setMessageLocation(resolveMessageViewLocation())
                val locationTarget = IntArray(2)
                target.getLocationOnScreen(locationTarget)
                targetRect = RectF(
                    locationTarget[0].toFloat(),
                    locationTarget[1].toFloat(),
                    (locationTarget[0] + target.width).toFloat(),
                    (locationTarget[1] + target.height).toFloat()
                )
                selfRect[paddingLeft, paddingTop, width - paddingRight] = height - paddingBottom
                marginGuide = (if (isTop) marginGuide else -marginGuide)
                startYLineAndCircle =
                    (if (isTop) targetRect.bottom else targetRect.top) + marginGuide
                stopY = yMessageView + indicatorHeight
                viewTreeObserver.addOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        mMessageView.okButton.setOnClickListener { view1: View? -> dismiss() }
    }
}