package de.flamesmp.manager;

import de.flamesmp.FlameRTP;
import de.flamesmp.api.RTPRequest;
import de.flamesmp.api.RTPResult;
import de.flamesmp.api.event.PostRTPEvent;
import de.flamesmp.api.event.PreRTPEvent;
import de.flamesmp.api.event.RTPLocationFoundEvent;
import de.flamesmp.config.WorldProfile;
import de.flamesmp.enums.FailReason;
import de.flamesmp.tasks.CountdownTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RTPManager {

    private static final Logger LOGGER = Logger.getLogger(RTPManager.class.getName());

    @Getter
    private final Map<UUID, CountdownTask> activeCountdowns = new ConcurrentHashMap<>();

    @Getter
    private final Map<UUID, Long> activeFinds = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, java.util.Deque<Location>> cachedLocations = new ConcurrentHashMap<>();

    public void shutdown() {
        for (final CountdownTask task : activeCountdowns.values()) task.cancel();
        activeCountdowns.clear();
        activeFinds.clear();
        cachedLocations.clear();
    }

    public @NotNull CompletableFuture<RTPResult> handle(final @NotNull RTPRequest request) {
        final long started = System.currentTimeMillis();
        final Player player = request.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (activeCountdowns.containsKey(uuid) || activeFinds.containsKey(uuid)) {
            return complete(request, RTPResult.failure(FailReason.ALREADY_TELEPORTING, 0L), "already-teleporting");
        }

        final WorldProfile profile = FlameRTP.getInstance().getRtpConfig().getProfile(request.getWorldName());
        if (profile == null) {
            return complete(request, RTPResult.failure(FailReason.WORLD_NOT_FOUND, 0L), "world-not-found");
        }
        if (!profile.isEnabled()) {
            return complete(request, RTPResult.failure(FailReason.WORLD_DISABLED, 0L), "world-disabled");
        }

        final World world = Bukkit.getWorld(profile.getWorldName());
        if (world == null) {
            return complete(request, RTPResult.failure(FailReason.WORLD_NOT_FOUND, 0L), "world-not-found");
        }

        final CooldownManager cooldown = FlameRTP.getInstance().getCooldownManager();
        if (!request.isBypassCooldown() && cooldown.isOnCooldown(player, profile.getWorldName())) {
            final String time = cooldown.formatRemaining(uuid, profile.getWorldName());
            FlameRTP.getInstance().getMessageManager().send(player, "on-cooldown", "time", time);
            return CompletableFuture.completedFuture(RTPResult.failure(FailReason.ON_COOLDOWN, 0L));
        }

        final EconomyManager economy = FlameRTP.getInstance().getEconomyManager();
        if (!request.isBypassCost() && economy.isEnabled() && profile.getCost() > 0) {
            if (!economy.canAfford(player, profile.getCost())) {
                FlameRTP.getInstance().getMessageManager().send(player, "insufficient-funds", "cost", profile.getCost());
                return CompletableFuture.completedFuture(RTPResult.failure(FailReason.INSUFFICIENT_FUNDS, 0L));
            }
        }

        final PreRTPEvent preEvent = new PreRTPEvent(request);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return CompletableFuture.completedFuture(RTPResult.failure(FailReason.NO_PERMISSION, 0L));
        }

        if (!request.isBypassCost() && economy.isEnabled() && profile.getCost() > 0) {
            if (!economy.withdraw(player, profile.getCost())) {
                return complete(request, RTPResult.failure(FailReason.INSUFFICIENT_FUNDS, 0L), "insufficient-funds");
            }
            FlameRTP.getInstance().getMessageManager().send(player, "withdrawn", "cost", profile.getCost());
        }

        activeFinds.put(uuid, started);
        FlameRTP.getInstance().getMessageManager().send(player, "searching");

        return findLocation(world, profile)
                .thenCompose(location -> {
                    activeFinds.remove(uuid);

                    if (location == null) {
                        if (!request.isBypassCost() && profile.getCost() > 0) economy.refund(player, profile.getCost());
                        return complete(request, RTPResult.failure(FailReason.NO_SAFE_LOCATION, System.currentTimeMillis() - started), "no-safe-location");
                    }

                    final RTPLocationFoundEvent foundEvent = new RTPLocationFoundEvent(request, location);
                    Bukkit.getPluginManager().callEvent(foundEvent);
                    if (foundEvent.isCancelled()) {
                        if (!request.isBypassCost() && profile.getCost() > 0) economy.refund(player, profile.getCost());
                        return CompletableFuture.completedFuture(RTPResult.failure(FailReason.NO_PERMISSION, System.currentTimeMillis() - started));
                    }

                    final Location finalLocation = foundEvent.getLocation();

                    if (request.isBypassCountdown() || profile.getCountdownSeconds() <= 0) {
                        return executeTeleport(request, profile, finalLocation, started);
                    }

                    return runCountdown(request, profile, finalLocation, started);
                })
                .exceptionally(throwable -> {
                    LOGGER.log(Level.SEVERE, "RTP pipeline failed for " + player.getName(), throwable);
                    activeFinds.remove(uuid);
                    if (!request.isBypassCost() && profile.getCost() > 0) economy.refund(player, profile.getCost());
                    FlameRTP.getInstance().getMessageManager().send(player, "internal-error");
                    final RTPResult result = RTPResult.failure(FailReason.INTERNAL_ERROR, System.currentTimeMillis() - started);
                    Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
                    return result;
                });
    }

    private @NotNull CompletableFuture<@Nullable Location> findLocation(final @NotNull World world, final @NotNull WorldProfile profile) {
        if (profile.isCacheEnabled()) {
            final java.util.Deque<Location> pool = cachedLocations.get(profile.getWorldName().toLowerCase());
            if (pool != null) {
                final Location pooled = pool.pollFirst();
                if (pooled != null) return CompletableFuture.completedFuture(pooled);
            }
        }
        return FlameRTP.getInstance().getLocationFinderManager().findSafeLocation(world, profile);
    }

    private @NotNull CompletableFuture<RTPResult> runCountdown(final @NotNull RTPRequest request, final @NotNull WorldProfile profile, final @NotNull Location destination, final long started) {
        final CompletableFuture<RTPResult> future = new CompletableFuture<>();
        final Player player = request.getPlayer();
        final UUID uuid = player.getUniqueId();

        final CountdownTask countdown = new CountdownTask(
                player,
                profile.getCountdownSeconds(),
                () -> {
                    activeCountdowns.remove(uuid);
                    executeTeleport(request, profile, destination, started).whenComplete((result, ex) -> {
                        if (ex != null) future.completeExceptionally(ex);
                        else future.complete(result);
                    });
                },
                () -> {
                    activeCountdowns.remove(uuid);
                    final EconomyManager economy = FlameRTP.getInstance().getEconomyManager();
                    if (!request.isBypassCost() && profile.getCost() > 0) economy.refund(player, profile.getCost());
                    final RTPResult result = RTPResult.failure(FailReason.PLAYER_MOVED, System.currentTimeMillis() - started);
                    Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
                    future.complete(result);
                }
        );

        activeCountdowns.put(uuid, countdown);
        countdown.start();
        return future;
    }

    private @NotNull CompletableFuture<RTPResult> executeTeleport(final @NotNull RTPRequest request, final @NotNull WorldProfile profile, final @NotNull Location destination, final long started) {
        final CompletableFuture<RTPResult> future = new CompletableFuture<>();
        final Player player = request.getPlayer();

        if (!player.isOnline()) {
            final RTPResult result = RTPResult.failure(FailReason.PLAYER_OFFLINE, System.currentTimeMillis() - started);
            Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
            future.complete(result);
            return future;
        }

        player.teleportAsync(destination).thenAccept(success -> {
            final long duration = System.currentTimeMillis() - started;

            if (!success) {
                if (!request.isBypassCost() && profile.getCost() > 0) FlameRTP.getInstance().getEconomyManager().refund(player, profile.getCost());
                final RTPResult result = RTPResult.failure(FailReason.INTERNAL_ERROR, duration);
                Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
                future.complete(result);
                return;
            }

            if (!request.isBypassCooldown()) {
                FlameRTP.getInstance().getCooldownManager().apply(player, profile.getWorldName(), profile.getCooldownSeconds() * 1000L);
            }

            FlameRTP.getInstance().getMessageManager().send(player, "teleported",
                    "x", destination.getBlockX(),
                    "y", destination.getBlockY(),
                    "z", destination.getBlockZ()
            );

            FlameRTP.getInstance().getFoliaLib().getScheduler().runAtEntity(player,
                    (task) -> player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f));

            final RTPResult result = RTPResult.success(destination, duration);
            Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
            future.complete(result);
        }).exceptionally(throwable -> {
            LOGGER.log(Level.SEVERE, "Teleport failed for " + player.getName(), throwable);
            if (!request.isBypassCost() && profile.getCost() > 0) FlameRTP.getInstance().getEconomyManager().refund(player, profile.getCost());
            final RTPResult result = RTPResult.failure(FailReason.INTERNAL_ERROR, System.currentTimeMillis() - started);
            Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
            future.complete(result);
            return null;
        });

        return future;
    }

    public void cancelFor(final @NotNull UUID uuid) {
        final CountdownTask task = activeCountdowns.remove(uuid);
        if (task != null) task.cancel();
        activeFinds.remove(uuid);
    }

    private @NotNull CompletableFuture<RTPResult> complete(final @NotNull RTPRequest request, final @NotNull RTPResult result, final @NotNull String messageKey) {
        FlameRTP.getInstance().getMessageManager().send(request.getPlayer(), messageKey);
        Bukkit.getPluginManager().callEvent(new PostRTPEvent(request, result));
        return CompletableFuture.completedFuture(result);
    }
}