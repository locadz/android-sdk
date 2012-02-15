package com.locadz.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 *
 */
public class Ration implements Serializable {

    private static final long serialVersionUID = 1;

    private final String allocationId;
    private final String networkName;
    private final int networkId;
    private final int weight;
    private final int priority;
    private final String key;

    @JsonCreator
    public Ration(@JsonProperty("nid") String allocationId,
                  @JsonProperty("nname") String networkName,
                  @JsonProperty("type") int networkId,
                  @JsonProperty("weight") int weight,
                  @JsonProperty("priority") int priority,
                  @JsonProperty("key") String key) {

        this.allocationId = allocationId;
        this.networkName = networkName;
        this.networkId = networkId;
        this.weight = weight;
        this.priority = priority;
        this.key = key;
    }

    public String getAllocationId() {
        return allocationId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public int getNetworkId() {
        return networkId;
    }

    public int getWeight() {
        return weight;
    }

    public int getPriority() {
        return priority;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ration)) return false;

        Ration ration = (Ration) o;

        if (networkId != ration.networkId) return false;
        if (priority != ration.priority) return false;
        if (weight != ration.weight) return false;
        if (allocationId != null ? !allocationId.equals(ration.allocationId) : ration.allocationId != null)
            return false;
        if (key != null ? !key.equals(ration.key) : ration.key != null) return false;
        if (networkName != null ? !networkName.equals(ration.networkName) : ration.networkName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = allocationId != null ? allocationId.hashCode() : 0;
        result = 31 * result + (networkName != null ? networkName.hashCode() : 0);
        result = 31 * result + networkId;
        result = 31 * result + weight;
        result = 31 * result + priority;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
