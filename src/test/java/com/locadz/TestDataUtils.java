package com.locadz;

import android.location.Location;
import com.locadz.model.AdUnitAllocation;
import com.locadz.model.Color;
import com.locadz.model.Extra;
import com.locadz.model.Ration;

import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
public class TestDataUtils {

    public static AdUnitContext getAdUnitContext() {
        return new AdUnitContext("TEST_UNIT", "0.1-SNAPSHOT", TestDataUtils.getLocation());
    }

    
    public static Location getLocation() {
        return new Location("test");
    }

    public static AdUnitAllocation getAdUnitAllocation() {
        Extra extra = new Extra(true, Color.White, Color.Black, 1000, 1);
        Ration firstRation = new Ration("allocId", "network1", 1, 50, 0, "akey");
        Ration secondRation = new Ration("allocId", "network2", 2, 50, 0, "akey");

        return new AdUnitAllocation(extra, Arrays.asList(firstRation, secondRation));
    }

    public static String getAdUnitAllocationAsString() {
        try {
            return SerializationUtils.toJson(getAdUnitAllocation());
        } catch (IOException shouldNeverHappen) {
            throw new RuntimeException(shouldNeverHappen);
        }
    }
}
