package com.adwhirl.adapters;

import android.app.Activity;
import android.util.Log;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlTargeting.Gender;
import com.inmobi.androidsdk.IMAdListener;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdRequest.ErrorCode;
import com.inmobi.androidsdk.IMAdRequest.GenderType;
import com.inmobi.androidsdk.IMAdView;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.util.HashMap;
import java.util.Map;

/**
 * An adapter for the InMobi Android SDK.
 * <p/>
 * Note: The InMobi site Id is looked up using ration.key
 */

public final class InMobiAdapter extends AdWhirlAdapter implements IMAdListener {

    public int adUnit = IMAdView.INMOBI_AD_UNIT_320X50; //default size 15

    public InMobiAdapter(AdUnitLayout locadzLayout, Ration ration, Extra extra) {
        super(locadzLayout, ration, extra);
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

        IMAdView adView = new IMAdView(activity, adUnit, ration.getKey());
        adView.setIMAdListener(this);
        IMAdRequest imAdRequest = new IMAdRequest();
        imAdRequest.setAge(AdWhirlTargeting.getAge());
        imAdRequest.setGender(this.getGender());
        imAdRequest.setLocationInquiryAllowed(extra.isLocationOn());
        imAdRequest.setTestMode(AdWhirlTargeting.getTestMode());
        imAdRequest.setKeywords(AdWhirlTargeting.getKeywords());
        imAdRequest.setPostalCode(AdWhirlTargeting.getPostalCode());

        // Setting tp key based on InMobi's implementation of this adapter.
        Map<String, String> map = new HashMap<String, String>();
        map.put("tp", "c_adwhirl");
        imAdRequest.setRequestParams(map);

        // Set the auto refresh off.
        adView.setRefreshInterval(IMAdView.REFRESH_INTERVAL_OFF);
        adView.loadNewAd(imAdRequest);
    }

    @Override
    public void onAdRequestCompleted(IMAdView adView) {
        Log.d(LocadzUtils.LOG_TAG, "InMobi success");

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        locadzLayout.submitPushSubViewRequest(adView);
    }

    @Override
    public void onAdRequestFailed(IMAdView adView, ErrorCode errorCode) {
        Log.d(LocadzUtils.LOG_TAG, "InMobi failure (" + errorCode + ")");
        rollover();
    }

    @Override
    public void onShowAdScreen(IMAdView adView) {
    }

    @Override
    public void onDismissAdScreen(IMAdView adView) {
    }

    public GenderType getGender() {
        Gender gender = AdWhirlTargeting.getGender();
        if (Gender.MALE == gender) {
            return GenderType.MALE;
        }
        if (Gender.FEMALE == gender) {
            return GenderType.FEMALE;
        }
        return GenderType.NONE;
    }

}
