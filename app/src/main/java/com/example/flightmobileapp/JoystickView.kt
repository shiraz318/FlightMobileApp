package com.example.flightmobileapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.lang.System.out
import kotlin.math.pow
import kotlin.math.sqrt

private const val OUTER_RADIUS = 330.0f

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var radius = 0.0f                   // Radius of the circle.
    private var startX = 0.0f
    private var startY = 0.0f
    private var center: PointF = PointF()
    private var initialCenter: PointF = PointF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width.toDouble(), height.toDouble()) / 4).toFloat()
        center = PointF(width / 2.0f, height / 2.0f)
        initialCenter = center
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
        paint.color = Color.LTGRAY
        canvas.drawCircle(center.x, center.y, radius, paint)

    }

    private fun closestIntersection(lineEnd: PointF): PointF {
        var intersection1: PointF
        var intersection2: PointF
        // Find the intersections point.
        var points: Array<PointF> = calculateIntersections(lineEnd)
        intersection1 = points[0]
        intersection2 = points[1]
        var dist1: Float = sqrt(
            ((intersection1.x - lineEnd.x) * (intersection1.x - lineEnd.x)) +
                    ((intersection1.y - lineEnd.y) * (intersection1.y - lineEnd.y))
        )

        var dist2: Float = sqrt(
            ((intersection2.x - lineEnd.x) * (intersection2.x - lineEnd.x)) +
                    ((intersection2.y - lineEnd.y) * (intersection2.y - lineEnd.y))
        )
        // Checking which point is the closest.
        var point: PointF
        if (dist1 < dist2) {
            point = intersection1;
        } else {
            point = intersection2
        }

        return point;
    }

    // Find the points of intersection.
    private fun calculateIntersections(point2: PointF): Array<PointF> {
        var intersection1: PointF
        var intersection2: PointF
        var a: Float
        var delta: Float
        var t: Float

        var dx: Float = point2.x - initialCenter.x
        var dy: Float = point2.y - initialCenter.y
        // Calculate A,C for line equation (there is no need to calculate B because it will always be zero).
        a = dx * dx + dy * dy
        var c: Float = -radius * radius;
        // Delta for finding solutions.
        delta = -4 * a * c;
        // Two solutions.
        t = ((sqrt(delta)) / (2 * a));
        intersection1 = PointF(initialCenter.x + t * dx, initialCenter.y + t * dy);
        intersection2 = PointF(initialCenter.x - t * dx, initialCenter.y - t * dy);
        return arrayOf(intersection1, intersection2)
    }

    private fun updatePosition(x: Float, y: Float): PointF {
        var isOut = false
        if (y < initialCenter.y - OUTER_RADIUS + radius) {
            isOut = true;
        } else if (y > OUTER_RADIUS + initialCenter.y - radius) {
            isOut = true
        }
        if (x < initialCenter.x - OUTER_RADIUS + radius) {
            isOut = true
        } else if (x > OUTER_RADIUS + initialCenter.x - radius) {
            isOut = true
        }
        if (!isOut) {
            return PointF(x, y)
        }
        return closestIntersection(PointF(x, y))
    }

    private fun touchMove(x: Float, y: Float) {

        center = updatePosition(x, y);
        // Will render again the screen.
        invalidate()

    }

    private fun resetCenter() {
        // operate animation.


        center = initialCenter
        // Will render again the screen.
        invalidate()
    }

    private fun updateCurrent(x: Float, y: Float): Boolean {

        startX = x
        startY = y

        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event == null) {
            return true
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> return updateCurrent(event.x, event.y)
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> resetCenter()
        }
        //return super.onTouchEvent(event)
        return true
    }
}