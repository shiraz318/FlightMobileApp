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
    private var startX = 0.0f                   // Radius of the circle.
    private var startY = 0.0f                   // Radius of the circle.
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


    // A function that updates the values of the knob according to whether it is in/on a circle or not.
    private fun updateKnobPosition(x: Float, y: Float): PointF {
        val powOfXY: Float = x.toDouble().pow(2.0).toFloat() + y.toDouble().pow(2.0).toFloat()
        val powDiameterOfBase: Float =
            (OUTER_RADIUS + initialCenter.x).toDouble().pow(2.0).toFloat()
        // The coordinates are in or on the circle.
        if (powOfXY <= powDiameterOfBase) {
            // this coordinates are good.
            Log.d("TAG", "coordinates are good")
            return PointF(x, y);
        }
        Log.d("TAG", "coordinates are bad")
        // Else powOfXY > powDiameterOfBase - thats mean that the coordinates are out of the circle.
        return closestIntersection(PointF(-x, -y))
    }        // Function that returns the closest intersection to lineEnd point.

    private fun closestIntersection(lineEnd: PointF): PointF {
        var intersection1: PointF
        var intersection2: PointF
        // Find the intersections point.
        var points: Array<PointF> = calculateIntersections(lineEnd)
        intersection1 = points[0]
        intersection2 = points[1]
        var dist1: Float = sqrt(
            intersection1.x.toDouble().pow(2.0) +
                    intersection1.y.toDouble().pow(2.0)
        ).toFloat()

        var dist2: Float = sqrt(
            intersection2.x.toDouble().pow(2.0) +
                    intersection2.y.toDouble().pow(2.0)
        ).toFloat()
        // Checking which point is the closest.
        if (dist1 < dist2) {
            return intersection1; }
        return intersection2;
    }

    // Find the points of intersection.
    private fun calculateIntersections(point2: PointF): Array<PointF> {
        var intersection1: PointF
        var intersection2: PointF
        var a: Float
        var delta: Float
        var t: Float

        var dx: Float = point2.x;
        var dy: Float = point2.y;
        // Calculate A,C for line equation (there is no need to calculate B because it will always be zero).
        a = dx * dx + dy * dy;
        var c: Float = -radius * radius;
        // Delta for finding solutions.
        delta = -4 * a * c;
        // Two solutions.
        t = ((sqrt(delta)) / (2 * a));
        intersection1 = PointF(t * dx, t * dy);
        intersection2 = PointF(-t * dx, -t * dy);
        return arrayOf(intersection1, intersection2)
    }

    private fun touchMove(x: Float, y: Float) {

        //canvas.drawCircle(center.x, center.y, radius, paint)
        //Log.d("TAG", "in touch move")
        var newX: Float = x
        var newY: Float = y
        var limit: Float = 330.0f
        Log.d("TAG", "x:")
        Log.d("TAG", center.x.toString())
        Log.d("TAG", "y:")
        Log.d("TAG", center.y.toString())
        //       center = updateKnobPosition(x, y)
//        var x1 = x
//        var y1 = y
//        if (x > initialCenter.x) {
//            x1 = x + radius
//        }else {
//            x1 = x - radius
//        }
//        if (y > initialCenter.y) {
//            y1 = y + radius
//        }else {
//            y1 = y - radius
//        }
//        val a = x1.toDouble().pow(2.0).toFloat() + y1.toDouble().pow(2.0).toFloat()
//        val b: Float = (OUTER_RADIUS + initialCenter.x).toDouble().pow(2.0).toFloat()
//        // The coordinates are in or on the circle.
//        if (a > b) {


        if (newX + radius > limit + initialCenter.x) {
            newX = limit + initialCenter.x - radius
        } else if (newX - radius < -limit + initialCenter.x) {
            newX = -limit + initialCenter.x + radius
        }
        if (newY + radius > limit + initialCenter.y) {
            newY = limit + initialCenter.y - radius
        } else if (newY - radius < -limit + initialCenter.x) {
            newY = -limit + initialCenter.y + radius
        }


//        }
        // Update position of the drawn items.


        center = PointF(newX, newY)
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