package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class TrajectoryView extends View {

    private static final int LINE_WIDTH = 15;
    private static final int POINT_RADIUS = 20;

    private Paint linePaint;
    private Paint pointPaint;
    private Path path;
    private float lastX;
    private float lastY;
    private int canvasWidth;
    private int canvasHeight;
    private List<PointF> trajectoryPoints;

    public TrajectoryView(Context context) {
        super(context);
        init();
    }

    public TrajectoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#1A237E"));
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setColor(Color.BLUE);
        pointPaint.setStyle(Paint.Style.FILL);

        path = new Path();
        lastX = 0;
        lastY = 0;
        trajectoryPoints = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, linePaint);
        canvas.drawCircle(lastX, lastY, POINT_RADIUS, pointPaint);
    }

    public void addPoint(float x, float y) {
        if (lastX == 0 && lastY == 0) {
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
        lastX = x;
        lastY = y;
        invalidate();
    }

    public void reset() {
        path.reset();
        lastX = 0;
        lastY = 0;
        trajectoryPoints.clear();
        invalidate();
    }

    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public void drawTrajectory(List<PointF> trajectoryPoints) {
        if (canvasWidth == 0 || canvasHeight == 0) {
            throw new IllegalStateException("Canvas size not set.");
        }

        reset();

        float xScale = canvasWidth / 1000f; // assume x coordinates are in the range of 0-1000
        float yScale = canvasHeight / 1000f; // assume y coordinates are in the range of 0-1000

        for (PointF point : trajectoryPoints) {
            float x = point.x * xScale;
            float y = canvasHeight - (point.y * yScale); // invert y-axis
            addPoint(x, y);
        }
        this.trajectoryPoints = trajectoryPoints;
    }

    public void updateView(float direction, int numSteps) {
        if (trajectoryPoints.isEmpty()) {
            return;
        }
        PointF lastPoint = trajectoryPoints.get(trajectoryPoints.size() - 1);
        float xScale = canvasWidth / 1000f; // assume x coordinates are in the range of 0-1000
        float yScale = canvasHeight / 1000f; // assume y coordinates are in the range of 0-1000
        float x = lastPoint.x * xScale;
        float y = canvasHeight - (lastPoint.y * yScale); // invert y-axis

        // update point
        lastX = x;
        lastY = y;
        // draw arrow indicating direction
        float arrowLength = POINT_RADIUS * 2;
        float arrowAngle = (float) Math.toRadians(30);
        float arrowX1 = x + (float) Math.sin(direction - arrowAngle) * arrowLength;
        float arrowY1 = y - (float) Math.cos(direction - arrowAngle) * arrowLength;
        float arrowX2 = x + (float) Math.sin(direction + arrowAngle) * arrowLength;
        float arrowY2 = y - (float) Math.cos(direction + arrowAngle) * arrowLength;
        path.moveTo(x, y);
        path.lineTo(arrowX1, arrowY1);
        path.moveTo(x, y);
        path.lineTo(arrowX2, arrowY2);
        invalidate();
    }
    public PointF getLastPoint() {
        if (trajectoryPoints.isEmpty()) {
            return null;
        }
        return trajectoryPoints.get(trajectoryPoints.size() - 1);
    }

}