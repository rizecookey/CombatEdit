# CombatEdit
A server-side Fabric mod that brings back 1.8-style combat to modern versions of Minecraft! Can additionally configure
default attributes for all entities and items.

## Requirements
This mod does not require client-side installation or the Fabric API (necessary Fabric API modules are bundled with the mod),
however, to configure attributes with a GUI, Mod Menu is required to open the configuration screen.

## Features
The default settings of this mod bring back 1.8-style combat. Additionally (since version 2.0), the following options
are configurable either via the options screen or the configuration file (see [Configuration](#Configuration)):
- Default [entity attributes](https://minecraft.wiki/w/Attribute#Attributes) for living entities (max health,
attack damage, attack speed, movement speed, ...)
- Default [item attribute modifiers](https://minecraft.wiki/w/Attribute#Modifiers) for all item types (additional damage,
attack speed, ...)
- Which attack sounds should be enabled or disabled
- Whether 1.8-style knockback should be enabled

Please note that although changing item and entity attributes while in a world is possible and I've tried my best to make
it work as seamlessly as possible, it could potentially still lead to unexpected behaviour and (probably smaller) bugs.

## Configuration
CombatEdit uses different concepts for configuring itself:

### Settings file
This is the main way for players to configure the mod. Configurations made in the settings file take precedence over the
base profile and profile extension configurations, and the settings file determines which base profile is to be used.
For users, the main way to configure the settings is using the GUI screen accessible via ModMenu in the Mods list (by
clicking on the gear button once CombatEdit is selected). The settings file is saved in `.config/combatedit/settings.json`
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
of any Attack Cooldown as a balancing measure). See [Mod developers](#Mod Developers) for more information.

## Datapacks
TODO

## Mod Developers
TODO