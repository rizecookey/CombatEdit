# CombatEdit
A server-side Fabric mod that brings back 1.8-style combat to modern versions of Minecraft! Can additionally configure
default attributes for all entities and items.

This mod does NOT require client-side installation or the Fabric API, however, to configure attributes with a GUI,
Mod Menu is required to open the configuration screen.

## Features
The default settings of this mod brings back 1.8-style combat. Additionally (since version 2.0), the following options
are configurable either via the options screen or the configuration file:
- Default [entity attributes](https://minecraft.wiki/w/Attribute#Attributes) for living entities (max health,
attack damage, attack speed, movement speed, ...)
- Default [item attribute modifiers](https://minecraft.wiki/w/Attribute#Modifiers) for all item types (additional damage,
attack speed, ...)
- Which attack sounds should be enabled or disabled
- Whether 1.8-style knockback should be enabled

To configure these options on a server, you can create and save your configuration on the client using the configuration screen
and then upload the config file (in `config/combatedit/config.json`) to the same directory inside the server directory.