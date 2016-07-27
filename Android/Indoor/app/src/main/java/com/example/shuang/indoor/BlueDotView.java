package com.example.shuang.indoor;

/**
 * Created by shuang on 20/07/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class BlueDotView extends SubsamplingScaleImageView{
    private float radius = 1.0f;
    private PointF dotCenter = null;
    private Bitmap compassIndicatorArrowBitmap; //指南针箭头
    private float compassIndicatorArrowRotateDegree; //箭头旋转角度
    private float rangeIndicatorMeters; //范围指示器
    private float compassIndicatorGap;
    private boolean drawIndicator = true;

    public BlueDotView(Context context) {
        this(context, null);
    }

    public BlueDotView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
        compassIndicatorArrowBitmap = BitmapFactory.decodeResource(this.getContext().getResources(), R.mipmap.compass);

        compassIndicatorGap = setValue(15.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReady()) {
            return;
        }
        canvas.save();
        drawDot(canvas);
        canvas.restore();
    }

    /**
     * 画定位点&方向指示器&范围指示器
     *
     * @param canvas
     */
    private void drawDot(Canvas canvas) {
        if (dotCenter != null) {
            PointF vPoint = sourceToViewCoord(dotCenter);
            float scaledRadius = getScale() * radius;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            //画范围指示器
            paint.setColor(getResources().getColor(R.color.ia_blue_tint));
            canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius * rangeIndicatorMeters, paint);
            //画定位点
            paint.setColor(0xff0000ff);
            canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);



            //画箭头
            if (drawIndicator && compassIndicatorArrowBitmap != null) {
                canvas.save();
                canvas.rotate(getCompassIndicatorArrowRotateDegree(), vPoint.x, vPoint.y);
                canvas.drawBitmap(compassIndicatorArrowBitmap,
                        vPoint.x - compassIndicatorArrowBitmap.getWidth() / 2,
                        vPoint.y - scaledRadius - compassIndicatorGap,
                        new Paint());
                canvas.restore();
            }
        }
    }

    public void setDrawIndicator(boolean drawIndicator) {
        this.drawIndicator = drawIndicator;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDotCenter(PointF dotCenter) {
        this.dotCenter = dotCenter;
    }

    protected float setValue(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    public void setCompassIndicatorArrowRotateDegree(float compassIndicatorArrowRotateDegree) {
        this.compassIndicatorArrowRotateDegree = compassIndicatorArrowRotateDegree;
    }

    public float getCompassIndicatorArrowRotateDegree() {
        return this.compassIndicatorArrowRotateDegree;
    }

    public void setRangeIndicatorMeters(float rangeIndicatorMeters) {
        this.rangeIndicatorMeters = rangeIndicatorMeters;
    }

}
