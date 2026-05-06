package org.dpdns.pisekpiskovec.combatupdated.api;

public enum RiskLevel {
    // Constants
    ZAYIN, TETH, HE, WAW, ALEPH;

    public int diffFrom(RiskLevel other) {
        return this.ordinal() - other.ordinal();
    }

    /**
     * Returns the greater of two Risk values. That is, the result is the argument closer to the value of ALEPH. If the arguments have the same value, the result is that same value.
     *
     * @param that another argument.
     * @return the larger of this and that.
     */
    public RiskLevel max(RiskLevel that) {
        return (this.ordinal() >= that.ordinal()) ? this : that;
    }

    /**
     * Returns the smaller of two Risk values. That is, the result the argument closer to the value of ZAYIN. If the arguments have the same value, the result is that same value.
     *
     * @param that another argument.
     * @return the smaller of this and that.
     */
    public RiskLevel min(RiskLevel that) {
        return (this.ordinal() <= that.ordinal()) ? this : that;
    }
}