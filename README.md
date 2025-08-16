Got you 😎 here’s a polished **GitHub-style README.md** for your **TreasureHunt plugin**, formatted for open-source or public repo use:

---

# 🗺️ TreasureHunt Plugin

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.8.8%2B-blue)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-Compatible-green)](https://www.spigotmc.org/)

**TreasureHunt** is a lightweight Minecraft plugin that adds **adventure, exploration, and rewards** to your server. Players hunt for hidden treasures while admins can configure hunts, rewards, and clues. No texture packs required.

---

## ✨ Features

* **Hidden Treasures** – Place treasure chests anywhere in the world.
* **Clues & Hints** – Players can get hints, riddles, or coordinates leading to treasures.
* **Rewards System** – Claim treasures for coins, items, or XP.
* **Multiple Hunts** – Support for simultaneous treasure hunts.
* **Broadcasts** – Notify the server when a treasure is found.
* **Fully Configurable** – Customize rewards, messages, and hints in `config.yml`.

---

## ⚡ Commands

### Player Commands

| Command           | Description                 | Permission          |
| ----------------- | --------------------------- | ------------------- |
| `/hunt`           | View active treasure hunts  | `treasurehunt.play` |
| `/hunt clue <id>` | Get the clue for a treasure | `treasurehunt.play` |
| `/hunt claim`     | Claim nearby treasure       | `treasurehunt.play` |

### Admin Commands

| Command                         | Description                            | Permission           |
| ------------------------------- | -------------------------------------- | -------------------- |
| `/hunt create <id>`             | Create a new treasure hunt             | `treasurehunt.admin` |
| `/hunt setlocation <id>`        | Set treasure location at your position | `treasurehunt.admin` |
| `/hunt setreward <id> <reward>` | Define reward for a treasure           | `treasurehunt.admin` |
| `/hunt start <id>`              | Start a treasure hunt                  | `treasurehunt.admin` |
| `/hunt stop <id>`               | Stop a treasure hunt                   | `treasurehunt.admin` |
| `/hunt reset`                   | Reset all hunts                        | `treasurehunt.admin` |

---

## ⚙️ Configuration

Example `config.yml`:

```yaml
rewards:
  default: "10 diamonds"
  special: "1000 coins, 1 elytra"
messages:
  found: "&a%player% has found treasure %id%!"
  clue: "&eClue for %id%: %clue%"
  no_treasure: "&cNo treasure nearby!"
```

---

## 🛠️ Project Structure


```


TreasureHunt/
 ├── build.gradle
 ├── settings.gradle
 ├── src/main/java/com/yourname/treasurehunt/
 │   ├── TreasureHuntPlugin.java
 │   ├── commands/HuntCommand.java
 │   ├── managers/TreasureManager.java
 │   ├── managers/RewardManager.java
 │   └── models/Treasure.java
 └── src/main/resources/
     ├── plugin.yml
     └── config.yml

```

---

## 📌 Why Use TreasureHunt?

* Engages players with **server-wide events**.
* Perfect for **weekly events, roleplay servers, or PvE adventures**.
* Lightweight and easy to maintain.

---

## 📄 License

MIT License © dammnranaah

---

If you want, I can also **write a concise AI prompt** so you can generate this **TreasureHunt plugin project skeleton** automatically in Fabric or Spigot. Do you want me to do that?
