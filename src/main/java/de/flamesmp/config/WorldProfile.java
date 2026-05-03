package de.flamesmp.config;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
@Builder
public class WorldProfile {

    private final @NotNull String worldName;

    // square or circle
    private final @NotNull Shape shape;

    private final int centerX;
    private final int centerZ;

    private final int minRadius;
    private final int maxRadius;

    private final int minY;
    private final int maxY;

    private final int cooldownSeconds;
    private final int countdownSeconds;
    private final int maxAttempts;

    private final double cost;

    private final boolean enabled;

    private final @NotNull Set<Biome> blacklistedBiomes;
    private final @NotNull Set<Material> unsafeBlocks;
    private final @NotNull Set<Material> standOnBlacklist;

    private final boolean useNetherRoof;
    private final boolean cacheEnabled;
    private final int cacheTargetSize;

    public enum Shape {
        SQUARE,
        CIRCLE
    }
}