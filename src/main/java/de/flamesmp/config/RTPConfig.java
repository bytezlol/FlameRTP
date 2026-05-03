package de.flamesmp.config;

import de.flamesmp.FlameRTP;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class RTPConfig {

    private static final Logger LOGGER = Logger.getLogger(RTPConfig.class.getName());

    private final Map<String, WorldProfile> profiles = new HashMap<>();

    private boolean economyEnabled;
    private String economyProvider;

    private boolean precacheEnabled;
    private int precacheIntervalTicks;
    private int precacheBatchSize;

    private boolean cancelOnMove;
    private boolean cancelOnDamage;

    private String defaultWorld;

    public void load() {
        FlameRTP.getInstance().reloadConfig();

        final FileConfiguration config = FlameRTP.getInstance().getConfig();

        this.economyEnabled = config.getBoolean("economy.enabled", false);
        this.economyProvider = config.getString("economy.provider", "vault");

        this.precacheEnabled = config.getBoolean("precache.enabled", true);
        this.precacheIntervalTicks = config.getInt("precache.interval-ticks", 200);
        this.precacheBatchSize = config.getInt("precache.batch-size", 2);

        this.cancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        this.cancelOnDamage = config.getBoolean("teleport.cancel-on-damage", true);

        this.defaultWorld = config.getString("default-world", "world");

        profiles.clear();

        final ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection == null) {
            LOGGER.warning("No 'worlds' section in config.yml — RTP will not work until you configure at least one world.");
            return;
        }

        for (final String worldName : worldsSection.getKeys(false)) {
            try {
                final ConfigurationSection section = worldsSection.getConfigurationSection(worldName);
                if (section == null) continue;

                final WorldProfile profile = parseProfile(worldName, section);
                profiles.put(worldName.toLowerCase(), profile);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse world profile: " + worldName, e);
            }
        }

        LOGGER.info("Loaded " + profiles.size() + " world profiles.");
    }

    private @NotNull WorldProfile parseProfile(final @NotNull String worldName, final @NotNull ConfigurationSection section) {
        final WorldProfile.Shape shape = WorldProfile.Shape.valueOf(
                section.getString("shape", "SQUARE").toUpperCase()
        );

        final Set<Biome> blacklistedBiomes = new HashSet<>();
        for (final String name : section.getStringList("blacklisted-biomes")) {
            final NamespacedKey key = NamespacedKey.fromString(name.toLowerCase());
            if (key == null) {
                LOGGER.warning("Invalid biome key: " + name + " in world " + worldName);
                continue;
            }
            final Biome biome = Registry.BIOME.get(key);
            if (biome == null) {
                LOGGER.warning("Unknown biome: " + name + " in world " + worldName);
                continue;
            }
            blacklistedBiomes.add(biome);
        }

        final Set<Material> unsafeBlocks = parseMaterials(section.getStringList("unsafe-blocks"));
        final Set<Material> standOnBlacklist = parseMaterials(section.getStringList("stand-on-blacklist"));

        return WorldProfile.builder()
                .worldName(worldName)
                .shape(shape)
                .centerX(section.getInt("center.x", 0))
                .centerZ(section.getInt("center.z", 0))
                .minRadius(section.getInt("radius.min", 500))
                .maxRadius(section.getInt("radius.max", 5000))
                .minY(section.getInt("y.min", 60))
                .maxY(section.getInt("y.max", 200))
                .cooldownSeconds(section.getInt("cooldown-seconds", 60))
                .countdownSeconds(section.getInt("countdown-seconds", 5))
                .maxAttempts(section.getInt("max-attempts", 25))
                .cost(section.getDouble("cost", 0.0))
                .enabled(section.getBoolean("enabled", true))
                .blacklistedBiomes(blacklistedBiomes)
                .unsafeBlocks(unsafeBlocks)
                .standOnBlacklist(standOnBlacklist)
                .useNetherRoof(section.getBoolean("nether-roof", false))
                .cacheEnabled(section.getBoolean("cache.enabled", true))
                .cacheTargetSize(section.getInt("cache.target-size", 25))
                .build();
    }

    private @NotNull Set<Material> parseMaterials(final @NotNull List<String> names) {
        final Set<Material> set = new HashSet<>();
        for (final String name : names) {
            final Material material = Material.matchMaterial(name);
            if (material != null) set.add(material);
            else LOGGER.warning("Unknown material in config: " + name);
        }
        return set;
    }

    public @Nullable WorldProfile getProfile(final @NotNull String worldName) {
        return profiles.get(worldName.toLowerCase());
    }
}