package org.dpdns.pisekpiskovec.combatupdated.api;

public interface IAdvancedEntity {
    RiskLevel getRiskLevel();
    ResistanceType getResistance(AttackType attackType);
    boolean isStaggered();
    ResistanceType getStaggered();
    void setStaggered(boolean isStaggered);
}
