package com.example.audiobookplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import java.io.IOException;
import java.util.ArrayList;

public class AudioBookPlayerService extends Service {
    private static final String CHANNEL_ID = "AudioBookPlayerChannel";
    private static final int NOTIFICATION_ID = 1;
    private final IBinder binder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<Audiobook> audiobookList;
    private int currentIndex;
    public boolean isPaused = false;
    public class LocalBinder extends Binder {
        public AudioBookPlayerService getService() {
            return AudioBookPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();  // Make sure this is called
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Log that the service has been started
        Log.d("AudioBookPlayerService", "Service started in foreground");

        // Ensure startForegroundService is called on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> startForegroundService());  // This will run on the main thread

        // Return the appropriate flag for how the service should behave after it’s finished
        return START_STICKY;  // or another appropriate flag, depending on your needs
    }

    // Call this method to start the service in the foreground
    public void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audiobook Player")
                .setContentText("Playing audiobook...")
                .setSmallIcon(R.drawable.ic_play)
                .build();

        startForeground(1, notification);
    }

    // Initialize the audiobook list and index (typically from Activity)
    public void setAudiobookList(ArrayList<Audiobook> list, int index) {
        this.audiobookList = list;
        this.currentIndex = index;
    }

    // Play audiobook based on the index
    public void playAudiobook(int index, boolean bookmarkplay, long positionplayed) {
        if (audiobookList == null || audiobookList.size() == 0) return;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            Audiobook currentAudiobook = audiobookList.get(index);
            String filePath = currentAudiobook.getFilePath();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync(); // Prepare asynchronously
            // Set the listener for when preparation is complete
            mediaPlayer.setOnPreparedListener(mp -> {
                if (bookmarkplay) {
                    // Seek to the bookmarked position
                    mp.seekTo((int) positionplayed); // `seekTo` expects an int in milliseconds
                }
                // Start playback after preparing (and seeking, if necessary)
                mp.start();
                showNotification("Playing audiobook...");
            });
            /*
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                currentIndex = index;
                showNotification("Playing audiobook...");
            });
            */

            // Start the service in the foreground when the audiobook starts
            startForegroundService();
            // Start the PlayerActivity and pass the filePath to it
            Intent intent = new Intent(this, AudiobookPlayerActivity.class);
            intent.putExtra("filePath", filePath);  // Pass the filePath
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // Start a new activity
            startActivity(intent);  // Start the activity
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pauseAudiobook() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    public void resumeAudiobook() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    // Stop the audiobook
    public void stopAudiobook() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    // Seek to a specific position in the audiobook
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    // Get current playback position
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    // Get the total duration of the audiobook
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    // Check if the audiobook is playing
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    /**
     * Shows the notification for the foreground service.
     */
    private void showNotification(String playbackStatus) {
        Log.d("AudioBookPlayerService", "Notification being built and started");

        Intent playIntent = new Intent(this, AudioBookPlayerService.class);
        playIntent.setAction("PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, AudioBookPlayerService.class);
        pauseIntent.setAction("PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, AudioBookPlayerService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audiobook Player")
                .setContentText(playbackStatus)
                .setSmallIcon(R.drawable.ic_audiobook)
                .addAction(R.drawable.ic_play, "Play", playPendingIntent)
                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // Prevents the notification from being swiped away
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Creates a notification channel for devices running Android Oreo and above.
     */
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audiobook Player Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d("AudioBookPlayerService", "Notification channel created");
            } else {
                Log.e("AudioBookPlayerService", "NotificationManager is null");
            }
        }
    }

    // Binder class to interact with the service
    public class AudioBookPlayerBinder extends Binder {
        public AudioBookPlayerService getService() {
            return AudioBookPlayerService.this;
        }
    }
}
