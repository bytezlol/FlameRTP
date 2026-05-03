package de.flamesmp.listener;

import de.flamesmp.FlameRTP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {

    public PlayerQuitListener() {
        FlameRTP.getInstance().getServer().getPluginManager().registerEvents(this, FlameRTP.getInstance());
    }

    @EventHandler
    public void onQuit(final @NotNull PlayerQuitEvent event) {
        FlameRTP.getInstance().getRtpManager().cancelFor(event.getPlayer().getUniqueId());
    }
}
