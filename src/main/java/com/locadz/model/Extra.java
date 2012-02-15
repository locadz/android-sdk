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

package com.locadz.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 *
 */
public final class Extra  implements Serializable {

    private static final long serialVersionUID = 1;

    private boolean locationOn;
    private final Color textColor;
    private final Color backgroundColor;
    private int cycleTime;
    private int transition;

    @JsonCreator
    public Extra(@JsonProperty("location_on") boolean locationOn,
                 @JsonProperty("text_color_rgb") Color textColor,
                 @JsonProperty("background_color_rgb") Color backgroundColor,
                 @JsonProperty("cycle_time") int cycleTime,
                 @JsonProperty("transition") int transition) {

        this.locationOn = locationOn;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.cycleTime = cycleTime;
        this.transition = transition;

    }

    public boolean isLocationOn() {
        return locationOn;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public int getCycleTime() {
        return cycleTime;
    }

    public int getTransition() {
        return transition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Extra)) return false;

        Extra extra = (Extra) o;

        if (cycleTime != extra.cycleTime) return false;
        if (locationOn != extra.locationOn) return false;
        if (transition != extra.transition) return false;
        if (backgroundColor != null ? !backgroundColor.equals(extra.backgroundColor) : extra.backgroundColor != null)
            return false;
        if (textColor != null ? !textColor.equals(extra.textColor) : extra.textColor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (locationOn ? 1 : 0);
        result = 31 * result + (textColor != null ? textColor.hashCode() : 0);
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        result = 31 * result + cycleTime;
        result = 31 * result + transition;
        return result;
    }
}
