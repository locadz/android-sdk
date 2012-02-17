package com.adwhirl.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import com.adwhirl.util.AdWhirlUtil;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Ration;
import com.mdotm.android.ads.MdotMManager;
import com.mdotm.android.ads.MdotMView;
import com.mdotm.android.ads.MdotMView.MdotMActionListener;

public class MdotMAdapter extends AdWhirlAdapter implements MdotMActionListener {

    public MdotMAdapter(AdUnitLayout locadzLayout, Ration ration, com.locadz.model.Extra extra) {
        super(locadzLayout, ration, extra);
    }


    @Override
    public void handle() {
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }

        try {
            String ration_key = ration.getKey();
            MdotMManager.setPublisherId(ration_key);
            MdotMManager.setMediationLayerName(AdWhirlUtil.ADWHIRL);
            MdotMManager.setMediationLayerVersion(AdWhirlUtil.VERSION);
        }
        // Thrown on invalid publisher id
        catch (IllegalArgumentException e) {
            locadzLayout.submitReloadAdRequest();
            return;
        }

        Activity activity = locadzLayout.getActivity();
        if (activity == null) {
            return;
        }
        MdotMView mdotm = new MdotMView(activity, this);

        mdotm.setListener(this);
        com.locadz.model.Color bg = getExtra().getBackgroundColor();
        com.locadz.model.Color fg = getExtra().getTextColor();

        int bgColor = Color.rgb(bg.getRed(), bg.getGreen(), bg.getBlue());
        int fgColor = Color.rgb(fg.getRed(), fg.getGreen(), fg.getBlue());

        mdotm.setBackgroundColor(bgColor);
        mdotm.setTextColor(fgColor);
    }

    public void adRequestCompletedSuccessfully(MdotMView adView) {
        Log.d(LocadzUtils.LOG_TAG, "MdotM success");

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }
        adView.setListener(null);
        adView.setVisibility(View.VISIBLE);

        locadzLayout.submitPushSubViewRequest(adView);
    }

    public void adRequestFailed(MdotMView adView) {
        Log.d(LocadzUtils.LOG_TAG, "MdotM failure");
        adView.setListener(null);

        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }
        locadzLayout.submitReloadAdRequest();
    }
}
