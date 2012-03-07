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

import android.util.Log;
import com.locadz.AdvertisingPreference;
import com.adwhirl.util.AdWhirlUtil;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This base class provides essence of environment for advertising adapter.<p>
 *
 * The {@link #getLocadzLayout} would give a object from {@link WeakReference},
 * hence the sub-class should concern null value of parent layout.<p>
 *
 * Sub-class should implement {@link #handle} method to initialize view for particular service of advertisement.<p>
 */
public abstract class AdWhirlAdapter {
    private final WeakReference<AdUnitLayout> layoutReference;

    protected final Ration ration;
    protected final Extra extra;

    // The preference of advertising for this adapter
    private final AdvertisingPreference advertisingPreference;

    /**
     * This method would be called when this adapter is choosed as target network of advertisement.<p>
     */
    public abstract void handle();

    /**
     * Construct this adapter by essential environment and information of advertising service.<p>
     *
     * @param layout The object of parent layout
     * @param ration The meta-data of advertisement service
     * @param extra The extra parameters for advertisement service
     */
    public AdWhirlAdapter(AdUnitLayout layout, Ration ration, Extra extra)
    {
        this.layoutReference = new WeakReference<AdUnitLayout>(layout);
        this.ration = ration;
        this.extra = extra;

        advertisingPreference = layout.getAdvertisingPreference();
    }

    public AdUnitLayout getLocadzLayout() {
        return layoutReference.get();
    }
    public Extra getExtra() {
        return extra;
    }
    /**
     * Gets the advertising preference of this adapter,
     * which comes from {@link AdUnitLayout} object while constructing this object.<p>
     *
     * @see AdUnitLayout#getAdvertisingPreference()
     */
    public AdvertisingPreference getAdvertisingPreference()
    {
        return advertisingPreference;
    }

    protected boolean isVisible() {
        return layoutReference.get() != null;
    }
    protected void rollover() {
        AdUnitLayout locadzLayout = layoutReference.get();
        if (locadzLayout != null) {
            locadzLayout.submitReloadAdRequest();
        }
    }

    /**
     * This utility method constructs a new {@link AdWhirlAdapter} by {@link Ration#getNetworkId} and
     * calls the {@link AdWhirlAdapter.handle} to initialize advertising service.<p>
     *
     * @param adWhirlLayout The object of parent layout
     * @param ration The meta-data of advertisement service
     * @param extra The extra parameters for advertisement service
     *
     * @see AdvertisingPreference
     */
    public static AdWhirlAdapter handle(
        AdUnitLayout adWhirlLayout, Ration ration, Extra extra
    ) throws Throwable {
        AdWhirlAdapter adapter;

        /**
         * Builds the adapter
         */
        try {
            adapter = new AdapterBuilder(adWhirlLayout, ration, extra).buildAdapter();
        } catch (BuildAdapterException e) {
            Log.e(LocadzUtils.LOG_TAG, "Exception occur when building adapter", e);
            throw e;
        }
        // :~)

        /**
         * Call handle() of built adapter
         */
        try {
            Log.d(LocadzUtils.LOG_TAG, String.format("Valid adapter %s, calling handle()", adapter.getClass().getSimpleName()));
            adapter.handle();
        } catch (Exception e) {
            Log.e(LocadzUtils.LOG_TAG, String.format("Handle() of adapter(%s) error", adapter.getClass().getSimpleName()), e);
            throw new Exception("Handle of adapter error", e);
        }
        // :~)

        return adapter;
    }

    /**
     * Added to tell adapter that it's view will be destroyed.<p>
     */
    public void willDestroy()
    {
        Log.d(LocadzUtils.LOG_TAG, "Generic adapter will get destroyed");
    }

    protected static String googleAdSenseCompanyName;
    protected static String googleAdSenseAppName;
    protected static String googleAdSenseChannel;
    protected static String googleAdSenseExpandDirection;

    public static void setGoogleAdSenseCompanyName(String name) {
        googleAdSenseCompanyName = name;
    }
    public static void setGoogleAdSenseAppName(String name) {
        googleAdSenseAppName = name;
    }
    public static void setGoogleAdSenseChannel(String channel) {
        googleAdSenseChannel = channel;
    }
    public static void setGoogleAdSenseExpandDirection(String direction) {
        googleAdSenseExpandDirection = direction;
    }
}

/**
 * This builder is used to build {@link AdWhirlAdapter}.<p>
 *
 * WARNING: Because this class hold a reference to {@link AdUnitLayout},
 * you must not hold the instance of this build in long-term lifetime.<p>
 */
class AdapterBuilder {
    private AdUnitLayout adWhirlLayout;
    private Ration ration;
    private Extra extra;

    private final static Map<Integer, AdapterInfo> mapOfAdapters; // The mapping information for building adapter
    static {
        /**
         * Construct the mapping of building adapters
         */
        Map<Integer, AdapterInfo> processMap = new HashMap<Integer, AdapterInfo>(10);
        processMap.put(AdWhirlUtil.NETWORK_TYPE_ADMOB, new AdapterInfo("com.google.ads.AdView", "com.adwhirl.adapters.GoogleAdMobAdsAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_ADSENSE, new AdapterInfo("com.google.ads.GoogleAdView", "com.adwhirl.adapters.AdSenseAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_INMOBI, new AdapterInfo("com.inmobi.androidsdk.IMAdView", "com.adwhirl.adapters.InMobiAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_MDOTM, new AdapterInfo("com.mdotm.android.ads.MdotMView", "com.adwhirl.adapters.MdotMAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_MILLENNIAL, new AdapterInfo("com.millennialmedia.android.MMAdView", "com.adwhirl.adapters.MillennialAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_MOBCLIX, new AdapterInfo("com.mobclix.android.sdk.MobclixAdView", "com.locadz.adapters.MobclixAdapter"));
        processMap.put(AdWhirlUtil.NETWORK_TYPE_ZESTADZ, new AdapterInfo("com.zestadz.android.ZestADZAdView", "com.adwhirl.adapters.ZestAdzAdapter"));

        mapOfAdapters = Collections.unmodifiableMap(processMap);
        // :~)
    }

    AdapterBuilder(AdUnitLayout newAdWhirlLayout, Ration newRation, Extra newExtra)
    {
        adWhirlLayout = newAdWhirlLayout;
        ration = newRation;
        extra = newExtra;
    }

    /**
     * Build adapter by id of advertising network.<p>
     */
    AdWhirlAdapter buildAdapter() throws BuildAdapterException
    {
        if (!mapOfAdapters.containsKey(ration.getNetworkId())) {
            String message = "Unsupported network: [" + ration.getNetworkId() + "]";
            Log.w(LocadzUtils.LOG_TAG, message);
            throw new BuildAdapterException(message);
        }

        /**
         * Load mapping of adapter and class of adapter
         */
        AdapterInfo buildInfo = mapOfAdapters.get(ration.getNetworkId());

        try {
            return checkAndBuildAdapter(buildInfo.dependencyClassName, (Class<AdWhirlAdapter>)Class.forName(buildInfo.adapterClassName));
        } catch (ClassNotFoundException e) {
            String message = "Can't load adapter: " + buildInfo.adapterClassName;
            Log.w(LocadzUtils.LOG_TAG, message);
            throw new BuildAdapterException(message);
        }
        // :~)
    }

    private AdWhirlAdapter checkAndBuildAdapter(
        String classNameOfDependency, Class<AdWhirlAdapter> adapterClassName
    ) throws BuildAdapterException
    {
        if (Log.isLoggable(LocadzUtils.LOG_TAG, Log.VERBOSE)) {
            Log.v(LocadzUtils.LOG_TAG,
                String.format(
                    "Processing dependency[%s] for adapter[%s]",
                    classNameOfDependency, adapterClassName.getSimpleName()
                )
            );
        }

        /**
         * Check the dependency of library from advertising service
         */
        if (!hasClassForName(classNameOfDependency)) {
            String message = "The dependency for network: [" + ration.getNetworkId() + "] is not exists. class: [" + classNameOfDependency + "]";
            Log.w(LocadzUtils.LOG_TAG, message);
            throw new BuildAdapterException(message);
        }
        // :~)

        AdWhirlAdapter adWhirlAdapter = null;

        try {
            Class<?>[] parameterTypes = new Class[3];
            parameterTypes[0] = AdUnitLayout.class;
            parameterTypes[1] = Ration.class;
            parameterTypes[2] = Extra.class;

            Constructor<AdWhirlAdapter> constructor = adapterClassName.getConstructor(parameterTypes);

            Object[] args = new Object[3];
            args[0] = adWhirlLayout;
            args[1] = ration;
            args[2] = extra;

            adWhirlAdapter = constructor.newInstance(args);
        } catch (Exception e) {
            throw new BuildAdapterException(e);
        }

        return adWhirlAdapter;
    }

    private boolean hasClassForName(String className)
    {
        try {
            Class.forName(className);
            if (Log.isLoggable(LocadzUtils.LOG_TAG, Log.VERBOSE)) {
                Log.v(LocadzUtils.LOG_TAG, "Loading dependency library [" + className + "] successfully");
            }
        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }

    private static class AdapterInfo {
        String dependencyClassName;
        String adapterClassName;

        AdapterInfo(String dependencyClassName, String adapterClassName)
        {
            this.dependencyClassName = dependencyClassName;
            this.adapterClassName = adapterClassName;
        }
    }
}

/**
 * This exception wrapper exceptions generated from construct instance of {@link AdWhirlAdapter}.<p>
 */
class BuildAdapterException extends Exception {
    BuildAdapterException(String message)
    {
        super("Build Adapter error: " + message);
    }
    BuildAdapterException(Throwable cause)
    {
        super(cause);
    }
}
