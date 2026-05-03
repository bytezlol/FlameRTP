package de.flamesmp.manager;

import de.flamesmp.FlameRTP;
import de.flamesmp.config.RTPConfig;
import de.flamesmp.config.WorldProfile;
import de.flamesmp.utility.ChunkSnapshotUtil;
import de.flamesmp.utility.MathUtil;
import de.flamesmp.utility.SafeLocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationFinderManager {

    private static final Logger LOGGER = Logger.getLogger(LocationFinderManager.class.getName());

    private final @NotNull ExecutorService snapshotExecutor;

    public LocationFinderManager() {
        this.snapshotExecutor = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                runnable -> {
                    final Thread thread = new Thread(runnable, "FlameRTP-Finder");
                    thread.setDaemon(true);
                    return thread;
                }
        );
    }

    public void shutdown() {
        snapshotExecutor.shutdown();
    }

    public @NotNull CompletableFuture<@Nullable Location> findSafeLocation(final @NotNull String worldName) {
        final RTPConfig config = FlameRTP.getInstance().getRtpConfig();
        final WorldProfile profile = config.getProfile(worldName);
        if (profile == null) return CompletableFuture.completedFuture(null);

        final World world = Bukkit.getWorld(profile.getWorldName());
        if (world == null) return CompletableFuture.completedFuture(null);

        return findSafeLocation(world, profile);
    }

    public @NotNull CompletableFuture<@Nullable Location> findSafeLocation(final @NotNull World world, final @NotNull WorldProfile profile) {
        return attemptFind(world, profile, new AtomicInteger(0));
    }

    private @NotNull CompletableFuture<@Nullable Location> attemptFind(final @NotNull World world, final @NotNull WorldProfile profile, final @NotNull AtomicInteger attempts) {
        if (attempts.incrementAndGet() > profile.getMaxAttempts()) {
            return CompletableFuture.completedFuture(null);
        }

        final int[] point = MathUtil.randomPoint(profile);
        final int worldX = point[0];
        final int worldZ = point[1];

        final int chunkX = worldX >> 4;
        final int chunkZ = worldZ >> 4;

        return ChunkSnapshotUtil.getSnapshot(world, chunkX, chunkZ, true)
                .thenCompose(snapshot -> validate(world, snapshot, worldX, worldZ, profile, attempts))
                .exceptionally(throwable -> {
                    LOGGER.log(Level.WARNING, "Snapshot fetch failed at " + worldX + "," + worldZ, throwable);
                    return null;
                })
                .thenCompose(found -> {
                    if (found != null) return CompletableFuture.completedFuture(found);
                    return attemptFind(world, profile, attempts);
                });
    }

    private @NotNull CompletableFuture<@Nullable Location> validate(final @NotNull World world, final @NotNull ChunkSnapshot snapshot, final int worldX, final int worldZ, final @NotNull WorldProfile profile, final @NotNull AtomicInteger attempts) {
        final Location candidate = SafeLocationUtil.findSafeY(world, snapshot, worldX, worldZ, profile);
        return CompletableFuture.completedFuture(candidate);
    }
}