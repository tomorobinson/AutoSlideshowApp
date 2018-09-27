package jp.techacademy.tomokazu.kawano.autoslideshowapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String PLAY = "再生";
    private static final String STOP = "停止";
    int buttonStatus = 0; //0:Play, 1:Stop

    ImageView imageView;
    Button nextButton;
    Button playButton;
    Button backButton;
    Cursor cursor;
    Timer mTimer;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されていれば、ID紐付け
                initialProcess();

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合、無条件にID紐付け
        } else {
            initialProcess();
        }
    }

    private void initialProcess() {
        imageView = (ImageView) findViewById(R.id.imageView);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);

        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);

        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(this);

        //画像ライブラリの情報取得及び初期画像の設定
        getContentsInfo();
    }


    @Override
    public void onClick(View v) {
        //ボタンの判定
        switch (v.getId()) {
            case R.id.next_button:
                //次へボタン押下時の処理
                setNextImage();
                break;

            case R.id.back_button:
                //戻るボタン押下時の処理
                setPreviousImage();
                break;

            case R.id.play_button:
                if (buttonStatus == 0) {
                    //再生時
                    playSlideShow();
                } else {
                    //停止時
                    stopSlideShow();
                }
                break;

            default:
                break;
        }
    }

    private void getContentsInfo() {
        // ContentResolverを使用して画像ライブラリの情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            // 初期画像を設定
            imageView.setImageURI(imageUri);
        }
    }

    private void setNextImage() {
        // 次の画像を取得

        if (!cursor.moveToNext()) {
            cursor.moveToFirst();
        }

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        imageView.setImageURI(imageUri);
    }

    private void setPreviousImage() {
        // 前の画像を取得

        if (!cursor.moveToPrevious()) {
            cursor.moveToLast();
        }

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        imageView.setImageURI(imageUri);
    }

    private void slideShow() {
        // サブスレッドを作成
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    // UIスレッドへ描画依頼
                    @Override
                    public void run() {
                        setNextImage();
                    }
                });
            }
        }, 2000, 2000);
    }

    private void playSlideShow() {
        playButton.setText(STOP);
        nextButton.setEnabled(false);
        backButton.setEnabled(false);
        slideShow();
        buttonStatus = 1; //ボタンステータスを停止に変更
    }

    private void stopSlideShow() {
        mTimer.cancel();
        mTimer = null;
        playButton.setText(PLAY);
        nextButton.setEnabled(true);
        backButton.setEnabled(true);
        buttonStatus = 0; //ボタンステータスを再生に変更
    }

    @Override
    protected void onStop() {
        super.onStop();
        cursor.close();
    }
}