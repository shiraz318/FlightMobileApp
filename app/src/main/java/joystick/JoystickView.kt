package joystick

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt


class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var innerRadius = 0.0f
    private var innerCenter: PointF = PointF()
    private var outerCenter: PointF = PointF()
    private var outerRadius: Float = 0.0f
    private var notifyChanges: () -> Unit = {}

    var innerCenterX: Float
        get() {
            return innerCenter.x
        }
        set(value) {
            innerCenter.x = value
        }
    var innerCenterY: Float
        get() {
            return innerCenter.y
        }
        set(value) {
            innerCenter.y = value
        }


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    // Happen when the size is changed - we need to reCalculate the center of the circles.
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        innerRadius = (min(width.toDouble(), height.toDouble()) / 4).toFloat()
        innerCenter = PointF(width / 2.0f, height / 2.0f)
        outerCenter = PointF(width / 2.0f, height / 2.0f)
        outerRadius = (min(width.toDouble(), height.toDouble()) / 2).toFloat()
    }

    // Return the minimum value from the given values.
    private fun min(num1: Double, num2: Double): Double {
        if (num1 < num2) {
            return num1
        }
        return num2
    }

    init {
        isClickable = true
    }

    // Draw the circles on a given canvas.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the outer circle.
        paint.color = Color.DKGRAY
        canvas.drawCircle(outerCenter.x, outerCenter.y, outerRadius, paint)
        // Draw the inner circle.
        paint.color = Color.LTGRAY
        canvas.drawCircle(innerCenter.x, innerCenter.y, innerRadius, paint)
    }

    // Find the closest intersection point of the given point and the inner circle.
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

    // Calculate the update position in the limits of the outer circle and returns it.
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

    // Happens when user touch the inner circle of the joystick.
    private fun touchMove(x: Float, y: Float) {
        // Update the location of the inner circle of the joystick.
        innerCenter = updatePosition(x, y)
        // Notify that the inner circle of the joystick position is changed.
        notifyChanges()
        // Will render again the screen.
        invalidate()
    }

    // Reset the inner circle center to the initial position.
    private fun resetCenter() {

        // Apply animation.
        applyAnimation()
        // Notify that the inner circle of the joystick position is changed.
        notifyChanges()
        // Will render again the screen.
        invalidate()
    }

    // Animation of the joystick.
    private fun applyAnimation() {
        val xAnim =
            ObjectAnimator.ofFloat(
                this,
                "innerCenterX",
                outerCenter.x

            )
                .apply {
                    duration = 200
                }
        val yAnim =
            ObjectAnimator.ofFloat(
                this,
                "innerCenterY",
                outerCenter.y
            )
                .apply {
                    duration = 200
                }
        AnimatorSet().apply {
            play(xAnim).with(yAnim)
            innerCenter = outerCenter
            start()
        }
    }

    // Happens when user touch the inner circle of the joystick.
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
            MotionEvent.ACTION_UP -> resetCenter()
        }
        return true
    }

    // Get the aileron value represented by the innerCenter.x
    fun getAileron(): Float {
        return innerCenter.x
    }

    // Get the elevator value represented by the innerCenter.y
    fun getElevator(): Float {
        return innerCenter.y
    }

    // Set the notifyChanges function.
    fun setFunction(function: () -> Unit) {
        notifyChanges = function
    }

    // Get the outer radius.
    fun getOuterRadius(): Float {
        return outerRadius
    }

    // Get the inner radius.
    fun getInnerRadius(): Float {
        return innerRadius
    }

    // Get the outerCenter.x
    fun getCenterX(): Float {
        return outerCenter.x
    }

    // Get the outerCenter.y
    fun getCenterY(): Float {
        return outerCenter.y
    }
}