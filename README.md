# RPGCore
A bukkit/spigot plugin that adds classes to the game
## Table of Contents
- [Overview](#Overview)
- [Classes](#Classes)
  - [Knight](#Knight)
    - [Basics](#Basics)
    - [Abilities](#Abilities)
    - [Leveling](#Leveling)
- [Backpack](#Backpack)
- [Commands](#Commands)
- [Permissions](#Permissions)
- [Issues](#Issues)
## Overview
Forces player to choose class on first join  
Shows current lvl and xp and exp for next lvl up in scoreboard  
## Classes
### Knight
#### Basics
+2% dmg with sword  
+1 durability on armor
#### Abilities
2m Cooldown  
- Sword Dash: Right Click a Player to deal damage with Strength 10
- Easy Crit: Crit (+ 30% dmg) with every hit for 5 sec
- Stun Blow: On next crit on another player the target gets 10 sec Blindness
- Bleed: 5% chance on hit to activate Bleed effect on a Player (loses 0.5 hearts/sec for 10 sec) \#op
#### Leveling
Killing...  
EnderDragon/Wither: 100xp  
Monster: 10xp  
Animal: 5xp  
Player: 20xp  
## Backpack
![Backpack Recipe](/src/main/resources/BackpackRecipe.png =250x)
## Commands
- /reloadrpgcore: Reloads the config
## Permissions
```
RPGCore.*:
    description: Grants access to all RPGCore commands
    children:
      RPGCore.admin:
        description: Grants access to all RPGCore admin commands
        children:
          RPGCore.reload:
            description: Perm to reload the config
      RPGCore.user:
        description: Grants access to all RPGCore user commands
        children:
```
## Issues
- Players have to rejoin on server reload
