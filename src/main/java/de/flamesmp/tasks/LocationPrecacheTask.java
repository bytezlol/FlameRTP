package de.flamesmp.tasks;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import de.flamesmp.FlameRTP;
import de.flamesmp.config.RTPConfig;
import de.flamesmp.config.WorldProfile;
import de.flamesmp.manager.RTPManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LocationPrecacheTask {

    private WrappedTask wrappedTask;

    public void start() {
        final RTPConfig config = FlameRTP.getInstance().getRtpConfig();
        if (!config.isPrecacheEnabled()) return;

        this.wrappedTask = FlameRTP.getInstance().getFoliaLib().getScheduler().runTimerAsync(this::tick, 100L, config.getPrecacheIntervalTicks());
    }

    public void stop() {
        if (wrappedTask != null) wrappedTask.cancel();
    }

    private void tick() {
        final RTPConfig config = FlameRTP.getInstance().getRtpConfig();
        final RTPManager rtpManager = FlameRTP.getInstance().getRtpManager();

        for (final Map.Entry<String, WorldProfile> entry : config.getProfiles().entrySet()) {
            final WorldProfile profile = entry.getValue();
            if (!profile.isEnabled() || !profile.isCacheEnabled()) continue;

            final World world = Bukkit.getWorld(profile.getWorldName());
            if (world == null) continue;

            final java.util.Deque<Location> pool = rtpManager.getCachedLocations().computeIfAbsent(profile.getWorldName().toLowerCase(), key -> new ConcurrentLinkedDeque<>());

            if (pool.size() >= profile.getCacheTargetSize()) continue;

            // spawn N concurrent finds up to batch size
            final int needed = Math.min(profile.getCacheTargetSize() - pool.size(), config.getPrecacheBatchSize());
            for (int i = 0; i < needed; i++) {
                fillOne(world, profile, pool);
            }
        }
    }

    private void fillOne(final @NotNull World world, final @NotNull WorldProfile profile, final @NotNull java.util.Deque<Location> pool) {
        FlameRTP.getInstance().getLocationFinderManager().findSafeLocation(world, profile)
                .thenAccept(location -> {
                    if (location == null) return;
                    if (pool.size() >= profile.getCacheTargetSize()) return;
                    pool.offerLast(location);
                });
    }
}
