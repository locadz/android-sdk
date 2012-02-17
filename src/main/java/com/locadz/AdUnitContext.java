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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public final class AdUnitContext implements Parcelable {

    private final String appVersion;

    private final String adUnitId;

    private final Location location;

    public AdUnitContext(String adUnitId, String appVerion, Location location) {
        this.adUnitId = adUnitId;
        this.appVersion = appVerion;
        this.location = location;
    }

    public AdUnitContext(String adUnitId, String appVerion) {
        this(adUnitId, appVerion, null);
    }

    public AdUnitContext(Parcel in) {
        this.adUnitId = in.readString();
        this.appVersion = in.readString();
        this.location = (Location) in.readParcelable(Location.class.getClassLoader());

    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getAdUnitId() {
        return adUnitId;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdUnitContext)) return false;

        AdUnitContext that = (AdUnitContext) o;

        if (adUnitId != null ? !adUnitId.equals(that.adUnitId) : that.adUnitId != null) return false;
        if (appVersion != null ? !appVersion.equals(that.appVersion) : that.appVersion != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appVersion != null ? appVersion.hashCode() : 0;
        result = 31 * result + (adUnitId != null ? adUnitId.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AdUnitContext{" +
            "adUnitId='" + adUnitId + '\'' +
            ", appVerion='" + appVersion + '\'' +
            ", location='" + location + '\'' +
            '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(adUnitId);
        parcel.writeString(appVersion);
        parcel.writeParcelable(location, flag);
    }

    public static final Parcelable.Creator<AdUnitContext> CREATOR = new Parcelable.Creator<AdUnitContext>() {
        public AdUnitContext createFromParcel(Parcel in) {
            return new AdUnitContext(in);
        }

        public AdUnitContext[] newArray(int size) {
            return new AdUnitContext[size];
        }
    };


    public static class Builder {

        private String appVersion;

        private String adUnitId;

        private Location location;

        public Builder() {
            
        }
        
        public Builder withAdUnitId(String adUnitId) {
            this.adUnitId = adUnitId;
            return this;
        }

        public Builder withAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public Builder copy(AdUnitContext adUnitContext) {
            this.adUnitId = adUnitContext.adUnitId;
            if (adUnitContext.appVersion != null) {
                this.appVersion = adUnitContext.appVersion;
            }
            if (adUnitContext.location != null) {
                this.location = adUnitContext.location;
            }
            return this;
        }

        public AdUnitContext build() {
            return new AdUnitContext(adUnitId, appVersion, location);
        }
    }
}
