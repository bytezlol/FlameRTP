package de.flamesmp.api;

import de.flamesmp.FlameRTP;
import de.flamesmp.config.WorldProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FlameRTPAPI {

    public @NotNull CompletableFuture<RTPResult> teleport(final @NotNull Player player) {
        return teleport(RTPRequest.builder()
                .player(player)
                .worldName(player.getWorld().getName())
                .build());
    }

    public @NotNull CompletableFuture<RTPResult> teleport(final @NotNull Player player, final @NotNull String worldName) {
        return teleport(RTPRequest.builder()
                .player(player)
                .worldName(worldName)
                .build());
    }

    public @NotNull CompletableFuture<RTPResult> teleport(final @NotNull RTPRequest request) {
        return FlameRTP.getInstance().getRtpManager().handle(request);
    }

    public @NotNull CompletableFuture<@Nullable Location> findSafeLocation(final @NotNull String worldName) {
        return FlameRTP.getInstance().getLocationFinderManager().findSafeLocation(worldName);
    }

    public @Nullable WorldProfile getProfile(final @NotNull String worldName) {
        return FlameRTP.getInstance().getRtpConfig().getProfile(worldName);
    }

    public boolean isOnCooldown(final @NotNull UUID uuid, final @NotNull String worldName) {
        return FlameRTP.getInstance().getCooldownManager().isOnCooldown(uuid, worldName);
    }

    public long getCooldownRemainingMillis(final @NotNull UUID uuid, final @NotNull String worldName) {
        return FlameRTP.getInstance().getCooldownManager().getRemainingMillis(uuid, worldName);
    }

    public void clearCooldown(final @NotNull UUID uuid, final @NotNull String worldName) {
        FlameRTP.getInstance().getCooldownManager().clear(uuid, worldName);
    }
}