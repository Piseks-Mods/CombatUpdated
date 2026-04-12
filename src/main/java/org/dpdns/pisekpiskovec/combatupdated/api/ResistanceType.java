package org.dpdns.pisekpiskovec.combatupdated.api;

public enum ResistanceType {
    // Constants
    FATAL(2.0), WEAK(1.5), NORMAL(1.0), ENDURED(0.5), INEFFECTIVE(0.25);

    private final double multiplicator;

    ResistanceType(double multiplicator) {
        this.multiplicator = multiplicator;
    }

    public double getMultiplicator() {
        return multiplicator;
    }
}
