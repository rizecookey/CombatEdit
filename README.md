# CombatEdit
A server-side Fabric mod that brings back 1.8-style combat to modern versions of Minecraft! Can additionally configure
default attributes for all entities and items and default item components.

## Requirements
This mod does not require client-side installation or the Fabric API (necessary Fabric API modules are bundled with the mod),
however, to configure attributes with a GUI, Mod Menu is required to open the configuration screen.

## Features
The default settings of this mod bring back 1.8-style combat. Additionally (since version 2.1), the following options
are configurable either via the options screen or the configuration file (see [Configuration](#Configuration)):
- Default [entity attributes](https://minecraft.wiki/w/Attribute#Attributes) for living entities (max health,
attack damage, attack speed, movement speed, ...)
- Default [item attribute modifiers](https://minecraft.wiki/w/Attribute#Modifiers) for all item types (additional attack
damage, attack speed, ...)
- Default [item components](https://minecraft.wiki/w/Data_component_format) for all item types (durability, unbreakability,
enchantments, ...)
- Which attack sounds should be enabled or disabled
- Whether 1.8-style knockback should be enabled

Please note that although changing item and entity defaults while in a world is possible and I've tried my best to make
it work as seamlessly as possible, it could potentially still lead to unexpected behaviour and (probably smaller) bugs.

## Configuration
CombatEdit uses different concepts for configuring itself:

### Settings file
This is the main way for players to configure the mod. Configurations made in the settings file take precedence over the
base profile and profile extension configurations, and the settings file determines which base profile is to be used.
For users, the main way to configure the settings is using the GUI screen accessible via ModMenu in the Mods list (by
clicking on the gear button once CombatEdit is selected). The settings file is saved in `config/combatedit/settings.json`
relative to the minecraft/server directory.

To configure these options on a server, you can create and save your configuration on the client using the configuration screen
and then upload the config file (in `config/combatedit/settings.json`) to the same directory inside the server directory.

### Base profiles
Base profiles determine the main configuration for entity and item attributes. CombatEdit includes two base profiles:
- the 1.8 Combat Profile (`combatedit:1_8_combat`), which is enabled by default and sets entity and item attributes values
to resemble a modern version of 1.8 Combat (using the same damage values as Bedrock Edition)
- the Vanilla profile (`combatedit:vanilla`), which leaves default entity and item attributes untouched.

Datapack creators can provide their own base profiles (see [Datapacks](#Datapacks)) to create different base configurations.

### Profile extensions
Profile extensions allow adjusting the configuration of a base profile (such as adding addition item modifiers or overriding
others set in the base profile). They are aimed at mod developers who want to provide different item or entity settings for
base profiles (for example by changing damage values for the 1.8 Combat profile on custom items to compensate for the lack
of any Attack Cooldown as a balancing measure). See [Mod developers](#Mod-Developers) for more information.

## Datapacks
Both base profiles and profile extension can be included in a datapack. The following directory tree describes where to
place base profile and profile extension files.
- `data`: the data directory within your datapack
  - `<your_namespace>`: Your custom datapack namespace
    - `combatedit`: All CombatEdit-related datapack files reside within here
      - `profile_extensions`: Contains profile extensions.
        - `<profile_namespace>`
          - `<profile_name>`: Contains `.json` profile extension files extending the base profile 
            `<profile_namespace>:<profile_name>`
      - `base_profiles`: Contains base profile `.json` files. A file named `<your_profile>.json` created here will create
        a base profile with the ID `<your_namespace>:<your_profile>`.

## Mod Developers
Mod developers can include base profiles in JSON format just like datapack creators by placing files inside their `data`
resource directory using the same directory structure as mentioned above for datapacks. Additionally, it is possible to
provide profile extensions programmatically: simply create a class implementing the
[CombatEditInitListener interface](src/main/java/net/rizecookey/combatedit/api/CombatEditInitListener.java) and specify
this implementation as a new entrypoint with the name `combatedit` within your `fabric.mod.json`. 

## JSON Format specifications
This section explains the JSON formats of the various configuration types. Sample datapacks for base profile and profile
extension creation can be found in the [examples](examples) directory.

### Base profiles
A base profile in JSON format is a JSON object containing the following keys:
- `name`: The display name of your base profile. Can be a text component or a simple string.
- `description`: The description of your base profile. Can be a text component or a simple string.
- `configuration`: The feature configuration for this base profile. See [Feature configuration](#feature-configuration).

### Profile extensions
A profile extension in JSON format is a JSON object containing the following keys:
- `priority`: An integer specifying the priority of this profile extension. A greater priority value results in this
  profile extension having priority over other profile extensions if overrides within them clash with each other.
- `configuration_overrides`: A feature configuration specifying the overrides this extension makes. See
  [Feature configuration](#feature-configuration).

### Feature configuration
The feature configuration format is common among base profiles, profile extensions and the settings file. It has the
following keys:
- `configuration_version`: An integer specifying the version of the feature configuration, which may change if breaking
  changes are made to the format. The current configuration version is `3`.
- `entity_attributes`: An array of [Entity attribute](#entity-attribute) objects specifying the default attributes for an
   entity type.
- `item_attributes`: An array of [Item attribute](#item-attribute) objects specifying the default attribute modifiers for
   an item.
- `item_components`: An array of [Item component](#item-component) objects specifying the default components for an item.
- `enabled_sounds`: A map of sound ID to boolean pairs specifying whether those sounds are enabled or not. Currently, the
  following sounds are configurable:
  - `minecraft:entity.player.attack.nodamage`
  - `minecraft:entity.player.attack.knockback`
  - `minecraft:entity.player.attack.weak`
  - `minecraft:entity.player.attack.strong`
  - `minecraft:entity.player.attack.sweep`
  - `minecraft:entity.player.attack.crit`
- `misc_options`: An object configuring [Miscellaneous options](#miscellaneous-options).

#### Entity attribute
Entity attribute entries have the following structure:
- `entity_id`: The identifier of the entity type to be modified
- `base_values`: An array of attribute base value objects, which have the following keys:
  - `attribute`: The identifier of the attribute whose value is changed
  - `base_value`: The base value to use for the specified attribute
- `override_default`: Boolean option. If set to `true`, the entity's default attributes are removed before applying the
  the changes specified in this entity attribute entry

#### Item attribute
Item attribute entries have the following structure:
- `item_id`: The identifier of the item to be modified
- `modifiers`: An array of attribute modifier objects, which have the following structure:
  - `attribute`: The attribute for which to add a modifier
  - `modifier_id`: A unique identifier for the attribute modifier. Optional, if not specified, a random identifier is
     used instead
  - `value`: The value for the modifier
  - `operation`: Operation type for the modifier. Can be one of the following:
    - `"add_value"`: Adds the value of this modifier to the base value of the attribute for the entity
    - `"add_multiplied_base"`: Multiplies the base value for the attribute by the specified value and adds the result to
      the total attribute value
    - `"add_multiplied_total"`: Multiplies the sum of the base value and all modifiers of type
      `"add_multiplied_base"` and adds the result to the total attribute value
  - `slot`: The slot in which the item has to be for the modifier to apply. Can be one of the following:
    - `"any"`: Applies in all slots
    - `"mainhand"`: Applies when the item is held in the main hand
    - `"offhand"`: Applies when the item is held in the off hand
    - `"hand"`: Applies when the item is held in any hand
    - `"feet"`: Applies when the item is equipped in the boots slot
    - `"legs"`: Applies when the item is equipped in the leggings slot
    - `"chest"`: Applies when the item is equipped in the chestplate slot
    - `"head"`: Applies when the item is equipped in the helmet slot
    - `"armor"`: Applies when the item is equipped in any armor slot (on players or animals)
    - `"body"`: Applies when the item is equipped on an animal
    - `"saddle"`: Applies when the item is equipped on a horse's saddle slot
- `override_default`: Boolean option. If set to `true`, the item's default attribute modifiers are removed before applying
  the changes specified by this entry

#### Item component
Item component entries have the following structure:
- `item_id`: The identifier of the item whose components are to be modified
- `changes`: An array of component changes to be made, which have the following structure:
  - `component_type`: The identifier of the component type for which to change the value
  - `change_type`: The change to be made by this entry, which is either:
    - `"set"` if a new value for this component type is to be set
    - `"remove"` if the default value for this component is to be removed from the item entirely
  - `value`: The value to set for this component. Is optional if the component type does not have values or the change
    type is `"remove"`

### Miscellaneous options
- `enable_1_8_knockback`: A boolean specifying whether 1.8 knockback should be enabled instead of the default knockback
  in modern Minecraft versions.
- `disable_sweeping_without_enchantment`: A boolean specifying whether sweeping effects should be disabled if the used
  item does not have a sweeping enchantment.