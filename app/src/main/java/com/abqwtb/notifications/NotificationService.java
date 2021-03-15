package com.abqwtb.notifications;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import com.abqwtb.R;
import com.abqwtb.model.BusStop;
import com.abqwtb.model.RealtimeTripInfo;
import com.abqwtb.viewmodel.StopRepository;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.abqwtb.ABQBusApplication.CHANNEL_ID;

public class NotificationService extends LifecycleService {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private StopRepository stopRepository;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            BusStop stop = (BusStop) msg.obj;

            int notificationId = (int) (new Date().getTime() - 1583934353);

            for (int i = 0; i < 10; i++) {
                try {
                    List<RealtimeTripInfo> update = stopRepository.getStopTimesSync(stop.getAgency(), stop.getId());
                    updateNotification(update,notificationId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(15_000);
                } catch (InterruptedException e) {
                    // Restore interrupt status.
                    Thread.currentThread().interrupt();
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    private void updateNotification(List<RealtimeTripInfo> stop, int notificationId) {

        RealtimeTripInfo info = stop.get(0);

        LocalTime time = LocalTime.parse(info.getScheduledTime());

        LocalTime now = LocalTime.now(DateTimeZone.forOffsetHours(-7));

        float actualMinutesFromNow = (time.getMillisOfDay() - now.getMillisOfDay() + 0.0f + (info.getSecondsLate() * 1000)) / (60 * 1000);

        Log.v("Times",String.format("Update %s bus %s %d",info.getRoute(),info.getScheduledTime(),info.getSecondsLate()));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_directions_bus)
                .setContentTitle("ABQWTB")
                .setContentText(String.format("%s bus is %.0f minutes away",info.getRoute(),actualMinutesFromNow))
                .setTimeoutAfter(60000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NotificationService.this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        stopRepository = new StopRepository();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        BusStop stop = (BusStop) intent.getSerializableExtra("stop");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = stop;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }
}
