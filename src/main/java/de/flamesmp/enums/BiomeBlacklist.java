package de.flamesmp.enums;

import org.bukkit.block.Biome;

import java.util.Set;

public final class BiomeBlacklist {

    public static final Set<Biome> DEFAULT_UNSAFE = Set.of(
            Biome.OCEAN,
            Biome.DEEP_OCEAN,
            Biome.COLD_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
            Biome.WARM_OCEAN,
            Biome.RIVER,
            Biome.FROZEN_RIVER
    );

    private BiomeBlacklist() {
        throw new AssertionError("No instances.");
    }
}