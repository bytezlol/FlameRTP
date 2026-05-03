package de.flamesmp.manager;

import de.flamesmp.FlameRTP;
import de.flamesmp.utility.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final Map<String, String> messages = new HashMap<>();

    public MessageManager() {
        load();
    }

    public void load() {
        messages.clear();

        final FileConfiguration config = FlameRTP.getInstance().getConfig();

        put(config, "prefix", "<#FF6B00><b>RTP <dark_gray>» <reset>");
        put(config, "searching", "<#FF6B00>Searching for a safe location...");
        put(config, "found", "<#8DFB08>Found a safe spot — teleporting!");
        put(config, "no-safe-location", "<#FF0000>Could not find a safe location. Try again.");
        put(config, "world-disabled", "<#FF0000>RTP is disabled in this world.");
        put(config, "world-not-found", "<#FF0000>That world does not exist.");
        put(config, "on-cooldown", "<#FF0000>You must wait <yellow><time> <#FF0000>before using RTP again.");
        put(config, "insufficient-funds", "<#FF0000>You need <yellow>$<cost> <#FF0000>to use RTP.");
        put(config, "withdrawn", "<gray>Withdrawn <yellow>$<cost> <gray>for RTP.");
        put(config, "already-teleporting", "<#FF0000>You are already being teleported.");
        put(config, "moved", "<#FF0000>Teleport cancelled — you moved.");
        put(config, "damaged", "<#FF0000>Teleport cancelled — you took damage.");
        put(config, "no-permission", "<#FF0000>You do not have permission for that.");
        put(config, "internal-error", "<#FF0000>An internal error occurred. Try again.");
        put(config, "countdown", "<#FF6B00>Teleporting in <yellow><time><#FF6B00>...");
        put(config, "teleported", "<#8DFB08>You were teleported to <yellow><x>, <y>, <z><#8DFB08>.");
        put(config, "reload-success", "<#8DFB08>Configuration reloaded.");
        put(config, "admin-cooldown-cleared", "<#8DFB08>Cooldown cleared for <yellow><player><#8DFB08>.");
        put(config, "admin-player-not-found", "<#FF0000>Player not found.");
        put(config, "admin-cache-cleared", "<#8DFB08>Cleared cached locations for <yellow><world><#8DFB08>.");
    }

    private void put(final @NotNull FileConfiguration config, final @NotNull String key, final @NotNull String def) {
        messages.put(key, config.getString("messages." + key, def));
    }

    public @NotNull String raw(final @NotNull String key) {
        return messages.getOrDefault(key, key);
    }

    public @NotNull String format(final @NotNull String key, final @NotNull Object... placeholders) {
        String message = raw(key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            message = message.replace("<" + placeholders[i] + ">", String.valueOf(placeholders[i + 1]));
        }
        return message;
    }

    public void send(final @NotNull CommandSender sender, final @NotNull String key, final @NotNull Object... placeholders) {
        sender.sendMessage(ColorUtil.parse(raw("prefix") + format(key, placeholders)));
    }

    public void actionBar(final @NotNull Player player, final @NotNull String key, final @NotNull Object... placeholders) {
        player.sendActionBar(ColorUtil.parse(format(key, placeholders)));
    }
}