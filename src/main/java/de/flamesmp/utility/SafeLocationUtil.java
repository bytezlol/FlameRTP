package de.flamesmp.utility;

import de.flamesmp.config.WorldProfile;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class SafeLocationUtil {

    private static final Set<Material> ALWAYS_UNSAFE_BLOCKS = Set.of(
            Material.LAVA,
            Material.WATER,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.POWDER_SNOW,
            Material.WITHER_ROSE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.POINTED_DRIPSTONE
    );

    private SafeLocationUtil() {
        throw new AssertionError("No instances.");
    }

    public static @Nullable Location findSafeY(final @NotNull World world, final @NotNull ChunkSnapshot snapshot, final int worldX, final int worldZ, final @NotNull WorldProfile profile) {

        final int relX = worldX & 0xF;
        final int relZ = worldZ & 0xF;

        final Biome biome = world.getBiome(worldX, profile.getMaxY(), worldZ);
        if (profile.getBlacklistedBiomes().contains(biome)) return null;

        if (profile.isUseNetherRoof()) {
            return findSafeYNether(world, snapshot, relX, relZ, worldX, worldZ, profile);
        }

        for (int y = profile.getMaxY(); y >= profile.getMinY(); y--) {
            final Material ground = snapshot.getBlockType(relX, y, relZ);

            if (ground.isAir()) continue;

            if (!isSafeGround(ground, profile)) return null;

            final Material body = snapshot.getBlockType(relX, y + 1, relZ);
            final Material head = snapshot.getBlockType(relX, y + 2, relZ);

            if (!body.isAir() || !head.isAir()) return null;

            return new Location(world, worldX + 0.5, y + 1.0, worldZ + 0.5);
        }

        return null;
    }

    private static @Nullable Location findSafeYNether(final @NotNull World world, final @NotNull ChunkSnapshot snapshot, final int relX, final int relZ, final int worldX, final int worldZ, final @NotNull WorldProfile profile) {
        final int maxY = Math.min(profile.getMaxY(), 124);
        for (int y = maxY; y >= profile.getMinY(); y--) {
            final Material ground = snapshot.getBlockType(relX, y, relZ);
            if (ground.isAir()) continue;
            if (!isSafeGround(ground, profile)) return null;

            final Material body = snapshot.getBlockType(relX, y + 1, relZ);
            final Material head = snapshot.getBlockType(relX, y + 2, relZ);

            if (!body.isAir() || !head.isAir()) continue; // keep scanning below if not safe

            return new Location(world, worldX + 0.5, y + 1.0, worldZ + 0.5);
        }
        return null;
    }

    private static boolean isSafeGround(final @NotNull Material material, final @NotNull WorldProfile profile) {
        if (ALWAYS_UNSAFE_BLOCKS.contains(material)) return false;
        if (profile.getUnsafeBlocks().contains(material)) return false;
        if (profile.getStandOnBlacklist().contains(material)) return false;
        return material.isSolid();
    }
}