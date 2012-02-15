/*
 Copyright 2009-2010 AdMob, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.adwhirl.adapters;

import android.app.Activity;
import android.util.Log;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;
import com.zestadz.android.AdManager;
import com.zestadz.android.ZestADZAdView;
import com.zestadz.android.ZestADZAdView.ZestADZListener;

public class ZestAdzAdapter extends AdWhirlAdapter implements ZestADZListener {

    public ZestAdzAdapter(AdUnitLayout locadzLayout, Ration ration, Extra extra) {
        super(locadzLayout, ration, extra);
    }

    @Override
    public void handle() {
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        try {
            AdManager.setadclientId(ration.getKey());
        }
        // Thrown on invalid client id.
        catch (IllegalArgumentException e) {
            locadzLayout.submitReloadAdRequest();
            return;
        }

        try {
            Activity activity = locadzLayout.getActivity();
            if (activity == null) {
                return;
            }

            ZestADZAdView adView = new ZestADZAdView(activity);
            adView.setListener(this);
            adView.displayAd();
        } catch (Exception e) {
            locadzLayout.submitReloadAdRequest();
        }
    }

    // This block contains the ZestADZ listeners

    /** *************************************************************** */
    public void AdReturned(ZestADZAdView adView) {
        Log.d(LocadzUtils.LOGID, "ZestADZ success");

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        locadzLayout.submitPushSubViewRequest(adView);
    }

    public void AdFailed(ZestADZAdView adView) {
        Log.d(LocadzUtils.LOGID, "ZestADZ failure");

        adView.setListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        locadzLayout.submitReloadAdRequest();
    }

}
