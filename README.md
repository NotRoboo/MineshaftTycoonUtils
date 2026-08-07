# MineshaftTycoon Utils

A Fabric client-side mod that adds quality-of-life tools on MineshaftTycoon — fishing tracking, refinery profit tracking, chat cleanup, and a warp shortcut, all wrapped in an in-game config menu.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for **Minecraft 1.21.11**.
2. Download and install the following required dependencies into your `mods` folder:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
3. Download the latest `mineshafttycoonutils` jar and drop it into your `mods` folder.
4. Launch Minecraft with the Fabric profile.
5. Run `/mstu` in-game to open the settings menu and configure things to your liking.

## Features

### Fishing Tracker & HUD
- Tracks sea creatures, treasure drops, plates, crates, trophy fish, double hooks, and Fortune Fragments as you fish.
- On-screen HUD with a fully draggable section order (Total, Treasure Drops, Trophy Fish, Sea Creatures, Crates) — remove a section from the order to hide it without losing the underlying count.
- Optional colored names for rarities, per-category breakdown lists, and a toggle to only show the HUD while you're actually in a designated fishing zone.
- Fortune Fragment and plate totals can show both the catch count and the raw item total (e.g. `Fortune Frags: 4 (495)`).
- Reset anytime with `/mstu resetfishinghud`.

### Profit Tracker & HUD
- Watches ore blocks as you mine them and estimates coins/hour and total profit.
- Understands refinery-based T5 ores and T4 ores.
- Auto-detects your refinery levels by reading the Refinery menu, and your Dune Ram pet level by reading the Pets menu — no manual setup needed.
- Optional Cash Register (+3%) boost toggle.
- Tracks rare ore drops (Lunar Fragment, Basalt Shard, Chisilite Shard, and more) with a HUD breakdown.
- Numbers can be shown in full or shortened (e.g. `489.39T` instead of `489,390,000,000,000`).
- Reset anytime with `/mstu resetprofittracker`.

### Timers/Durations HU
- Reads time remaining of different potions/buffs from `/potsoff` menu
- Reads both `/potsdur` and hologram for PetAd timer
- Reads Il's restock timer from `Il's Wares`menu
- TODO - Add grown plant status (Will be very basic "Plot #: Growing|Grown")
- TODO - Forge/Compactor/Other useful timers

### Chat Message Hider
Individually toggleable filters to cut down on chat spam:
- T1–T4 join/welcome-back messages
- Pet XP messages
- General pet messages
- Fishing catch spam (rare/ultra-rare drops always still show)
- Fortune Fragment spam
- Discord advertisement messages
- PvE defeat and CPS-limit warning messages

### Warp & PetHelper
- Type `/warp <name>` or `/pet <name>` to open the warp menu and automatically click the matching warp or pet for you (covers both the Warp and Space Warp menus).
- Tab-completion suggests short warp names as you type.
- Disabled by default — enable it in the config first, and use at your own risk since it interacts with the server's menu automatically.

### Config GUI
- Open the full settings menu at any time, with an optional search query to jump straight to a setting.

## Commands

| Command | Description |
|---|---|
| `/mstu` | Opens the config GUI |
| `/mstu <search term>` | Opens the config GUI with that term pre-searched |
| `/mstu resetfishinghud` | Resets all fishing tracker counts |
| `/mstu resetprofittracker` | Resets the profit tracker and ore drop counts |

`/mineshafttycoonutils` and `/mstutils` work as full aliases for `/mstu`.

## Notes

- This is a **client-side** mod.
- The Warp Helper and Pet Helper is allowed by MST Staff, however it does technically automate clicks - use at your own risk.
- Refinery/Dune Ram auto-detection work by reading menu contents you open normally, so they should auto update as you upgrade.
