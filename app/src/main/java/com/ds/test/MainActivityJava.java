package com.ds.test;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.drivesmartsdk.enums.DSResult;
import com.drivesmartsdk.models.TrackingStatus;
import com.drivesmartsdk.singleton.DSTrackerLite;
import com.ds.test.databinding.ActivityMainBinding;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class MainActivityJava extends AppCompatActivity{

    private ActivityMainBinding binding;
    private DSTrackerLite dsTrackerLite;

    private String apkID;
    private String userID;
    private Handler handlerTrip;
    private String userSession="";


    public static final String[] PERMISSIONS_GPS = { Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] PERMISSIONS_GPS_AUTO = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    private void defineConstants() {
        apkID = "";
        userID = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        setContentView(binding.getRoot());

        defineConstants();
        prepareEnvironment();

        prepareView();
    }

    private void prepareView() {
        handlerTrip = new Handler(getMainLooper());
        binding.logText.setMovementMethod(new ScrollingMovementMethod());

        checkPerms();
        binding.checkPermButton.setOnClickListener(view -> checkPerms());

        binding.startTripButton.setOnClickListener(view -> {
            Intent serviceIntent = new Intent(MainActivityJava.this, LiteServiceJava.class);
            serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
            serviceIntent.putExtra("PMD", "PMD");
            startService(serviceIntent);

        });
        binding.stopTripButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivityJava.this, LiteServiceJava.class);
            stopService(intent);

            handlerTrip.removeCallbacks(updateTimerThread);

        });
        binding.setUserButton.setOnClickListener(view -> {
            if(!userSession.isEmpty()) {
                identifyEnvironmet(userSession);
            }else{
                addLog("no user-session info");
            }
        });
        binding.getUserButton.setOnClickListener(view -> {
            if(!binding.userId.getText().toString().isEmpty()) {
                getOrAddUser(binding.userId.getText().toString());
            }
        });
    }

    private void checkPerms() {
        if(UtilsMethods.isDozing(this)){
            binding.permBatteryStatus.setText(getString(R.string.optimized));
        }else{
            binding.permBatteryStatus.setText(getString(R.string.no_optimized));
        }

        if(UtilsMethods.isGPSEnabled(this)){
            binding.permGpsStatus.setText(getString(R.string.enabled));
        }else{
            binding.permGpsStatus.setText(getString(R.string.disabled));
        }

        if(UtilsMethods.checkPermissions(this, PERMISSIONS_GPS)){
            binding.permGpsSemiStatus.setText(getString(R.string.ok));
        }else{
            binding.permGpsSemiStatus.setText(getString(R.string.nok));
        }

        if(UtilsMethods.checkPermissions(this, PERMISSIONS_GPS_AUTO)){
            binding.permGpsAutoStatus.setText(getString(R.string.ok));
        }else{
            binding.permGpsAutoStatus.setText(getString(R.string.nok));
        }
    }

    private void getOrAddUser(String user) {
        dsTrackerLite.getOrAddUserIdBy(user, new Continuation<String>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                if(o instanceof String){
                    userSession = o.toString();
                    addLog("User id created: " + o.toString());
                }
            }
        });
    }

    private void prepareEnvironment() {
        dsTrackerLite = DSTrackerLite.getInstance(MainActivityJava.this);
        dsTrackerLite.configure(apkID, dsResult -> {
            if (dsResult instanceof DSResult.Success) {
                addLog("DSTracker configured");
                identifyEnvironmet(userID);
            }else{
                String error = ((DSResult.Error) dsResult).toString();
                addLog("Configure DSTracker: "+error);
            }
            return null;
        });
    }

    private void identifyEnvironmet(String uid) {
        dsTrackerLite.setUserId(uid, result -> {
            addLog("Defining USER ID: " + uid);
            return null;
        });
    }

    // ****************************************v****************************************
    // ******************************* Client Stuff ************************************
    // ****************************************v****************************************

    private void addLog(String text) {
        binding.logText.append("\n" + text);
    }

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            TrackingStatus beanStatus = dsTrackerLite.getStatus();

            addLog("Timer: " + convertMillisecondsToHMmSs(beanStatus.getServiceTime()));
            addLog("Distance: " + beanStatus.getTotalDistance());

            handlerTrip.postDelayed(this, 2000);
        }
    };

    private String convertMillisecondsToHMmSs(Long millisenconds) {
        Long seconds = millisenconds / 1000;
        Long s = seconds % 60;
        Long m = seconds / 60 % 60;
        Long h = seconds / (60 * 60) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
    // ****************************************v****************************************
    // ******************************* Client Stuff ************************************
    // ****************************************v****************************************
}
