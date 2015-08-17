package de.boe_dev.spotifystreamer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.logging.Handler;

import de.boe_dev.spotifystreamer.functions.ItemDetailsWrapper;

/**
 * Created by ben on 10.08.15.
 */
public class PlayerActivity extends Activity {

    private TextView artist, album, track;
    private ImageView cover;
    private SeekBar seekBar;
    private ToggleButton play;
    private Button prev, next;

    private ArrayList<TopTrackModel> list;
    private int pos;

    MediaPlayerService mediaPlayerService;
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        if (savedInstanceState == null) {
            start();
            initView();
        } else {
            ItemDetailsWrapper wrap = (ItemDetailsWrapper) savedInstanceState.getSerializable("list");
            list = wrap.getItemDetails();
            pos = savedInstanceState.getInt("pos");
            initView();
        }
    }

    private void setupPlayer() {
        Intent player = new Intent(getApplicationContext(), MediaPlayerService.class)
                .putExtra("previewUrl", list.get(pos).getPreviewUrl());
        bindService(player, playerConnection, Context.BIND_AUTO_CREATE);
    }

    private void start() {
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) getIntent().getSerializableExtra("list");
        list = wrap.getItemDetails();
        pos = getIntent().getExtras().getInt("pos");
    }

    private void initView() {
        artist = (TextView) findViewById(R.id.media_player_artist);
        album = (TextView) findViewById(R.id.media_player_album);
        track = (TextView) findViewById(R.id.media_player_track);
        cover = (ImageView) findViewById(R.id.media_player_cover);
        seekBar = (SeekBar) findViewById(R.id.media_player_seek_bar);
        play = (ToggleButton) findViewById(R.id.media_player_play);
        prev = (Button) findViewById(R.id.media_player_prev);
        next = (Button) findViewById(R.id.media_player_next);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayerService.setPosition(seekBar.getProgress());
            }
        });

        play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mediaPlayerService.play();
                    seekBarAction();
                } else {
                    mediaPlayerService.pause();
                }

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(playerConnection);
                play.setChecked(false);
                if (pos == 0) {
                    pos = (list.size() - 1);
                } else {
                    pos--;
                }
                fillView();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(playerConnection);
                play.setChecked(false);
                if (pos == (list.size() - 1)) {
                    pos = 0;
                } else {
                    pos++;
                }
                fillView();
            }
        });
        fillView();
    }

    private void fillView() {

        setupPlayer();
        artist.setText(list.get(pos).getArtist());
        album.setText(list.get(pos).getAlbum());
        track.setText(list.get(pos).getName());

        if (!list.get(pos).getImageUrl().equals("")) {
            Picasso.with(this).load(list.get(pos).getImageUrl()).into(cover);
        } else {
            Picasso.with(this).load(R.drawable.image_not_found).into(cover);
        }
    }

    private void seekBarAction(){
        new Thread() {
            public void run() {
                while (mediaPlayerService.getCurrentPosition() != mediaPlayerService.getDuration()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayerService.getCurrentPosition());
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", pos);
        ItemDetailsWrapper wrapper = new ItemDetailsWrapper(list);
        outState.putSerializable("list", wrapper);
    }

    private ServiceConnection playerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MyLocalBinder binder = (MediaPlayerService.MyLocalBinder) service;
            mediaPlayerService = binder.getService();
            isBound = true;
            seekBar.setMax(mediaPlayerService.getDuration());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}