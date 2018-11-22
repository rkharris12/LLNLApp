package com.example.david.alpha;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Set;

public class ActiveHoursActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;

    private boolean sensorRegistered = false;

    public void onAccuracyChanged(Sensor sensor, int num) {

    }

    public void onCreate(Context context) {
        sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        //sensorMan.registerListener(this, accelerometer,
        //        SensorManager.SENSOR_DELAY_NORMAL);
        sensorRegistered = true;
    }

    private int hitCount = 0;
    private double hitSum = 0;
    private double hitResult = 0;

    private final int SAMPLE_SIZE = 50;
    private final double THRESHOLD = 0.2;

    //@Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("ActiveHours: ", "Sensor Event Detected");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt( x*x + y*y + z*z );
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel*0.9f + delta;

            if (hitCount <= SAMPLE_SIZE) {
                hitCount++;
                hitSum += Math.abs(mAccel);
            } else {
                hitResult = hitSum/SAMPLE_SIZE;

                Log.d("Sensor", String.valueOf(hitResult));
                if (hitResult > THRESHOLD) {
                    Log.d("Accelerometer: ", "Walking");
                } else {
                    Log.d("Accelerometer: ", "Not Walking");
                }

                hitCount = 0;
                hitSum = 0;
                hitResult = 0;
            }
        }
    }

    //David Edit 11/21: Simplified logic and ensured sensors that don't start with
    //  but contain "SGM" are included
    public static boolean checkAttached(Location location) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            String bluetoothDevice = bt.getName();
            int startingIndex = bluetoothDevice.indexOf("SGM");
            if (startingIndex != -1) {
                return true;
            }
        }
        return false;
    }

}
