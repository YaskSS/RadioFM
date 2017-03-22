package com.example.yass.bloger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yass.bloger.core.App;
import com.example.yass.bloger.db.SharedPrefHelper;
import com.example.yass.bloger.db.Streams;
import com.example.yass.bloger.network.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PlayService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlayService";
    public static String URL_STREAM = "http://jazz128instr.streamr.ru";//"http://radio.bloger.fm:";
    private IBinder binder = new MyBinder();
    private BroadcastReceiver receiver;

    private String currentChannel = "";
    private MediaPlayer mediaPlayer;
    private AudioManager am;

    private NotificationManager nm;

    //private static final String nameSoundURL = "/json.xsl";
    private String currentSound = "";
    private boolean isFirstStart = false;

    String choiseChannel = "";

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
        //currentChannel = SharedPrefHelper.getInstance().getSound();
        currentChannel = SharedPrefHelper.getInstance().getChoiseChannel();

    }

    private void initPlayer() {
        am = (AudioManager) getSystemService(AUDIO_SERVICE);

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        isFirstStart = intent.getBooleanExtra("first", false);
        if (isFirstStart) {
            setupDataPlayer(URL_STREAM);
        } else {

        setupStatusPlayer(intent.getBooleanExtra("play", false));}

        autoUpdate();
        return START_STICKY;
    }

    public void setupDataPlayer(String choise) {

        choiseChannel = choise;
        Log.i(TAG, choise);

      //  if (!currentChannel.equals(SharedPrefHelper.getInstance().getSound())) {
        if (!currentChannel.equals(SharedPrefHelper.getInstance().getChoiseChannel())) {
            if (mediaPlayer == null)
                return;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            currentChannel = SharedPrefHelper.getInstance().getChoiseChannel();
        }
      //      currentChannel = SharedPrefHelper.getInstance().getSound();
      //      Log.i(TAG, currentChannel.toString());
      //  }
        try {
            mediaPlayer = new MediaPlayer();
            if (choise.equals("") || choise == null || choise.equals(URL_STREAM)){
                mediaPlayer.setDataSource(URL_STREAM);
            } else {
                mediaPlayer.setDataSource(Streams.getInstance().getStreams().get(choise));
            }/*+ SharedPrefHelper.getInstance().getPORT() + SharedPrefHelper.getInstance().getSound()*/
            mediaPlayer.prepareAsync();
            Log.i(TAG,"Я ТУТ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    private void autoUpdate() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new FetchItemsTask().execute();
                autoUpdate();
            }
        }, 5000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG, "Completion " + mediaPlayer.toString());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.i(TAG, "Error " + mediaPlayer.toString());
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
            Intent intent = new Intent(ActivityLaunch.BROADCAST_ACTION);
            App.getContext().sendBroadcast(intent);
        Log.i(TAG, "Prepared" + mediaPlayer.toString());
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        nm.cancel(13);
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
        String nameSound = "";

        @Override
        protected Void doInBackground(Void... params) {
            try {

                String result = new Request()
                        .getUrlString(Streams.getInstance().getRequests().get(choiseChannel));
                try {
                    /*result = result.substring(11);
                    result = result.substring(0, result.length() - 2);
                    JSONObject jObject = new JSONObject(result);
                    JSONObject aJsonString = jObject.getJSONObject("/blogerfm-128");
                    nameSound = aJsonString.getString("title");*/
                    JSONObject jsonObject = new JSONObject(result);
                    nameSound = jsonObject.getString("artist") + " - " + jsonObject.getString("song");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!nameSound.equals(currentSound)) {
                notificationMessage(nameSound);
                currentSound = nameSound;

                SharedPrefHelper.getInstance().saveNameSound(nameSound);
                Intent intent = new Intent(ActivityLaunch.BROADCAST_ACTION);
                App.getContext().sendBroadcast(intent);
            }
        }
    }

    void notificationMessage(String nameSound) {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_play)
              //  .setLargeIcon(BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.logo))
                .setTicker("Playing")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Radio")
                .setContentText(nameSound);
        Notification notification = builder.build();

        nm.notify(13, notification);
    }

    private void setupStatusPlayer(boolean isPlay) {
        if (isPlay) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }
    }

    public boolean getWorkStatusPlayer(){
        if (mediaPlayer == null){
            return false;
        } else {
            return true;
        }
    }

    public void setLevelVolume(int lvl){
        double dev= 6.66;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (lvl/dev), (int) (lvl/dev));
    }


    public class MyBinder extends Binder {

        public PlayService getService() {
            return PlayService.this;
        }

    }

    public void stopPlay(){
        mediaPlayer.stop();
    }
}
