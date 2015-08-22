package de.boe_dev.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
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

    private MediaPlayer mediaPlayer;
    private final IBinder MediaPlayerBinder = new MyLocalBinder();


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d("MediaPlayerSerivce", "onBind");
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) intent.getSerializableExtra("list");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        return MediaPlayerBinder;
    }

    public void setupPlayer(ArrayList<TopTrackModel> list, int pos) {
        this.list = list;
        this.pos = pos;
        loadPlayer();
    }

    public void play() {
        mediaPlayer.start();
    }

    public void pause() {
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

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(list.get(pos).getPreviewUrl());
            preparded = false;
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //goto next?
    }

    public class MyLocalBinder extends Binder {
        MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

}
