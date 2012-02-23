Adwhirl Features
=================


 - send device hash to system.
 - get locale string for fetching configuration from server.
 - create connection to various web service api.
   - fetching configuration
   - fetching images for custom ads.

 - cache the configuration as json string in the SharedPreference.
 - get current location.
 - rotate ads
 - send impression and click information to server.

 - generate platform-specific AdView and push this adview on the top of AdUnitLayout.


Development Tips
================

Why jar project instead of apklib
---------------------------------

Ths Locadz Android SDK is released as a jar project instead of apklib. The apklib is simply a source package. If we release 
Locadz Android SDK as apklib, we have to include all 3rd party SDK libraries, which, we couldn't and shouldn't do due to 
technical and licensing issues.

As a result, the Locadz Android SDK is released as a jar project. 

