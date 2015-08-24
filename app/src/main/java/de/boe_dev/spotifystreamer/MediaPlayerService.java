package de.boe_dev.spotifystreamer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
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

    private String sntSeekPos;
    private int intSeekPos;
    private int mediaPosition;
    private int mediaMax;
    private final Handler handler = new Handler();
    private static int songEnden;
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

        return super.onStartCommand(intent, flags, startId);
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
            mediaMax = mediaPlayer.getDuration();
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            sendBroadcast(seekIntent);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        Log.d("MediaPlayerSerivce", "onBind");
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) intent.getSerializableExtra("list");
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
    }

    public void pause() {
        sendBufferingBroadcast("2");
        mediaPlayer.pause();
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
        }

    }

    public void loadPlayer() {

        if (mediaPlayer != null) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
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

    public boolean isPreparded() {
        return preparded;
    }

    public String getImageUrl() {
        return list.get(pos).getImageUrl();
    }

    public String getPreviewUrl() {
        return list.get(pos).getPreviewUrl();
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

}
