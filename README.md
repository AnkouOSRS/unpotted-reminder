# Unpotted Reminder

### Description
This plugin reminds you to drink your boost potions when you are in combat and have a boost potion in your inventory. 
The overlay will go away once you drink a potion or after the customizable timeout.
You can configure a threshold where you will only be reminded once your stats fall below a certain level.

### Note
Defensive attack styles for ranged and magic are currently not supported, and defense is instead tracked as a melee skill 
for all intents and purposes. Magic is currently unable to differentiate between casting combat and non-combat skills and 
as such is **disabled by default**.

### Changelog
**11/11/22**
- Initial Release  

**12/1/22**
- Added support for regular combat, attack, and strength potions, and both divine and regular super attack and super strength potions. 
- Added example video to this readme.  

**12/13/22** 
- Added support for defensive magic and ranged attack styles.
- Check strength boost instead of defense boost when attacking with defensive melee.

**1/12/23**
- Added optional cooldown for notifier.
- Only alert for imbued heart when it is actually available. Credit to JuliusAF on GitHub for most of this logic.

**7/27/24**
- Add new options for which style to check the boost for when using melee. You can choose between strength only or both 
attack and strength. Defense boost is effectively no longer being checked.
- Add saturated heart and fix the logic that determines if your imbued or saturated heart is available.
- Add Divine magic potions.
- Fix longrange on Tumeken's shadow, which was incorrectly tracking as melee.
- Fix issue where sipping potions or using smelling salts in TOA caused an alert or failed to clear an existing one