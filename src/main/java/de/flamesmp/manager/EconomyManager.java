package de.flamesmp.manager;

import de.flamesmp.FlameRTP;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;


public class EconomyManager {

    private static final Logger LOGGER = Logger.getLogger(EconomyManager.class.getName());

    @Getter
    private @Nullable Economy economy;

    @Getter
    private boolean enabled;

    public EconomyManager() {
        if (!FlameRTP.getInstance().getRtpConfig().isEconomyEnabled()) {
            this.enabled = false;
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            LOGGER.warning("Economy enabled in config but Vault is not installed.");
            this.enabled = false;
            return;
        }

        final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.warning("No economy provider registered with Vault.");
            this.enabled = false;
            return;
        }

        this.economy = rsp.getProvider();
        this.enabled = true;
        LOGGER.info("Hooked into economy: " + economy.getName());
    }

    public boolean canAfford(final @NotNull Player player, final double cost) {
        if (!enabled || cost <= 0) return true;
        if (player.hasPermission("flamertp.cost.bypass")) return true;
        return economy != null && economy.has(player, cost);
    }

    public boolean withdraw(final @NotNull Player player, final double cost) {
        if (!enabled || cost <= 0) return true;
        if (player.hasPermission("flamertp.cost.bypass")) return true;
        if (economy == null) return true;
        return economy.withdrawPlayer(player, cost).transactionSuccess();
    }

    public void refund(final @NotNull Player player, final double cost) {
        if (!enabled || cost <= 0) return;
        if (player.hasPermission("flamertp.cost.bypass")) return;
        if (economy == null) return;
        economy.depositPlayer(player, cost);
    }
}