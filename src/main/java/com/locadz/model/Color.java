package com.locadz.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 *
 */
public final class Color implements Serializable {

    private static final long serialVersionUID = 1;

    private int red;

    private int green;

    private int blue;

    private int alpha;

    @JsonCreator

    public Color(
        @JsonProperty("red") int red,
        @JsonProperty("green") int green,
        @JsonProperty("blue") int blue,
        @JsonProperty("alpha") int alpha) {

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        if (alpha == 0) {
            return String.format("#%02X%02X%02X", red, green, blue);
        }
        return String.format("#%X%02X%02X%02X", alpha, red, green, blue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color)) return false;

        Color color = (Color) o;

        if (alpha != color.alpha) return false;
        if (blue != color.blue) return false;
        if (green != color.green) return false;
        if (red != color.red) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = red;
        result = 31 * result + green;
        result = 31 * result + blue;
        result = 31 * result + alpha;
        return result;
    }
}
