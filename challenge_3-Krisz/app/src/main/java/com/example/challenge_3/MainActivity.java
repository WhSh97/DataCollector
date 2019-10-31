package com.example.challenge_3;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


/*
Todo: remake the app from scratch commenting everything, you'll need it for the report
Todo: reed the slides from the first lecture, they might help. Same thing with the ones from Theme3
Todo: even a non-working version of the navigator would be cool
Todo: finish the app until the morning, be here at 8:30
*/

/*
Todo:
 Planing for the app:
 *use accelerometer + features on it with weka
 *if this really does not work, try thresholding, shouldn't be that difficult (calculate a sigma for this)
*/

//f
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.TimeUnit;

import static com.example.challenge_3.Calcuations.Maximum;
import static com.example.challenge_3.Calcuations.Mean;
import static com.example.challenge_3.Calcuations.Minimum;
import static com.example.challenge_3.Calcuations.StandardDev;



public class MainActivity extends AppCompatActivity {


    static final float ALPHA = 0.2f;

    protected float[] accelVals;


    //declare initial variables for data gathering
    private static final int PROX_SENSOR_SENSITIVITY = 4;
    private String currActivity = "NoActivitySelected";
    private final String MAIN_TAG = this.getClass().getSimpleName();
    private SensorManager sm;
    private boolean sendData;
    private boolean predictData;
    private TextView textView;
    private Switch sendDataSwitch;
    private Switch predictDataSwitch;
    private Button stopButton;
    private Button startButton;
    public String proximity;
    public List<String[]> myDataList = new ArrayList<String[]>();
    //creating the calculator

    //declaring varaibles for data classification

    private String predictedActivity;

    private Sensor accelero;
    private Sensor magnetic;
    private Sensor prox;

    private SensorEventListener allsel;

    private static double xAco;
    private static double yAco;
    private static double zAco;
    private long tAco;

    private static double xMag;
    private static double yMag;
    private static double zMag;
    private long tMag;
   ;

    //declaring global thresholds
    private static long lastChange = 0;
    private static final long threshold = 500;

    //declaring path for CSV
    File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    String fileName = "AnalysisData.csv";
    String filePath = baseDir + File.separator + fileName;
    File f = new File(filePath);
    CSVWriter writer = null;

    List<Double> ListXacco = new ArrayList<Double>();
    List<Double> ListYacco = new ArrayList<Double>();
    List<Double> ListZacco = new ArrayList<Double>();

    List<Double> ListXmagno = new ArrayList<Double>();
    List<Double> ListYmagno = new ArrayList<Double>();
    List<Double> ListZmagno = new ArrayList<Double>();

    private double MaxXacco;
    private double MeanXacco;
    private double MinXacco;
    private double STDXacco;

    private double MaxYacco;
    private double MeanYacco;
    private double MinYacco;
    private double STDYacco;

    private double MaxZacco;
    private double MeanZacco;
    private double MinZacco;
    private double STDZacco;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        stopButton = findViewById(R.id.stopButton);
        startButton = findViewById(R.id.startButton);


        predictData = true;
        predictDataSwitch = findViewById(R.id.predictDataSwitch);
        predictDataSwitch.setChecked(predictData);
        predictDataSwitch.setOnCheckedChangeListener(new PredictDataSwitchListener());

        sendData = true;
        sendDataSwitch = findViewById(R.id.sendDataSwitch);
        sendDataSwitch.setChecked(sendData);
        sendDataSwitch.setOnCheckedChangeListener(new SendDataSwitchListener());

