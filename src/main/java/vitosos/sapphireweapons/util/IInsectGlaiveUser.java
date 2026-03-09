package vitosos.sapphireweapons.util;

public interface IInsectGlaiveUser {
    boolean isVaulting();
    void setVaulting(boolean vaulting);

    int getVaultWindupTicks();
    void setVaultWindupTicks(int ticks);

    boolean canAirDodge();
    void setCanAirDodge(boolean canAirDodge);

    int getGlaiveAttackTicks();
    void setGlaiveAttackTicks(int ticks);
    void decrementGlaiveAttackTicks();

    boolean isGlaiveAttackingActive();
    void setGlaiveAttackingActive(boolean active);

    int getAerialChainCount();
    void setAerialChainCount(int count);

    int getNukeWindupTicks();
    void setNukeWindupTicks(int ticks);

    boolean hasUsedAerialAttack();
    void setHasUsedAerialAttack(boolean used);

    int getGlaiveInvulnTicks();
    void setGlaiveInvulnTicks(int ticks);

    boolean isKinsectDeployed();
    void setKinsectDeployed(boolean deployed);

    int getRedEssenceTicks();
    void setRedEssenceTicks(int ticks);

    int getWhiteEssenceTicks();
    void setWhiteEssenceTicks(int ticks);

    int getOrangeEssenceTicks();
    void setOrangeEssenceTicks(int ticks);

    int getTripleBuffTicks();
    void setTripleBuffTicks(int ticks);

    void grantEssence(int type); // 0 = Red, 1 = White, 2 = Orange
}
