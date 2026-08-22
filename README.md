<h1 align="center">MineshaftTycoon Utils</h1>

<p align="center">Client-side Fabric mod for MineshaftTycoon — fishing & profit tracking, chat cleanup, glyph ranks, HUDs, and more.</p>

<p align="center">
  <a href="https://github.com/NotRoboo/mineshafttycoonutils/releases"><b>Download Latest</b></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/NotRoboo/mineshafttycoonutils/releases">all releases</a>
</p>

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for **Minecraft 1.21.11**.
2. Install the required mods:
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
3. Download the latest jar from [Releases](https://github.com/NotRoboo/mineshafttycoonutils/releases) and place it in your `mods` folder.
4. Launch with the Fabric profile and run `/mstu` to open the config.

## Features

- **Player Message Formatting** – Reorder ranks, hide Hypixel ranks, custom name colors, white chat for non-ranks.
- **Glyph Ranks** – Pixel-art glyphs instead of `[T#]` / staff ranks in chat, tab list, nametags & holograms.
- **Information HUD** – Sprint status, click status, equipped pet.
- **Fishing Tracker** – Tracks sea creatures, treasure, crates, trophy fish, double hooks, Fortune Fragments + draggable HUD. Reset with `/mstu resetfishinghud`.
- **Profit Tracker** – Coins/hour & total profit from mining (refinery-aware, auto-detects levels). Rare drop breakdown. Reset with `/mstu resetprofittracker`.
- **Magma / Bag Value HUDs** – Ore bag value with Cash Reg, Refinery & Dune Ram buffs.
- **Timers HUD** – Potion durations, PetAd timer, Il's restock.
- **Chat Filters** – Toggleable filters for join messages, pet XP, fishing spam, Discord ads, etc.
- **Warp & Pet Helper** – `/warp <name>` / `/pet <name>` auto-clicks the menu (disabled by default, use at your own risk).
- **Custom Scoreboard** – Reorder lines while keeping live values.
- **Misc** – ToggleAttack / ToggleUse keybinds, Night Vision blocker, Force Tab List Sort.

## Commands

| Command | Description |
|---------|-------------|
| `/mstu` | Open config GUI |
| `/mstu <search>` | Open config with search pre-filled |
| `/mstu edithud` | HUD position editor |
| `/mstu hudpositionsreset` | Reset HUD positions |
| `/mstu resetfishinghud` | Reset fishing tracker |
| `/mstu resetprofittracker` | Reset profit tracker |

Aliases: `/mineshafttycoonutils`, `/mstutils`

## Notes

- Fully **client-side**.
- Warp/Pet Helper is staff-allowed but automates clicks — use at your own risk.
- Refinery & Dune Ram levels auto-detect when you open the relevant menus.