# RPGCore
A bukkit/spigot plugin that adds classes to the game  
Idea and [concept](https://sourceb.in/beb6d82099) by Xp10d3  
## Table of Contents
- [Overview](#Overview)
- [Classes](#Classes)
  - [Knight](#Knight)
    - [Basics](#Basics)
    - [Abilities](#Abilities)
    - [Leveling](#Leveling)
  - [Mage](#Mage)
    - [Basics](#Basics)
    - [Spells](#Spells)
    - [Leveling](#Leveling)
  - [Archer](#Archer)
    - [Basics](#Basics)
    - [Spells](#Spells)
    - [Leveling](#Leveling) 
  - [Citizen](#Citizen)
- [Recipes](#Recipes)
- [Commands](#Commands)
  - [AdminCommands](#Admin&nbsp;Commands)
  - [User&nbsp;Commands](#User&nbsp;Commands)
- [Permissions](#Permissions)
- [Issues](#Issues)
## Overview
Forces player to choose class on first join  
Shows current lvl and xp and exp for next lvl up in scoreboard  
Spawns spell books in Lootchests in Dungeons  
Spell books can be combined by dropping them together  
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
### Mage
#### Basics
Chance of 2/5/7/15% on level 1/5/10/50 to prevent damage on hit  
#### Spells
All spells can be leveled up
- Projectile (1 sec): Just a normal arrow
- Fireball (5 sec): Shoots a fireball that causes a big explosion but not much damage  
- Freeze (5 sec): Freezes a few blocks of water in a small radius  
- Poison (30 sec): Right click a player to give him poison for a few seconds  
- Lightning (60 sec): Summons a lightning where the player is looking at
- Retreat (10-40 sec): Player gets invisible and speed 3 effect for a few seconds but can't hit a player
- Invsteal (15 sec): Steal an item from the target's inventory (10% chance)
#### Leveling
Gets spellLevel / 2 ep for every spell they cast (min 1)
### Archer
#### Basics
+20/25/30% damage with bow on level 1/10/50  
3/7% chance of automatically hitting the nearest entity when shooting  
Speed 1  
Armor has -2 durability on level < 20
#### Spells
Arrowrain (10 sec): 
- Unlock at lvl 5  
- Arrows on fire on lvl 10
- Shoots 10 arrows at once (have to be in your inv)  
#### Leveling
+5 ep for every hit on an entity  
### Citizen
- Just a normal Minecrafter  
## Backpacks
![Ability Backpack Recipe](/src/main/resources/BackpackRecipe.png)  
![Spell Backpack Recipe](/src/main/resources/SpellBackpackRecipe.png)  
![Arrowrain Recipe](/src/main/resources/ArrowrainRecipe.png)  
![Wand Recipe](/src/main/resources/WandRecipe.png)  
## Commands
### Admin&nbsp;Commands
- /aswitch \[Player] [Class]: Switches Players class to Class  
- /areset [Player]: Resets Players stats in his active class  
- /reloadrpgcore: Reloads the config  
### User&nbsp;Commands
- All Abilities and Spells can be activated with a Command  
- /class: Spawns a Hologram with all information about the active class. Despawns after 30 sec  
- /switch: Saves Inventory to file and opens the switch dialog from the first join  
- /reset: Resets the stats of the currently active class  
## Permissions
```
RPGCore.*:
    description: Grants access to all RPGCore commands
    children:
      RPGCore.admin:
        description: Grants access to all RPGCore admin commands
        children:
          RPGCore.forceswitch:
            description: Perm to change a players class
            default: op
          RPGCore.forcereset:
            description: Perm to reset a players class
            default: op
          RPGCore.reload:
            description: Perm to reload the config
            default: op
      RPGCore.user:
        description: Grants access to all RPGCore user commands
        children:
          RPGCore.use:
            description: Perm to use the RPG system
            default: true
          RPGCore.class:
            description: Perm to show info about your class
            default: true
          RPGCore.switch:
            description: Perm to switch your class
            default: true
          RPGCore.reset:
            description: Perm to reset your class
            default: true
```
## Issues
- Players have to rejoin on server reload
