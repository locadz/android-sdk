/*
 * Copyright (c) 2012. Blue Tang Studio LLC. All rights reserved.
 */

package com.locadz;

import android.util.AttributeSet;

/**
 * A collection of attributes, as found associated with a tag in an XML document.
 */
public class AdUnitLayoutAttributeSet {

    /**
     * The XML namespace for this layout({@value #XML_NAMESPACE}).<p>
     */
    public static final String XML_NAMESPACE = "http://api.locadz.com/android/";
    public static final String XML_ATTR_TEST_MODE = "test_mode";
    public static final String XML_ATTR_ADUNIT_ID = "adunit_id";

    private final AttributeSet attributeSet;

    public AdUnitLayoutAttributeSet(AttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public boolean getTestMode(boolean defaultValue) {
        return attributeSet.getAttributeBooleanValue(XML_NAMESPACE, XML_ATTR_TEST_MODE, defaultValue);
    }
    
    public String getAdUnitId() {
        return attributeSet.getAttributeValue(XML_NAMESPACE, XML_ATTR_ADUNIT_ID);
    }
}
