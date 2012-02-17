package com.adwhirl.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.ScrollView;
import com.adwhirl.AdWhirlTargeting;
import com.google.ads.AdSenseSpec;
import com.google.ads.AdSenseSpec.AdFormat;
import com.google.ads.AdSenseSpec.ExpandDirection;
import com.google.ads.AdViewListener;
import com.google.ads.GoogleAdView;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Color;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.util.ArrayList;
import java.util.List;

public class AdSenseAdapter extends AdWhirlAdapter implements AdViewListener {
    private GoogleAdView adView;

    public AdSenseAdapter(AdUnitLayout layout, Ration ration, Extra extra) {
        super(layout, ration, extra);
    }

    @Override
    public void handle() {
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (isVisible() == false || locadzLayout == null) {
            return;
        }

        String clientId = ration.getKey();

        if (clientId == null || !clientId.startsWith("ca-mb-app-pub-")) {
            // Invalid publisher ID
            Log.w(LocadzUtils.LOG_TAG, "Invalid AdSense client ID");
            rollover();
            return;
        }
        if (TextUtils.isEmpty(googleAdSenseCompanyName)
            || TextUtils.isEmpty(googleAdSenseAppName)) {
            // Missing required parameters
            Log.w(LocadzUtils.LOG_TAG,
                "AdSense company name and app name are required parameters");
            rollover();
            return;
        }

        ExtendedAdSenseSpec spec = new ExtendedAdSenseSpec(clientId);
        spec.setCompanyName(googleAdSenseCompanyName);
        spec.setAppName(googleAdSenseAppName);
        if (!TextUtils.isEmpty(googleAdSenseChannel)) {
            spec.setChannel(googleAdSenseChannel);
        }

        spec.setAdFormat(AdFormat.FORMAT_320x50);

        boolean testMode = AdWhirlTargeting.getTestMode();
        spec.setAdTestEnabled(testMode);

        adView = new GoogleAdView(locadzLayout.getContext());
        adView.setAdViewListener(this);

        Color bgColor = getExtra().getBackgroundColor();
        spec.setColorBackground(rgbToHex(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()));

        final AdWhirlTargeting.Gender gender = AdWhirlTargeting.getGender();
        spec.setGender(gender);

        final int age = AdWhirlTargeting.getAge();
        spec.setAge(age);

        final String keywords = AdWhirlTargeting.getKeywordSet() != null ? TextUtils
            .join(",", AdWhirlTargeting.getKeywordSet())
            : AdWhirlTargeting.getKeywords();
        if (!TextUtils.isEmpty(keywords)) {
            spec.setKeywords(keywords);
        }

        // According to AdSense guidelines, we cannot display an expandable ad in a
        // ListView or ScrollView
        boolean canExpand = true;
        ViewParent p = locadzLayout.getParent();
        if (p == null) {
            // Null parent may indicate that the ad is inside of a ListView header
            canExpand = false;
        } else {
            do {
                if (p instanceof ListView || p instanceof ScrollView) {
                    canExpand = false;
                    break;
                }
                p = p.getParent();
            } while (p != null);
        }

        if (canExpand && googleAdSenseExpandDirection != null) {
            try {
                ExpandDirection dir = ExpandDirection
                    .valueOf(googleAdSenseExpandDirection);
                spec.setExpandDirection(dir);
            } catch (IllegalArgumentException e) {
                // If an invalid expand direction is passed, don't set the expand
                // direction
            }
        }

        // The GoogleAdView has to be in the view hierarchy to make a request
        adView.setVisibility(View.INVISIBLE);
        locadzLayout.addView(adView, new LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT));

        adView.showAds(spec);
    }

    // This block contains the AdSense listeners

    /** *************************************************************** */
    public void onStartFetchAd() {
    }

    public void onFinishFetchAd() {
        Log.d(LocadzUtils.LOG_TAG, "AdSense success");
        adView.setAdViewListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        locadzLayout.removeView(adView);
        adView.setVisibility(View.VISIBLE);
        locadzLayout.submitPushSubViewRequest(adView);
    }

    public void onClickAd() {
    }

    public void onAdFetchFailure() {
        Log.d(LocadzUtils.LOG_TAG, "AdSense failure");
        adView.setAdViewListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        locadzLayout.removeView(adView);
        locadzLayout.submitReloadAdRequest();
    }

    /** *************************************************************** */
    // End of AdSense listeners
    private String rgbToHex(int r, int g, int b) {
        String rHex = channelValueToHex(r);
        String gHex = channelValueToHex(g);
        String bHex = channelValueToHex(b);

        if (rHex == null || gHex == null || bHex == null) {
            return null;
        }

        return new StringBuilder(rHex).append(gHex).append(bHex).toString();
    }

    private String channelValueToHex(int channelValue) {
        if (channelValue < 0 || channelValue > 255) {
            return null;
        }

        if (channelValue <= 15) {
            return "0" + Integer.toHexString(channelValue);
        } else {
            return Integer.toHexString(channelValue);
        }
    }

    // Targeting class to generate/set AdSense targeting codes.
    class ExtendedAdSenseSpec extends AdSenseSpec {
        public int ageCode = -1;
        public int genderCode = -1;

        public ExtendedAdSenseSpec(String clientId) {
            super(clientId);
        }

        public void setAge(int age) {
            if (age <= 0) {
                ageCode = -1;
            } else if (age <= 17) {
                ageCode = 1000;
            } else if (age <= 24) {
                ageCode = 1001;
            } else if (age <= 34) {
                ageCode = 1002;
            } else if (age <= 44) {
                ageCode = 1003;
            } else if (age <= 54) {
                ageCode = 1004;
            } else if (age <= 64) {
                ageCode = 1005;
            } else {
                ageCode = 1006;
            }
        }

        public void setGender(AdWhirlTargeting.Gender gender) {
            if (gender == AdWhirlTargeting.Gender.MALE) {
                genderCode = 1;
            } else if (gender == AdWhirlTargeting.Gender.FEMALE) {
                genderCode = 2;
            } else {
                genderCode = -1;
            }
        }

        @Override
        public List<Parameter> generateParameters(Context context) {
            List<Parameter> parameters = new ArrayList<Parameter>(super
                .generateParameters(context));

            if (ageCode != -1) {
                parameters.add(new Parameter("cust_age", Integer.toString(ageCode)));
            }
            if (genderCode != -1) {
                parameters.add(new Parameter("cust_gender", Integer
                    .toString(genderCode)));
            }

            return parameters;
        }
    }
}
