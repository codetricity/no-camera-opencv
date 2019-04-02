package com.example.opencvsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;

import org.theta4j.osc.CommandResponse;
import org.theta4j.osc.CommandState;
import org.theta4j.webapi.TakePicture;
import org.theta4j.webapi.Theta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    /**
     * if you have a real camera, uncomment the lines below
     * */
//public class MainActivity extends PluginActivity {
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setKeyCallback(keyCallback);
//        Log.d("THETADEBUG", "set key callback");
//        if (isApConnected()) {
//        }
//    }
///////////////////////////////////////////
// End comment section
///////////////////////////////////////////

    // load native library
    static {
        System.loadLibrary("opencvsample");
    }

    Button takePictureButton;
    ImageView thetaImageView;
    TextView statusTextView;
    // on the RICOH THETA V, there is no function button. People often use the
    // wifi button on the side of the camera to process images or change settings
    Button processButton;
    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    String basepath = extStorageDirectory + "/DCIM/100RICOH/";

    String picturePath;
    private ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService thetaExecutor = Executors.newSingleThreadExecutor();


    URL inputFileUrl;
    private final String TAG = "THETADEBUG";

    int imageNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thetaImageView = findViewById(R.id.thetaImageId);
        takePictureButton = findViewById(R.id.takePictueButtonId);
        processButton = findViewById(R.id.processButtonId);
        thetaImageView = findViewById(R.id.thetaImageId);
        thetaImageView.setImageResource(R.drawable.theta);

        checkPermission();

        File thetaMediaDir = new File(basepath);
        if (!thetaMediaDir.exists()) {
            thetaMediaDir.mkdirs();
        }

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picturePath = takeThetaPicture();
                Log.d(TAG, "received image path " + picturePath);

                /**
                 * Call your image processing or file transfer method here or
                 * trigger it with a button press.
                 * If you want to process your image when the picture is taken,
                 * uncomment the line below.
                 */
                // processImage(picturePath);


            }
        });

        processButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This section is only if you want to trigger your image
             * processing or file transfer when a button is pressed
             * on the camera.  If you start the image process when the
             * picture is taken, you can delete the entire method.
             * @param v
             */
            @Override
            public void onClick(View v) {
                processImage(picturePath);
                Toast.makeText(MainActivity.this, "Processed image: " +
                        picturePath, Toast.LENGTH_LONG).show();
            }
        });

    }

    // native functions
    public native String version();
    public native byte[] rgba2bgra(int width, int height, byte[] src);

    private void processImage(String thetaPicturePath) {


        // load the picture from the drawable resource
//        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.park);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Log.d(TAG, thetaPicturePath);
        Bitmap img = BitmapFactory.decodeFile(thetaPicturePath, options);

        // get the byte array from the Bitmap instance
        ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
        img.copyPixelsToBuffer(byteBuffer);

        // call the process from the native library
        byte[] dst = rgba2bgra(img.getWidth(), img.getHeight(), byteBuffer.array());

        // set the output image on an ImageView
        Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
        thetaImageView.setImageBitmap(bmp);

    }



    public String takeThetaPicture() {

        InputStream in = null;
        OutputStream out = null;
        String thetaImagePath = null;
        AssetManager assetManager = getResources().getAssets();

        String[] thetaImageFiles = null;

        try {
            thetaImageFiles =  assetManager.list("100RICOH");
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            if (imageNumber >= thetaImageFiles.length) {
                imageNumber = 0;
                Log.d(TAG, "Set Image Number to Zero");
            }

            // copy file
            in = assetManager.open("100RICOH/" + thetaImageFiles[imageNumber]);
            out = new FileOutputStream(basepath  + thetaImageFiles[imageNumber]);
            copyFile(in, out);

            in.close();
            in = null;
            out.flush();
            out.close();
            out= null;
            Log.d(TAG, "copied file " + thetaImageFiles[imageNumber]);

            InputStream inputStream = assetManager.open("100RICOH/" + thetaImageFiles[imageNumber]);
            Drawable d = Drawable.createFromStream(inputStream, null);
            thetaImageView.setImageDrawable(d);
            inputStream.close();
            inputStream = null;
            thetaImagePath = basepath + thetaImageFiles[imageNumber];

            // increment image number last
            imageNumber = imageNumber + 1;

        } catch (IOException e) {
            e.printStackTrace();
        }




        return thetaImagePath;


    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }



    private KeyCallback keyCallback = new KeyCallback() {

        Theta theta = Theta.createForPlugin();

        @Override
        public void onKeyDown(int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                thetaExecutor.submit(() -> {
                    CommandResponse<TakePicture.Result> response = null;

                    try {
                        response = theta.takePicture();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    while (response.getState() != CommandState.DONE) {
                        try {
                            response = theta.commandStatus(response);
                            Thread.sleep(100);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "fileUrl: " + response.getResult().getFileUrl());

                    inputFileUrl = response.getResult().getFileUrl();


                });
            }
        }
        @Override
        public void onKeyUp(int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) {
                processImage(getImagePath());
            }

        }

        @Override
        public void onKeyLongPress(int keyCode, KeyEvent keyEvent) {

        }
    };

    public String getImagePath() {
        String[] parts = inputFileUrl.toString().split("/");
        int length = parts.length;
        String filepath = Environment.getExternalStorageDirectory().getPath() +
                "/DCIM/100RICOH/" +
                parts[length - 1];
        Log.d(TAG, filepath);
        return filepath;
    }

    public void checkPermission() {
        statusTextView = findViewById(R.id.statusViewId);
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED)) {
            statusTextView.setText("Ready");
            Toast.makeText(this, "storage permission good", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "WARNING: Need to enable storage permission",
                    Toast.LENGTH_LONG).show();
            statusTextView.setText("Check Permissions");
        }
    }

}