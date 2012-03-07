/*
 * Copyright (c) 2012. Blue Tang Studio LLC. All rights reserved.
 */

package com.locadz;

import android.text.TextUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * This bean represnets data to setup the user preference of advertising.<p>
 *
 * The usage of preference depends on implementation of adapter, which is
 * defined by advertising service.<p>
 */
public class AdvertisingPreference {
    /**
     * Defines the constants of gender.<p>
     *
     * @see #getGender
     */
    public static enum Gender {
        /**
         * Default value for gender.<p>
         */
        UNKNOWN,
        /**
         * As male.<p>
         */
        MALE,
        /**
         * As female.<p>
         */
        FEMALE;
    }

    private boolean testMode = false;
    private Gender gender = Gender.UNKNOWN;
    private GregorianCalendar birthday;
    private String postalCode;
    private String keywords;
    private Set<String> keywordSet = new HashSet<String>(12);

    public AdvertisingPreference() {}
    /**
     * A copy-constructor for safty of usage.<p>
     *
     * @param anotherPreference The source of preference to be copied
     */
    public AdvertisingPreference(AdvertisingPreference anotherPreference)
    {
        setBirthDate(anotherPreference.getBirthDate());
        setTestMode(anotherPreference.getTestMode());
        setGender(anotherPreference.getGender());
        setPostalCode(anotherPreference.getPostalCode());
        setKeywordSet(anotherPreference.getKeywordSet());
    }

    /**
     * Sets the age of user.
     * This is a convenient method as do in {@link #setBirthDate}<p>
     *
     * @param newAge The age of user
     *
     * @see #setBirthDate
     * @see #getAge
     */
    public void setAge(int newAge)
    {
        birthday = new GregorianCalendar(
            Calendar.getInstance().get(Calendar.YEAR) - newAge,
            0, 1
        );
    }
    /**
     * Gets the age of user.
     * This is a convenient method as do in {@link #getBirthDate}<p>
     *
     * @return The age of user calculated by now or null if {@link #getBirthDate} is null
     *
     * @see #getBirthDate
     * @see #setAge
     */
    public int getAge()
    {
        if (getBirthDate() == null) {
            return -1;
        }

        return Calendar.getInstance().get(Calendar.YEAR)
            - birthday.get(Calendar.YEAR);
    }

    /**
     * Sets whether usage for adapter in testing model or not.<p>
     *
     * @param newTestMode The value of testing mode
     *
     * @see #getTestMode
     */
    public void setTestMode(boolean newTestMode) { this.testMode = newTestMode; }
    /**
     * Gets the value of testing model.<p>
     *
     * @return true if set as testing mode
     *
     * @see #setTestMode
     */
    public boolean getTestMode() { return this.testMode; }

    /**
     * Sets the gender of user.<p>
     *
     * @param newGender The value of gender
     *
     * @see #getGender
     */
    public void setGender(Gender newGender) { this.gender = newGender; }
    /**
     * Gets the value of gender of user. Default value is {@link Gender#UNKNOWN}.<p>
     *
     * @return The value of gender
     *
     * @see #setGender
     */
    public Gender getGender() { return this.gender; }

    /**
     * Sets the birthday of user.<p>
     *
     * @param newBirthDay The value of birthday
     *
     * @see #setAge
     * @see #getBirthDate
     */
	public void setBirthDate(GregorianCalendar newBirthDay) { this.birthday = newBirthDay; }
    /**
     * Gets the birthday of user.<p>
     *
     * @return The value of birthday
     *
     * @see #getAge
     * @see #setBirthDate
     */
	public GregorianCalendar getBirthDate() { return this.birthday; }

    /**
     * Sets the postal code of user.<p>
     *
     * @param newPostalCode The value of postal code
     *
     * @see #getPostalCode
     */
    public void setPostalCode(String newPostalCode) { this.postalCode = newPostalCode; }
    /**
     * Gets the postal code of user.<p>
     *
     * @return The value of postal code
     *
     * @see #setPostalCode
     */
    public String getPostalCode() { return this.postalCode; }

    /**
     * Gets the keyword for advertising as literal string seperated by ",".<p>
     *
     * This method is the data of keywords from {@link #getKeywordSet}.<p>
     *
     * @return The value of keyword as literal string
     *
     * @see #getKeywordSet
     */
    public String getKeywords()
    {
        return TextUtils.join(",", getKeywordSet());
    }

    /**
     * Sets the keyword for advertising as set of strings.<p>
     *
     * @param newKeywordSet The value of a set of keywords
     *
     * @see #getKeywordSet
     */
    public void setKeywordSet(Set<String> newKeywordSet) { this.keywordSet = newKeywordSet; }
    /**
     * Gets the keyword for advertising as set of strings. Default value is an empty {@link Set}<p>
     *
     * This method is irrelevent to {@link #getKeywords}.<p>
     *
     * @return The keywords as a set of string
     *
     * @see #getKeywords
     * @see #setKeywordSet
     */
    public Set<String> getKeywordSet()
    {
        return new HashSet<String>(this.keywordSet);
    }

    /**
     * Adds a keyword to {@link #getKeywordSet}.<p>
     *
     * @param newKeyword The value of new keyword
     */
    public void addKeyword(String newKeyword)
    {
        getKeywordSet().add(newKeyword);
    }
}
