# Accessibility

Time Capsule should be usable by every player who can play Minecraft. This page lists what works today, the known limits, and how to report a problem.

## What works today

- **Text for everything important.** The time left, the open date and the capsule's state appear as text: in the message above the hotbar when you right-click a capsule, and in the item tooltip. You never have to read the clock texture.
- **Narrator.** The setup screen uses Minecraft's standard buttons, so the Narrator reads the timer mode, the day buttons and **Seal capsule**. Item slots work like a chest's.
- **Subtitles.** Every capsule sound has one: opening and closing, sealing, and "Time Capsule locked" when you try a capsule that can't open yet.
- **Your date format.** Real-world open dates follow your computer's language and time zone settings.
- **GUI scale.** The setup screen follows Minecraft's GUI Scale setting.
- **Less motion.** Set `animate_lid=false` in `config/timecapsule.properties` to keep lids still. The sealed clock's animation is slow, covers only the top of the block, and never flashes.
- **Low-end PCs.** Capsules add almost no work per frame or per tick, so the mod runs on older computers.

## Known limits

- The Narrator doesn't read the day count shown between the arrow buttons, because it's drawn as text rather than a button. The chat message after sealing does say how many days you chose.
- Only English is available so far. Translations are welcome; see [CONTRIBUTING.md](CONTRIBUTING.md).
- The mod hasn't been tested with colorblind filters or high-contrast resource packs yet.

## Report a problem

Open an issue with the [Accessibility problem](https://github.com/Httpsluch/TimeCapsule/issues/new?template=accessibility.yml) template. Tell us what got in the way and what you use to play, such as the Narrator, a large GUI scale or a resource pack. Accessibility reports are treated as bugs.