        textView = findViewById(R.id.resultTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // CSV File exists
        if (f.exists() && !f.isDirectory()) {
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }
        //CSV File does not exist
        else {
            Log.d("Test", "I'm in the eslse");
            try {
                writer = new CSVWriter(new FileWriter(filePath), ',');
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] data = {"Activity",
                    "MaxXacco", "MeanXacco", "MinXacco", "STDXacco",
                    "MaxYacco", "MeanYacco", "MinYacco", "STDYacco",
                    "MaxZacco", "MeanZacco", "MinZacco", "STDZacco","tAcco"
            };
            writer.writeNext(data);

            try {
                writer.close();
            } catch (IOException e) {
                Toast.makeText(this, "Writer is broken", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                Toast.makeText(this, "Writer is broken", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.aiming:
                currActivity = "Aiming";
                return true;
            case R.id.defending:
                currActivity = "Defending";
                return true;
            case R.id.shooting:
                currActivity = "Shooting";
                return true;

            default:
                currActivity = "NoActivitySelected";
                return true;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startScan(View view) {
        stopButton.setEnabled(true);
        startButton.setEnabled(false);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Toast.makeText(this, "Scan is broken", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // register sensor and Activity from field
        accelero = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        prox = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        allsel = new AllSensorEventListener();
        sm.registerListener(allsel, accelero, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(allsel, prox, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(allsel, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public class AllSensorEventListener implements SensorEventListener {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //float [] mySensorEvent={0, 0, 0};
                //float[] mySensorSmooth={};
                //mySensorEvent=lowPass(sensorEvent.values, accelVals);
                xAco = sensorEvent.values[0];
                yAco = sensorEvent.values[1];
                zAco = sensorEvent.values[2];
                tAco = sensorEvent.timestamp;
                ListXacco.add(xAco);
                ListYacco.add(yAco);
                ListZacco.add(zAco);
                //textView.append(Float.toString(mySensorEvent[1])+'\n');
                //textView.append(Double.toString(mySensorEvent[1])+'\n');

            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                xMag = sensorEvent.values[0];
                yMag = sensorEvent.values[1];
                zMag = sensorEvent.values[2];
                tMag = sensorEvent.timestamp;

                ListXmagno.add(xMag);
                ListYmagno.add(yMag);
                ListZmagno.add(zMag);


            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (sensorEvent.values[0] >= -PROX_SENSOR_SENSITIVITY && sensorEvent.values[0] <= PROX_SENSOR_SENSITIVITY) {

                    //Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
                    proximity = "near";
                } else {
                    proximity = "far";
                    //Toast.makeText(getApplicationContext(), "far", Toast.LENGTH_SHORT).show();
                }
            }

            if (System.currentTimeMillis() - lastChange > threshold) {
                lastChange = System.currentTimeMillis();
                    if (predictData && currActivity != "NoActivitySelected") {
                        MaxXacco=Maximum(ListXacco);
                        MeanXacco=Mean(ListXacco);
                        MinXacco=Minimum(ListXacco);
                        STDXacco=StandardDev(ListXacco);

                        MaxYacco=Maximum(ListYacco);
                        MeanYacco=Mean(ListYacco);
                        MinYacco=Minimum(ListYacco);
                        STDYacco=StandardDev(ListYacco);

                        MaxZacco=Maximum(ListZacco);
                        MeanZacco=Mean(ListZacco);
                        MinZacco=Minimum(ListZacco);
                        STDZacco=StandardDev(ListZacco);

                        ListXacco.clear();
                        ListYacco.clear();
                        ListZacco.clear();

                        double toPrintSensor=-777;
                        predictedActivity="NoActivity";
                        if(MeanYacco>8.49 && MeanYacco<8.65) { //done with one_sigma
                            toPrintSensor=MeanYacco;
                            predictedActivity="Aiming sigma_1";
                        }
                        else if(MeanYacco>8.41 && MeanYacco<8.73){
                            toPrintSensor=MeanYacco;
                            predictedActivity="Aiming sigma_2";
                        }

                        if(MeanZacco<-9.57&&MeanZacco>-9.77)
                        {  if(proximity=="near")
                            predictedActivity="Defending sigma_1 "+' '+proximity;
                        else
                            predictedActivity="Defending sigma_1";
                            toPrintSensor=MeanZacco;
                        }
                        else if(MeanZacco<-9.47&&MeanZacco>-9.87){
                            if(proximity=="near")
                                predictedActivity="Defending sigma_2 "+' '+proximity;
                            else
                                predictedActivity="Defending sigma_2";
                            toPrintSensor=MeanZacco;
                            }
                        if(MeanXacco>9.52&&MeanXacco<9.70) {
                            toPrintSensor=MeanXacco;
                            predictedActivity = "Shooting sigma_1";
                        }
                        else if(MeanXacco>9.43&&MeanXacco<9.79) {
                            toPrintSensor=MeanXacco;
                            predictedActivity = "Shooting sigma_2";
                        }

                        if(!predictedActivity.equals("NoActivity"))
                        {
                            Toast.makeText(getApplicationContext(),predictedActivity,Toast.LENGTH_SHORT).show();
                            textView.append(Double.toString(toPrintSensor)+'\n');
                        }
                        if (sendData && !currActivity .equals("NoActivitySelected")) {
                            String[] myData = {currActivity,
                                    Double.toString(MaxXacco),
                                    Double.toString(MeanXacco),
                                    Double.toString(MinXacco),
                                    Double.toString(STDXacco),
                                    Double.toString(MaxYacco),
                                    Double.toString(MeanYacco),
                                    Double.toString(MinYacco),
                                    Double.toString(STDYacco),
                                    Double.toString(MaxZacco),
                                    Double.toString(MeanZacco),
                                    Double.toString(MinZacco),
                                    Double.toString(STDZacco),
                                    Double.toString((tAco))
                            };
                   myDataList.add(myData);
                    }


                }
            }
        }

        


            @Override
            public void onAccuracyChanged (Sensor sensor,int i){

            }
        }


        public void stopScan(View view) {
            stopButton.setEnabled(false);
            startButton.setEnabled(true);

            sm.unregisterListener(allsel);

        }

        public void endApp(View view) {
            finish();
            try {
                writer.close();
            } catch (IOException e) {
                Toast.makeText(this, "Writer is broken", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        public void WriteData(View v) {
            for (int i = 0; i < myDataList.size(); i++) {
                writer.writeNext(myDataList.get(i));
            }
            myDataList.clear();
        }


        public double magnitude(double x, double y, double z) {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public class PredictDataSwitchListener implements CompoundButton.OnCheckedChangeListener {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//            Log.d(MAIN_TAG, "switched: "+compoundButton.toString());
                predictData = b;
                Toast.makeText(getApplicationContext(), "predict data changed to: " + predictData, Toast.LENGTH_SHORT).show();
            }
        }

        public class SendDataSwitchListener implements CompoundButton.OnCheckedChangeListener {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendData = b;
                Toast.makeText(getApplicationContext(), "send data changed to: " + sendData, Toast.LENGTH_SHORT).show();
            }
        }

        public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currActivity = (String) adapterView.getItemAtPosition(i);
                Log.d(MAIN_TAG, currActivity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        }

        protected float[] lowPass( float[] input, float[] output ) {
            if ( output == null ){
                float[] copy=new float[3];
                System.arraycopy(input, 0, copy, 0, input.length);
                return  copy;
            }

            for ( int i=0; i<input.length; i++ ) {
                output[i] = output[i] + ALPHA * (input[i] - output[i]);
            }
            return output;
    }

    }
