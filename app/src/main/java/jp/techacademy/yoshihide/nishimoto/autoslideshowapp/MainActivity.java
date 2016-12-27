package jp.techacademy.yoshihide.nishimoto.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    ContentResolver resolver;
    Cursor cursor;
    Button button1;
    Button button2;
    Button button3;
    Boolean on_play;
    private Handler mHandler = new Handler();
    Timer timer;
    MyTimerTask m_timerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();
        resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);

        on_play=false;

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getFirstContent();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getFirstContent();
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {
            getPreviousContent();
        } else if (v.getId() == R.id.button2) {
            show_slideshow();
        } else if (v.getId() == R.id.button3) {
            getNextContent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getFirstContent();
                }
                break;
            default:
                break;
        }
    }

    private void getFirstContent() {

        if (cursor.moveToFirst()) {

            get_Content();

        }
    }

    private void getPreviousContent() {

        if (cursor.moveToPrevious()) {

            get_Content();

        } else {

            cursor.moveToLast();

            get_Content();

        }
    }

    private void show_slideshow() {

        if(!on_play) {

            on_play = true;

            button1.setEnabled(false);
            button3.setEnabled(false);
            button2.setText("停止");

            if (cursor.moveToFirst()) {

                timer = new Timer();
                m_timerTask = new MyTimerTask();

                timer.schedule(m_timerTask,2000, 2000);

            }

        } else {

            on_play = false;

            button1.setEnabled(true);
            button3.setEnabled(true);
            button2.setText("再生");

            m_timerTask.cancel();

        }

    }

    private void getNextContent() {

        if (cursor.moveToNext()) {

            get_Content();

        } else {

            cursor.moveToFirst();

            get_Content();

        }
    }

    private void get_Content() {

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        Log.d("getContent","uri:"+imageUri);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);

    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            mHandler.post( new Runnable() {
                public void run() {
                    getNextContent();
                }
            });

        }

    }

}
