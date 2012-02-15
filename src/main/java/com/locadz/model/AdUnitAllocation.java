package com.locadz.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
@JsonPropertyOrder({ "extra", "rations" })
public final class AdUnitAllocation implements Serializable {

    private static final long serialVersionUID = 1;

    private final Extra extra;

    private final List<Ration> rations;

    @JsonCreator
    public AdUnitAllocation(
        @JsonProperty("extra") Extra extra,
        @JsonProperty("rations") List<Ration> rations
    ) {
        this.extra = extra;
        this.rations = rations;
    }

    public Extra getExtra() {
        return extra;
    }

    public List<Ration> getRations() {
        return rations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdUnitAllocation)) return false;

        AdUnitAllocation that = (AdUnitAllocation) o;

        if (extra != null ? !extra.equals(that.extra) : that.extra != null) return false;
        if (rations != null ? !rations.equals(that.rations) : that.rations != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = extra != null ? extra.hashCode() : 0;
        result = 31 * result + (rations != null ? rations.hashCode() : 0);
        return result;
    }
}
