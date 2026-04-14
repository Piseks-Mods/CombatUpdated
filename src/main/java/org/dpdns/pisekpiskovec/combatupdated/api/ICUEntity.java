package org.dpdns.pisekpiskovec.combatupdated.api;

public interface ICUEntity {
    RiskLevel getRiskLevel();

    ResistanceType getResistance(AttackType attackType);

    boolean isStaggered();

    void setStaggered(boolean staggered);

    default ResistanceType getEffectiveResistance(AttackType attackType) {
        return isStaggered() ? ResistanceType.FATAL : getResistance(attackType);
    }
}
