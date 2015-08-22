package de.boe_dev.spotifystreamer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.boe_dev.spotifystreamer.functions.ItemDetailsWrapper;

/**
 * Created by ben on 10.08.15.
 */
public class PlayerActivity extends Activity {

    private TextView artist, album, track, playedTime, compleatTime;
    private ImageView cover;
    private SeekBar seekBar;
    private ToggleButton play;
    private Button prev, next;

    private ArrayList<TopTrackModel> list;
    private int pos;
    private static boolean isBound = false;
    private boolean mBufferBroadcasRegistered;
    private int playTime = 0;

    private MediaPlayerService mediaPlayerService;
    private ProgressDialog progressBuffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        if (savedInstanceState == null) {
            startPlayerService();
            start();
            initView();
        } else {
            ItemDetailsWrapper wrap = (ItemDetailsWrapper) savedInstanceState.getSerializable("list");
            list = wrap.getItemDetails();
            pos = savedInstanceState.getInt("pos");
            initView();
            play.setSelected(savedInstanceState.getBoolean("play"));
        }
    }

    @Override
    protected void onResume() {
        if (!mBufferBroadcasRegistered) {
            registerReceiver(broadcastBufferReceiver, new IntentFilter(MediaPlayerService.BUFFER));
            mBufferBroadcasRegistered = true;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mBufferBroadcasRegistered) {
            unregisterReceiver(broadcastBufferReceiver);
            mBufferBroadcasRegistered = false;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
//        if (mediaPlayerService.isPlaying() && Build.VERSION.SDK_INT >= 21) {
//            Notification notification = new Notification.Builder(this)
//                    .setVisibility(Notification.VISIBILITY_PUBLIC)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .addAction(android.R.drawable.ic_media_previous, "Previous", null) //TODO change this
//                    .addAction(android.R.drawable.ic_media_pause, "Pause", null) //TODO change this
//                    .addAction(android.R.drawable.ic_media_next, "Next", null) //TODO change this
//                    .setStyle(new Notification.MediaStyle())
//                    .setContentTitle(list.get(pos).getName())
//                    .setContentText(list.get(pos).getArtist() + "\n" + list.get(pos).getAlbum())
//                    .build();
//
//            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.notify(0, notification);
//        }

        if (isBound) {
            getApplicationContext().unbindService(playerConnection);
            isBound = false;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.media_player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mediaPlayerService.getPreviewUrl());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void startPlayerService() {
        Intent player = new Intent(getApplicationContext(), MediaPlayerService.class);
        getApplicationContext().bindService(player, playerConnection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
    }

    private void start() {
        ItemDetailsWrapper wrap = (ItemDetailsWrapper) getIntent().getSerializableExtra("list");
        list = wrap.getItemDetails();
        pos = getIntent().getExtras().getInt("pos");
    }

    private void setupPlayer() {
        new Thread() {
            @Override
            public void run() {
                mediaPlayerService.setupPlayer(list, pos);
            }
        }.start();
    }



    private void initView() {
        artist = (TextView) findViewById(R.id.media_player_artist);
        album = (TextView) findViewById(R.id.media_player_album);
        track = (TextView) findViewById(R.id.media_player_track);
        playedTime = (TextView) findViewById(R.id.media_player_played_time);
        compleatTime = (TextView) findViewById(R.id.media_player_compleat_time);
        cover = (ImageView) findViewById(R.id.media_player_cover);
        seekBar = (SeekBar) findViewById(R.id.media_player_seek_bar);
        play = (ToggleButton) findViewById(R.id.media_player_play);
        prev = (Button) findViewById(R.id.media_player_prev);
        next = (Button) findViewById(R.id.media_player_next);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayerService.setPosition(seekBar.getProgress());
                }
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

                new Thread() {
                    @Override
                    public void run() {
                        mediaPlayerService.previous(play.isChecked());
                        if (play.isChecked()) {
                            seekBarAction();
                        }
                    }
                }.start();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    @Override
                    public void run() {
                        mediaPlayerService.next(play.isChecked());
                        if (play.isChecked()) {
                            seekBarAction();
                        }
                    }
                }.start();
            }
        });

    }

    private void fillView() {
        if (mediaPlayerService != null) {
            artist.setText(mediaPlayerService.getArtist());
            album.setText(mediaPlayerService.getAlbum());
            track.setText(mediaPlayerService.getName());
            seekBar.setProgress(0);
            if (!mediaPlayerService.getImageUrl().equals("")) {
                Picasso.with(this).load(mediaPlayerService.getImageUrl()).into(cover);
            } else {
                Picasso.with(this).load(R.drawable.image_not_found).into(cover);
            }
        }
    }

    private void seekBarAction() {
        new Thread() {
            public void run() {
                while (mediaPlayerService.isPlaying() && mediaPlayerService.getCurrentPosition() != mediaPlayerService.getDuration()) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(mediaPlayerService.getCurrentPosition());
                                playedTime.setText(secondsToString((mediaPlayerService.getCurrentPosition() / 1000)));

                            }
                        });
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("play", play.isChecked());
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
            setupPlayer();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("PlayerActivity", "Disconnected");
            isBound = false;
        }
    };

    private void showProgressDialog(Intent bufferIntent) {

        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);

        switch (bufferIntValue) {
            case 0:
                if (progressBuffer != null) {
                    fillView();
                    seekBar.setMax(mediaPlayerService.getDuration());
                    playedTime.setText(secondsToString((mediaPlayerService.getCurrentPosition() / 1000)));
                    playTime = (mediaPlayerService.getDuration() / 1000);
                    compleatTime.setText(secondsToString(playTime));
                    progressBuffer.dismiss();
                }
                break;

            case 1:
                progressBuffer = ProgressDialog.show(PlayerActivity.this, "Buffering...", "Data will load...", true);
                break;

            case 2:
                play.setChecked(false);
                break;

            case 3:
                seekBar.setProgress(0);
                playedTime.setText("00:00");
                break;

            case 4:
                playedTime.setText(secondsToString((mediaPlayerService.getCurrentPosition() / 1000)));
                break;
        }

    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgressDialog(intent);
        }
    };



}
