`Ver esta guía en otros idiomas:`  [![DSTracker](https://img.shields.io/badge/DSTracker%20Integration-English-success)](https://github.com/DriveSmart-MobileTeam/dstracker_lite_integration_sample/blob/main/README.md)
[![DSTracker](https://img.shields.io/badge/DSTracker%20Integration-Java-success)](https://github.com/DriveSmart-MobileTeam/dstracker_lite_integration_sample/blob/main/README-JAVA-ES.md)

# Integración de DSTracker (Kotlin)

En esta guía de inicio rápido, se describe cómo configurar la librería de Drive-Smart en tu app para que puedas evaluar los viajes realizados a través de dispositivos Android.

La configuración de la librería de Drive-Smart requiere realizar tareas en el IDE. Para finalizar la configuración, deberás realizar un viaje de prueba a fin de confirmar el funcionamiento correcto del entorno.


## Requisitos
Si aún no lo has hecho, descarga e instala el entorno de desarrollo y las librerias de Android. La integración se realizará sobre la siguiente versión:
* Android Studio Artic Fox | 2020.3.1
* Runtime version: 11.0.10+
* Gradle 7.0+
* En tu IDE aseguraté de tener configurado JAVA 11

![java11.jpg](https://i.imgur.com/2IcZ1Tv.jpeg)

## Instalación

* En el archivo `settings.gradle` de **nivel de proyecto**, agrega el complemento de Maven con la licencia de Drive-Smart para Gradle como dependencia.

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
* En tu archivo `build.gradle` de **nivel de app**, aplica el complemento del SDK para Gradle:

```
dependencies {
	// ......
	implementation 'DriveSmart:DS-Tracker:1.0'
  	// ......
}
```


## Permisos

Es necesario definir los permisos correspondientes, en caso contrario la libreria responderá distintos mensajes de error.

Permisos en `Manifest` del proyecto:

```
<!-- ... -->

<!-- Servicio para creación/consulta usuario -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

<!-- Evaluación de viajes -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- ... -->
```
Permisos de ubicación que deben consultarse y estar activos en las clases del proyecto.
```
// ...
Manifest.permission.ACCESS_COARSE_LOCATION
Manifest.permission.ACCESS_FINE_LOCATION
// ...
```

Además de los permisos básicos para la evaluación de viajes indicados previamente, es necesario el siguiente permiso para poder activar la grabación automática de viajes:

```
// ...
Manifest.permission.ACCESS_BACKGROUND_LOCATION
// ...
```

Si todos los permisos indicados están correctamente configurados, el entorno estará configurado y se podrán realizar viajes.



## Configuración
* En el archivo `Java o Kotlin` del **proyecto**, agrega el objeto principal de la libreria e inicializa:

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

## Vinculación de usuarios
Para que la librería de Drive-Smart pueda crear viajes se necesita un identificador de usuario *único.*

```kotlin
// ... 
dsTrackerLite.setUserId(uid) {
            addLog("Defining USER ID: $uid")
        }
// ... 
```

Para obtener un identificador de usuario válido, se puede consultar el siguiente servicio, el cual creará un nuevo usuario en el sistema de Drive-Smart o devolverá el usuario en caso de existir.

```kotlin
private fun getOrAddUser(user: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val session = getUserSession(user)

            userSession = session
            addLog("User id created: $session")
            }
    }
```

Si el objeto recibido es valido, a continuación, se debe definir el userID en el método de la librería ya comentado


## Paso 4: Análisis de viajes

Para iniciar un viaje es preciso incluir el método del SDK *start(String)* en un servicio.
```kotlin
//...
dsTrackerLite.start(partnerMetaData)
//...
```

Una vez el viaje finalice, según el ciclo de vida del servicio, se debe llamar al metodo *stop()* para finalizar el análisis de viaje.
```kotlin
//...
dsTrackerLite.stop()
//...
```
Los eventos se envían tal y como se van recogiendo, pero es posible que se acabe la recogida de información y no se hayan enviado todos los eventos. Esto puede ocurrir, por ejemplo, en momentos en los que el dispositivo no tiene conexión a internet.

Para forzar el envío del viaje a los servidores para su procesado es necesario invocar el método:
*upload(this)*;
```kotlin
//...
dsTrackerLite.upload(service)
//...
```

### Información del viaje:
Una vez iniciado un viaje, DSTracker ofrece un de método para poder obtener información del viaje. *TrackingStatus* se obtiene a través del método *getStatus()* con la información:
+ Distancia total
+ Tiempo de viaje.
+ Trip ID
+ Estado del GPS.
+ Estado del viaje.


