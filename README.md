# Installation

1. The PHP scripts in "Source Code/Server/" have to be uploaded to a webserver,
and a database has to be configured in `mysqli_connect.php` and have 
`devices.sql` imported via phpMyAdmin.

2. Import the project to [Android Studio IDE](https://developer.android.com/studio/) and compile it.
Prior to compilation, it is essential to properly configure the `baseUrl` attribute in `GlobalHelpClass` of the Android app to point to the correct domain and directory where the scripts were uploaded. For example:

```sh
baseUrl = “http://mydomain.com/radar/”; 
```

3. Users must enable the **“unknown sources”** or **“mock locations”** setting at their 
Android device's settings, which can be found in the **“Developer options”**. 
Having done that, they can proceed to install the amt.apk and run the application.