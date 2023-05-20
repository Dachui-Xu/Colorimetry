package com.example.colorimetry;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;
    Button bt_getPicture;
    Button bt_picture;
    Button bt_takePhoto;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch st_filer;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch st_S;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch st_V;
    TextView tv_colorMin;
    TextView tv_colorMax;
    /* access modifiers changed from: private */
    public int colorMax = 6;
    /* access modifiers changed from: private */
    public int colorMin = 0;
    TextView et_eValue;
    /* access modifiers changed from: private */

    public String imagePath = null;

    ImageView iv_picture;
    SeekBar sb_colorMax;
    SeekBar sb_colorMin;

    private static final String TAG = "tag";
    private String imageBase64;
    public volatile static int progressCount = 0;//进度条
    public static boolean filerFlag = false;
    public static boolean SFlag = false;
    public static boolean VFlag = false;
    public static int SVaule = 43;
    public static int VVaule = 46;
    ImageMethod imageMethod = new ImageMethod();
    Bitmap bpSeek;//用于滑动条
    Bitmap bp; //用于按键点击
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(android.R.style.Animation_Activity);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        setContentView((int) R.layout.activity_main);
        init();
        initLoadOpenCV();
        //CameraActivity.setClipRatio(1, 1);

        sb_colorMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorMin = i;
                tv_colorMin.setText(String.valueOf(colorMin));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bpSeek = loadBitmap(bpSeek);
                        imageMethod.detectColorRange(bpSeek, colorMin, colorMax);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                iv_picture.setImageBitmap(bpSeek);
                            }
                        });
                    }
                }).start();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

             //   Toast.makeText(MainActivity.this, "min" + colorMin, Toast.LENGTH_SHORT).show();
            }
        });
        this.sb_colorMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorMax = i;
                tv_colorMax.setText(String.valueOf(colorMax));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bpSeek = loadBitmap(bpSeek);
                        imageMethod.detectColorRange(bpSeek, colorMin, colorMax);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                iv_picture.setImageBitmap(bpSeek);
                            }
                        });
                    }
                }).start();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
               // Toast.makeText(MainActivity.this, "max" + MainActivity.this.colorMax, Toast.LENGTH_SHORT).show();
            }

        });
        //获得RGB值
        this.bt_getPicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //进度条线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            progressBar.setProgress(progressCount);
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                progressCount = 0;
                new Thread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                buttonChange(false);

                            }
                        });
                        bp = loadBitmap(bp);
                        et_eValue.setTextColor(0xFF03DAC5);
                        et_eValue.setText("正在分析中，请稍等。。。");
                        String str = String.valueOf(imageMethod.detectVariousColor(bp, MainActivity.this.colorMin, MainActivity.this.colorMax));
                        progressCount = 95;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                iv_picture.setImageBitmap(bp);
                            }
                        });

                        et_eValue.setTextColor(0xFFFF00FF);
                        et_eValue.setText(str);
                        progressCount = 100;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                buttonChange(true);
                            }
                        });
                    }
                }).start();
            }
        });

        //filer switch 的监听
        st_filer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    filerFlag = true;
                } else {
                    filerFlag = false;
                }
            }
        });

        //st_s 监听是否为0
        st_S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    SFlag = true;
                    SVaule = 0;
                    ImageMethod.HSV_S_V(SVaule,VVaule);
                } else {
                    SFlag = false;
                    SVaule = 43;
                    ImageMethod.HSV_S_V(SVaule,VVaule);
                }
            }
        });
        //st_v 监听是否为0
        st_V.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    VFlag = true;
                    VVaule = 0;
                    ImageMethod.HSV_S_V(SVaule,VVaule);
                } else {
                    VFlag = false;
                    VVaule = 43;
                    ImageMethod.HSV_S_V(SVaule,VVaule);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获得相册、相机返回的结果，并显示
        if (CameraActivity.LISTENING) {
            Log.e("TAG", "返回的Uri结果：" + CameraActivity.IMG_URI);
            Log.e("TAG", "返回的File结果：" + CameraActivity.IMG_File.getPath());
            imagePath = CameraActivity.IMG_File.getPath();
            CameraActivity.LISTENING = false;   //关闭获取结果
            iv_picture.setImageURI(CameraActivity.IMG_URI);  //显示图片到控件
        }
    }

    public void takePhoto(View view) {
        progressCount = 0;
        startActivity(new Intent(MainActivity.this, CameraActivity.class).putExtra(CameraActivity.ExtraType, CameraActivity.CAMERA));
    }

    public void choosePhoto(View view) {
        progressCount = 0;
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            Toast.makeText(this, "打开手机相册^^", Toast.LENGTH_SHORT).show();
            doOpenAlbum();
            return;
        }else{
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);}
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length <= 0 || grantResults[0] != 0) {
                Toast.makeText(this, "没有获得摄像头权限^-^", Toast.LENGTH_SHORT).show();
            } else {

            }
        } else if (requestCode != 0) {
        } else {
            if (grantResults.length <= 0 || grantResults[0] != 0) {
                Toast.makeText(this, "没有获得读写权限^-^", Toast.LENGTH_SHORT).show();
            } else {
                doOpenAlbum();
            }
        }
    }

    private void doOpenAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 0) {
        } else {
            if (Build.VERSION.SDK_INT < 19) {
                handleImageBeforeApi19(data);
            } else {
                handleImageOnApi19(data);
            }
        }
    }

    private void handleImageOnApi19(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (TextUtils.equals(uri.getAuthority(), "com.android.providers.media.documents")) {
                this.imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_id=" + documentId.split(":")[1]);
            } else if (TextUtils.equals(uri.getAuthority(), "com.android.providers.downloads.documents")) {
                if (documentId == null || !documentId.startsWith("msf:")) {
                    this.imagePath = getImagePath(ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId).longValue()), (String) null);
                } else {
                    resolveMSFContent(uri, documentId);
                    return;
                }
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            this.imagePath = getImagePath(uri, (String) null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            this.imagePath = uri.getPath();
        }
        displayImage(this.imagePath);
    }

    private void resolveMSFContent(Uri uri, String documentId) {
        File file = new File(getCacheDir(), "temp_file" + getContentResolver().getType(uri).split("/")[1]);
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            while (true) {
                int read = inputStream.read(buffer);
                int read2 = read;
                if (read != -1) {
                    outputStream.write(buffer, 0, read2);
                } else {
                    outputStream.flush();
                    Bitmap bitmap2 = BitmapFactory.decodeFile(file.getAbsolutePath());
                    this.iv_picture.setImageBitmap(bitmap2);
                    this.imageBase64 = ImageUtil.imageToBase64(bitmap2);
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void handleImageBeforeApi19(Intent data) {
        String imagePath2 = getImagePath(data.getData(), (String) null);
        this.imagePath = imagePath2;
        displayImage(imagePath2);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, (String[]) null, selection, (String[]) null, (String) null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("_data"));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath2) {
        Log.d(TAG, "displayImage: ------------" + imagePath2);
        if (imagePath2 != null) {
            Bitmap bitmap2 = BitmapFactory.decodeFile(imagePath2);
            this.iv_picture.setImageBitmap(bitmap2);
            this.imageBase64 = ImageUtil.imageToBase64(bitmap2);
        }
    }

    private void init() {
        this.bt_getPicture = (Button) findViewById(R.id.bt_getPicture);
        this.iv_picture = (ImageView) findViewById(R.id.iv_picture);
        this.sb_colorMin = (SeekBar) findViewById(R.id.sb_colorMin);
        this.sb_colorMax = (SeekBar) findViewById(R.id.sb_colorMax);
        this.et_eValue = (TextView) findViewById(R.id.et_eValue);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        st_filer = (Switch) findViewById(R.id.filer);
        iv_picture.setImageResource(R.drawable.example);
        tv_colorMax = (TextView) findViewById(R.id.colorMaxValue);
        tv_colorMin = (TextView) findViewById(R.id.colorMinValue);
        bt_picture = (Button) findViewById(R.id.bt_picture);
        bt_takePhoto = (Button) findViewById(R.id.bt_takePhoto);
        et_eValue.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        st_S = (Switch) findViewById(R.id.st_s);
        st_V = (Switch) findViewById(R.id.st_v);
        //setLogo();

    }

    private void initLoadOpenCV() {
        if (OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "欢迎使用Colorimetry", Toast.LENGTH_SHORT).show();
            //   Toast.makeText(getApplicationContext(), "加载OpenCV库成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "加载OpenCV库失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap loadBitmap(Bitmap bp) {
        if (imagePath == null) {
            @SuppressLint("ResourceType") InputStream is = getResources().openRawResource(R.drawable.example);
            bp = BitmapFactory.decodeStream(is);
        } else {
            bp = BitmapFactory.decodeFile(MainActivity.this.imagePath);
        }
        return bp;
    }
    private void buttonChange(boolean state){
        if(state){
            bt_takePhoto.setEnabled(true);
            bt_picture.setEnabled(true);
            bt_getPicture.setEnabled(true);
            sb_colorMax.setEnabled(true);
            sb_colorMin.setEnabled(true);
            st_filer.setEnabled(true);
        }
        else{
            bt_takePhoto.setEnabled(false);
            bt_picture.setEnabled(false);
            bt_getPicture.setEnabled(false);
            sb_colorMax.setEnabled(false);
            sb_colorMin.setEnabled(false);
            st_filer.setEnabled(false);
        }
    }


}
