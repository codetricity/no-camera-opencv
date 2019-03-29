package com.example.opencvsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // load native library
    static {
        System.loadLibrary("opencvsample");
    }

    private TextView mTextView;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OpenCV version
        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("OpenCV version: " + version());
        mImageView = (ImageView) findViewById(R.id.imageView);

        processImage();


    }

    // native functions
    public native String version();
    public native byte[] rgba2bgra(int width, int height, byte[] src);

    private void processImage() {


        // load the picture from the drawable resource
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.park);

        // get the byte array from the Bitmap instance
        ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
        img.copyPixelsToBuffer(byteBuffer);

        // call the process from the native library
        byte[] dst = rgba2bgra(img.getWidth(), img.getHeight(), byteBuffer.array());

        // set the output image on an ImageView
        Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
        mImageView.setImageBitmap(bmp);

    }

}