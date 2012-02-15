package com.locadz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.locadz.model.AdUnitAllocation;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(RobolectricTestRunner.class)
public class ConfigServiceTest  {

    @Test
    public void testLoadConfigFromRemote() {
        ConfigService service = new ConfigService();
        
        String expected = "TEST_DATA";
        Robolectric.addPendingHttpResponse(200, expected);
        
        String res = service.loadFromRemote(TestDataUtils.getAdUnitContext());

        Assert.assertEquals(expected, res);

    }

    @Test
    public void testLoadConfigFromRemoteButFailed() {
        ConfigService service = new ConfigService();

        Robolectric.addPendingHttpResponse(500, "");

        String res = service.loadFromRemote(TestDataUtils.getAdUnitContext());

        Assert.assertEquals(null, res);
    }

    @Test
    public void testLoadFromEmptySharedPreferences() {
        ConfigService service = new ConfigService();
        String res = service.loadFromSharedPreferences(TestDataUtils.getAdUnitContext());

        Assert.assertEquals(null, res);
    }

    @Test
    public void testWriteAndLoadFromSharedPreferences() {
        ConfigService service = new ConfigService();
        
        String expected = "TEST_DATA";

        AdUnitContext context = TestDataUtils.getAdUnitContext();
        service.writeSharedPreferences(context, expected);

        Assert.assertEquals(expected, service.loadFromSharedPreferences(context));
    }

    @Test
    public void testLoadExpiredConfigFromSharedPreferences() {

        AdUnitContext adUnitContext = TestDataUtils.getAdUnitContext();

        // write a config entry first.
        SharedPreferences perfs = Robolectric.getShadowApplication().getApplicationContext()
            .getSharedPreferences(adUnitContext.getAdUnitId(), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = perfs.edit();
        editor.putString("config", "old config");
        editor.putLong("timestamp", 0);
        editor.commit();

        ConfigService service = new ConfigService();
        Assert.assertEquals(null, service.loadFromSharedPreferences(adUnitContext));
    }

    @Test
    public void testOverwriteSharedPreferences() {

        AdUnitContext adUnitContext = TestDataUtils.getAdUnitContext();


        // write a config entry first.
        SharedPreferences perfs = Robolectric.getShadowApplication().getApplicationContext()
            .getSharedPreferences(adUnitContext.getAdUnitId(), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = perfs.edit();
        editor.putString("config", "old config");
        editor.putLong("timestamp", 0);
        editor.commit();

        ConfigService service = new ConfigService();
        
        String expected = "NEW CONFIG";

        service.writeSharedPreferences(adUnitContext, expected);
        Assert.assertEquals(expected, service.loadFromSharedPreferences(adUnitContext));
    }

    @Test
    public void testGetAdUnitAllocation() {
        Robolectric.addPendingHttpResponse(HttpStatus.SC_OK, TestDataUtils.getAdUnitAllocationAsString());
        
        ConfigService service = new ConfigService();
        AdUnitAllocation actual = service.getAdUnitAllocation(
            TestDataUtils.getAdUnitContext());

        Assert.assertEquals(TestDataUtils.getAdUnitAllocation(), actual);
    }

    @Test
    public void testOnHandleIntent() throws InterruptedException {

        AdUnitContext adUnitContext = TestDataUtils.getAdUnitContext();
        Robolectric.addPendingHttpResponse(HttpStatus.SC_OK, TestDataUtils.getAdUnitAllocationAsString());

        // register a broadcast receiver to receive response.
        final Intent[] response = { null };
        Robolectric.getShadowApplication().registerReceiver(
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    response[0] = intent;
                }
            },
            new IntentFilter(IntentConstants.ACTION_SHOW_AD)
        );

        // send a ad request to config service.
        Intent intent = ConfigService.createIntent(
            Robolectric.getShadowApplication().getApplicationContext(),
            adUnitContext);
        ConfigService service = new ConfigService();
        service.onHandleIntent(intent);

        Assert.assertNotNull(response[0]);
        Assert.assertEquals(response[0].getAction(), IntentConstants.ACTION_SHOW_AD);
        Assert.assertEquals(response[0].getStringExtra(IntentConstants.EXTRA_ADUNIT_ID), adUnitContext.getAdUnitId());
        Assert.assertNotNull(response[0].getSerializableExtra(IntentConstants.EXTRA_RATION));
        Assert.assertNotNull(response[0].getSerializableExtra(IntentConstants.EXTRA_EXTRA));
    }
}
