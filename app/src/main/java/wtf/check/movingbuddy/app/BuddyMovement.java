package wtf.check.movingbuddy.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 05.03.2015.
 */
public class BuddyMovement {
    private static final String TAG = "Movement";
    private Buddy buddy;
    private BuddyService backgroundBuddyService;
    private Position movement;

    private boolean isMoving;

    private List<BuddyMoveListener> listeners = new ArrayList<>();

    private long timeMillisecondsLastUpdate;

    public BuddyMovement(Buddy buddy, BuddyService buddyService) {
        this.buddy = buddy;
        this.backgroundBuddyService = buddyService;
        movement = new Position(0, 0);
        timeMillisecondsLastUpdate = 0;
        isMoving = false;
    }

    public BuddyService getBackgroundBuddyService() {
        return backgroundBuddyService;
    }

    public void removeListener(BuddyService buddyService) {
        if (buddyService != null && listeners.contains(buddyService)) {
            listeners.remove(buddyService);
        }
    }

    public Position getMovement() {
        return movement;
    }

    public void addListener(BuddyMoveListener listener) {
        listeners.add(listener);
    }

    private void prepareUpdate() {
        timeMillisecondsLastUpdate = System.currentTimeMillis();
    }

    private void doUpdate() {
        isMoving = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMoving) {
                    long thisRunTime = System.currentTimeMillis();

                    // time between updates in Seconds
                    float dt = ((float) thisRunTime - timeMillisecondsLastUpdate) / 1000f;

//                    Log.v(TAG, "timeMS:" + timeMillisecondsLastUpdate +
//                                    " dt:" + dt +
//                                    " movement:" + movement.toString()
//                    );

                    // set time for next run;
                    timeMillisecondsLastUpdate = thisRunTime;

                    // modify buddy position
                    buddy.addToPosition(movement.x * dt, movement.y * dt);

                    // slow up
                    synchronized (movement) {
                        movement.x -= movement.x * dt;
                        movement.y -= movement.y * dt;
                    }

                    // wait a bit (at least 1 ms otherwise could a loop could occur)
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (movement.x < 1 && movement.x > -1) {
                        if (movement.y < 1 && movement.y > -1) {
                            isMoving = false;
                        }
                    }

                    // notify others
                    synchronized (this) {
                        moved();
                    }
                }


            }

        }).start();


//        if (movement.x < 1 && movement.x > -1) {
//            if (movement.y < 1 && movement.y > -1) {
//                // pause
//            } else {
//                Thread.yield();
//                doUpdate();
//            }
//        } else {
//            Thread.yield();
//            doUpdate();
//        }
    }

    public void update() {
        if (!isMoving) {
            prepareUpdate();
            doUpdate();
        }
    }


    public void moved() {
        for (BuddyMoveListener listener : listeners) {
            listener.moved();
        }
    }

    public interface BuddyMoveListener {
        public void moved();
    }
}
