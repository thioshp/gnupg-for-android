package info.guardianproject.gpg;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import info.guardianproject.gpg.pinentry.PinEntryActivity;
import info.guardianproject.gpg.pinentry.ServerSocketThread;

public class PinentryService extends Service {

    public static final String TAG = "PinentryService";
    private static final int SERVICE_FOREGROUND_ID = 8473;

    private ServerSocketThread pinentryHelperThread;

    private void startDaemons() {
        Log.i(TAG, "start daemons in " + NativeHelper.app_opt.getAbsolutePath());
        synchronized (this) {

            pinentryHelperThread = new ServerSocketThread(this);
            pinentryHelperThread.start();
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        // since this service is a separate process, it has its own instance of
        // NativeHelper
        NativeHelper.setup(this);
        goForeground();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    public class LocalBinder extends Binder {
        public PinentryService getService() {
            return PinentryService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        startDaemons();
        return START_STICKY;
    }

    public void startPinentry() {
        Log.d(TAG, "starting activity!");
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void goForeground() {
        Log.d(TAG, "goForeground()");

        startForeground(SERVICE_FOREGROUND_ID, buildNotification());
    }

    private Notification buildNotification() {

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setSmallIcon(R.drawable.icon);
        b.setContentTitle(getText(R.string.pinentry_service_label));
        b.setContentText(getText(R.string.pinentry_service_started));
        b.setTicker(getText(R.string.pinentry_service_started));
        b.setDefaults(Notification.DEFAULT_VIBRATE);
        b.setWhen(System.currentTimeMillis());
        b.setOngoing(true);
        b.setContentIntent(PendingIntent.getService(getApplicationContext(), 0,
            new Intent(), 0));

        return b.build();
    }

}
