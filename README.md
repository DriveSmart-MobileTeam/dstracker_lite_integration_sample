`See this guide in other languages:`  [![DSTracker](https://img.shields.io/badge/DSTracker%20Integration-Spanish-success)](https://github.com/DriveSmart-MobileTeam/dstracker_lite_integration_sample/blob/main/README-ES.md)
[![DSTracker](https://img.shields.io/badge/DSTracker%20Integration-Java-success)](https://github.com/DriveSmart-MobileTeam/dstracker_lite_integration_sample/blob/main/README-JAVA.md)

# DSTracker Integration (Kotlin)

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

* In the **project level** `settings.gradle` file, add the Maven plugin with the DriveSmart License for Gradle as a dependency.

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
	implementation 'DriveSmart:DS-Tracker:1.0'
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
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

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

If all the permissions indicated are correctly configured, the environment will be configured and trips can be made.



## Configuration
* In the **project** file, add the library main object and initialize it:

  ```kotlin
    // ...
    private DSTrackerLite dsTrackerLite
    private lateinit var apkID: String
    private lateinit var userID: String
    // ...
  
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
        defineConstants()
        prepareEnvironment() 
        // ...
    }
  
    private fun defineConstants() {
        apkID = ""
        userID = ""
    }
  
    private fun prepareEnvironment() {
        dsTrackerLite = DSTrackerLite.getInstance(this)
        dsTrackerLite.configure(apkID) { dsResult: DSResult ->
            if (dsResult is DSResult.Success) {
                addLog("DSTracker configured")
                identifyEnvironmet(userID)
            } else {
                val error: String = dsResult.toString()
                addLog("Configure DSTracker: $error")
            }
        }
    }
  
    // ...
  ```

## User linking
A unique user identifier is required for the DriveSmart Library to create trips.

```kotlin
// ... 
dsTrackerLite.setUserId(uid) {
            addLog("Defining USER ID: $uid")
        }
// ... 
```

To obtain a valid user identifier, the following service can be consulted, whitch will create a new user in the DriveSmart System or return the user if it exist.

```kotlin
private fun getOrAddUser(user: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val session = getUserSession(user)

            userSession = session
            addLog("User id created: $session")
            }
    }
```

If the received object is valid, then the userId must be defined in the library method already commented.


## Step 4: Trip analysis

To start a trip, you must include the SDK method *start(String)* in a service

* It is necessary to declare in the project's Manifest the service that the Drive-Smart library will use *

```kotlin
//...
dsTrackerLite.start(partnerMetaData)
//...
```

Once the trip ends, according to the lifecycle of the service, the *stop()* method must be called to end the trip analysis.
```kotlin
//...
dsTrackerLite.stop()
//...
```

The events are sent as they are collected, but it is that the tracking is finished and not all the events have been sent. This can happen, for example, when the device does not have internet connection.

To force send the trip to the servers for processing, it is necessary to invoke the method *upload()*.
```kotlin
//...
dsTrackerLite.upload()
//...
```

## DSManagerInterface

We can also see through the DSManagerInterface the results of the start() and stop() calls.

```
interface DSManagerInterface:DSMotionManagerInterface {
fun startService( result: DSResult)
fun stopService(result: DSResult)
fun statusEventService(result: DSResult)
}
```

### Trip info:
Once a trip has started, DSTracker offers a method for obtaining trip information. *TrackingStatus* is obtained throught the *getStatus()* method with the info:
+ Total distance
+ Trip time
+ Trio id
+ GPS Status
+ Trip Status


