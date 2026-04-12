package org.dpdns.pisekpiskovec.combatupdated.api;

public interface IAdvancedEntity {
    public RiskLevel getRiskLevel();
    public ResistanceType getResistance(AttackType attackType);
    public boolean isStaggered();
}
