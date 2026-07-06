package net.rizecookey.combatedit.custom_extensions;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.rizecookey.combatedit.CombatEdit;
import net.rizecookey.combatedit.api.CombatEditApi;
import net.rizecookey.combatedit.api.CombatEditInitListener;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;
import net.rizecookey.combatedit.configuration.Settings;
import net.rizecookey.combatedit.configuration.representation.ItemComponents;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SwordBlockingExtension implements CombatEditInitListener {
    private static final String BLOCK_COMPONENT_VALUE = "{disable_cooldown_scale:0,block_sound:\"minecraft:entity.player.hurt\",damage_reductions:[{base:0,factor:0.5,horizontal_blocking_angle:180,type:[\"arrow\",\"explosion\",\"fireball\",\"fireworks\",\"mob_attack\",\"mob_projectile\",\"player_attack\",\"player_explosion\",\"spear\",\"spit\",\"sting\",\"trident\",\"wither_skull\"]}]}";

    @Override
    public void onCombatEditInit(CombatEditApi combatEditApi) {
        combatEditApi.registerProfileExtension(this::provideExtension);
    }

    public ProfileExtension provideExtension(BaseProfile baseProfile, DefaultsSupplier defaultsSupplier) {
        Settings settings = CombatEdit.getInstance().getCurrentSettings();

        if (!settings.isSwordBlockingEnabled()) {
            return new ProfileExtension(new MutableConfiguration(), Integer.MIN_VALUE);
        }

        List<ItemComponents> modifications = new ArrayList<>();
        Identifier blocksAttacksId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(DataComponents.BLOCKS_ATTACKS);

        BuiltInRegistries.ITEM.getOrThrow(ItemTags.SWORDS).stream()
                .map(Holder::value)
                .forEach(item -> {
                    ItemComponents.ComponentChangeEntry modifierEntry = new ItemComponents.ComponentChangeEntry(
                            blocksAttacksId,
                            ItemComponents.ChangeType.SET,
                            BLOCK_COMPONENT_VALUE
                    );
                    ItemComponents components = new ItemComponents(BuiltInRegistries.ITEM.getKey(item), List.of(modifierEntry));
                    modifications.add(components);
                });

        return new ProfileExtension(new MutableConfiguration(null, modifications, null, null, null), Integer.MIN_VALUE + 1);
    }
}
