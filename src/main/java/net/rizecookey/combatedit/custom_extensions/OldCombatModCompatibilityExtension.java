package net.rizecookey.combatedit.custom_extensions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rizecookey.combatedit.api.CombatEditApi;
import net.rizecookey.combatedit.api.CombatEditInitListener;
import net.rizecookey.combatedit.api.extension.DefaultsSupplier;
import net.rizecookey.combatedit.configuration.BaseProfile;
import net.rizecookey.combatedit.configuration.ProfileExtension;
import net.rizecookey.combatedit.configuration.representation.ItemAttributes;
import net.rizecookey.combatedit.configuration.representation.MutableConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OldCombatModCompatibilityExtension implements CombatEditInitListener {
    @Override
    public void onCombatEditInit(CombatEditApi combatEditApi) {
        combatEditApi.registerProfileExtension(ResourceLocation.fromNamespaceAndPath("combatedit", "1_8_combat"), this::provideExtension);
    }

    /**
     * This profile extension ensures that custom items not present in vanilla will have attack speed modifiers removed
     * from them by default.
     */
    public ProfileExtension provideExtension(BaseProfile baseProfile, DefaultsSupplier defaultsSupplier) {
        List<ItemAttributes> modifications = new ArrayList<>();

        Set<Item> modifiedItems = baseProfile.getConfiguration().getItemAttributes()
                .stream()
                .map(attr -> BuiltInRegistries.ITEM.getValue(attr.getItemId()))
                .collect(Collectors.toSet());

        BuiltInRegistries.ITEM.stream()
                .filter(item -> !modifiedItems.contains(item)
                        && !ItemAttributeModifiers.EMPTY.equals(defaultsSupplier.items().getVanillaAttributeModifiers(item)))
                .forEach(item -> {
                    ItemAttributeModifiers component = defaultsSupplier.items().getVanillaAttributeModifiers(item);
                    assert component != null;
                    if (component.modifiers().stream().noneMatch(entry -> entry.attribute().equals(Attributes.ATTACK_SPEED))) {
                        return;
                    }

                    List<ItemAttributes.ModifierEntry> newEntries = component.modifiers().stream()
                            .filter(entry -> !entry.attribute().equals(Attributes.ATTACK_SPEED))
                            .map(entry -> new ItemAttributes.ModifierEntry(
                                    entry.attribute().unwrapKey().orElseThrow().location(),
                                    entry.modifier().id(),
                                    entry.modifier().amount(),
                                    entry.modifier().operation(),
                                    entry.slot())).toList();

                    modifications.add(new ItemAttributes(BuiltInRegistries.ITEM.getKey(item), newEntries, true));
                });

        return new ProfileExtension(new MutableConfiguration(modifications, null, null, null), Integer.MIN_VALUE);
    }
}
