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
import com.adwhirl.util.AdWhirlUtil;
import com.locadz.AdUnitLayout;
import com.locadz.LocadzUtils;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    }

    public AdUnitLayout getLocadzLayout() {
        return layoutReference.get();
    }
    public Extra getExtra() {
        return extra;
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
     * This utility method constructs a new {@link AdWhirlAdapter} by {@link Ration#getNetworkId}.<p>
     *
     * @param adWhirlLayout The object of parent layout
     * @param ration The meta-data of advertisement service
     * @param extra The extra parameters for advertisement service
     *
     * @see AdWhirlTargeting
     */
    public static AdWhirlAdapter handle(
        AdUnitLayout adWhirlLayout, Ration ration, Extra extra
    ) throws Throwable {
        AdWhirlAdapter adapter = AdWhirlAdapter.getAdapter(adWhirlLayout, ration, extra);
        if (adapter != null) {
            Log.d(LocadzUtils.LOG_TAG, String.format("Valid adapter %s, calling handle()", adapter.getClass().getSimpleName()));
            adapter.handle();
        } else {
            throw new Exception("Invalid adapter");
        }
        return adapter;
    }
    private static AdWhirlAdapter getAdapter(
        AdUnitLayout adWhirlLayout,
         Ration ration, Extra extra
     ) {
        try {
            switch (ration.getNetworkId()) {
                case AdWhirlUtil.NETWORK_TYPE_ADMOB:
                    if (Class.forName("com.google.ads.AdView") != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.GoogleAdMobAdsAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_INMOBI:
                    if (Class.forName("com.inmobi.androidsdk.IMAdView")
                        != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.InMobiAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_QUATTRO:
                    if (Class.forName("com.qwapi.adclient.android.view.QWAdView")
                        != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.QuattroAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_MILLENNIAL:
                    if (Class.forName("com.millennialmedia.android.MMAdView") != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.MillennialAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_ADSENSE:
                    if (Class.forName("com.google.ads.GoogleAdView") != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.AdSenseAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_ZESTADZ:
                    if (Class.forName("com.zestadz.android.ZestADZAdView") != null) {
                        return getNetworkAdapter("com.adwhirl.adapters.ZestAdzAdapter",
                            adWhirlLayout, ration, extra);
                    } else {
                        return unknownAdNetwork(adWhirlLayout, ration);
                    }

                case AdWhirlUtil.NETWORK_TYPE_MDOTM:
                    return getNetworkAdapter("com.adwhirl.adapters.MdotMAdapter",
                        adWhirlLayout, ration, extra);

                case AdWhirlUtil.NETWORK_TYPE_ONERIOT:
                    return getNetworkAdapter("com.adwhirl.adapters.OneRiotAdapter",
                        adWhirlLayout, ration, extra);

                case AdWhirlUtil.NETWORK_TYPE_NEXAGE:
                    return getNetworkAdapter("com.adwhirl.adapters.NexageAdapter",
                        adWhirlLayout, ration, extra);

                case AdWhirlUtil.NETWORK_TYPE_MOBCLIX:
                    return getNetworkAdapter("com.adwhirl.adapters.MobclixAdapter",
                        adWhirlLayout, ration, extra);

//                case AdWhirlUtil.NETWORK_TYPE_CUSTOM:
//                    return new CustomAdapter(adWhirlLayout, ration, extra);
//
//                case AdWhirlUtil.NETWORK_TYPE_GENERIC:
//                    return new GenericAdapter(adWhirlLayout, ration, extra);
//
//                case AdWhirlUtil.NETWORK_TYPE_EVENT:
//                    return new EventAdapter(adWhirlLayout, ration, extra);

                default:
                    return unknownAdNetwork(adWhirlLayout, ration);
            }
        } catch (ClassNotFoundException e) {
            return unknownAdNetwork(adWhirlLayout, ration);
        } catch (VerifyError e) {
            Log.e("AdWhirl", "Caught VerifyError", e);
            return unknownAdNetwork(adWhirlLayout, ration);
        }
    }
    private static AdWhirlAdapter getNetworkAdapter(
        String networkAdapter, AdUnitLayout adWhirlLayout,
        Ration ration, Extra extra
    ) {
        AdWhirlAdapter adWhirlAdapter = null;

        try {
            @SuppressWarnings("unchecked")
            Class<? extends AdWhirlAdapter> adapterClass =
                (Class<? extends AdWhirlAdapter>) Class.forName(networkAdapter);

            Class<?>[] parameterTypes = new Class[3];
            parameterTypes[0] = AdUnitLayout.class;
            parameterTypes[1] = Ration.class;
            parameterTypes[2] = Extra.class;

            Constructor<? extends AdWhirlAdapter> constructor =
                adapterClass.getConstructor(parameterTypes);

            Object[] args = new Object[3];
            args[0] = adWhirlLayout;
            args[1] = ration;
            args[2] = extra;

            adWhirlAdapter = constructor.newInstance(args);
        } catch (ClassNotFoundException e) {
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }

        return adWhirlAdapter;
    }
    private static AdWhirlAdapter unknownAdNetwork(
        AdUnitLayout adWhirlLayout,
        Ration ration
    ) {
        Log.w(LocadzUtils.LOG_TAG, "Unsupported ration type: " + ration.getNetworkId());
        return null;
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
