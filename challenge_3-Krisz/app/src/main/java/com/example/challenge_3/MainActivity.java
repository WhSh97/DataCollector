package com.example.challenge_3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity {


    //declare initial variables for data gathering
    private static final int PROX_SENSOR_SENSITIVITY = 4;
    private String currActivity;
    private final String MAIN_TAG = this.getClass().getSimpleName();
    private SensorManager sm;
    private boolean sendData;
    private boolean predictData;
    private TextView textView;
    private EditText textInput;
    private Switch sendDataSwitch;
    private Switch predictDataSwitch;
    private int groupNr;
    private Button stopButton;
    private Button startButton;
    public String proximity;

    private Map<String, Map<String, List<Double>>> recording;
    private Map<String, List<Double>> recordingAccelero;
    private Map<String, List<Double>> recordingLinAccelero;
    private Map<String, List<Double>> recordingGyro;
    //private Map<String, List<Double>> recordingMagno;
    //private Map<String, List<Double>> recordingProx;

    //creating the calculator
    public Calcuations calc = new Calcuations();

    //declaring varaibles for data classification
    private Classifier mClassifier = null;
    private Instances dataUnpredicted;
    private List<String> classes;
    private ArrayList<Attribute> attributeList;
    //put them in the same order, don't know if it really matters
    private final Attribute attributeMinAco = new Attribute("MinAcoSMV");
    private final Attribute attributeMaxAco = new Attribute("MaxAcoSMV");
    private final Attribute attributeMeanAco = new Attribute("MeanAcoSMV");
    private final Attribute attributeModeAco = new Attribute("ModeAcoSMV");
    private final Attribute attributeStdAco = new Attribute("StdAcoSMV");

    private final Attribute attributeMinLinAco = new Attribute("MinLinAcoSMV");
    private final Attribute attributeMaxLinAco = new Attribute("MaxLinAcoSMV");
    private final Attribute attributeMeanLinAco = new Attribute("MeanLinAcoSMV");
    private final Attribute attributeModeLinAco = new Attribute("ModeLinAcoSMV");
    private final Attribute attributeStdLinAco = new Attribute("StdLinAcoSMV");

    private final Attribute attributeMinGyro = new Attribute("MinGyroSMV");
    private final Attribute attributeMaxGyro = new Attribute("MaxGyroSMV");
    private final Attribute attributeMeanGyro = new Attribute("MeanGyroSMV");
    private final Attribute attributeModeGyro = new Attribute("ModeGyroSMV");
    private final Attribute attributeStdGyro = new Attribute("StdGyroSMV");

//    private final Attribute attributeMinProx = new Attribute("MinProxSMV");
//    private final Attribute attributeMaxProx = new Attribute("MaxProxSMV");
//    private final Attribute attributeMeanProx = new Attribute("MeanProxSMV");
//    private final Attribute attributeModeProx = new Attribute("ModeProxSMV");
//    private final Attribute attributeStdProx = new Attribute("StdProxSMV");

    private String predictedActivity;

    private Sensor accelero;
    private Sensor lin_accelero;
    private Sensor gyro;
    //private Sensor magnetic;
    private Sensor prox;

    private SensorEventListener allsel;

    private static double xGyr;
    private static double yGyr;
    private static double zGyr;
    private long tGyr;

    private static double xAco;
    private static double yAco;
    private static double zAco;
    private long tAco;

    private static double xLinAco;
    private static double yLinAco;
    private static double zLinAco;
    private long tlin_Aco;

//    private static double xMag;
//    private static double yMag;
//    private static double zMag;
//    private long tMag;

    //private static  double xProx;

    //declaring global thresholds
    private static long lastChange = 0;
    private static final long threshold = 500;

    //declaring path for CSV
    File baseDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    String fileName = "AnalysisData.csv";
    String filePath = baseDir + File.separator + fileName;
    File f = new File(filePath);
    CSVWriter writer = null;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // set up dropdown menu / spinner
//        Spinner spinner = findViewById(R.id.activitySpinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.activity_spinner, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//
//        spinner = findViewById(R.id.activitySpinner);
//        spinner.setOnItemSelectedListener(new SpinnerActivity());

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

        //textInput = findViewById(R.id.groupNumberText);
        //groupNr = Integer.parseInt(textInput.getText().toString());

        recordingAccelero = new HashMap<>(3);
        recordingLinAccelero = new HashMap<>(3);
        recordingGyro = new HashMap<>(3);
        //recordingMagno = new HashMap<>(3);
        //recordingProx= new HashMap<>();

        recording = new HashMap<>();
        recording.put("Accelero", recordingAccelero);
        recording.put("LinAccelero", recordingLinAccelero);
        recording.put("Gyro", recordingGyro);
        //recording.put("Magno", recordingMagno);
        //recording.put("Prox", recordingProx);

        for (Map.Entry<String, Map<String, List<Double>>> mapEntry : recording.entrySet()) {
            mapEntry.getValue().put("x", new ArrayList<Double>());
            mapEntry.getValue().put("y", new ArrayList<Double>());
            mapEntry.getValue().put("z", new ArrayList<Double>());
            mapEntry.getValue().put("magnitude", new ArrayList<Double>());
            recording.put(mapEntry.getKey(), mapEntry.getValue());
        }

//        Log.d(MAIN_TAG, "size: " + recording.entrySet());
//        for (Map.Entry<String, Map<String, Double>> mapEntry : recording.entrySet()) {
//            Log.d(MAIN_TAG, "kes:" + mapEntry.getValue().get("min"));
//        }

        AssetManager assetManager = getAssets();
        try {
//            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("p1-rightpocket-naivebayes-model.model"));
//            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("all_part-left-pocket-j48.model"));
//            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("self_collected-random_forest.model"));
//            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("self_collected-lmt_trees.model"));
            //mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("best-trees-randomforrest.model"));
            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("model6.model"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show();


        predictedActivity = "";
        classes = new ArrayList<String>(3) {
            {
                add("Aiming");
//                add("NotAimingStanding");
//                //add("NotAimingHand");
//                add("NotAimingWalking");
//                add("AimingWalking");
                //add("Defending");
                add("Shooting");
                add("NotAiming");
            }
        };

//        Log.d(MAIN_TAG, "the arraylist is: "+classes.toString());

        attributeList = new ArrayList<Attribute>(5) {
            {
                add(attributeMinAco);
                add(attributeMaxAco);
                add(attributeMeanAco);
                add(attributeModeAco);
                add(attributeStdAco);
                add(attributeMinLinAco);
                add(attributeMaxLinAco);
                add(attributeMeanLinAco);
                add(attributeModeLinAco);
                add(attributeStdLinAco);
                add(attributeMinGyro);
                add(attributeMaxGyro);
                add(attributeMeanGyro);
                add(attributeModeGyro);
                add(attributeStdGyro);
//                add(attributeMinProx);
//                add(attributeMaxProx);
//                add(attributeMeanProx);
//                add(attributeModeProx);
//                add(attributeStdProx);
                Attribute attributeClass = new Attribute("@@class@@", classes);
                add(attributeClass);
            }
        };
        dataUnpredicted = new Instances("TestInstances", attributeList, 1);
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);


        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // File exist
        if (f.exists() && !f.isDirectory()) {
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }
        //File does not exist
        else {
            Log.d("Test","I'm in the eslse");
            try {
                writer = new CSVWriter(new FileWriter(filePath), ',');
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String[] data={"Activity","MinAcoSMV", "MaxAcoSMV", "MeanAcoSMV","ModeAcoSMV","StdAcoSMV"
                    ,"MinLinAcoSMV", "MaxLinAcoSMV", "MeanLinAcoSMV","ModeLinAcoSMV","StdLinAcoSMV",
                    "MinGyroSMV", "MaxGyroSMV", "MeanGyroSMV","ModeGyroSMV","StdGyroSMV",
                    //"MinProxSMV", "MaxProxSMV", "MeanProxSMV","ModeProxSMV","StdProxSMV"
            };
            writer.writeNext(data);
            Log.d("Test","I managed to write the headers");

            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
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
        Toast.makeText(this, "Selected Item: " +item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.aiming:
                currActivity="Aiming";
                return true;
            case R.id.not_aiming:
                currActivity="Not Aiming";
                return true;
            case R.id.defending:
                currActivity="Defending";
                return true;
            case R.id.shooting:
                currActivity="Shooting";
                return true;
//            case R.id.NotAimingStanding:
//                currActivity="NotAimingStanding";
//                return true;
//            case R.id.NotAimingHand:
//                currActivity="NotAimingHand";
//                return true;
//            case  R.id.NotAimingWalking:
//                currActivity="NotAimingWalking";
//                return true;
//            case R.id.AimingWalking:
//                currActivity="AimingWalking";
//                return true;

//                    add("NotAimingStanding");
//                add("NotAimingHand");
//                add("NotAimingWalking");
//                add("AimingWalking");
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startScan(View view) {
        stopButton.setEnabled(true);
        startButton.setEnabled(false);

        // register sensor and Activity from field
        accelero = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lin_accelero = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        prox=sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // this
        allsel = new AllSensorEventListener();
        sm.registerListener(allsel, accelero, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(allsel, lin_accelero, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(allsel, gyro, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(allsel, prox, SensorManager.SENSOR_DELAY_FASTEST);
        //sm.registerListener(allsel, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public class AllSensorEventListener implements SensorEventListener {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            String result = "";

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                xAco = sensorEvent.values[0];
                yAco = sensorEvent.values[1];
                zAco = sensorEvent.values[2];
                tAco = sensorEvent.timestamp;
                double magnitudeAco = magnitude(xAco, yAco, zAco);

                recording.get("Accelero").get("x").add(xAco);
                recording.get("Accelero").get("y").add(yAco);
                recording.get("Accelero").get("z").add(zAco);
                recording.get("Accelero").get("magnitude").add(magnitudeAco);

            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                xLinAco = sensorEvent.values[0];
                yLinAco = sensorEvent.values[1];
                zLinAco = sensorEvent.values[2];
                tlin_Aco = sensorEvent.timestamp;
                double magnitudeLinAco = magnitude(xLinAco, yLinAco, zLinAco);

                recording.get("LinAccelero").get("x").add(xLinAco);
                recording.get("LinAccelero").get("y").add(yLinAco);
                recording.get("LinAccelero").get("z").add(zLinAco);
                recording.get("LinAccelero").get("magnitude").add(magnitudeLinAco);

            }
//            else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                xMag = sensorEvent.values[0];
//                yMag = sensorEvent.values[1];
//                zMag = sensorEvent.values[2];
//                tMag = sensorEvent.timestamp;
//                double magnitudeMag = magnitude(xMag, yMag, zMag);
//
//                recording.get("Magno").get("x").add(xMag);
//                recording.get("Magno").get("y").add(yMag);
//                recording.get("Magno").get("z").add(zMag);
//                recording.get("Magno").get("magnitude").add(magnitudeMag);

            //}
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                xGyr = sensorEvent.values[0];
                yGyr = sensorEvent.values[1];
                zGyr = sensorEvent.values[2];
                tGyr = sensorEvent.timestamp;
                double magnitudeGyr = magnitude(xGyr, yGyr, zGyr);

                recording.get("Gyro").get("x").add(xGyr);
                recording.get("Gyro").get("y").add(yGyr);
                recording.get("Gyro").get("z").add(zGyr);
                recording.get("Gyro").get("magnitude").add(magnitudeGyr);
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (sensorEvent.values[0] >= -PROX_SENSOR_SENSITIVITY && sensorEvent.values[0] <= PROX_SENSOR_SENSITIVITY) {
                    //near
                    Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
                    proximity="near";
                } else {
                    //far
                    proximity="far";
                    Toast.makeText(getApplicationContext(), "far", Toast.LENGTH_SHORT).show();
                }
            }

            if (System.currentTimeMillis() - lastChange > threshold) {
                lastChange = System.currentTimeMillis();

                final Map<String, Double> acceleroMap = new HashMap<>();
                final Map<String, Double> linAcceleroMap = new HashMap<>();
                final Map<String, Double> gyroMap = new HashMap<>();
                final Map<String, Double> magnoMap = new HashMap<>();


                for (Map.Entry<String, Map<String, List<Double>>> mapEntry : recording.entrySet()) {
                    List<Double> list = mapEntry.getValue().get("magnitude");
                    Log.d(MAIN_TAG, list.toString());
                    if (!calc.IsEmpty(list)) {
                        switch (mapEntry.getKey()) {
                            case "Accelero":
                                acceleroMap.put("min", calc.Minimum(list));
                                acceleroMap.put("max", calc.Maximum(list));
                                acceleroMap.put("std", calc.StandardDev(list));
                                acceleroMap.put("mean", calc.Mean(list));
                                acceleroMap.put("mode", calc.Mode(list));
                                break;
                            case "LinAccelero":
                                linAcceleroMap.put("min", calc.Minimum(list));
                                linAcceleroMap.put("max", calc.Maximum(list));
                                linAcceleroMap.put("std", calc.StandardDev(list));
                                linAcceleroMap.put("mean", calc.Mean(list));
                                linAcceleroMap.put("mode", calc.Mode(list));
                                break;
                            case "Gyro":
                                gyroMap.put("min", calc.Minimum(list));
                                gyroMap.put("max", calc.Maximum(list));
                                gyroMap.put("std", calc.StandardDev(list));
                                gyroMap.put("mean", calc.Mean(list));
                                gyroMap.put("mode", calc.Mode(list));
                                break;
                            case "Magno":
                                magnoMap.put("min", calc.Minimum(list));
                                magnoMap.put("max", calc.Maximum(list));
                                magnoMap.put("std", calc.StandardDev(list));
                                magnoMap.put("mean", calc.Mean(list));
                                magnoMap.put("mode", calc.Mode(list));
                                break;
                        }
                    } else {
                        switch (mapEntry.getKey()) {
                            case "Accelero":
                                acceleroMap.put("min", 0.0);
                                acceleroMap.put("max", 0.0);
                                acceleroMap.put("std", 0.0);
                                acceleroMap.put("mean", 0.0);
                                acceleroMap.put("mode", 0.0);
                                break;
                            case "LinAccelero":
                                linAcceleroMap.put("min", 0.0);
                                linAcceleroMap.put("max", 0.0);
                                linAcceleroMap.put("std", 0.0);
                                linAcceleroMap.put("mean", 0.0);
                                linAcceleroMap.put("mode", 0.0);
                                break;
                            case "Gyro":
                                gyroMap.put("min", 0.0);
                                gyroMap.put("max", 0.0);
                                gyroMap.put("std", 0.0);
                                gyroMap.put("mean", 0.0);
                                gyroMap.put("mode", 0.0);
                                break;
                            case "Magno":
                                magnoMap.put("min", 0.0);
                                magnoMap.put("max", 0.0);
                                magnoMap.put("std", 0.0);
                                magnoMap.put("mean", 0.0);
                                magnoMap.put("mode", 0.0);
                                break;
                        }
                    }
                }
                resetRecording();
                DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
                    {
//
                        setValue( attributeMinAco, acceleroMap.get("min"));
                        setValue( attributeMaxAco, acceleroMap.get("max"));
                        setValue( attributeMeanAco,acceleroMap.get("mean") );
                        setValue( attributeModeAco,acceleroMap.get("mode") ) ;
                        setValue( attributeStdAco, acceleroMap.get("std") ) ;
                        
                        setValue( attributeMinGyro,gyroMap.get("min") );
                        setValue( attributeMaxGyro,gyroMap.get("max") );
                        setValue( attributeMeanGyro,gyroMap.get("mean") );
                        setValue( attributeModeGyro,gyroMap.get("mode") );
                        setValue( attributeStdGyro,gyroMap.get("std") ) ;
                        
                        setValue( attributeMinLinAco, linAcceleroMap.get("min") );
                        setValue( attributeMaxLinAco, linAcceleroMap.get("max") );
                        setValue( attributeMeanLinAco, linAcceleroMap.get("mean") );
                        setValue( attributeModeLinAco, linAcceleroMap.get("mode") ) ;
                        setValue( attributeStdLinAco, linAcceleroMap.get("std") ) ;

                    }
                };

                double predictionDouble = 0;
                //predictData=false;
                if (predictData) {
                    newInstance.setDataset(dataUnpredicted);
                    try {
                        predictionDouble = mClassifier.classifyInstance(newInstance);
                        predictedActivity = newInstance.classAttribute().value((int) predictionDouble);
                        Log.d(MAIN_TAG, "the prediction was: "+ predictionDouble + " which i think is: " + predictedActivity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    predictionDouble = -1;
                    predictedActivity = "None";
                }

                String baseInformation = "currAct= " + currActivity +
                        ", predDoub= " + predictionDouble +
                        ", predAct= " + predictedActivity;

                textView.append(baseInformation+"\n");

//
//

//                String[] data = {""};
//                writer.writeNext(data);
//
//                try {
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                result +=  baseInformation
//                        ", xmin: " + min+
//                        ", xmax: " + max +
//                        ", groupnr= " + groupNr
//                        ", acosmv= " +   acoSmv +
//                        ", linsmv= " +    linacoSmv +
//                        ", gyrosmv= "+    gyrSmv+
//                        ", xAco: " +    xAco +
//                        ", yAco: " +    yAco +
//                        ", zAco: " +    zAco +
//                        ", xLinAco: " + xLinAco +
//                        ", yLinAco: " + yLinAco +
//                        ", zLinAco: " + zLinAco +
//                        ", xGyr: " +    xGyr +
//                        ", yGyr: " +    yGyr +
//                        ", zGyr: " +    zGyr +
//                        ", xMag: " +    xMag +
//                        ", yMag: " +    yMag +
//                        ", zMag: " +    zMag
                ;


                Log.d(MAIN_TAG, "time: " + sensorEvent.timestamp + " " +result);

//                if (sendData) {
//
////
//                            "activity="+predictedActivity+
//                            "&MinAcoSMV="+acceleroMap.get("min")+
//                            "&MaxAcoSMV="+acceleroMap.get("max")+
//                            "&MeanAcoSMV="+acceleroMap.get("mean")+
//                            "&ModeAcoSMV="+acceleroMap.get("mode")+
//                            "&StdAcoSMV="+acceleroMap.get("std")+
//                            "&MinLinAcoSMV="+linAcceleroMap.get("min")+
//                            "&MaxLinAcoSMV="+linAcceleroMap.get("max")+
//                            "&MeanLinAcoSMV="+linAcceleroMap.get("mean")+
//                            "&ModeLinAcoSMV="+linAcceleroMap.get("mode")+
//                            "&StdLinAcoSMV="+linAcceleroMap.get("std")+
//                            "&MinGyroSMV="+gyroMap.get("min")+
//                            "&MaxGyroSMV="+gyroMap.get("max")+
//                            "&MeanGyroSMV="+gyroMap.get("mean")+
//                            "&ModeGyroSMV="+gyroMap.get("mode")+
//                            "&StdGyroSMV="+gyroMap.get("std")
//                }
                  if(sendData){
                      String[] myData={currActivity,
                              Double.toString(acceleroMap.get("min")),
                              Double.toString(acceleroMap.get("max")),
                              Double.toString(acceleroMap.get("mean")),
                              Double.toString(acceleroMap.get("mode")),
                              Double.toString(acceleroMap.get("std")),
                              Double.toString(linAcceleroMap.get("min")),
                              Double.toString(linAcceleroMap.get("max")),
                              Double.toString(linAcceleroMap.get("mean")),
                              Double.toString(linAcceleroMap.get("mode")),
                              Double.toString(linAcceleroMap.get("std")),
                              Double.toString(gyroMap.get("min")),
                              Double.toString(gyroMap.get("max")),
                              Double.toString(gyroMap.get("mean")),
                              Double.toString(gyroMap.get("mode")),
                              Double.toString(gyroMap.get("std")),
                      };
                      writer.writeNext(myData);
                  }


            }
        }


        @Override
        public void onAccuracyChanged (Sensor sensor,int i){

        }
    }


    public void stopScan(View view){
        stopButton.setEnabled(false);
        startButton.setEnabled(true);

        sm.unregisterListener(allsel);
        groupNr++;

        textInput.getText().clear();
        textInput.getText().insert(0, Integer.toString(groupNr));
    }

    public void endApp(View view) {
        finish();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetRecording() {
        for (Map.Entry<String, Map<String, List<Double>>> mapEntry : recording.entrySet()) {
            for (String attribute : mapEntry.getValue().keySet()) {
                mapEntry.getValue().put(attribute, new ArrayList<Double>());
            }
            recording.put(mapEntry.getKey(), mapEntry.getValue());
        }
    }

    public double magnitude(double x, double y, double z) {
        return Math.sqrt(x*x + y*y + z*z);
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
}
