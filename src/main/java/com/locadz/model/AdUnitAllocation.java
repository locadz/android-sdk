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
