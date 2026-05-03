# FlameRTP - TODO

## Bug Fixes
- [ ] Fix biome check falling back to live `world.getBiome()` call on Canvas servers
- [ ] Ensure cooldown persists across server restarts (currently in-memory only)

## Performance
- [ ] Implement smarter cache warming - prioritize worlds with most `/rtp` usage
- [ ] Skip already-loaded chunks during precache to reduce chunk load overhead
- [ ] Add configurable max concurrent async location finds per world

## Features - Core
- [ ] **Per-world cooldowns** - different cooldown durations per world profile
- [ ] **Per-permission cooldowns** - rank-based cooldown override (e.g. VIP gets 30s vs 120s)
- [ ] **Safe location scoring** - rank candidates by quality (flat ground, near resources) instead of first match
- [ ] **Exclusion zones** - define rectangular/circular areas where players cannot land (e.g. spawn region)
- [ ] **Height-adaptive Y range** - automatically respect world build height limits
- [ ] **Dimension support** - proper End teleportation handling (avoid void, outer islands)

## Economy
- [ ] **Per-world cost** - override global cost per world profile
- [ ] **Refund on fail** - refund player if no safe location found after max attempts
- [ ] **Cost display in countdown** - show deduction amount in action bar

## Statistics & Storage
- [ ] **SQLite/MySQL storage backend** - persist cooldowns, teleport counts, and player stats
- [ ] **Per-player teleport history** - store last N RTP destinations per player
- [ ] **Admin stats command** - `/rtpadmin stats <player>` showing total teleports, last location, cooldown status
- [ ] **Global stats** - total teleports performed, cache hit rate, average attempts per find

## Admin & UX
- [ ] **PlaceholderAPI support** - expose cooldown, cost, and cache size as placeholders
- [ ] **GUI menu** - optional chest GUI for world selection instead of command arguments
- [ ] **Warm-up animation** - particle/sound effect during countdown
- [ ] **Configurable teleport sound** - per-world sound on successful teleport
- [ ] **Discord webhook** - optional notification on teleport (for moderation/logging)
- [ ] **bStats integration** - anonymous usage metrics

## API
- [ ] **FlameRTPAPI event system** - fire `PreRTPEvent` and `PostRTPEvent` for other plugins to hook into
- [ ] **Programmatic cache control** - expose cache fill/clear via API
- [ ] **Custom location validators** - allow other plugins to register their own safety checks

## Documentation
- [ ] Full config.yml reference in wiki
- [ ] API usage guide with examples
- [ ] Folia-specific setup guide
- [ ] Migration guide from BetterRTP / EssentialsX /wild