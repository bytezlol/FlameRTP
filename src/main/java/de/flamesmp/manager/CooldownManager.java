package de.flamesmp.manager;

import de.flamesmp.FlameRTP;
import de.flamesmp.cache.CooldownCache;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CooldownManager {

    private final @NotNull CooldownCache cache;

    public CooldownManager() {
        this.cache = FlameRTP.getInstance().getCooldownCache();
    }

    public boolean isOnCooldown(final @NotNull UUID uuid, final @NotNull String worldName) {
        return cache.has(uuid, worldName);
    }

    public boolean isOnCooldown(final @NotNull Player player, final @NotNull String worldName) {
        if (player.hasPermission("flamertp.cooldown.bypass")) return false;
        return cache.has(player.getUniqueId(), worldName);
    }

    public void apply(final @NotNull Player player, final @NotNull String worldName, final long durationMillis) {
        if (player.hasPermission("flamertp.cooldown.bypass")) return;
        cache.add(player.getUniqueId(), worldName, durationMillis);
    }

    public void clear(final @NotNull UUID uuid, final @NotNull String worldName) {
        cache.remove(uuid, worldName);
    }

    public long getRemainingMillis(final @NotNull UUID uuid, final @NotNull String worldName) {
        return cache.getRemainingMillis(uuid, worldName);
    }

    public @NotNull String formatRemaining(final @NotNull UUID uuid, final @NotNull String worldName) {
        final long ms = getRemainingMillis(uuid, worldName);
        if (ms <= 0) return "0s";

        final long seconds = ms / 1000;
        final long minutes = seconds / 60;
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}