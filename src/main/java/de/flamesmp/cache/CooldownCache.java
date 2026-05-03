package de.flamesmp.cache;

import de.flamesmp.FlameRTP;
import de.flamesmp.storage.LocationCacheStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownCache {

    private final @NotNull LocationCacheStorage storage;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public CooldownCache() {
        this.storage = FlameRTP.getInstance().getLocationCacheStorage();
        storage.loadCooldowns().thenAccept(cooldowns::putAll);
    }

    private @NotNull String key(final @NotNull UUID uuid, final @NotNull String worldName) {
        return uuid + ":" + worldName.toLowerCase();
    }

    public boolean has(final @NotNull UUID uuid, final @NotNull String worldName) {
        final Long expires = cooldowns.get(key(uuid, worldName));
        if (expires == null) return false;

        if (System.currentTimeMillis() > expires) {
            remove(uuid, worldName);
            return false;
        }

        return true;
    }

    public void add(final @NotNull UUID uuid, final @NotNull String worldName, final long durationMillis) {
        final long expires = System.currentTimeMillis() + durationMillis;
        cooldowns.put(key(uuid, worldName), expires);
        storage.saveCooldown(uuid, worldName, expires);
    }

    public void remove(final @NotNull UUID uuid, final @NotNull String worldName) {
        cooldowns.remove(key(uuid, worldName));
        storage.deleteCooldown(uuid, worldName);
    }

    public long getRemainingMillis(final @NotNull UUID uuid, final @NotNull String worldName) {
        final Long expires = cooldowns.get(key(uuid, worldName));
        if (expires == null) return 0L;

        final long remaining = expires - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    public Map<String, Long> getAll() {
        return cooldowns;
    }
}