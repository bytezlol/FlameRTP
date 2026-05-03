package de.flamesmp.listener;

import de.flamesmp.FlameRTP;
import de.flamesmp.tasks.CountdownTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDamageListener implements Listener {

    public PlayerDamageListener() {
        FlameRTP.getInstance().getServer().getPluginManager().registerEvents(this, FlameRTP.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(final @NotNull EntityDamageEvent event) {
        if (!FlameRTP.getInstance().getRtpConfig().isCancelOnDamage()) return;
        if (!(event.getEntity() instanceof final Player player)) return;
        if (player.hasPermission("flamertp.bypass.damage")) return;

        final CountdownTask task = FlameRTP.getInstance().getRtpManager().getActiveCountdowns().get(player.getUniqueId());
        if (task != null) {
            FlameRTP.getInstance().getMessageManager().send(player, "damaged");
            task.cancel();
        }
    }
}
