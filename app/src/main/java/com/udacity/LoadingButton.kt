package com.udacity

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

private const val START_ANGLE = 0f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rect: RectF = RectF(0f, 0f, 0f, 0f)

    private var circleAnimator: ValueAnimator
    private var currentSweepAngle = 0

    private val circleColor = ContextCompat.getColor(context, R.color.colorAccent)
    private var arcs: List<Arc>

    private var widthSize = 0
    private var heightSize = 0

    @Volatile
    private var progress: Double = 0.0

    private var valueAnimator = ValueAnimator()

    private var buttonBgColor: Int
    private var buttonTextColor: Int

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 40.0f
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed)
    { p, old, new ->

        if (new == ButtonState.Clicked) {
            buttonState = ButtonState.Loading
        }
    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        progress = (it.animatedValue as Float).toDouble()
        invalidate()
        requestLayout()
    }

    init {
        isClickable = true

        valueAnimator =
            AnimatorInflater.loadAnimator(context, R.animator.loading_animation) as ValueAnimator
        valueAnimator.addUpdateListener(updateListener)

        circleAnimator =
            AnimatorInflater.loadAnimator(
                context,
                R.animator.circle_loading_animation
            ) as ValueAnimator
        circleAnimator.addUpdateListener { valueAnimator ->
            currentSweepAngle = valueAnimator.animatedValue as Int
        }

        arcs = listOf(Arc(0f, 360f, circleColor))

        val attr = context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0)
        try {

            buttonBgColor = attr.getColor(
                R.styleable.LoadingButton_bgColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )

            buttonTextColor = attr.getColor(
                R.styleable.LoadingButton_textColor,
                ContextCompat.getColor(context, R.color.white)
            )
        } finally {
            attr.recycle()
        }
    }


    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Clicked
        valueAnimator.start()
        circleAnimator.start()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = 0f
        paint.color = buttonBgColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = resources.getColor(R.color.colorPrimaryDark)
            canvas.drawRect(
                0f, 0f,
                (width * (progress / 100)).toFloat(), height.toFloat(), paint
            )
        }

        var buttonText =
            if (buttonState == ButtonState.Loading) resources.getString(R.string.button_loading)
            else resources.getString(R.string.button_name)

        paint.color = buttonTextColor
        canvas.drawText(
            buttonText,
            (width / 2).toFloat(),
            ((height + paint.textSize / 2) / 2),
            paint
        )

        val diameter = 40f
        val top = height / 2 - diameter / 2
        val bottom = height / 2 + diameter / 2
        val left = 4 * width / 5f
        val right = left + diameter

        rect.set(left, top, right, bottom)

        arcs.forEach { arc ->
            if (currentSweepAngle > arc.start + arc.sweep) {
                paint.color = arc.color
                canvas.drawArc(
                    rect,
                    START_ANGLE + arc.start,
                    arc.sweep,
                    true,
                    paint
                )
            } else {
                if (currentSweepAngle > arc.start) {
                    paint.color = arc.color
                    canvas.drawArc(
                        rect,
                        START_ANGLE + arc.start,
                        currentSweepAngle - arc.start,
                        true,
                        paint
                    )
                }
            }
        }

        if (progress == 100.0) {
            paint.color = buttonBgColor
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            buttonText = resources.getString(R.string.button_name)
            paint.color = buttonTextColor
            canvas.drawText(
                buttonText,
                (width / 2).toFloat(),
                ((height + paint.textSize / 2) / 2),
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}

private class Arc(val start: Float, val sweep: Float, val color: Int)