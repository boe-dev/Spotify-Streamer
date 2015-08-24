package de.boe_dev.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.boe_dev.spotifystreamer.functions.ItemDetailsWrapper;

/**
 * Created by ben on 15.08.15.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {

    private ArrayList<TopTrackModel> list;
    private int pos;
    private boolean preparded = false;
    private static final int NOTIFICATION_ID = 13;
    private boolean isPausedInCall = false;

    private MediaPlayer mediaPlayer;
    private final IBinder MediaPlayerBinder = new MyLocalBinder();
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private Target loadtarget;

    private Bitmap iconBitmap;

    private int mediaPosition;
    private final Handler handler = new Handler();
    public static final String SEEK_BROADCAST = "de.boe_dev.media_player_seek";

    public static final String BUFFER = "de.boe_dev.media_player_buffer";
    private Intent bufferIntent, seekIntent;

    private int headsetSwitch = 1;


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        handler.removeCallbacks(sendUpdatesToUi);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bufferIntent = new Intent(BUFFER);
        seekIntent = new Intent(SEEK_BROADCAST);
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //This code should pause the music if a phone call comes in
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            pause();
                            isPausedInCall = true;
                        }
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null && isPausedInCall) {
                            play();
                        }

                }

                super.onCallStateChanged(state, incomingNumber);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        setupHandler();

        if( intent != null && intent.getAction() != null ) {
            Log.d("MediaPlayerService", intent.getAction());
            if (intent.getAction().equals("ACTION_PREVIOUS")) {
                previous(mediaPlayer.isPlaying());
            } else if (intent.getAction().equals("ACTION_NEXT")) {
                next(mediaPlayer.isPlaying());
            } else if (intent.getAction().equals("ACTION_PAUSE")) {
                pause();
            } else if (intent.getAction().equals("ACTION_PLAY")) {
                play();
            } else if (intent.getAction().equals("ACTION_STOP")) {
                mediaPlayer.pause();
                sendBufferingBroadcast("2");
            }
        }

        return START_STICKY;
    }

    private void setupHandler() {
        handler.removeCallbacks(sendUpdatesToUi);
        handler.postDelayed(sendUpdatesToUi, 300);
    }

    private Runnable sendUpdatesToUi = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 300);
        }
    };

    private void LogMediaPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPosition = mediaPlayer.getCurrentPosition();
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            sendBroadcast(seekIntent);
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        return MediaPlayerBinder;
    }


    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

        private boolean headsetConnect = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra("state")) {
                if (headsetConnect && intent.getIntExtra("state", 0) == 0) {
                    headsetConnect = false;
                    headsetSwitch = 0;
                } else if (!headsetConnect && intent.getIntExtra("state", 0) == 1) {
                    headsetConnect = true;
                    headsetSwitch = 1;
                }
            }

            if (headsetSwitch == 0) {
                pause();
            }

        }
    };

    public void setupPlayer(ArrayList<TopTrackModel> list, int pos) {
        this.list = list;
        this.pos = pos;
        loadPlayer();
    }

    public void play() {
        mediaPlayer.start();
        sendBufferingBroadcast("5");
        loadNotification(getImageUrl());

    }

    public void pause() {
        mediaPlayer.pause();
        sendBufferingBroadcast("2");
        loadNotification(getImageUrl());
    }

    public void next(boolean play) {

        if (pos == (list.size() - 1)) {
            pos = 0;
        } else {
            pos++;
        }
        loadPlayer();
        if (play) {
            play();
        } else {
            loadNotification(getImageUrl());
        }

    }

    public void previous(boolean play) {
        if (pos == 0) {
            pos = (list.size() - 1);
        } else {
            pos--;
        }
        loadPlayer();
        if (play) {
            play();
        } else {
            loadNotification(getImageUrl());
        }

    }

    public void loadPlayer() {

        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        sendBufferingBroadcast("1");
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(list.get(pos).getPreviewUrl());
            preparded = false;
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
        1 is buffering, 0 is buffering is completed
     */
    private void sendBufferingBroadcast(String state) {
        bufferIntent.putExtra("buffering", state);
        sendBroadcast(bufferIntent);
    }


    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setPosition(int position) {
        mediaPlayer.seekTo(position);
    }

    public String getArtist() {
        return list.get(pos).getArtist();
    }

    public String getAlbum() {
        return list.get(pos).getAlbum();
    }

    public String getName() {
        return list.get(pos).getName();
    }

    public String getImageUrl() {
        return list.get(pos).getImageUrl();
    }

    public String getPreviewUrl() {
        return list.get(pos).getPreviewUrl();
    }

    public void showNotification(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= 21 && SpotifyPreferences.getInstance(this).isNotification()) {

            Notification.Action action;
            if (mediaPlayer.isPlaying()) {
                action = generateAction(android.R.drawable.ic_media_pause, "Pause", "ACTION_PAUSE");

            } else {
                action = generateAction(android.R.drawable.ic_media_play, "Play", "ACTION_PLAY");
            }

            Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
            intent.setAction( "ACTION_STOP" );
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

            Notification notification = new Notification.Builder(this)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", "ACTION_PREVIOUS"))
                    .addAction(action)
                    .addAction(generateAction(android.R.drawable.ic_media_next, "Next", "ACTION_NEXT"))
                    .setDeleteIntent(pendingIntent)
                    .setStyle(new Notification.MediaStyle())
                    .setContentTitle(list.get(pos).getName())
                    .setContentText(list.get(pos).getArtist() + " - " + list.get(pos).getAlbum())
                    .build();

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

    }



    public void loadNotification(String url) {
        // I know that it was better to do this with Picasso, but I picasso have to run on main thread
        new DownloadImageTask().execute(url);
    }

    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        if (Build.VERSION.SDK_INT >= 21) {
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            intent.setAction(intentAction);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            return new Notification.Action.Builder(icon, title, pendingIntent).build();
        } else {
            return null;
        }

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        preparded = true;
        sendBufferingBroadcast("0");
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        sendBufferingBroadcast("4");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendBufferingBroadcast("2");
        sendBufferingBroadcast("3");
    }

    public class MyLocalBinder extends Binder {
        MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            showNotification(result);
        }
    }

}
