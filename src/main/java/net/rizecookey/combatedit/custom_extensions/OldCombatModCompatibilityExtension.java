package net.rizecookey.combatedit.custom_extensions;

import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
        combatEditApi.registerProfileExtension(Identifier.of("combatedit", "1_8_combat"), this::provideExtension);
    }

    /**
     * This profile extension ensures that custom items not present in vanilla will have attack speed modifiers removed
     * from them by default.
     */
    public ProfileExtension provideExtension(BaseProfile baseProfile, DefaultsSupplier defaultsSupplier) {
        List<ItemAttributes> modifications = new ArrayList<>();

        Set<Item> modifiedItems = baseProfile.getConfiguration().getItemAttributes()
                .stream()
                .map(attr -> Registries.ITEM.get(attr.getItemId()))
                .collect(Collectors.toSet());

        Registries.ITEM.stream()
                .filter(item -> !modifiedItems.contains(item)
                        && !AttributeModifiersComponent.DEFAULT.equals(defaultsSupplier.items().getVanillaAttributeModifiers(item)))
                .forEach(item -> {
                    AttributeModifiersComponent component = defaultsSupplier.items().getVanillaAttributeModifiers(item);
                    assert component != null;
                    if (component.modifiers().stream().noneMatch(entry -> entry.attribute().equals(EntityAttributes.ATTACK_SPEED))) {
                        return;
                    }

                    List<ItemAttributes.ModifierEntry> newEntries = component.modifiers().stream()
                            .filter(entry -> !entry.attribute().equals(EntityAttributes.ATTACK_SPEED))
                            .map(entry -> new ItemAttributes.ModifierEntry(
                                    entry.attribute().getKey().orElseThrow().getValue(),
                                    entry.modifier().id(),
                                    entry.modifier().value(),
                                    entry.modifier().operation(),
                                    entry.slot())).toList();

                    modifications.add(new ItemAttributes(Registries.ITEM.getId(item), newEntries, true));
                });

        return new ProfileExtension(new MutableConfiguration(modifications, null, null, null), Integer.MIN_VALUE);
    }
}
