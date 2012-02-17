/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.locadz;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Helper class to encapsulate some of the common code needed to determine the
 * current location using FINE (GPS) provider. 
 * <p/>
 * If the most recent location is available for the FINE provider, and it is relatively
 * recent (within FIX_RECENT_BUFFER_TIME -- currently 30 seconds), it is returned back to
 * the caller using a Message indicating the results. 
 * <p/>
 * IF the most recent location is either not available, or too old to be used, the
 * a LocationListener is kicked off for a specified duration. Once the LocationListener 
 * either gets a good Location update, or the time is elapsed, a Message is sent back 
 * to the caller indicating the results.
 * <p/>
 * Example usage from an Activity:
 * <p/>
 * <pre>
 *     
 *     Handler handler = new Handler() {
 *        public void handleMessage(Message m) {
 *           Log.d(LOG_TAG, "Handler returned with message: " + m.toString());
 *           if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_FOUND) {
 *              Toast.makeText(Activity.this, "HANDLER RETURNED -- lat:" + m.arg1 + " lon:" + m.arg2, Toast.LENGTH_SHORT)
 *                       .show();
 *           } else if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_NULL) {
 *              Toast.makeText(Activity.this, "HANDLER RETURNED -- unable to get location", Toast.LENGTH_SHORT).show();
 *           } else if (m.what == LocationHelper.MESSAGE_CODE_PROVIDER_NOT_PRESENT) {
 *              Toast.makeText(Activity.this, "HANDLER RETURNED -- provider not present", Toast.LENGTH_SHORT).show();
 *           }
 *        }
 *     };
 *     
 *     LocationHelper helper = new LocationHelper(locationManager, handler, LOG_TAG);
 *     helper.getCurrentLocation(handler); 
 * </pre> 
 * 
 * @author ccollins
 */
public class LocationHelper {

   public static final int MESSAGE_CODE_LOCATION_FOUND = 1;
   public static final int MESSAGE_CODE_LOCATION_NULL = 2;
   public static final int MESSAGE_CODE_PROVIDER_NOT_PRESENT = 3;   

   private static final int FIX_RECENT_BUFFER_TIME = 30000;

   private LocationManager locationMgr;
   private LocationListener locationListener;
   private Handler handler;
   private Runnable handlerCallback;
   private String providerName;
   private String logTag;
   
   /**
    * Construct with a LocationManager, and a Handler to pass back Messages via.
    * 
    * @param locationMgr
    * @param handler
    */
   public LocationHelper(LocationManager locationMgr, Handler handler, String logTag) {
      this.locationMgr = locationMgr;
      this.locationListener = new LocationListenerImpl();
      this.handler = handler;      
      this.handlerCallback = new Thread() {
         public void run() {
            endListenForLocation(null);
         }
      };

      Criteria criteria = new Criteria();
      // use Criteria to get provider (and could use COARSE, but doesn't work in emulator)
      // (FINE will use EITHER network/gps, whichever is the best enabled match, except in emulator must be gps)
      // (NOTE: network won't work unless enabled - Settings->Location & Security Settings->Use wireless networks)
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
      this.providerName = locationMgr.getBestProvider(criteria, true);
      
      this.logTag = logTag;
   }

   /**
    * Invoke the process of getting the current Location.
    * Expect Messages to be returned via the Handler passed in at construction with results.
    * 
    * @param durationSeconds amount of time to poll for location updates
    */
   public void getCurrentLocation(int durationSeconds) {

      if (this.providerName == null) {
         // return 2/0/0 if provider is not enabled
         Log.d(logTag, "Location provideName null, provider is not enabled or not present.");
         sendLocationToHandler(MESSAGE_CODE_PROVIDER_NOT_PRESENT, null);
         return;
      }

      // first check last KNOWN location (and if the fix is recent enough, use it)
      // NOTE -- this does NOT WORK in the Emulator
      // (if you send a DDMS "manual" time or geo fix, you get correct DATE, 
      // but fix time starts at 00:00 and seems to increment by 1 second each time sent)
      // to test this section (getLastLocation being recent enough), you need to use a real device
      Location lastKnown = locationMgr.getLastKnownLocation(providerName);
      if (lastKnown != null && lastKnown.getTime() >= (System.currentTimeMillis() - FIX_RECENT_BUFFER_TIME)) {
         Log.d(logTag, "Last known location recent, using it: " + lastKnown.toString());
         // return lastKnown lat/long on Message via Handler
         sendLocationToHandler(MESSAGE_CODE_LOCATION_FOUND, lastKnown);
      } else {
         // last known is relatively old, or doesn't exist, use a LocationListener 
         // and wait for a location update for X seconds
         Log.d(logTag, "Last location NOT recent, setting up location listener to get newer update.");
         listenForLocation(providerName, durationSeconds);
      }
   }

   private void sendLocationToHandler(int msgId, Location location) {
      Message msg = Message.obtain(handler, msgId, location);
      handler.sendMessage(msg);
   }

   private void listenForLocation(String providerName, int durationSeconds) {
      locationMgr.requestLocationUpdates(providerName, 0, 0, locationListener);
      handler.postDelayed(handlerCallback, durationSeconds * 1000);
   }

   private void endListenForLocation(Location loc) {
      locationMgr.removeUpdates(locationListener);
      handler.removeCallbacks(handlerCallback);
      if (loc != null) {
         sendLocationToHandler(MESSAGE_CODE_LOCATION_FOUND, loc);
      } else {
         sendLocationToHandler(MESSAGE_CODE_LOCATION_NULL, null);
      }
   }

   private class LocationListenerImpl implements LocationListener {
      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {
         Log.d(logTag, "Location status changed to:" + status);
         switch (status) {
            case LocationProvider.AVAILABLE:
               break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
               break;
            case LocationProvider.OUT_OF_SERVICE:
               endListenForLocation(null);
         }
      }

      @Override
      public void onLocationChanged(Location loc) {         
         if (loc == null) {
            return;
         }
         Log.d(logTag, "Location changed to:" + loc.toString());
         endListenForLocation(loc);
      }

      @Override
      public void onProviderDisabled(String provider) {
         endListenForLocation(null);
      }

      @Override
      public void onProviderEnabled(String provider) {
      }
   }
}