package net.rizecookey.combatedit.api;

/**
 * Represents an entry point that listens to the initialization of CombatEdit.
 */
public interface CombatEditInitListener {
    /**
     * This method is run when CombatEdit is being initialized. It provides the
     * CombatEdit API interface which can be used to register profile extensions.
     *
     * @param combatEditApi The CombatEdit API over which profile extension can be registered
     */
    void onCombatEditInit(CombatEditApi combatEditApi);
}
