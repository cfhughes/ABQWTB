package com.abqwtb.stops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class StopClockView extends View {
    private float diameter;
    private List<RealtimeTripInfo> trips = new ArrayList<>();
    private float leftMargin;

    public StopClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        // Figure out how big we can make the pie.
        diameter = Math.min(ww, hh)-60;

        leftMargin = (ww - diameter)/2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(9.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);

        //canvas.drawArc(50,50,diameter-50,diameter-50,-90f,90f, true, paint);

        for (int i = 0; i < trips.size();i++){
            RealtimeTripInfo trip = trips.get(i);

            Paint tripPaint = new Paint(0);
            tripPaint.setColor(Color.BLACK);

            Paint actualPaint = new Paint(0);
            actualPaint.setAntiAlias(true);

            actualPaint.setColor(Color.parseColor("#"+trip.getColor()));

            LocalTime time = LocalTime.parse(trip.getScheduledTime());

            LocalTime now = LocalTime.now(DateTimeZone.forOffsetHours(0));

            float scheduledFromNow = (time.getMillisOfDay() - now.getMillisOfDay() + 0.0f) / (60 * 60 * 1000);

            float actualFromNow = trip.secondsFromNow() / (60.0f * 60.0f);

            //canvas.drawArc(0,0,diameter*0.8f,diameter*0.8f,268 - (scheduledFromNow*360),4, true, tripPaint);
            if (actualFromNow < 0) actualFromNow = 0;
            if (actualFromNow < 0.95 ) {
                canvas.drawArc(leftMargin, 45, diameter + leftMargin, diameter + 45, 268 - (actualFromNow * 360), 4, true, actualPaint);
            }
        }

        canvas.drawCircle(leftMargin+diameter/2.0f,diameter/2.0f+45,diameter/2.0f,paint);

        canvas.drawCircle(leftMargin+diameter/2.0f,diameter/2.0f+45,diameter/29.0f,paint);

        canvas.drawLine(leftMargin+(diameter/2.0f),0,leftMargin+(diameter/2.0f),diameter/2.0f,paint);

        Paint redPaint = new Paint(0);
        redPaint.setColor(Color.RED);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeWidth(40);
        canvas.drawArc(leftMargin+15, 60, diameter+leftMargin-15,diameter+30,-120,360/12,false,redPaint);
    }

    public void setTrips(List<RealtimeTripInfo> trips) {

        this.trips = trips;

        invalidate();

    }
}
