package wtf.check.movingbuddy.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by roman on 05.03.2015.
 */
public class SensorMovement extends BuddyMovement implements SensorEventListener {

    private static final String TAG = "sensor movement";
    private final SensorManager sensorManager;
    private static final float SENSOR_AMPLIFIER = 0.1f;

    public SensorMovement(Buddy buddy, BuddyService buddyService) {
        super(buddy, buddyService);

        sensorManager = (SensorManager) super.getBackgroundBuddyService().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                , SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null) {
            // @Todo: check if the manipulation on super.getMovement should be in a synchronized block!
            super.getMovement().add(-event.values[0] * SENSOR_AMPLIFIER, event.values[1] * SENSOR_AMPLIFIER);
//            Log.v(TAG, "sensor moved: " + -event.values[0] * SENSOR_AMPLIFIER + "/"+event.values[1] * SENSOR_AMPLIFIER + " movement:" + getMovement().toString());
            update();

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
