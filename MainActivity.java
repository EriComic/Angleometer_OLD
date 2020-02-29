package com.bigLITTLE.Angle_v3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // System Sensor hanteraren startar
    private SensorManager mSensorManager;

    // Hämtar variabler för mina sensorer
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    // Skapar Arrays för de 2 sensorerna med
    // 3 i varje för deras X Y Z värden
    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    // Introducerar Texten i appen där värden ska stå
    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;
    private TextView testRoll;
    private TextView testPitch;

    // System Display. Behöver introduceras för att känna av skärm rotation
    private Display mDisplay;

    public static final String TAG = "AngleMk3";
    final float rad2deg = (float)(180.0f/Math.PI);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Bestämmer att min content ska ligga på activity main XML

        //Hitta texten där text ska stå
        mTextSensorAzimuth = (TextView) findViewById(R.id.value_azimuth);
        mTextSensorPitch = (TextView) findViewById(R.id.value_pitch);
        mTextSensorRoll = (TextView) findViewById(R.id.value_roll);
        testRoll = (TextView) findViewById(R.id.testRoll);
        testPitch = (TextView) findViewById(R.id.testPitch);

        // Hämtar sensorerna till variablerna.
        // getDefaultSensor() metoden ger Null ifall de inte finns
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Tillåter appen att see skärmens rotation
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = wm.getDefaultDisplay();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ser till att alla sensorer finns
        // Och lyssnar på sensorerna för deras värden
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Tar bort sensor Listeners ifall appen stängs av (sparar ström)
        mSensorManager.unregisterListener(this);
    }

    // onSensorChanged get attributen att
    // koden körs varje gång sensor värden ändras
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //Tar typ av sensor och fortsätter på switch
        int sensorType = sensorEvent.sensor.getType();

        // SensorEvent kallas flera gånger pga onSensorChanged
        // Detta klonar värdet så det inte plötsligt byts
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }
        // Räknar ut RotationMatrix och kombinerar de till en array
        //Sedan applicerar dess kordinater och jämför det med verkliga världens värden
        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix, null, mAccelerometerData, mMagnetometerData);

        // skiftar runt värden när enheten roteras så de inte blir incorrecta när
        // skärmen roterar
        float[] rotationMatrixAdjusted = new float[9];
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                rotationMatrixAdjusted = rotationMatrix.clone();
                break;
            case Surface.ROTATION_90:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                        rotationMatrixAdjusted);
                break;
            case Surface.ROTATION_180:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                        rotationMatrixAdjusted);
                break;
            case Surface.ROTATION_270:
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                        rotationMatrixAdjusted);
                break;
        }

        // Få ut rotations värden ur rotation matrix. Värdet här är i Radianer
        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrixAdjusted, orientationValues);
        }

        // Få ut de individuella värdena i array. Y X Z
        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];


        // Ersätt texten i layout med värdena
        mTextSensorAzimuth.setText(getResources().getString(R.string.value_format, azimuth));
        mTextSensorPitch.setText(getResources().getString(R.string.value_format, pitch));
        mTextSensorRoll.setText(getResources().getString(R.string.value_format, roll));
        testRoll.setText(getResources().getString(R.string.value_format, roll*rad2deg));
        testPitch.setText(getResources().getString(R.string.value_format, pitch*rad2deg));

        }

    // onAccuracyChanged måste finnas för att onSensorChanged ska fungera.
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}