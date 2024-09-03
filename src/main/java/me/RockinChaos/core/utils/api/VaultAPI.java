/*
 * ChaosCore
 * Copyright (C) CraftationGaming <https://www.craftationgaming.com/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.RockinChaos.core.utils.api;

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class VaultAPI {
    private static VaultAPI vault;
    private Economy econ = null;

    /**
     * Creates a new VaultAPI instance.
     */
    public VaultAPI() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.setupEconomy();
        }
    }

    /**
     * Gets the instance of the VaultAPI.
     *
     * @param regen - If the VaultAPI should have a new instance created.
     * @return The VaultAPI instance.
     */
    public static @Nonnull VaultAPI getVault(final boolean regen) {
        if (vault == null || regen) {
            vault = new VaultAPI();
        }
        return vault;
    }

    /**
     * Sets the Economy instance.
     */
    private void setupEconomy() {
        try {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                if (!Core.getCore().isStarted()) {
                    SchedulerUtils.runLater(1L, this::setupEconomy);
                }
            } else {
                RegisteredServiceProvider<Economy> rsp = Core.getCore().getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                    return;
                }
                this.econ = rsp.getProvider();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Gets the Vault Economy instance.
     *
     * @return Gets the current economy instance.
     */
    public @Nonnull Economy getEconomy() {
        return this.econ;
    }

    /**
     * Checks if Vault is enabled.
     *
     * @return If Vault is enabled.
     */
    public boolean vaultEnabled() {
        return Bukkit.getServer().getPluginManager().isPluginEnabled("Vault") && econ != null;
    }

    /**
     * Gets the balance of the Player.
     *
     * @param player - The Player having their balance found.
     * @return The balance of the Player.
     */
    public double getBalance(final @Nonnull Player player) {
        return this.econ.getBalance(player);
    }

    /**
     * Withdrawals the specified cost from the Players balance.
     *
     * @param player - The Player being transacted.
     * @param cost   - The cost to be charged to the Player.
     */
    public void withdrawBalance(final @Nonnull Player player, final double cost) {
        this.econ.withdrawPlayer(player, cost);
    }
}