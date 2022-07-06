# Spells & Shields

![WIP](https://i.imgur.com/seFcWq0.png "This project is WIP! The basic structure is far advanced but the mod is still missing features. A lot more spells will be added to it!")

![GIF](https://thumbs.gfycat.com/UniformSelfreliantAdder-size_restricted.gif "Demonstration GIF")

## Features

- A new **mana** system that works similarly to the health bar.
- Numerous **spells** which can be learned by exploring different **spell trees**.
- New **potions**, **enchantments** and **effects** to properly embed the new features into vanilla Minecraft.
- The physical aspect of PvP is still very strong, viable and deadly against magic.
- Different **configs** and **commands** to allow proper **customization** for players and servers.
- Every new feature and mechanic is well thought through to try and not make any vanilla aspect obsolete.

### Spells

Spells can be equipped in **spell trees** after learning them. You have **5 spell slots** available in total. Each spell slot can be equipped individually with a spell. To fire that spell you use the **key binding** associated with the slot the spell is in. You can always view your equipped spells in your survival inventory. Spells typically just consume mana as a cost to fire them, but it can differ for each spell.

### Spell Trees

You gain access to your available spells and spell trees in the **Enchanting Table** (yes, the vanilla one). Similar to enchantments, you unlock spells by consuming experience levels. The cost for each spell can differ. The spells and spell trees you can interact with on an Enchanting Table are **dependent on the amount of bookshelves** placed around it (maximum amount is 32). This means that you can not equip or learn certain spells and spell trees if the surrounding enchanting power (bookshelves) is too low (they will not show up at all).

### Mana

Mana is the source of energy to perform spells. The mana regeneration is always static at **1 full mana unit per 5 seconds** (you can hold up to 10 mana units by default). Of course, your current mana can be influenced by different potions and effects. The mana bar is shown in blue, unless the Leaking effect is applied to you, in which case the colour turns into a slight green. Additionally, there is **extra mana** which is shown in pourple. Extra mana works just like the absorption effect works in terms of health: Whenever you burn mana while having extra mana it is burned from the extra mana resource. Extra mana is given in certain occasions or by certain effects and does not regenerate once burned. There is also a mana boost effect that may affect your maximum mana.

### Potions

There are new potions which interact with your new mana resource. They mirror those potions that interact with your health in time and amplification:

- **Potion of Instant Mana** = **Water Bottle** + **Lapis Lazuli**: Instantly restores mana, similar to the Potion of Healing.
- **Potion of Mana Bomb** = **Potion of Instant Mana** + **Fermented Spider Eye**: Instantly burns mana, similar to the Potion of Harming.
- **Potion of Replenishment** = **Water Bottle** + **Tube Coral Fan**: Restores mana over time, similar to the Potion of Regeneration.
- **Potion of Leaking** = **Water Bottle** + **Dead Tube Coral Fan**: Burns mana over time, similar to the Potion of Poison.

### Enchantments

- **Magic Protection**: Applied to **armour**. Works similar to existing "specific" Protection enchantments (eg. Projectile Projection): Protects twice as well against magic damage as the Protection enchantment does but is incompatible with other Protection enchantments.
- **Mana Blade**: TODO Consumes mana to increase damage on hit.
- **Mana Shield**: TODO

### Commands

- **/spells progression learn &lt;targets&gt; &lt;spell&gt;**: Lorem ipsum.
- **/spells progression learn &lt;targets&gt; all**: Lorem ipsum.
- **/spells progression forget &lt;targets&gt; &lt;spell&gt;**: Lorem ipsum.
- **/spells progression forget &lt;targets&gt; all**: Lorem ipsum.
- **/spells progression reset &lt;targets&gt;**: Lorem ipsum.
- **/spells slots set &lt;targets&gt; &lt;slot&gt; &lt;spell&gt;**: Lorem ipsum.
- **/spells slots remove &lt;targets&gt; &lt;slot&gt;**: Lorem ipsum.
- **/spells slots clear &lt;targets&gt;**: Lorem ipsum.

## Configuration Files

- You can configure both basic client side (eg. where to put the Mana Bar) and server side settings.
- Every **spell** is **individually configurable**, including their mana costs, consumed items when firing them or their individual effects (eg. damage of a Fire Ball spell).
- **Spell trees** can be redesigned, removed or added to your liking. Every from spells to required bookshelves is customizable.
- Configuration files **do not need** to be **synchronized from the server to the client**. The server can keep exact spells settings and their spell trees secret with the information only being revealed on players' discovery.
- All configuration files are inside the **.minecraft/config/spells_and_shields** folder.

### client.toml and server.toml

Contains very basic settings. Everything is explained inside said configuration files. Most values can be changed while the game is open.

### spell_trees Folder

Every available spell tree is loaded from in here. As explained, you can add, remove or edit any file in here. Every .json file is seen as potential spell tree and parsed (the file name does not matter). If a file is of invalid format it will not be loaded and an error is dispatched in the log file (the game continues and does not crash).

### spells Folder

Most spells are configurable and their config files are in here. Only ever edit the values of these files - do not add, remove or rename any file in here as the game would simple ignore them or recreate them in their default setting.
