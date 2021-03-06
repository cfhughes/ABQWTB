package com.abqwtb.stops;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.abqwtb.model.RealtimeTripInfo;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class StopClockView extends View {
    private float diameter;
    private List<RealtimeTripInfo> trips = new ArrayList<>();

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
        diameter = Math.min(ww, hh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        paint.setColor(0xffff1010);

        //canvas.drawArc(50,50,diameter-50,diameter-50,-90f,90f, true, paint);

        for (int i = 0; i < trips.size();i++){
            RealtimeTripInfo trip = trips.get(i);

            Paint tripPaint = new Paint(0);
            tripPaint.setColor(Color.BLACK);

            Paint actualPaint = new Paint(0);
            actualPaint.setColor(Color.BLUE);



            LocalTime time = LocalTime.parse(trip.getScheduledTime());

            LocalTime now = LocalTime.now(DateTimeZone.forOffsetHours(-7));

            //float scheduledFromNow = (time.getMillisOfDay() - now.getMillisOfDay() + 0.0f) / (60 * 60 * 1000);

            float actualFromNow = (time.getMillisOfDay() - now.getMillisOfDay() + 0.0f + (trip.getSecondsLate() * 1000)) / (60 * 60 * 1000);

            //canvas.drawArc(0,0,diameter*0.8f,diameter*0.8f,268 - (scheduledFromNow*360),4, true, tripPaint);

            canvas.drawArc(0,0,diameter,diameter,268 - (actualFromNow*360),4, true, actualPaint);
        }



    }

    public void setTrips(List<RealtimeTripInfo> trips) {

        this.trips = trips;

        invalidate();

    }
}
