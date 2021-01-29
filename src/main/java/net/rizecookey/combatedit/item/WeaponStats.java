package net.rizecookey.combatedit.item;

import net.minecraft.item.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WeaponStats {
    static Map<Class<? extends ToolItem>, Function<ToolItem, Float>> damageOverrides = new HashMap<>();

    static {
        damageOverrides.put(SwordItem.class, toolItem -> toolItem.getMaterial().getAttackDamage() + 4.0F);
        damageOverrides.put(AxeItem.class, toolItem -> toolItem.getMaterial().getAttackDamage() + 3.0F);
        damageOverrides.put(PickaxeItem.class, toolItem -> toolItem.getMaterial().getAttackDamage() + 2.0F);
        damageOverrides.put(ShovelItem.class, toolItem -> toolItem.getMaterial().getAttackDamage() + 1.0F);
        damageOverrides.put(HoeItem.class, toolItem -> toolItem.getMaterial().getAttackDamage());
    }

    public static float getAttackDamage(ToolItem toolItem) {
        Class<? extends ToolItem> toolClass = toolItem.getClass();
        if (damageOverrides.containsKey(toolClass)) {
            return damageOverrides.get(toolClass).apply(toolItem);
        }
        return 0.0F;
    }

    public static boolean changeAttackDamage(ToolItem toolItem) {
        return toolItem.getMaterial() != null && damageOverrides.containsKey(toolItem.getClass());
    }
}
