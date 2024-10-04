package net.rizecookey.combatedit.utils;

import net.minecraft.util.Identifier;

public final class ReservedIdentifiers {
    private ReservedIdentifiers() {}

    public static final String RESERVED_NAMESPACE = "combatedit.special";
    public static final Identifier ATTACK_DAMAGE_MODIFIER_ID_ALT = Identifier.of(RESERVED_NAMESPACE, "base_attack_damage");
    public static final Identifier ATTACK_SPEED_MODIFIER_ID_ALT = Identifier.of(RESERVED_NAMESPACE, "base_attack_speed");
}
