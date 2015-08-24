package de.boe_dev.spotifystreamer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.boe_dev.spotifystreamer.functions.ItemDetailsWrapper;

/**
 * Created by ben on 10.08.15.
 */
public class MediaPlayerDialog extends DialogFragment  {

    private Toolbar toolbar;
    private TextView album, track, playedTime, compleatTime;
    private ImageView cover;
    private SeekBar seekBar;
    private ToggleButton play;
    private Button prev, next;

    private Context context;
    private ArrayList<TopTrackModel> list;
    private int pos;
    private static boolean isBound = false;
    private boolean mBufferBroadcasRegistered, mSeekBarBroadcastRegistered;
    private int playTime = 0;

    private MediaPlayerService mediaPlayerService;
    private ProgressDialog progressBuffer;

    public MediaPlayerDialog(Context context, ArrayList<TopTrackModel> list, int pos) {
        this.context = context;
        if (list != null) {
            this.list = list;
            this.pos = pos;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_media_player, container, false);
        initView(rootView);
        if (savedInstanceState == null) {
            startPlayerService();
        } else {
            ItemDetailsWrapper wrap = (ItemDetailsWrapper) savedInstanceState.getSerializable("list");
            list = wrap.getItemDetails();
            pos = savedInstanceState.getInt("pos");
            play.setSelected(savedInstanceState.getBoolean("play"));
            fillView();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        if (!mBufferBroadcasRegistered) {
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MediaPlayerService.DATA));
            mBufferBroadcasRegistered = true;
        }
        if (!mSeekBarBroadcastRegistered) {
            getActivity().registerReceiver(seekBroadcastReceiver, new IntentFilter(MediaPlayerService.SEEK_BROADCAST));
            mSeekBarBroadcastRegistered = true;
        }
        super.onResume();
    }


    @Override
    public void onPause() {
        if (mBufferBroadcasRegistered) {
            getActivity().unregisterReceiver(broadcastReceiver);
            mBufferBroadcasRegistered = false;
        }
        if (mSeekBarBroadcastRegistered) {
            getActivity().unregisterReceiver(seekBroadcastReceiver);
            mSeekBarBroadcastRegistered = false;
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (isBound) {
            context.unbindService(playerConnection);
            isBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("play", play.isChecked());
        outState.putInt("pos", pos);
        ItemDetailsWrapper wrapper = new ItemDetailsWrapper(list);
        outState.putSerializable("list", wrapper);
    }

    private void initView(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.media_player_toolbar);
        album = (TextView) view.findViewById(R.id.media_player_album);
        track = (TextView) view.findViewById(R.id.media_player_track);
        playedTime = (TextView) view.findViewById(R.id.media_player_played_time);
        compleatTime = (TextView) view.findViewById(R.id.media_player_compleat_time);
        cover = (ImageView) view.findViewById(R.id.media_player_cover);
        seekBar = (SeekBar) view.findViewById(R.id.media_player_seek_bar);
        play = (ToggleButton) view.findViewById(R.id.media_player_play);
        prev = (Button) view.findViewById(R.id.media_player_prev);
        next = (Button) view.findViewById(R.id.media_player_next);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_share) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mediaPlayerService.getPreviewUrl());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    return true;
                }
                return false;
            }
        });
        toolbar.inflateMenu(R.menu.media_player_menu);

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
                if (isChecked && !mediaPlayerService.isPlaying()) {
                    mediaPlayerService.play();
                } else if (!isChecked && mediaPlayerService.isPlaying()) {
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
                    }
                }.start();
            }
        });

    }

    private void startPlayerService() {
        Intent player = new Intent(context, MediaPlayerService.class);
        context.bindService(player, playerConnection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
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


    private void setupPlayer() {
        if (list != null) {
            new Thread() {
                @Override
                public void run() {
                    mediaPlayerService.setupPlayer(list, pos);
                }
            }.start();
        } else {
            fillView();
        }
    }

    private void fillView() {
        if (mediaPlayerService != null) {
            getDialog().setTitle(mediaPlayerService.getArtist());
            album.setText(mediaPlayerService.getAlbum());
            track.setText(mediaPlayerService.getName());
            seekBar.setProgress((mediaPlayerService.getCurrentPosition() / 1000));
            if (!mediaPlayerService.getImageUrl().equals("")) {
                Picasso.with(context).load(mediaPlayerService.getImageUrl()).into(cover);
            } else {
                Picasso.with(context).load(R.drawable.image_not_found).into(cover);
            }

            seekBar.setMax(mediaPlayerService.getDuration());
            playedTime.setText(secondsToString((mediaPlayerService.getCurrentPosition() / 1000)));
            playTime = (mediaPlayerService.getDuration() / 1000);
            compleatTime.setText(secondsToString(playTime));
            if (mediaPlayerService.isPlaying()) {
                play.setChecked(true);
            } else {
                play.setChecked(false);
            }
        }
    }

    private String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }


    private void handlerBroadcast(Intent intent) {

        String dataValue = intent.getStringExtra(getString(R.string.data));
        int dataIntValue = Integer.parseInt(dataValue);

        switch (dataIntValue) {
            case 0:
                if (progressBuffer != null) {
                    fillView();
                    progressBuffer.dismiss();
                }
                break;

            case 1:
                progressBuffer = ProgressDialog.show(context, getString(R.string.buffering), getString(R.string.data_will_load), true);
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

            case 5:
                play.setChecked(true);
                break;
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handlerBroadcast(intent);
        }
    };

    private BroadcastReceiver seekBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentValue = intent.getStringExtra("counter");
            int seekIntValue = Integer.parseInt(intentValue);
            seekBar.setProgress(seekIntValue);
            playedTime.setText(secondsToString((seekIntValue / 1000)));
        }
    };

}
