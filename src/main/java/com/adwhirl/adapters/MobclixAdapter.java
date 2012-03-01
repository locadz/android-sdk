package com.adwhirl.adapters;

import android.app.Activity;
import android.util.Log;
import com.adwhirl.AdWhirlTargeting;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

/**
 * This adapter provides advertise adapter for <a href="www.mobclix.com">MobClix</a>.<p>
 * This adapter would use {@link MobclixMMABannerXLAdView} to display advertisement.<p>
 *
 * To use this adapter, add following configuration to your android application:<p>
 *
 * <ol>
 * <li>Download API of MobxClix and link the library to your project</li>
 * <li>In AndroidManifest.xml:<br/>
 *      <pre><code>
 *          <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.mobclix.demo" android:versionName="1.0.0" android:versionCode="1">
 *              <application android:icon="@drawable/icon" android:label="@string/app_name" android:debuggable="true">
 *                  <activity android:name=".MobclixDemo"
 *                      android:label="@string/app_name">
 *                      ...
 *                  </activity>
 *                  ...
 *                  <meta-data android:name="com.mobclix.APPLICATION_ID" android:value="insert-your-application-key"/>
 *                  <activity android:name="com.mobclix.android.sdk.MobclixBrowserActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
 *              </application>
 *              ...
 *              <uses-permission android:name="android.permission.INTERNET" />
 *              <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 *              <!-- ACCESS_NETWORK_STATE is optional -->
 *              <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *          </manifest>
 *      </pre></code>
 * </li>
 * </ol>
 *
 * You may link to use 
 * For more information, see documentations of MobClix.<p>
 */
public class MobclixAdapter extends AdWhirlAdapter {
    /**
     * Constructor inherit fro {@link AdWhirlAdapter}.<p>
     */
    public MobclixAdapter(AdUnitLayout locadzLayout, Ration ration, Extra extra)
    {
        super(locadzLayout, ration, extra);
        Log.v(LocadzUtils.LOG_TAG, "Initialize MobclixAdapter");
    }

    @Override
    public void handle()
    {
        /**
         * Get the activity from containing layout of Locadz
         */
        AdUnitLayout locadzLayout = getLocadzLayout();
        if (locadzLayout == null) {
            return;
        }
        Activity activity = locadzLayout.getActivity();
        if (activity == null) {
            return;
        }
        // :~)

        /**
         * Initialize the view of MobxClix and send a request
         */
        Log.v(LocadzUtils.LOG_TAG, "Initialize MobclixAdView");
        MobclixAdView adView = new MobclixMMABannerXLAdView(activity);
        adView.addMobclixAdViewListener(new MobclixAdapterListener(AdWhirlTargeting.getKeywords()));
        adView.getAd();
        // :~)
    }

    /**
     * Listener class defined by MobClix to handle event of advertising and configuration of user preference.<p>
     */
    private class MobclixAdapterListener implements MobclixAdViewListener {
        private String keywords;

        /**
         * The keywords sent to MobClix.
         *
         * @see #keywords
         */
        MobclixAdapterListener(String newKeywords)
        {
            keywords = newKeywords;
        }

        @Override
        public void onSuccessfulLoad(MobclixAdView adView)
        {
            Log.v(LocadzUtils.LOG_TAG, "MobclixAdView loading successfully");

            /**
             * Call the containing layout to renew the loaded advertisement from MobClix
             */
            AdUnitLayout locadzLayout = getLocadzLayout();
            if (locadzLayout == null) {
                return;
            }
            locadzLayout.submitPushSubViewRequest(adView);
            // :~)
        }
        @Override
        public void onFailedLoad(MobclixAdView adView, int errorCode)
        {
            Log.e(LocadzUtils.LOG_TAG, "failure to load MobclixAdView. Code: [" + errorCode + "]");

            adView.removeMobclixAdViewListener(this); // Remove listener in Mobxclix view object

            /**
             * Call the containing layout of Locadz to reload the advertisement
             */
            AdUnitLayout locadzLayout = getLocadzLayout();
            if (locadzLayout == null) {
                return;
            }
            locadzLayout.submitReloadAdRequest();
            // :~)
        }
        @Override
        public boolean onOpenAllocationLoad(MobclixAdView adView, int openAllocationCode)
        {
            if (Log.isLoggable(LocadzUtils.LOG_TAG, Log.VERBOSE)) {
                Log.v(LocadzUtils.LOG_TAG, "The ad request of MobClix returned open allocation code: " + openAllocationCode);
            }
            return false;
        }
        @Override
        public void onAdClick(MobclixAdView adView)
        {
            Log.v(LocadzUtils.LOG_TAG, "Advertisement of MobClix has been clicked");
        }
        @Override
        public void onCustomAdTouchThrough(MobclixAdView adView, String string)
        {
            Log.v(LocadzUtils.LOG_TAG, "Advertisement of MobClix has been thouched");
        }
        @Override
        public String keywords() { return keywords; }
        @Override
        public String query() { return null; }
    }
}
