package net.rizecookey.combatedit.configuration;

import net.rizecookey.combatedit.entity.EntityAttributeModifierProvider;
import net.rizecookey.combatedit.item.ItemAttributeModifierProvider;

public record Configuration(ItemAttributeModifierProvider itemModifierConfiguration, EntityAttributeModifierProvider entityModifierConfiguration) {
}
