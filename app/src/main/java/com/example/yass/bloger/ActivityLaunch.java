package com.example.yass.bloger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yass.bloger.db.SharedPrefHelper;
import com.example.yass.bloger.db.Streams;
import com.rey.material.widget.ProgressView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class ActivityLaunch extends AppCompatActivity {

    PlayService mBoundService;
    boolean isServiceBound = false;

    private static final String TAG = "ActivityLaunch";
    private String currentChoise = "";
    private TextView nameSoundTextView;
    private ImageButton titleImageButton;
    private Button playImageButton;
    private Button blogerfm64Button;
    private Button blogerfm128Button;
    private ImageButton faceImageButton;
    private ImageButton vkImageButton;
    private ImageButton twitterImageButton;
    private Intent intentBrowser = null;
    private DiscreteSeekBar discreteSeekBar;

    public boolean isPlaySound = false;
    boolean isFirstStart = true;

    private Intent intent;

    private BroadcastReceiver receiver;
    public final static String BROADCAST_ACTION = "com.example.yass.bloger";

    private ProgressView progressBar;
    ProgressDialog progressDialog;


    @Override
    public void onStart() {
        super.onStart();
        intent = new Intent(getApplicationContext(), PlayService.class);
        // startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        initViews();
        setVisibility();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Connection...");
        Log.i(TAG, SharedPrefHelper.getInstance().getNameSound());

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                nameSoundTextView.setText(SharedPrefHelper.getInstance().getNameSound());

                setStatusProgressDialog(false);
                isFirstStart = false;
            }
        };

        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intFilt);

        progressBar = (ProgressView) findViewById(R.id.fragment_photo_page_progress_bar);

        currentChoise = SharedPrefHelper.getInstance().getChoiseChannel();
        Log.i(TAG, "currentChoise = " +currentChoise);
    }

    private void initViews() {
        nameSoundTextView = (TextView) findViewById(R.id.nameSound_TextView);
        titleImageButton = (ImageButton) findViewById(R.id.blogerLabel_Image);
        playImageButton = (Button) findViewById(R.id.play_pause_Button);
        blogerfm64Button = (Button) findViewById(R.id.blogerfm64_TextView);
        blogerfm128Button = (Button) findViewById(R.id.blogerfm128_TextView);

        faceImageButton = (ImageButton) findViewById(R.id.face_Image);
        vkImageButton = (ImageButton) findViewById(R.id.vk_Image);
        twitterImageButton = (ImageButton) findViewById(R.id.twitter_Image);

        discreteSeekBar = (DiscreteSeekBar) findViewById(R.id.discrete1);
        discreteSeekBar.setTrackColor(R.color.dsb_progress_color);

        playImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isFirstStart) {
                    setStatusProgressDialog(true);
                }
                if (isPlaySound) {
                    isPlaySound = false;
                    setIconForButton();
                    Log.d("MainActivity", "pause");
                    intent.putExtra("first", isFirstStart);
                    intent.putExtra("play", isPlaySound);
                    startService(intent);
                    setVisibility();

                } else if (!isPlaySound) {
                    startAndPlayPlayer();
                }
                Log.d("MainActivity", "listener");
            }
        });

        titleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(ActivityLaunch.this);
                builderSingle.setIcon(R.drawable.radio_icon);
                builderSingle.setTitle("Select type music");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ActivityLaunch.this, android.R.layout.select_dialog_item);
                arrayAdapter.add("JAZZ");
                arrayAdapter.add("ROCK");


                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String choise = arrayAdapter.getItem(which);
                        Log.i(TAG,"choise = "+ choise);

                        if (!currentChoise.equals(choise)) {
                            mBoundService.stopPlay();
                            if (mBoundService.getWorkStatusPlayer()) {
                                mBoundService.setupDataPlayer(choise);
                                setStatusProgressDialog(true);
                                Log.i(TAG, "startIntent");
                            } else {
                                if (isFirstStart) {
                                    setStatusProgressDialog(true);
                                }
                                 startAndPlayPlayer();
                            }
                            startAndPlayPlayer();
                            SharedPrefHelper.getInstance().saveChoiseChannel(choise);
                            currentChoise = SharedPrefHelper.getInstance().getChoiseChannel();
                        }
                    }

                });
                builderSingle.show();
            }
        });

        blogerfm64Button.setOnClickListener(onClickListener);
        blogerfm128Button.setOnClickListener(onClickListener);

        faceImageButton.setOnClickListener(onClickListenerImageButton);
        vkImageButton.setOnClickListener(onClickListenerImageButton);
        twitterImageButton.setOnClickListener(onClickListenerImageButton);

        discreteSeekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                Log.i(TAG, String.valueOf(value));
                if (isServiceBound){
                    mBoundService.setLevelVolume(value);
                }
                return value;
            }
        });
    }

    private void setIconForButton() {
        if (isPlaySound) {
            playImageButton.setBackgroundResource(R.drawable.pause);
        } else if (!isPlaySound) {
            playImageButton.setBackgroundResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");
        if (!isPlaySound && isFinishing()) {
            stopService(new Intent(getApplicationContext(), PlayService.class));
        }

        if (isServiceBound) {
            unbindService(mServiceConnection);
            isServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
        unregisterReceiver(receiver);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            String currentChannel = SharedPrefHelper.getInstance().getSound();

            switch (button.getId()) {
                case R.id.blogerfm64_TextView:
                    SharedPrefHelper.getInstance().saveSound(SharedPrefHelper.getInstance().SOUND_64);
                    Log.i(TAG, "blogerfm64_TextView:");
                    break;

                case R.id.blogerfm128_TextView:
                    SharedPrefHelper.getInstance().saveSound(SharedPrefHelper.getInstance().SOUND_128);
                    Log.i(TAG, "blogerfm128_TextView:");
                    break;
            }

            if (!SharedPrefHelper.getInstance().getSound().equals(currentChannel)) {
                if (mBoundService.getWorkStatusPlayer()) {
                    mBoundService.setupDataPlayer(PlayService.URL_STREAM);
                    setStatusProgressDialog(true);
                    Log.i(TAG, "startIntent");
                } else {
                    if (isFirstStart) {
                        setStatusProgressDialog(true);
                    }
                    startAndPlayPlayer();
                }
            }
        }
    };

    View.OnClickListener onClickListenerImageButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ImageButton imageButton = (ImageButton) view;

            switch (imageButton.getId()) {
                case R.id.face_Image:
                    intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/login.php?skip_api_login=1&api_key=966242223397117&signed_next=1&next=https%3A%2F%2Fwww.facebook.com%2Fsharer.php%3Fsrc%3Dsp%26u%3Dhttp%253A%252F%252Fplayer.radiojazzfm.ru%252F%26t%3DRadio%2BJAZZ%2B%257C%2B%25D0%259E%25D0%25BD%25D0%25BB%25D0%25B0%25D0%25B9%25D0%25BD-%25D0%25BF%25D0%25BB%25D0%25B5%25D0%25B5%25D1%2580%26description%26picture&cancel_url=https%3A%2F%2Fwww.facebook.com%2Fdialog%2Freturn%2Fclose%3Ferror_code%3D4201%26error_message%3DUser%2Bcanceled%2Bthe%2BDialog%2Bflow%23_%3D_&display=popup&locale=ru_RU" /*"https://www.facebook.com/bloger.fm/"*/));

                    break;
                case R.id.vk_Image:
                    intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/share.php?url=http%3A%2F%2Fplayer.radiojazzfm.ru%2F&title=Radio%20JAZZ%20%7C%20%D0%9E%D0%BD%D0%BB%D0%B0%D0%B9%D0%BD-%D0%BF%D0%BB%D0%B5%D0%B5%D1%80&description=&image=" /*"http://vk.com/club100785888"*/));

                    break;
                case R.id.twitter_Image:
                    intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?status=Radio%20JAZZ%20%7C%20%D0%9E%D0%BD%D0%BB%D0%B0%D0%B9%D0%BD-%D0%BF%D0%BB%D0%B5%D0%B5%D1%80%20http%3A%2F%2Fplayer.radiojazzfm.ru%2F" /*"https://twitter.com/BlogerFm"*/));

                    break;
            }
            startActivity(intentBrowser);
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.MyBinder myBinder = (PlayService.MyBinder) service;
            mBoundService = myBinder.getService();
            isServiceBound = true;
        }
    };

    private void setStatusProgressDialog(boolean status) {
        if (status) {
            progressDialog.show();
        } else {
            progressDialog.hide();
        }
    }

    private void startAndPlayPlayer() {
        isPlaySound = true;
        setIconForButton();
        Log.d("MainActivity", "play");
        intent.putExtra("first", isFirstStart);
        intent.putExtra("play", isPlaySound);
        startService(intent);
        setVisibility();
    }


    private void setVisibility() {
       /* if (isPlaySound) {
            blogerfm64Button.setVisibility(View.VISIBLE);
            blogerfm128Button.setVisibility(View.VISIBLE);
        } else {*/
            blogerfm64Button.setVisibility(View.GONE);
            blogerfm128Button.setVisibility(View.GONE);
        //}
    }

}

