package net.rizecookey.combatedit.item;

import net.minecraft.item.*;

public class WeaponStats {
    public static float getAttackDamage(Class<? extends Item> itemClass, ToolMaterial material) {
        if (SwordItem.class.isAssignableFrom(itemClass)) {
            return material.getAttackDamage() + 4.0F;
        }
        else if (AxeItem.class.isAssignableFrom(itemClass)) {
            return material.getAttackDamage() + 3.0F;
        }
        else if (PickaxeItem.class.isAssignableFrom(itemClass)) {
            return material.getAttackDamage() + 2.0F;
        }
        else if (ShovelItem.class.isAssignableFrom(itemClass)) {
            return material.getAttackDamage() + 1.0F;
        }
        else if (HoeItem.class.isAssignableFrom(itemClass)) {
            return material.getAttackDamage();
        }
        return 0.0F;
    }
}
