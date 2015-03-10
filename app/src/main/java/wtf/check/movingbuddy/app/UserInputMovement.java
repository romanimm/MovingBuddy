package wtf.check.movingbuddy.app;

import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by roman on 05.03.2015.
 */
public class UserInputMovement extends BuddyMovement implements BuddyService.BuddyCollision {
    private static final String TAG = "UserInputMovement";

    private VelocityTracker velocityTracker = null;

    private Position movement;

    public UserInputMovement(final Buddy buddy, BuddyService buddyService, ImageView head) {
        super(buddy, buddyService);

        movement = super.getMovement();

        head.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int index = event.getActionIndex();
                int action = event.getActionMasked();
                int pointerId = event.getPointerId(index);

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (velocityTracker == null) {
                            // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                            velocityTracker = VelocityTracker.obtain();
                        } else {
                            // Reset the velocity tracker back to its initial state.
                            velocityTracker.clear();
                        }
                        // Add a user's movement to the tracker.
                        velocityTracker.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        buddy.getPosition().set(event.getRawX() - buddy.getWidth() / 2, event.getRawY() - buddy.getHeight() / 2);
                        moved();
                        velocityTracker.addMovement(event);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // When you want to determine the velocity, call
                        // computeCurrentVelocity(). Then call getXVelocity()
                        // and getYVelocity() to retrieve the velocity for each pointer ID.
                        velocityTracker.computeCurrentVelocity(250);
                        // Return a VelocityTracker object back to be re-used by others.
                        movement.set(VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId)
                                , VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId));
//                        movement.calculateSpeedFromPoint();
//                        Log.i(TAG,"movement measured:"+movement.toString());
                        runMovement();
                        velocityTracker.recycle();
                        velocityTracker = null;
                        break;
                }
                return true;
            }
        });

        buddyService.addCollisionListener(this);
        runMovement();
    }

    private void runMovement() {
        super.update();
    }

    @Override
    public void collideLeft() {
        movement.x = -movement.x;
    }

    @Override
    public void collideRight() {
        movement.x = -movement.x;
    }

    @Override
    public void collideTop() {
        movement.y = -movement.y;
    }

    @Override
    public void collideBottom() {
        movement.y = -movement.y;
    }
}
