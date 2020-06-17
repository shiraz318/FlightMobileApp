package com.example.flightmobileapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.System.out
import kotlin.math.pow
import kotlin.math.sqrt


class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var innerRadius = 0.0f
    private var startX = 0.0f
    private var startY = 0.0f
    private var innerCenter: PointF = PointF()
    private var outerCenter: PointF = PointF()
    private var outerRadius: Float = 0.0f
    private var notifyChanges: () -> Unit = {}
    private var applyAnimation: () -> Unit = {}

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        innerRadius = (min(width.toDouble(), height.toDouble()) / 4).toFloat()
        innerCenter = PointF(width / 2.0f, height / 2.0f)
        outerCenter = innerCenter
        outerRadius = (min(width.toDouble(), height.toDouble()) / 2).toFloat()
    }

    private fun min(num1: Double, num2: Double): Double {
        if (num1 < num2) {
            return num1
        }
        return num2
    }

    init {
        isClickable = true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the outer circle.
        paint.color = Color.DKGRAY
        canvas.drawCircle(outerCenter.x, outerCenter.y, outerRadius, paint)
        // Draw the inner circle.
        paint.color = Color.LTGRAY
        canvas.drawCircle(innerCenter.x, innerCenter.y, innerRadius, paint)
    }

    // Find the closeset intersection point of the given point and the inner circle.
    private fun closestIntersection(lineEnd: PointF): PointF {
        // Find the intersections point.
        val points: Array<PointF> = calculateIntersections(lineEnd)
        val intersection1 = points[0]
        val intersection2 = points[1]
        // Calculate the distance from the intersection 1 point to the current location.
        val dist1: Float = sqrt(
            ((intersection1.x - lineEnd.x) * (intersection1.x - lineEnd.x)) +
                    ((intersection1.y - lineEnd.y) * (intersection1.y - lineEnd.y))
        )
        // Calculate the distance from the intersection 2 point to the current location.
        val dist2: Float = sqrt(
            ((intersection2.x - lineEnd.x) * (intersection2.x - lineEnd.x)) +
                    ((intersection2.y - lineEnd.y) * (intersection2.y - lineEnd.y))
        )
        // Checking which point is the closest.
        val point: PointF
        point = if (dist1 < dist2) {
            intersection1
        } else {
            intersection2
        }
        return point
    }

    // Find the intersection points of the given point and the inner circle.
    private fun calculateIntersections(point2: PointF): Array<PointF> {
        // Calculate equation of circle -
        // (x - outerCenter.x)^2 + (y - outerCenter.y)^2 = innerRadius^2
        val dx: Float = point2.x - outerCenter.x
        val dy: Float = point2.y - outerCenter.y
        // Calculate A,C for line equation
        // (there is no need to calculate B because it will always be zero).
        val a = dx * dx + dy * dy
        val c: Float = -innerRadius * innerRadius;
        // Delta for finding solutions.
        val delta = -4 * a * c;
        // Two solutions.
        val t = ((sqrt(delta)) / (2 * a));
        val intersection1 = PointF(outerCenter.x + t * dx, outerCenter.y + t * dy);
        val intersection2 = PointF(outerCenter.x - t * dx, outerCenter.y - t * dy);
        return arrayOf(intersection1, intersection2)
    }

    private fun updatePosition(x: Float, y: Float): PointF {
        var isOut = false
        if (y < outerCenter.y - outerRadius + innerRadius) {
            isOut = true;
        } else if (y > outerRadius + outerCenter.y - innerRadius) {
            isOut = true
        }
        if (x < outerCenter.x - outerRadius + innerRadius) {
            isOut = true
        } else if (x > outerRadius + outerCenter.x - innerRadius) {
            isOut = true
        }
        if (!isOut) {
            return PointF(x, y)
        }
        return closestIntersection(PointF(x, y))
    }


    private fun touchMove(x: Float, y: Float) {
        innerCenter = updatePosition(x, y);
        notifyChanges()

        // Will render again the screen.
        invalidate()
    }

    private fun resetCenter() {
        // operate animation.
        applyAnimation()
        innerCenter = outerCenter
        notifyChanges()
        // Will render again the screen.
        invalidate()
    }

    private fun updateCurrent(x: Float, y: Float): Boolean {
        startX = x
        startY = y
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event == null) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return updateCurrent(event.x, event.y)
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> resetCenter()
        }
        return true
    }

    fun getAileron(): Float {
        return innerCenter.x
    }

    fun getElevator(): Float {
        return innerCenter.y
    }

    fun setFunction(function: () -> Unit) {
        notifyChanges = function
    }

    fun getOuterRadius(): Float {
        return outerRadius
    }

    fun getInnerRadius(): Float {
        return innerRadius
    }

    fun getCenterX(): Float {
        return outerCenter.x
    }

    fun getCenterY(): Float {
        return outerCenter.y
    }

}