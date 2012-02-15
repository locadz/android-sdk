/*
 Copyright 2011 Google, Inc.

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
import android.content.Context;
import android.util.Log;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.util.AdWhirlUtil;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.text.SimpleDateFormat;

public class GoogleAdMobAdsAdapter extends AdWhirlAdapter implements AdListener {

    private AdView adView;

    public GoogleAdMobAdsAdapter(AdUnitLayout locadzLayout, Ration ration, Extra extra) {
        super(locadzLayout, ration, extra);
    }

    protected String birthdayForAdWhirlTargeting() {
        return (AdWhirlTargeting.getBirthDate() != null) ?
            new SimpleDateFormat("yyyyMMdd").
                format(AdWhirlTargeting.getBirthDate().getTime()) : null;
    }

    protected AdRequest.Gender genderForAdWhirlTargeting() {
        switch (AdWhirlTargeting.getGender()) {
            case MALE:
                return AdRequest.Gender.MALE;
            case FEMALE:
                return AdRequest.Gender.FEMALE;
            default:
                return null;
        }
    }

    @Override
    public void handle() {
        AdUnitLayout locadzLayout = getLocadzLayout();

        if (locadzLayout == null) {
            return;
        }

        Activity activity = locadzLayout.getActivity();
        if (activity == null) {
            return;
        }

        adView = new AdView(activity, AdSize.BANNER, ration.getKey());

        adView.setAdListener(this);
        adView.loadAd(requestForAdWhirlLayout(locadzLayout));
    }

    @Override
    public void willDestroy() {
        log("AdView will get destroyed");
        if (adView != null) {
            adView.destroy();
        }
    }

    protected void log(String message) {
        Log.d(LocadzUtils.LOGID, "GoogleAdapter " + message);
    }

    protected AdRequest requestForAdWhirlLayout(AdUnitLayout layout) {
        AdRequest result = new AdRequest();

        if (AdWhirlTargeting.getTestMode()) {
            result.addTestDevice(AdRequest.TEST_EMULATOR);
        }

        if (AdWhirlTargeting.getTestMode()) {
            Activity activity = layout.getActivity();
            if (activity != null) {
                Context context = activity.getApplicationContext();
                String deviceId = AdWhirlUtil.getEncodedDeviceId(context);
                result.addTestDevice(deviceId);
            }
        }
        result.setGender(genderForAdWhirlTargeting());
        result.setBirthday(birthdayForAdWhirlTargeting());

        if (getExtra().isLocationOn()) {
            result.setLocation(layout.getAdUnitContext().getLocation());
        }

        result.setKeywords(AdWhirlTargeting.getKeywordSet());

        return result;
    }

    @Override
    public void onDismissScreen(Ad arg0) {
    }

    @Override
    public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
        log("failure (" + arg1 + ")");

        arg0.setAdListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();

        if (locadzLayout == null) {
            return;
        }

        locadzLayout.submitReloadAdRequest();
    }

    @Override
    public void onLeaveApplication(Ad arg0) {
    }

    @Override
    public void onPresentScreen(Ad arg0) {
    }

    @Override
    public void onReceiveAd(Ad arg0) {
        log("success");

        AdUnitLayout locadzLayout = getLocadzLayout();

        if (locadzLayout == null) {
            return;
        }

        if (!(arg0 instanceof AdView)) {
            log("invalid AdView");
            return;
        }

        AdView adView = (AdView) arg0;
        locadzLayout.submitPushSubViewRequest(adView);
    }
}
