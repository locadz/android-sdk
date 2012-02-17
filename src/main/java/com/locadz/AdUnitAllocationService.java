/*
 * Copyright 2012. Blue Tang Studio LLC.
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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.locadz.model.AdUnitAllocation;
import com.locadz.model.Ration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Service that retrieve the ad unit allocations from external source and cache locally in SharedPreference. */
public final class AdUnitAllocationService extends IntentService {

    private static final int CACHE_EXPIRATION_PERIOD = 30 * 60 * 1000; // 30 minutes.

    private final static String PREFS_STRING_TIMESTAMP = "timestamp";
    private final static String PREFS_STRING_CONFIG = "config";

    public AdUnitAllocationService() {
        super(AdUnitAllocationService.class.getCanonicalName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AdUnitContext adUnitContext = (AdUnitContext) intent.getParcelableExtra(IntentConstants.EXTRA_ADUNIT_CONTEXT);

        AdUnitAllocation adUnitAllocation = getAdUnitAllocation(adUnitContext);

        if (adUnitAllocation != null) {
            Ration ration = getRandomRation(adUnitAllocation.getRations());

            // send response through broadcast mechanism.
            // all active LocadzLayout will receive this message, they have to filter message by themselves.
            Intent response = new Intent(IntentConstants.ACTION_SHOW_AD);

            response.putExtra(IntentConstants.EXTRA_ADUNIT_ID, adUnitContext.getAdUnitId());
            response.putExtra(IntentConstants.EXTRA_RATION, ration);
            response.putExtra(IntentConstants.EXTRA_EXTRA, adUnitAllocation.getExtra());

            sendBroadcast(response);
        }
    }

    /**
     * Select a random ration form the provided rations.
     * @param rations   the candidates.
     * @return a random ration from the candidates.
     */
    private Ration getRandomRation(List<Ration> rations) {

        Random random = new Random();
        int targetWeight = random.nextInt(100);
        int c = 0;
        Ration ret = null;
        Iterator<Ration> it = rations.iterator();

        while (it.hasNext()) {
            ret = it.next();
            c += ret.getWeight();
            if (c >= targetWeight) {
                break;
            }
        }

        return ret;
    }

    /**
     * Get the allocation configuration for the adunit.
     * @param adUnitContext the context of the adunit.
     * @return the allocation configuration for the adunit.
     */
    AdUnitAllocation getAdUnitAllocation(AdUnitContext adUnitContext) {
        String jsonString = loadFromSharedPreferences(adUnitContext);

        if (jsonString == null || "".equals(jsonString)) {
            jsonString = loadFromRemote(adUnitContext);
            if (jsonString != null) {
                writeSharedPreferences(adUnitContext, jsonString);
            }
        }
        try {
            if (jsonString != null) {
                return SerializationUtils.fromJson(jsonString, AdUnitAllocation.class);
            }
        } catch (IOException e) {
            Log.d(LocadzUtils.LOG_TAG, "Failed to de-serialize json config.", e);
        }
        return null;
    }

    /**
     * load the allocation configuration for the adunit from SharedPreferences.
     *
     * @param adUnitContext the context of the adunit.
     * @return the allocation configuration for the adunit.
     */
    String loadFromSharedPreferences(AdUnitContext adUnitContext) {
        SharedPreferences perfs = this.getApplicationContext()
            .getSharedPreferences(adUnitContext.getAdUnitId(), Context.MODE_PRIVATE);

        long lastUpdateTime = perfs.getLong(PREFS_STRING_TIMESTAMP, 0);
        if (System.currentTimeMillis() - lastUpdateTime <= CACHE_EXPIRATION_PERIOD) {
            return perfs.getString(PREFS_STRING_CONFIG, "");
        }
        return null;
    }

    /**
     * write the allocation configuration for the adunit to SharedPreferences.
     *
     * @param adUnitContext the context of the adunit.
     * @param config        the configuration as a json String.
     *
     */
    void writeSharedPreferences(AdUnitContext adUnitContext, String config) {
        SharedPreferences perfs = this.getApplicationContext()
            .getSharedPreferences(adUnitContext.getAdUnitId(), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = perfs.edit();
        editor.putString(PREFS_STRING_CONFIG, config);
        editor.putLong(PREFS_STRING_TIMESTAMP, System.currentTimeMillis());
        editor.commit();
    }

    /**
     * load the allocation configuration for the adunit from external source.
     *
     * @param adUnitContext the context of the adunit.
     * @return the allocation configuration for the adunit as a json string.
     */
    String loadFromRemote(AdUnitContext adUnitContext) {

        Log.d(LocadzUtils.LOG_TAG, String.format("Fetching config with %s", adUnitContext));

        HttpClient httpClient = HttpClientFactory.getInstance();
        URI uri = LocadzUtils.getInfoUri(adUnitContext);
        HttpGet httpGet = new HttpGet(uri);

        String ret = null;
        try {

            HttpResponse httpResponse = httpClient.execute(httpGet);

            // if response is 1xx, 2xx or 3xx, we would return the response body
            if (httpResponse.getStatusLine().getStatusCode() < HttpStatus.SC_BAD_REQUEST) {
                Log.d(LocadzUtils.LOG_TAG, httpResponse.getStatusLine().toString());

                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    ret = EntityUtils.toString(entity);

                }
            }
        } catch (ClientProtocolException e) {
            Log.e(LocadzUtils.LOG_TAG, "Caught ClientProtocolException in loadFromRemote()", e);
        } catch (IOException e) {
            Log.e(LocadzUtils.LOG_TAG, "Caught IOException in loadFromRemote()", e);
        }
        return ret;
    }

    /**
     * Create an intent that will trigger this service.
     * @param context       the context to send the intent to.
     * @param adUnitContext the adUnitContext for the receiving {@link AdUnitLayout}.
     *
     * @return an intent that will trigger this service.
     */
    public static Intent createIntent(Context context, AdUnitContext adUnitContext) {
        Intent ret = new Intent(context, AdUnitAllocationService.class);
        ret.putExtra(IntentConstants.EXTRA_ADUNIT_CONTEXT, adUnitContext);
        return ret;
    }
}
