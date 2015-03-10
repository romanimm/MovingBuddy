package wtf.check.movingbuddy.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private Context context;
    private Intent buddyIntent;
    private ArrayList<ImageButton> buddyList;
    private Buddy buddy;
    private static final int INTENT_RESULT_CODE_IMAGE_PICKER = 1;

    private BuddyService boundService;
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        this.buddyIntent = new Intent(this, BuddyService.class);
        this.buddy = Buddy.getInstance();

        setContentView(R.layout.activity_main);

        // ToDo: read data from running Service

        Log.i("main", "started");

        LinearLayout selectBuddyLayout = (LinearLayout) findViewById(R.id.select_buddy_layout);

        buddyList = initializeBuddyList();

        final ImageButton pickImage = (ImageButton) findViewById(R.id.imageButton_pickImage);
        pickImage.setOnClickListener(new View.OnClickListener() {
            ImageButton thisButton = pickImage;

            @Override
            public void onClick(View v) {
                Intent intentImagePicker = new Intent(Intent.ACTION_PICK);
                intentImagePicker.setType("image/*");
                startActivityForResult(intentImagePicker, INTENT_RESULT_CODE_IMAGE_PICKER);

                disableBuddyButtons();
                thisButton.setEnabled(false);
            }
        });

        for (ImageButton buddyButton : buddyList) {
            selectBuddyLayout.addView(buddyButton);
        }

        ((CheckBox) findViewById(R.id.main_checkBox_userInput)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUserInput();
            }
        });
        
        ((CheckBox) findViewById(R.id.main_checkBox_sensorInput)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSensorInput();
            }
        });

        final Switch serviceEnabled = (Switch) findViewById(R.id.toggleButtonServiceEnabled);
        serviceEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceEnabled.isChecked()) {
                    startService();
                } else {
                    try {
                        boundService.disableUserInput();
                    } catch (NullPointerException npe) {
                        Log.w(TAG, "unable to unsubscribe (user input)" + npe.getMessage());
                    }
                    try {
                        boundService.disableSensorInput();
                    } catch (NullPointerException npe) {
                        Log.w(TAG, "unable to unsubscribe (sensor input)" + npe.getMessage());
                    }
                    stopService();

                }
            }
        });
        serviceEnabled.setChecked(true);
        startService();
        doBindService();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case INTENT_RESULT_CODE_IMAGE_PICKER:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        InputStream bitmapStream = getContentResolver().openInputStream(imageUri);
                        Bitmap image = BitmapFactory.decodeStream(bitmapStream);
                        buddy.setDrawableBitmap(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                }
        }
    }

    private void close() {
        finish();
    }

    private void startService() {
        if (!BuddyService.isRunning()) {
            startService(buddyIntent);
        }

    }

    private void stopService() {
        if (BuddyService.isRunning()) {
            stopService(buddyIntent);
        }
    }

    private ArrayList<ImageButton> initializeBuddyList() {
        ArrayList<ImageButton> list = new ArrayList<>();

        Class<?> drawableClass = R.drawable.class;
        final Field[] fields = drawableClass.getDeclaredFields();
        for (Field field : fields) {
            final int drawableId;

            if (field.getName().startsWith("buddy_")) {
                try {
                    drawableId = field.getInt(drawableClass);

                    final ImageButton buddyButton = new ImageButton(context);
                    buddyButton.setImageResource(drawableId);
                    buddyButton.setOnClickListener(new View.OnClickListener() {
                        ImageButton thisButton = buddyButton;

                        @Override
                        public void onClick(View v) {
                            disableBuddyButtons();
                            setBuddyDrawable(drawableId);
                            thisButton.setEnabled(false);
                        }
                    });
                    list.add(buddyButton);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    private void disableBuddyButtons() {
        for (ImageButton button : buddyList) {
            button.setEnabled(true);
        }
    }

    public void setBuddyDrawable(int drawableId) {
        buddy.setDrawableId(drawableId);
    }


    private void updateUserInput() {
        if (!mIsBound) {
            Toast.makeText(context, "Service is not bound!", Toast.LENGTH_LONG).show();
        } //@Todo: think about: "should the if be replaced with else if?" and take a look at updateSensorInput
        if (((CheckBox) findViewById(R.id.main_checkBox_userInput)).isChecked()) {
            boundService.enableUserInput();
        } else {
            boundService.disableUserInput();
        }
    }

    private void updateSensorInput() {
        if (!mIsBound) {
            Toast.makeText(context, "Service is not bound!", Toast.LENGTH_LONG).show();
        } //@Todo: think about: "should the if be replaced with else if?" and take a look at updateUserInput
        if (((CheckBox) findViewById(R.id.main_checkBox_sensorInput)).isChecked()) {
            boundService.enableSensorInput();
        } else {
            boundService.disableSensorInput();
        }
    }

    private String TAG = "main";
    private ServiceConnection serviceConnection = new ServiceConnection() {
        // from: http://developer.android.com/reference/android/app/Service.html
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((BuddyService.LocalBinder) service).getService();
            Log.i(TAG, "bound!");
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
            Log.i(TAG, "unbound");
        }
    };

    void doBindService() {
        Log.i(TAG, "binding...");
        if (bindService(buddyIntent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(serviceConnection);
            Log.i(TAG, "unbinding...");
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService(); //onStop does unbind... sometimes
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
