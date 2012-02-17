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

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.adwhirl.adapters.AdWhirlAdapter;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.locadz.LocadzUtils.LOG_TAG;

/**
 *  The base canvas for the 3rd party AD View.<p/>
 *
 * Workflow:
 * <ol>
 *  <li>When layout is added to an Activity. The AdUnitLayout would register a {@link BroadcastReceiver} 
 *      to receive SHOW_AD response.</li>
 *  <li>Periodically, the AdUnitLayout would send a {@link Intent} to {@link AdUnitAllocationService} to fetch the
 *      next AD to show.</li>
 *  <li>{@link AdUnitAllocationService} will reply the request in a SHOW_AD Intent. When the {@link AdUnitLayout}
 *      receive this intent, it will create an {@link AdWhirlAdapter} for that intent</li>
 *  <li>{@link AdWhirlAdapter} will fetch and push the AdView to the {@link AdUnitLayout}.</li>
 *  <li>When next SHOW_AD response arrives, the {@link AdUnitLayout} will replace the existing adapter 
 *      with the new adapter.</li>
 * </ol>
 * 
 * <ul>
 *    <li>If the {@link AdWhirlAdapter}, fails to fetch new ADs. The {@link AdWhirlAdapter} is responsible to
 *    call {@link #submitReloadAdRequest()} to trigger loading new adapter immediately.</li>
 *    <li>If a {@link AdWhirlAdapter} derived classes fails to do so, the previous adapter will still occupy
 *      the space until next SHOW_AD request and response comming from {@link AdUnitAllocationService}</li>
 * </ul>
 *
 *  <ul>
 *      <li>When the parent Activity is not visible, this layout should stop sending intents to {@link AdUnitAllocationService}</li>
 *      <li>When the parent Activity is visible again, this layout should start to send intents to {@link AdUnitAllocationService}
 *          using the previous used cycle time.</li>
 *  </ul>
 *
 *
 * TODO: add fetch location code.
 * TODO: implements stop scheduling when activity is not visible.
 */
public class AdUnitLayout extends RelativeLayout {

    public static final int GET_LOCATION_TIMEOUT = 30000;
    /** the adUnitId of this layout. */
    private String adUnitId;

    /** the context of this layout. */
    private AdUnitContext adUnitContext;

    /** some adView require activity so that we store it here. **/
    private WeakReference<Activity> activityReference;

    /** the boardcast receiver, we have to un-register by ourself when the layout is detach from the parent Activity. */
    private ShowAdIntentReceiver adIntentReceiver;

    // Added so we can tell the previous adapter that it is being destroyed.
    private AdWhirlAdapter previousAdapter;
    private AdWhirlAdapter currentAdapter;

    /** scheduler for executing some command periodically. */
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * the current scheduled reload request. When the cycle time changes, we need to cancel current scheduled action
     * and create a new one.
     */
    private ScheduledFuture scheduledReload;

    /** number indicate how often shall this layout flip adapters. */
    private long cycleTime = 30 * 1000; // 30 seconds.

    /**
     * Indicate whether the parent Activity is visible or not. The {@link View#getVisibility()} returns the visibility
     * of a View. But a view can be visible while the parent Activity is still invisible.
     */
    private boolean visible = true;

    /**
     * Constructor for XML style.
     * @param context
     * @param set
     */
    public AdUnitLayout(Context context, AttributeSet set) {
        super(context, set);
        init((Activity) context, LocadzUtils.getAdUnitId(context));
    }

    /**
     * Constructor for initializing the layout programmatically.
     * @param context
     */
    public AdUnitLayout(Activity context, String adUnitId) {
        super(context);
        init(context, adUnitId);
    }

