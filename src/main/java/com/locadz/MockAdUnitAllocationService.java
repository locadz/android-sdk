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
import com.adwhirl.util.AdWhirlUtil;
import com.locadz.model.AdUnitAllocation;
import com.locadz.model.Color;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Mock ConfigService for testing use. */
public final class MockAdUnitAllocationService extends IntentService {

    public MockAdUnitAllocationService() {
        super(MockAdUnitAllocationService.class.getCanonicalName());
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

    AdUnitAllocation getAdUnitAllocation(AdUnitContext adUnitContext) {
        Extra extra = new Extra(true, Color.White, Color.Black, 30000, 1);
        String adUnitId = adUnitContext.getAdUnitId();
        Ration firstRation = new Ration(adUnitId, "network1", AdWhirlUtil.NETWORK_TYPE_ADMOB, 50, 0, "a14ec33314d3549");
        Ration secondRation = new Ration(adUnitId, "network2", AdWhirlUtil.NETWORK_TYPE_MDOTM, 50, 0, "9df678f13e85ead9eda875e8007ab395");
        return new AdUnitAllocation(extra, Arrays.asList(firstRation, secondRation));
    }

    public static Intent createIntent(Context context, AdUnitContext adUnitContext) {
        Intent ret = new Intent(context, MockAdUnitAllocationService.class);
        ret.putExtra(IntentConstants.EXTRA_ADUNIT_CONTEXT, adUnitContext);
        return ret;
    }
}
