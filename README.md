See this guide in other languages:  [![DSTracker](https://img.shields.io/badge/DSTracker%20Integration-Spanish-success)](https://github.com/DriveSmart-MobileTeam/dstracker_lite_integration_sample/blob/main/README-ES.md)

This quick start guide describes how to configure the DriveSmart Tracker library in your app so that you can evaluate the driving activity tracked by Android devices.

The configuration of DriveSmart Tracker library requires IDE tasks. To finish the setup, you will need to perform a driving test to confirm the correct operation of the environment.

## Requirements
If you haven't already, download and install the Android Development Environment and libraries. The integration will be carried out on the next versions:
* Android Studio Artic Fox | 2020.3.1
* Runtime version: 11.0.10+
* Gradle 7.0+
* In your IDE make sure you have Java 11 configured

![java11.jpg](https://i.imgur.com/2IcZ1Tv.jpeg)

## Installation

* In the **project level** `settings.gradle` filede, add the Maven plugin with the DriveSmart License for Gradle as a dependency.

  ```yaml
  dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
          google()
          mavenCentral()
          maven {
              url 'https://tfsdrivesmart.pkgs.visualstudio.com/5243836b-8777-4cb6-aded-44ab518bc748/_packaging/Android_Libraries/maven/v1'
              name 'Android_Libraries'
              credentials {
                  username "user"
                  password "password"
              }
          }
      }
  }
  ```
* In the **app level** `build.gradle` file, apply the SDK plugin for Gradle:

```
dependencies {
	// ......
	implementation 'DriveSmart:DS-SDK:5.20.31'
  	// ......
}
```


## Permissions

It is necessary to define the corresponding permissions, otherwise the library will respond with different error messages.

Project Permissions in `Manifest`:

```
<!-- ... -->
<!-- Services for user creation/query -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

<!-- Trip evaluation -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- Automatic trip evaluation -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<!-- ... -->
```
Location permissions to be queried and be active in project classes.
```
// ...
Manifest.permission.ACCESS_COARSE_LOCATION
Manifest.permission.ACCESS_FINE_LOCATION
// ...
```

In addition to the basic permissions for the evaluation of trips indicated previously, the following permission is mandatory to be able to activate the automatic recording of trips:

```
// ...
Manifest.permission.ACCESS_BACKGROUND_LOCATION
// ...
```

Finally, confirm that the app is not included as an optimized app. To do this, you can check the status with the following code extract:

```javascript
// ...
PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
boolean isbatteryOptimized = powerManager.isDeviceIdleMode() && !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
// ...
```

If all the permissions indicated are correctly configured, the environment will be configured and trips can be made.



## Configuration
* In the **project** `Java or Kotlin` file, add the library main object and initialize it:

  ```java
  // ...
  private DSTrackerLite dsTrackerLite;
  // ...
  
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
  	super.onViewCreated(view, savedInstanceState);
    	// ...
    	dsTrackerLite = DSTrackerLite.getInstance(requireActivity());
    		dsTrackerLite.configure(apkID, dsResult -> {
            	if (dsResult instanceof Success) {
              	Log.e("DRIVE-SMART", "DSTracker configured");          
                }else{
                	String error = ((DSResult.Error) dsResult).getError().getDescription();
                	Log.e("DRIVE-SMART", error);
                }
            	return null;
              });
          }
  	// ...
  }
  ```

## User linking
A unique user identifier is required for the DriveSmart Library to create trips.

```javascript
// ... 
dsManager.setUserId(USERID, result -> {
    Log.e("DRIVE-SMART", "Defining USER ID: " + USERID);          
    return null;
});
// ... 
```

To obtain a valid user identifier, the following service can be consulted, whitch will create a new user in the DriveSmart System or return the user if it exist.

```javascript
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
```

If the received object is valid, then the userId must be defined in the library method already commented.


## Step 4: Trip analysis

To start a trip, you must include the SDK method *start(String)* in a service

*Es necesario declarar en el Manifest del proyecto el servicio que usará la librería de Drive-Smart*
```
//...
dsTrackerLite.start(partnerMetaData);
//...
```

Once the trip ends, according to the lifecycle of the service, the *stop()* method must be called to end the trip analysis.
```
//...
dsTrackerLite.stop();
//...
```
To send the trip to the servers for processing, it is necessary to invoke the method *upload(this)*.
```
//...
dsTrackerLite.upload(service);
//...
```

### Trip info:
Once a trip has started, DSTracker offers a method for obtaining trip information. *TrackingStatus* is obtained throught the *getStatus()* method with the info:
+ Total distance
+ Trip time
+ Trio id
+ GPS Status
+ Trip Status


