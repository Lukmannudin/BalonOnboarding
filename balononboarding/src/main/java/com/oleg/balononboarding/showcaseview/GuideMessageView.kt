package com.oleg.balononboarding.showcaseview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Spannable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.oleg.balononboarding.R

class GuideMessageView internal constructor(context: Context) : LinearLayout(context) {
    private val mPaint: Paint
    private val mRect: RectF
    var mTitleTextView: TextView
    var mContentTextView: TextView
    var okButton: Button
    var childContentButton: LinearLayout
    private val childContent: LinearLayout
    var padding = 0
    var paddingBetween = 0
    var buttonText = ""
    var location = IntArray(2)

    fun setTitle(title: String?) {
        if (title == null) {
            removeView(mTitleTextView)
            return
        }
        mTitleTextView.text = title
    }

    fun setContentText(content: String?) {
        mContentTextView.text = content
    }

    fun setContentSpan(content: Spannable?) {
        mContentTextView.text = content
    }

    fun setContentTypeFace(typeFace: Typeface?) {
        mContentTextView.typeface = typeFace
    }

    fun setTitleTypeFace(typeFace: Typeface?) {
        mTitleTextView.typeface = typeFace
    }

    fun setTitleTextSize(size: Int) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setContentTextSize(size: Int) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size.toFloat())
    }

    fun setColor(color: Int) {
        mPaint.alpha = 255
        mPaint.color = color
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        getLocationOnScreen(location)
        mRect[paddingLeft.toFloat(), paddingTop.toFloat(), width - paddingRight - 20.toFloat()] =
            height - paddingBottom.toFloat()
        canvas.drawRoundRect(mRect, 15f, 15f, mPaint)
    }

    private fun convertDp(margin: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, margin.toFloat(), resources
                .displayMetrics
        ).toInt()
    }

    init {
        val density = context.resources.displayMetrics.density
        setWillNotDraw(false)
        orientation = VERTICAL
        mRect = RectF()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.strokeCap = Paint.Cap.ROUND
        padding = (10 * density).toInt()
        paddingBetween = (3 * density).toInt()
        childContent = LinearLayout(context)
        childContent.orientation = VERTICAL
        mTitleTextView = TextView(context)
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        mTitleTextView.setTextColor(resources.getColor(R.color.pale_sky))
        mTitleTextView.setTypeface(mTitleTextView.typeface, Typeface.BOLD)
        childContent.addView(mTitleTextView)
        mContentTextView = TextView(context)
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        mContentTextView.setTextColor(resources.getColor(R.color.pale_sky))
        childContent.addView(mContentTextView)
        addView(
            childContent,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        childContentButton = LinearLayout(context)
        childContentButton.orientation = HORIZONTAL
        childContentButton.gravity = Gravity.CENTER
        okButton = Button(context)
        val buttonParams = LayoutParams(220, 90)
        buttonParams.setMargins(0, 0, 50, 16)
        okButton.text = buttonText
        okButton.textAlignment = TEXT_ALIGNMENT_CENTER
        okButton.setTextColor(Color.WHITE)
        okButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        okButton.gravity = TEXT_ALIGNMENT_CENTER
        okButton.background = ContextCompat.getDrawable(context, R.drawable.button_background)
        okButton.layoutParams = buttonParams
        okButton.includeFontPadding = false
        okButton.isAllCaps = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            okButton.typeface = resources.getFont(R.font.montserrat_bold)
        }
        childContentButton.addView(okButton)
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        params.setMargins(convertDp(10), convertDp(10), convertDp(10), convertDp(10))
        addView(childContentButton, params)
    }
}