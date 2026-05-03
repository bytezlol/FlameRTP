package de.flamesmp.utility;

import de.flamesmp.FlameRTP;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ChunkSnapshotUtil {

    private ChunkSnapshotUtil() {
        throw new AssertionError("No instances.");
    }

    public static @NotNull CompletableFuture<ChunkSnapshot> getSnapshot(final @NotNull World world, final int chunkX, final int chunkZ, final boolean generate) {
        final CompletableFuture<ChunkSnapshot> future = new CompletableFuture<>();

        world.getChunkAtAsync(chunkX, chunkZ, generate).thenAccept(chunk ->
                Bukkit.getRegionScheduler().run(FlameRTP.getInstance(), world, chunkX, chunkZ, task -> {
                    try {
                        final ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
                        future.complete(snapshot);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                })
        );

        return future;
    }
}