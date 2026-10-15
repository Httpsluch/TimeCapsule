## 1.0.0

First release.

- **Time Capsule block:** a chest you seal for a set time. Craft it from 7 iron ingots, a clock and a chest.
- **Setup screen when placed:** choose **Real-world days** or **In-game days**, pick 1 to 365 days, add items and written books, then seal it.
- **Sealed capsules** show the time left when right-clicked, and resist explosions, the Wither and the Ender Dragon.
- **Pick it up like a shulker box:** breaking a capsule keeps everything inside. A sealed capsule stays sealed, so you can move it, or hide it for someone else to find, but nobody can open it early.
- **The item's tooltip shows when it opens:** the date and time for real-world capsules (in your own time zone), or the in-game time left. What's inside stays secret.
- **When the time is up,** the next right-click opens the capsule, with an optional chat announcement. After that it works like a chest that keeps its contents when broken.
- **Animated lid and clock:** the lid swings open while someone is looking inside, and a sealed capsule's clock face ticks.
- **Sounds:** chest sounds when opening and closing, a heavy lock sound when sealing, and a locked rattle when someone tries a sealed capsule.
- Capsules can't be put inside other capsules.
- **Settings** in `config/timecapsule.properties`: default timer mode, default and maximum days, the open announcement, and the lid animation.
- **Lightweight:** capsules don't run code every tick (the lid moves through block states, not a custom renderer), and hoppers can't pull items out of them.
- **Supports** Fabric, Forge and NeoForge on Minecraft 1.20.1, 1.21.1, 26.1.2, 26.2 and 26.3.