    /**
     * Initialize the layout.
     * @param context   the parent Activity.
     * @param adUnitId  the adUnit for this page.
     */
    protected void init(Activity context, String adUnitId) {
        if (adUnitId == null) {
            throw new IllegalArgumentException("ADUNIT is not presented.");
        }

        this.adUnitId = adUnitId;
        activityReference = new WeakReference<Activity>(context);

        // TODO: allow use to change the app ver.
        adUnitContext = new AdUnitContext(adUnitId, "1.0");

        // disable the scroll bars.
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        // register a new listener.
        adIntentReceiver = new ShowAdIntentReceiver(adUnitId, this);
        IntentFilter intentFilter = new IntentFilter(IntentConstants.ACTION_SHOW_AD);
        this.getContext().registerReceiver(adIntentReceiver, intentFilter);


        int accessLocationPermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (accessLocationPermission == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Handler handler = new Handler() {
                public void handleMessage(Message m) {
                    Log.d(LOG_TAG, "Handler returned with message: " + m.toString());
                    if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_FOUND) {
                        Location location = (Location) m.obj;
                        adUnitContext = new AdUnitContext.Builder()
                            .copy(adUnitContext)
                            .withLocation(location)
                            .build();
                    } else if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_NULL) {

                    } else if (m.what == LocationHelper.MESSAGE_CODE_PROVIDER_NOT_PRESENT) {
                    }
                    submitReloadAdRequest();
                }
            };
            LocationHelper helper = new LocationHelper(locationManager, handler, LOG_TAG);
            helper.getCurrentLocation(GET_LOCATION_TIMEOUT);
        } else {
            submitReloadAdRequest();
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void onDetachedFromWindow() {
        this.getContext().unregisterReceiver(adIntentReceiver);
    }

    /** {@inheritDoc} */
    @Override
    protected void onWindowVisibilityChanged (int visibility) {
        visible = visibility == View.VISIBLE;
    }

    /**
     *
     * @return true if the parent Activity is visible.
     */
    protected boolean isActivityVisible() {
        return visible;
    }

    /**
     * Schedule reloading ADs in the given interval.
     * @param delayMillis   delays between each reload in ms.
     */
    protected void scheduleReload(long delayMillis) {
        synchronized (scheduler) {

            boolean requireReschedule = false;
            if (scheduledReload == null) {
                requireReschedule = true;
            } else if (cycleTime != delayMillis) {
                requireReschedule = true;
                scheduledReload.cancel(true);
            }

            cycleTime = delayMillis;

            if (requireReschedule) {
                scheduledReload = scheduler.scheduleWithFixedDelay(
                    new ReloadAdRunnable(getActivity(), adUnitContext),
                    cycleTime,
                    cycleTime,
                    TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     *  Remove old ad views and push the new one.
     *  @param subView the adview to push.
     */
    protected void pushSubView(ViewGroup subView) {
        this.removeAllViews();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.addView(subView, layoutParams);

        Log.d(LocadzUtils.LOG_TAG, "Added subview");
        countImpression();
    }

    /**
     *  submit a push view request to Android's handler. This will remove
     *  old ad view and push a new one to this layout asynchronously.
     *
     * @param subView   the adview to push.
     */
    public void submitPushSubViewRequest(ViewGroup subView) {
        Log.d(LocadzUtils.LOG_TAG, String.format("Scheduled pushSubView(%s)", subView));
        getHandler().post(new ViewAdRunnable(this, subView));
    }

    /**
     *
     * @return  the context of this AdUnitLayout.
     */
    public AdUnitContext getAdUnitContext() {
        return adUnitContext;
    }

    /**
     *
     * @return  the parent Activity or null if the activity has been recycled by the system.
     */
    public Activity getActivity() {
        return activityReference.get();
    }

    private void countImpression() {
    }

    /**
     *
     * @param ration
     * @param extra
     */
    private void rotateAd(Ration ration, Extra extra) {
        if (isActivityVisible()) {
            try {
                // Tell the previous adapter that its view will be destroyed.
                if (this.previousAdapter != null) {
                    this.previousAdapter.willDestroy();
                }
                this.previousAdapter = this.currentAdapter;
                this.currentAdapter = AdWhirlAdapter.handle(this, ration, extra);

                // if the cycle time has changed, reschedule the reloading ads Runnable.
                if (extra.getCycleTime() != cycleTime) {
                    scheduleReload(extra.getCycleTime());
                }
            } catch (Throwable t) {
                Log.w(LocadzUtils.LOG_TAG, "Caught an exception in adapter:", t);
                submitReloadAdRequest();
                return;
            }
        }
    }

    /**
     * Submit a reload AD request asynchronously.
     *
     * TODO: find a better name for this function.
     */
    public void submitReloadAdRequest() {
        Intent intent = AdUnitAllocationService.createIntent(this.getContext(), adUnitContext);
        getActivity().startService(intent);
    }

    /**
     * Broadcast receiver that receives SHOW_AD intent and replace the existing adaptor with the
     * new one specified in the intent.
     */
    private static final class ShowAdIntentReceiver extends BroadcastReceiver {

        private final String adUnitId;
        
        private final WeakReference<AdUnitLayout> locadzLayoutWeakReference;

        public ShowAdIntentReceiver(String adUnitId, AdUnitLayout layout) {
            this.adUnitId = adUnitId;
            locadzLayoutWeakReference = new WeakReference<AdUnitLayout>(layout);
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {

            // verify the Intent is for this adunit.
            if (IntentConstants.ACTION_SHOW_AD.equals(intent.getAction())) {
                String targetAdUnitId = intent.getStringExtra(IntentConstants.EXTRA_ADUNIT_ID);

                if (adUnitId.equals(targetAdUnitId)) {
                    AdUnitLayout locadzLayout = locadzLayoutWeakReference.get();

                    // if the parent AdUnitLayout does not exist anymore, de-register this broadcast receiver
                    // from the system.
                    if (locadzLayout == null) {
                        context.unregisterReceiver(this);
                    } else {
                        Ration ration = (Ration)intent.getSerializableExtra(IntentConstants.EXTRA_RATION);
                        Extra extra = (Extra)intent.getSerializableExtra(IntentConstants.EXTRA_EXTRA);

                        if (locadzLayout.isActivityVisible()) {
                            // onReceive() is invoked in the Main thread, we need to
                            // replace the ad in the background thread.
                            locadzLayout.getHandler().post(new RotateAdRunnable(locadzLayout, ration, extra));
                        }
                    }
                }
            }
        }
    }


    /**
     * Runnable running on the Main Thread that rotates adapters.
     */
    private static final class RotateAdRunnable implements Runnable {

        private final WeakReference<AdUnitLayout> locadzLayoutWeakReference;

        private final Ration ration;
        
        private final Extra extra;
        
        public RotateAdRunnable(AdUnitLayout layout, Ration ration, Extra extra) {
            this.ration = ration;
            this.extra = extra;
            locadzLayoutWeakReference = new WeakReference<AdUnitLayout>(layout);
        }

        @Override
        public void run() {
            AdUnitLayout locadzLayout = locadzLayoutWeakReference.get();
            if (locadzLayout != null) {
                locadzLayout.rotateAd(ration, extra);
            }
        }
    }

    /**
     * Runnable running on the Main Thread that push an AdView to the layout.
     */
    private static final class ViewAdRunnable implements Runnable {

        private final WeakReference<AdUnitLayout> locadzLayoutWeakReference;

        private ViewGroup subView;

        public ViewAdRunnable(AdUnitLayout layout, ViewGroup subView) {
            locadzLayoutWeakReference = new WeakReference<AdUnitLayout>(layout);
            this.subView = subView;
        }

        @Override
        public void run() {
            AdUnitLayout locadzLayout = locadzLayoutWeakReference.get();
            if (locadzLayout != null) {
                locadzLayout.pushSubView(subView);
            }
        }
    }

    /**
     * Runnable running by the scheduler that sends VIEW_AD intends to ConfigService
     * periodically.
     */
    private static final class ReloadAdRunnable implements Runnable {

        private final WeakReference<Context> contextWeakReference;

        private final AdUnitContext adUnitContext;

        public ReloadAdRunnable(Context context, AdUnitContext adUnitContext) {
            contextWeakReference = new WeakReference<Context>(context);
            this.adUnitContext = adUnitContext;
        }

        @Override
        public void run() {
            Context context = contextWeakReference.get();
            if (context != null) {
                Intent intent = AdUnitAllocationService.createIntent(context, adUnitContext);
                context.startService(intent);
            }
        }
    }
    
}
