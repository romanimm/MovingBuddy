package wtf.check.movingbuddy.app;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.*;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 02.03.2015.
 */
public class BuddyService extends Service implements Buddy.BuddyListener, BuddyMovement.BuddyMoveListener {//, SensorEventListener {
    private WindowManager windowManager;
    private ImageView head;
    private WindowManager.LayoutParams params;

    private Buddy buddy;

    private int height;
    private int width;

    private static String TAG = "Service";

    private List<BuddyCollision> collisionListener = new ArrayList<BuddyCollision>();

    private static boolean running;

    private static UserInputMovement userInputMovement;
    private static SensorMovement sensorMovement;

    private final IBinder serviceBinder = new LocalBinder();

//     @Todo: handle orientation! currently only vertical

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {
        super.onCreate();
        this.buddy = Buddy.getInstance();
        running = true;
        buddy.addListener(this);
        Log.i("service", "Service Start");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        head = new ImageView(this);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;

        // disable inputs:
        //params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        params.x = 70;
        params.y = 100;

        windowManager.addView(head, params);


        /*@ToDo: There is a bug: you have to select a buddy so that the screen size is correct.
            I thought that the problem is time related...
        */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buddy.setDrawableId(R.drawable.buddy_goldi); // set default buddy
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        if (head != null) windowManager.removeView(head);

    }

    public static boolean isRunning() {
        return running;
    }

    @Override
    public void updateImage(ImageType imageType) {
        switch (imageType) {
            case Bitmap:
                head.setImageBitmap(buddy.getDrawableBitmap());
                break;
            case ResourceId:
                head.setImageResource(buddy.getDrawableId());
                break;
        }
        updateBuddySize();
        updateScreenWidth();
    }

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateScreenWidth() {
        Point size = new Point(0, 0);
        windowManager.getDefaultDisplay().getSize(size);

        width = size.x - head.getWidth();
        height = getResources().getDisplayMetrics().heightPixels - head.getHeight() - head.getHeight();
    }

    private void updateBuddySize() {
        buddy.setWidth(head.getWidth());
        buddy.setHeight(head.getHeight());
    }

    @Override
    public void moved() {
        // do not allow movement outside of the display
        if (buddy.getPosition().x > width) {
            buddy.getPosition().x = width;
            collideRight();
        }
        if (buddy.getPosition().x < 0) {
            buddy.getPosition().x = 0;
            collideLeft();
        }
        if (buddy.getPosition().y > height) {
            buddy.getPosition().y = height;
            collideBottom();
        }
        if (buddy.getPosition().y < 0) {
            buddy.getPosition().y = 0;
            collideTop();
        }

        // set position
        params.x = (int) buddy.getPosition().x;
        params.y = (int) buddy.getPosition().y;

        // update screen
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                try {
                    windowManager.updateViewLayout(head, params);
                } catch (IllegalArgumentException iae) {
                    // happens sometimes if the service is already finished!
                    Log.e(TAG, "did you forget to unsubscribe from the BuddyMoveListener?");
                    iae.printStackTrace();
                }
            }
        });
    }

    public void addCollisionListener(BuddyCollision listener) {
        collisionListener.add(listener);
    }

    private void collideLeft() {
        for (BuddyCollision listener : collisionListener) {
            listener.collideLeft();
            Log.i(TAG, "collide left");
        }
    }

    private void collideRight() {
        for (BuddyCollision listener : collisionListener) {
            listener.collideRight();
            Log.i(TAG, "collide right");
        }
    }

    private void collideTop() {
        for (BuddyCollision listener : collisionListener) {
            listener.collideTop();
            Log.i(TAG, "collide top");
        }
    }

    private void collideBottom() {
        for (BuddyCollision listener : collisionListener) {
            listener.collideBottom();
            Log.i(TAG, "collide bottom");
        }
    }

    public void enableUserInput() {
        userInputMovement = new UserInputMovement(buddy, this, head);
        userInputMovement.addListener(this);
    }

    public void disableUserInput() {
        userInputMovement.removeListener(this);
        userInputMovement = null;
    }

    public void enableSensorInput() {
        sensorMovement = new SensorMovement(buddy, this);
        sensorMovement.addListener(this);
    }

    public void disableSensorInput() {
        sensorMovement.removeListener(this);
        sensorMovement = null;
    }

    public interface BuddyCollision {
        public void collideLeft();

        public void collideRight();

        public void collideTop();

        public void collideBottom();
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        BuddyService getService() {
            return BuddyService.this;
        }
    }
}
