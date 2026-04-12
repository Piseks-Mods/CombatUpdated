package org.dpdns.pisekpiskovec.combatupdated.api;

public enum StaggerType {
    // Constants
    STAGGER(ResistanceType.FATAL);

    private final ResistanceType resistanceType;

    StaggerType(ResistanceType resistanceType) {
        this.resistanceType = resistanceType;
    }

    public ResistanceType getResistance() {
        return resistanceType;
    }
}
