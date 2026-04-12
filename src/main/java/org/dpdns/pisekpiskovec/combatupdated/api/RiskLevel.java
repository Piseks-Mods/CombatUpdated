package org.dpdns.pisekpiskovec.combatupdated.api;

public enum RiskLevel {
    // Constants
    ZAYIN, TETH, HE, WAW, ALEPH;

    public int diffFrom(RiskLevel other) {
        return this.ordinal() - other.ordinal();
    }
}
