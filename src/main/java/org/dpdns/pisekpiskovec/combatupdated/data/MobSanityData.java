package org.dpdns.pisekpiskovec.combatupdated.data;

import java.util.List;

public record MobSanityData(List<InflictEntry> panicGains) {

    /** Sentinel - mob has no sanity section. */
    public static final MobSanityData NONE = new MobSanityData(List.of());

    public boolean isPresent() {
        return this != NONE;
    }
}
