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
package me.RockinChaos.core.handlers;

import de.domedd.betternick.BetterNick;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.api.LegacyAPI;
import me.RockinChaos.core.utils.protocol.events.PlayerEnterCreativeEvent;
import me.RockinChaos.core.utils.protocol.events.PlayerExitCreativeEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class PlayerHandler {

    private static final HashMap<String, ItemStack[]> craftingItems = new HashMap<>();
    private static final HashMap<String, ItemStack[]> craftingOpenItems = new HashMap<>();
    private static final HashMap<String, ItemStack[]> creativeCraftingItems = new HashMap<>();
    private static final int PLAYER_CRAFT_INV_SIZE = 5;

    /**
     * Safely closes the players inventory in order to call the InventoryCloseEvent.
     * Fixes a bug with player.closeInventory() not calling the event by default, breaking crafting items.
     *
     * @param player - The player to have their inventory closed.
     */
    public static void safeInventoryClose(final Player player) {
        player.openInventory(Bukkit.createInventory(player.getInventory().getHolder(), 9));
        player.closeInventory();
    }

    /**
     * Removes the Player from Creative Mode.
     *
     * @param who    - The Player being referenced.
     * @param altWho - The other Player being referenced (if any).
     */
    public static void setMode(final CommandSender who, final Player altWho, final GameMode gamemode, final boolean silent, final boolean doSave) {
        Bukkit.getPluginManager().callEvent(new PlayerExitCreativeEvent(who, altWho, gamemode, silent, doSave));
    }

    /**
     * Sets the Player to Creative Mode.
     *
     * @param who    - The Player being referenced.
     * @param altWho - The other Player being referenced (if any).
     */
    public static void setCreative(final CommandSender who, final Player altWho) {
        Bukkit.getPluginManager().callEvent(new PlayerEnterCreativeEvent(who, altWho, false, false, false));
    }

    /**
     * Refreshes the Creative stats for the Player.
     *
     * @param who - The Player being referenced.
     */
    public static void refreshCreative(final CommandSender who) {
        Bukkit.getPluginManager().callEvent(new PlayerEnterCreativeEvent(who, null, true, false, true));
    }

    /**
     * Checks if the Entity is a real player.
     *
     * @param entity - The entity being checked.
     * @return If the entity is a real Player.
     */
    public static boolean isPlayer(final Entity entity) {
        try {
            if (Core.getCore().getDependencies().citizensEnabled() && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity)) {
                return false;
            } else if (!(entity instanceof Player)) {
                return false;
            } else if (PlayerHandler.getPlayerID((Player) entity) == null || PlayerHandler.getPlayerID((Player) entity).isEmpty()) {
                return false;
            } else if (Objects.requireNonNull(((Player) entity).getAddress()).getHostString() == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Clears the entire inventory of the Player.
     *
     * @param player - The Player being referenced.
     */
    public static void clearItems(final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final Inventory craftView = player.getOpenInventory().getTopInventory();
        inventory.setHelmet(new ItemStack(Material.AIR));
        inventory.setChestplate(new ItemStack(Material.AIR));
        inventory.setLeggings(new ItemStack(Material.AIR));
        inventory.setBoots(new ItemStack(Material.AIR));
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            PlayerHandler.setOffHandItem(player, new ItemStack(Material.AIR));
        }
        inventory.clear();
        craftView.clear();
    }

    /**
     * Checks if the InventoryView is a player crafting inventory.
     *
     * @param view - The InventoryView to be checked.
     * @return If the currently open inventory is a player crafting inventory.
     */
    public static boolean isCraftingInv(final InventoryView view) {
        return (!view.getType().name().equalsIgnoreCase("HOPPER") && !view.getType().name().equalsIgnoreCase("BREWING") && view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE);
    }

    /**
     * Checks if the player is currently in creative mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in creative mode.
     */
    public static boolean isCreativeMode(final Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    /**
     * Checks if the player is currently in adventure mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in adventure mode.
     */
    public static boolean isAdventureMode(final Player player) {
        return player.getGameMode() == GameMode.ADVENTURE;
    }

    /**
     * Checks if the player is currently in adventure mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in adventure mode.
     */
    public static boolean isSurvivalMode(final Player player) {
        return player.getGameMode() == GameMode.SURVIVAL;
    }

    /**
     * Checks if the player has an open menu while left-clicking.
     *
     * @param view   - The InventoryView being compared.
     * @param action - The action being checked.
     * @return If the player is currently interacting with an open menu.
     */
    public static boolean isMenuClick(final InventoryView view, final Action action) {
        return isCraftingInv(view) || (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK);
    }

    /**
     * Gets the current crafting slot contents of the player.
     *
     * @param player - the Player to get the crafting contents of.
     * @return The ItemStack list of crafting slot contents.
     */
    public static ItemStack[] getTopContents(final Player player) {
        ItemStack[] tempContents = player.getOpenInventory().getTopInventory().getContents();
        ItemStack[] contents = new ItemStack[5];
        for (int i = 0; i <= 4; i++) {
            contents[i] = tempContents[i].clone();
        }
        return contents;
    }

    /**
     * Sets the currently selected hotbar slot for the specified player.
     *
     * @param player - The player to have their slot set.
     */
    public static void setHotbarSlot(final Player player, int slot) {
        if (slot <= 8 && slot >= 0) {
            player.getInventory().setHeldItemSlot(slot);
        }
    }

    /**
     * Gets the current ItemStack in the players Main Hand,
     * If it is empty it will get the ItemStack in the Offhand,
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @return The current ItemStack in the players hand.
     */
    public static ItemStack getHandItem(final Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9") && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return player.getInventory().getItemInMainHand();
        } else if (ServerUtils.hasSpecificUpdate("1_9") && player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return null;
    }

    /**
     * Gets the current ItemStack in the players hand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @param type   - The hand type to get.
     * @return The current ItemStack in the players hand.
     */
    public static ItemStack getPerfectHandItem(final Player player, final String type) {
        if (ServerUtils.hasSpecificUpdate("1_9") && type != null && type.equalsIgnoreCase("HAND")) {
            return player.getInventory().getItemInMainHand();
        } else if (ServerUtils.hasSpecificUpdate("1_9") && type != null && type.equalsIgnoreCase("OFF_HAND")) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return null;
    }

    /**
     * Gets the current ItemStack in the players Main Hand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @return The current ItemStack in the players hand.
     */
    public static ItemStack getMainHandItem(final Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            return player.getInventory().getItemInMainHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return null;
    }

    /**
     * Gets the current ItemStack in the players Offhand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @return The current ItemStack in the players hand.
     */
    public static ItemStack getOffHandItem(final Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return null;
    }

    /**
     * Sets the specified ItemStack to the players Main Hand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to have the item set.
     * @param item   - The ItemStack to be set.
     */
    public static void setMainHandItem(final Player player, final ItemStack item) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            player.getInventory().setItemInMainHand(item);
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            LegacyAPI.setInHandItem(player, item);
        }
    }

    /**
     * Sets the specified ItemStack to the players Offhand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to have the item set.
     * @param item   - The ItemStack to be set.
     */
    public static void setOffHandItem(final Player player, final ItemStack item) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            player.getInventory().setItemInOffHand(item);
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            LegacyAPI.setInHandItem(player, item);
        }
    }

    /**
     * Resolves a bug where canceling an experience level event causes it to visually glitch
     * and remain showing the uncanceled experience levels.
     * <p>
     * This simply gets the players current experience levels and resets
     * them to cause a clientside update.
     *
     * @param player - The player to have their levels set.
     */
    public static void updateExperienceLevels(final Player player) {
        SchedulerUtils.runLater(1L, () -> {
            player.setExp(player.getExp());
            player.setLevel(player.getLevel());
        });
    }

    /**
     * Updates the specified players inventory.
     *
     * @param player - The player to have their inventory updated.
     */
    public static void updateInventory(final Player player) {
        updateInventory(player, null, 0L);
    }

    /**
     * Updates the specified players inventory.
     *
     * @param player - The player to have their inventory updated.
     * @param delay  - The ticks to wait before updating the inventory.
     */
    public static void updateInventory(final Player player, final long delay) {
        updateInventory(player, null, delay);
    }

    /**
     * Updates the specified players inventory.
     *
     * @param player - The player to have their inventory updated.
     * @param item   - The item expected to be updated.
     * @param delay  - The ticks to wait before updating the inventory.
     */
    public static void updateInventory(final Player player, final ItemStack item, final long delay) {
        SchedulerUtils.runAsyncLater(delay, () -> {
            try {
                for (int i = 0; i < 36; i++) {
                    final ItemStack invItem = player.getInventory().getItem(i);
                    if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                        ReflectionUtils.sendPacketPlayOutSetSlot(player, invItem, (i < 9 ? (i + 36) : i));
                    }
                }
                if (ServerUtils.hasSpecificUpdate("1_9")) {
                    if (item == null || (getOffHandItem(player) != null && Objects.requireNonNull(getOffHandItem(player)).clone().isSimilar(item))) {
                        ReflectionUtils.sendPacketPlayOutSetSlot(player, getOffHandItem(player), 45);
                    }
                }
                if (isCraftingInv(player.getOpenInventory())) {
                    for (int i = 4; i >= 0; i--) {
                        final ItemStack invItem = player.getOpenInventory().getTopInventory().getItem(i);
                        if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                            ReflectionUtils.sendPacketPlayOutSetSlot(player, invItem, i);
                        }
                    }
                    for (int i = 0; i <= 3; i++) {
                        final ItemStack invItem = player.getInventory().getItem(i);
                        if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                            ReflectionUtils.sendPacketPlayOutSetSlot(player, player.getInventory().getItem(i + 36), (8 - i));
                        }
                    }
                }
            } catch (Exception e) {
                ServerUtils.sendDebugTrace(e);
            }
        });
    }

    /**
     * Gets the current skull owner of the specified item.
     *
     * @param item - The item to have its skull owner fetched.
     * @return The ItemStacks current skull owner.
     */
    public static String getSkullOwner(final ItemStack item) {
        if (ServerUtils.hasSpecificUpdate("1_12") && item != null && item.hasItemMeta() && ItemHandler.isSkull(item.getType())
                && ((SkullMeta) Objects.requireNonNull(item.getItemMeta())).hasOwner() && ItemHandler.usesOwningPlayer()) {
            String owner = Objects.requireNonNull(((SkullMeta) item.getItemMeta()).getOwningPlayer()).getName();
            if (owner != null) {
                return owner;
            }
        } else if (item != null && item.hasItemMeta()
                && ItemHandler.isSkull(item.getType())
                && ((SkullMeta) Objects.requireNonNull(item.getItemMeta())).hasOwner()) {
            String owner = LegacyAPI.getSkullOwner(((SkullMeta) item.getItemMeta()));
            if (owner != null) {
                return owner;
            }
        }
        return "NULL";
    }

    /**
     * Gets the Player instance from their String name.
     *
     * @param playerName - The player name to be transformed.
     * @return The fetched Player instance.
     */
    public static Player getPlayerString(final String playerName) {
        Player args = null;
        try {
            args = Bukkit.getPlayer(UUID.fromString(playerName));
        } catch (Exception ignored) {
        }
        if (playerName != null && Core.getCore().getDependencies().nickEnabled()) {
            try {
                de.domedd.betternick.api.nickedplayer.NickedPlayer np = new de.domedd.betternick.api.nickedplayer.NickedPlayer(LegacyAPI.getPlayer(playerName));
                if (np.isNicked()) {
                    return LegacyAPI.getPlayer(np.getRealName());
                } else {
                    return LegacyAPI.getPlayer(playerName);
                }
            } catch (NoClassDefFoundError e) {
                if (BetterNick.getApi().isPlayerNicked(LegacyAPI.getPlayer(playerName))) {
                    return LegacyAPI.getPlayer(BetterNick.getApi().getRealName(LegacyAPI.getPlayer(playerName)));
                } else {
                    return LegacyAPI.getPlayer(playerName);
                }
            }
        } else if (playerName != null && Core.getCore().getDependencies().nickAPIEnabled()) {
            if (xyz.haoshoku.nick.api.NickAPI.isNicked(xyz.haoshoku.nick.api.NickAPI.getPlayerOfNickedName(playerName))) {
                return xyz.haoshoku.nick.api.NickAPI.getPlayerOfNickedName(playerName);
            } else {
                return LegacyAPI.getPlayer(playerName);
            }
        } else if (args == null) {
            return LegacyAPI.getPlayer(playerName);
        }
        return args;
    }

    /**
     * Gets the String Name of the Player.
     *
     * @param player - The player to have their String Name fetched.
     * @return The String Name of the player.
     */
    public static String getPlayerName(final Player player) {
        try {
            if (player != null && Core.getCore().getDependencies().nickEnabled()) {
                try {
                    de.domedd.betternick.api.nickedplayer.NickedPlayer np = new de.domedd.betternick.api.nickedplayer.NickedPlayer(player);
                    if (np.isNicked()) {
                        return np.getRealName();
                    } else {
                        return player.getName();
                    }
                } catch (NoClassDefFoundError e) {
                    if (BetterNick.getApi().isPlayerNicked(player)) {
                        return BetterNick.getApi().getRealName(player);
                    } else {
                        return player.getName();
                    }
                }
            } else if (player != null && Core.getCore().getDependencies().nickAPIEnabled()) {
                if (xyz.haoshoku.nick.api.NickAPI.isNicked(player)) {
                    return xyz.haoshoku.nick.api.NickAPI.getOriginalName(player);
                } else {
                    return player.getName();
                }
            } else if (player != null) {
                return player.getName();
            }
        } catch (Exception e) {
            return player.getName();
        }
        return "";
    }

    /**
     * Gets the UUID of the Player.
     * If the UUID does not exist it will fetch their String name.
     *
     * @param player - The player to have their UUID fetched.
     * @return The UUID of the player or if not found, their String name.
     */
    public static String getPlayerID(final Player player) {
        try {
            if (player != null && ServerUtils.hasSpecificUpdate("1_8")) {
                return player.getUniqueId().toString();
            } else if (player != null && Core.getCore().getDependencies().nickEnabled()) {
                try {
                    de.domedd.betternick.api.nickedplayer.NickedPlayer np = new de.domedd.betternick.api.nickedplayer.NickedPlayer(player);
                    if (np.isNicked()) {
                        if (ServerUtils.hasSpecificUpdate("1_8")) {
                            return np.getUniqueId().toString();
                        } else {
                            return np.getRealName();
                        }
                    } else {
                        return player.getName();
                    }
                } catch (NoClassDefFoundError e) {
                    if (BetterNick.getApi().isPlayerNicked(player)) {
                        return BetterNick.getApi().getRealName(player);
                    } else {
                        return player.getName();
                    }
                }
            } else if (player != null && Core.getCore().getDependencies().nickAPIEnabled()) {
                if (xyz.haoshoku.nick.api.NickAPI.isNicked(player)) {
                    return xyz.haoshoku.nick.api.NickAPI.getOriginalName(player);
                } else {
                    return player.getName();
                }
            } else if (player != null) {
                return player.getName();
            }
        } catch (Exception e) {
            return player.getName();
        }
        return "";
    }

    /**
     * Gets the UUID of the OfflinePlayer.
     * If the UUID does not exist it will fetch their String name.
     *
     * @param player - The OfflinePlayer instance to have their UUID fetched.
     * @return The UUID of the player or if not found, their String name.
     */
    public static String getOfflinePlayerID(final OfflinePlayer player) {
        try {
            if (player != null && ServerUtils.hasSpecificUpdate("1_8")) {
                return player.getUniqueId().toString();
            } else if (player != null && Core.getCore().getDependencies().nickEnabled()) {
                try {
                    de.domedd.betternick.api.nickedplayer.NickedPlayer np = new de.domedd.betternick.api.nickedplayer.NickedPlayer((BetterNick) player);
                    if (np.isNicked()) {
                        if (ServerUtils.hasSpecificUpdate("1_8")) {
                            return np.getUniqueId().toString();
                        } else {
                            return np.getRealName();
                        }
                    } else {
                        return player.getName();
                    }
                } catch (NoClassDefFoundError e) {
                    if (BetterNick.getApi().isPlayerNicked((Player) player)) {
                        return BetterNick.getApi().getRealName((Player) player);
                    } else {
                        return player.getName();
                    }
                }
            } else if (player != null && Core.getCore().getDependencies().nickAPIEnabled()) {
                if (xyz.haoshoku.nick.api.NickAPI.isNickedName(Objects.requireNonNull(player.getName()))) {
                    return xyz.haoshoku.nick.api.NickAPI.getOriginalName(xyz.haoshoku.nick.api.NickAPI.getPlayerOfNickedName(player.getName()));
                } else {
                    return player.getName();
                }
            } else if (player != null) {
                return player.getName();
            }
        } catch (Exception e) {
            return player.getName();
        }
        return "";
    }

    /**
     * Quick saves the current inventories crafting items.
     *
     * @param player - The player having their crafting items saved.
     */
    public static void quickCraftSave(final Player player) {
        if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
            ItemStack[] contents = new ItemStack[5];
            for (int i = 0; i <= 4; i++) {
                contents[i] = player.getOpenInventory().getTopInventory().getContents()[i].clone();
            }
            craftingItems.put(PlayerHandler.getPlayerID(player), contents);
        }
    }

    /**
     * Emulates the Player dropping the ItemStack.
     *
     * @param player - The Player being referenced.
     * @param item   - The item to be dropped.
     */
    public static void dropItem(final Player player, final ItemStack item) {
        Location location = player.getLocation();
        location.setY(location.getY() + 1);
        Item dropped = player.getWorld().dropItem(location, item);
        dropped.setVelocity(location.getDirection().multiply(.30));
    }

    /**
     * Gets the crafting items HashMap of players crafting contents.
     *
     * @return The HashMap of players and their crafting contents.
     */
    public static HashMap<String, ItemStack[]> getCraftItems() {
        return craftingItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addCraftItems(final Player player, final ItemStack[] items) {
        craftingItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Gets the crafting items HashMap of players prior to creative crafting contents.
     *
     * @return The HashMap of players and their prior to creative crafting contents.
     */
    public static HashMap<String, ItemStack[]> getCreativeCraftItems() {
        return creativeCraftingItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addCreativeCraftItems(final Player player, final ItemStack[] items) {
        creativeCraftingItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Removes the Player and their Crafting items from a HashMap.
     *
     * @param player - The player being referenced.
     */
    public static void removeCreativeCraftItems(final Player player) {
        creativeCraftingItems.remove(PlayerHandler.getPlayerID(player));
    }

    /**
     * Gets the crafting items HashMap of players prior to opened inventory crafting contents.
     *
     * @return The HashMap of players and their prior to opened inventory crafting contents.
     */
    public static HashMap<String, ItemStack[]> getOpenCraftItems() {
        return craftingOpenItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addOpenCraftItems(final Player player, final ItemStack[] items) {
        craftingOpenItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Removes the Player and their Crafting items from a HashMap.
     *
     * @param player - The player being referenced.
     */
    public static void removeOpenCraftItems(final Player player) {
        craftingOpenItems.remove(PlayerHandler.getPlayerID(player));
    }

    /**
     * Constantly cycles through the players crafting slots saving them to a HashMap for later use.
     */
    public static void cycleCrafting() {
        SchedulerUtils.runAsyncTimer(15L, 0L, () -> {
            Collection<?> playersOnlineNew;
            Player[] playersOnlineOld;
            try {
                if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                    if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                        playersOnlineNew = ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                        for (Object objPlayer : playersOnlineNew) {
                            if (((Player) objPlayer).isOnline() && PlayerHandler.isCraftingInv(((Player) objPlayer).getOpenInventory())) {
                                ItemStack[] tempContents = ((Player) objPlayer).getOpenInventory().getTopInventory().getContents();
                                ItemStack[] contents = new ItemStack[5];
                                for (int i = 0; i <= 4; i++) {
                                    if (tempContents[i] != null) {
                                        contents[i] = tempContents[i].clone();
                                    }
                                }
                                craftingItems.put(PlayerHandler.getPlayerID(((Player) objPlayer)), contents);
                            } else {
                                craftingItems.remove(PlayerHandler.getPlayerID((Player) objPlayer));
                            }
                        }
                    }
                } else {
                    playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                    for (Player player : playersOnlineOld) {
                        if (player.isOnline() && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
                            ItemStack[] tempContents = player.getOpenInventory().getTopInventory().getContents();
                            ItemStack[] contents = new ItemStack[5];
                            for (int i = 0; i <= 4; i++) {
                                contents[i] = tempContents[i].clone();
                            }
                            craftingItems.put(PlayerHandler.getPlayerID(player), contents);
                        } else {
                            craftingItems.remove(PlayerHandler.getPlayerID(player));
                        }
                    }
                }
            } catch (Exception e) {
                ServerUtils.sendDebugTrace(e);
            }
        });
    }

    /**
     * Attempts to get the Block that the Player is targeting.
     * Excludes transparent.
     *
     * @param player - The Player targeting the block.
     * @param range  - How far the block is allowed to be.
     * @return If the targeted block (if any).
     */
    public static Block getTargetBlock(final Player player, final int range) {
        Block block = null;
        try {
            Set<Material> ignore = new HashSet<>();
            if (ServerUtils.hasSpecificUpdate("1_13")) {
                ignore.addAll(Arrays.asList(Material.AIR, Material.WATER, Material.LAVA));
            } else {
                ignore.addAll(Arrays.asList(Material.AIR, ItemHandler.getMaterial("STATIONARY_WATER", null), ItemHandler.getMaterial("FLOWING_WATER", null), Material.WATER,
                        ItemHandler.getMaterial("STATIONARY_LAVA", null), ItemHandler.getMaterial("FLOWING_LAVA", null), Material.LAVA));
            }
            block = player.getTargetBlock(ignore, range);
        } catch (final Throwable t1) {
            try {
                HashSet<Byte> ignore = new HashSet<>(Arrays.asList((byte) 0, (byte) 8, (byte) 9, (byte) 10, (byte) 11));
                block = (Block) player.getClass().getMethod("getTargetBlock", HashSet.class, int.class).invoke(player, ignore, range);
            } catch (Exception e) {
                ServerUtils.sendSevereTrace(e);
            }
        }
        return block;
    }

    /**
     * Gets the Nearby Players from the specified Players Location inside the Range.
     *
     * @param player - The Player that is searching for Nearby Players.
     * @param range  - The distance to check for Nearby Players.
     * @return The String name of the Nearby Player.
     */
    public static String getNearbyPlayer(final Player player, final int range) {
        ArrayList<Location> sight = new ArrayList<>();
        ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
        Location origin = player.getEyeLocation();
        sight.add(origin.clone().add(origin.getDirection()));
        sight.add(origin.clone().add(origin.getDirection().multiply(range)));
        sight.add(origin.clone().add(origin.getDirection().multiply(range + 3)));
        for (Location location : sight) {
            for (Entity entity : entities) {
                if (Math.abs(entity.getLocation().getX() - location.getX()) < 1.3) {
                    if (Math.abs(entity.getLocation().getY() - location.getY()) < 1.5) {
                        if (Math.abs(entity.getLocation().getZ() - location.getZ()) < 1.3) {
                            if (entity instanceof Player) {
                                return entity.getName();
                            }
                        }
                    }
                }
            }
        }
        return (Core.getCore().getLang().getLangMessage("placeholders.PLAYER_INTERACT") != null ? Core.getCore().getLang().getLangMessage("placeholders.PLAYER_INTERACT") : "INVALID");
    }

    /**
     * Executes an input of methods for the currently online players.
     *
     * @param input - The methods to be executed.
     */
    public static void forOnlinePlayers(final Consumer<Player> input) {
        try {
		  /* New method for getting the current online players.
			 This is for MC 1.12+
			*/
            if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                for (Object objPlayer : ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]))) {
                    input.accept(((Player) objPlayer));
                }
            }
		  /* New old for getting the current online players.
			 This is for MC versions below 1.12.

			 @deprecated Legacy version of getting online players.
			*/
            else {
                for (Player player : ((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]))) {
                    input.accept(player);
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }
}