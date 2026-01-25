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

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.CompatUtils;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public static void safeInventoryClose(final @Nonnull Player player) {
        player.openInventory(Bukkit.createInventory(player.getInventory().getHolder(), 9));
        player.closeInventory();
    }

    /**
     * Removes the Player from Creative Mode.
     *
     * @param who    - The Player being referenced.
     * @param altWho - The other Player being referenced (if any).
     */
    public static void setMode(final @Nonnull CommandSender who, final @Nullable Player altWho, final @Nonnull GameMode gamemode, final boolean silent, final boolean doSave) {
        Bukkit.getPluginManager().callEvent(new PlayerExitCreativeEvent(who, altWho, gamemode, silent, doSave));
    }

    /**
     * Sets the Player to Creative Mode.
     *
     * @param who    - The Player being referenced.
     * @param altWho - The other Player being referenced (if any).
     * @param silent - If the event should be called quietly.
     */
    public static void setCreative(final @Nonnull CommandSender who, final @Nullable Player altWho, final boolean silent) {
        Bukkit.getPluginManager().callEvent(new PlayerEnterCreativeEvent(who, altWho, false, false, silent));
    }

    /**
     * Refreshes the Creative stats for the Player.
     *
     * @param who - The Player being referenced.
     */
    public static void refreshCreative(final @Nonnull CommandSender who) {
        Bukkit.getPluginManager().callEvent(new PlayerEnterCreativeEvent(who, null, true, false, true));
    }

    /**
     * Checks if the Entity is a real player.
     *
     * @param entity - The entity being checked.
     * @return If the entity is a real Player.
     */
    public static boolean isPlayer(final @Nonnull Entity entity) {
        try {
            if (Core.getCore().getDependencies().citizensEnabled() && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity)) {
                return false;
            } else if (!(entity instanceof Player)) {
                return false;
            } else {
                if (PlayerHandler.getPlayerID((Player) entity).isEmpty()) {
                    return false;
                } else if (Objects.requireNonNull(((Player) entity).getAddress()).getHostString() == null) {
                    return false;
                }
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
    public static void clearItems(final @Nonnull Player player) {
        final PlayerInventory inventory = player.getInventory();
        final Inventory craftView = CompatUtils.getTopInventory(player);
        inventory.setHelmet(new ItemStack(Material.AIR));
        inventory.setChestplate(new ItemStack(Material.AIR));
        inventory.setLeggings(new ItemStack(Material.AIR));
        inventory.setBoots(new ItemStack(Material.AIR));
        player.setItemOnCursor(new ItemStack(Material.AIR));
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            PlayerHandler.setOffHandItem(player, new ItemStack(Material.AIR));
        }
        inventory.clear();
        craftView.clear();
    }

    /**
     * Checks if the InventoryView is a player crafting inventory.
     *
     * @param object - The InventoryView or Player to be checked.
     * @return If the currently open inventory is a player crafting inventory.
     */
    public static boolean isCraftingInv(final @Nonnull Object object) {
        final Object view = (object instanceof Player ? CompatUtils.getOpenInventory(object) : object);
        return (!CompatUtils.getInventoryType(view).name().equalsIgnoreCase("HOPPER") && !CompatUtils.getInventoryType(view).name().equalsIgnoreCase("BREWING") && CompatUtils.getTopInventory(view).getSize() == PLAYER_CRAFT_INV_SIZE);
    }

    /**
     * Checks if the player is currently in creative mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in creative mode.
     */
    public static boolean isCreativeMode(final @Nonnull Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    /**
     * Checks if the player is currently in adventure mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in adventure mode.
     */
    public static boolean isAdventureMode(final @Nonnull Player player) {
        return player.getGameMode() == GameMode.ADVENTURE;
    }

    /**
     * Checks if the player is currently in adventure mode.
     *
     * @param player - The player to be checked.
     * @return If the player is currently in adventure mode.
     */
    public static boolean isSurvivalMode(final @Nonnull Player player) {
        return player.getGameMode() == GameMode.SURVIVAL;
    }

    /**
     * Checks if the player has an open menu while left-clicking.
     *
     * @param player   - The Player being referenced.
     * @param action - The action being checked.
     * @return If the player is currently interacting with an open menu.
     */
    public static boolean isMenuClick(final @Nonnull Player player, final @Nonnull Action action) {
        return !isCraftingInv(player) && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);
    }

    /**
     * Gets the current crafting slot contents of the player.
     *
     * @param player - the Player to get the crafting contents of.
     * @return The ItemStack list of crafting slot contents.
     */
    public static @Nonnull ItemStack[] getTopContents(final @Nonnull Player player) {
        ItemStack[] tempContents = CompatUtils.getTopInventory(player).getContents();
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
    public static void setHotbarSlot(final @Nonnull Player player, int slot) {
        if (slot <= 8 && slot >= 0) {
            player.getInventory().setHeldItemSlot(slot);
        }
    }

    /**
     * Gets the next empty hotbar slot, looping right from the current slot.
     *
     * @param inventory - The player's inventory
     * @param currentSlot - The starting slot index
     * @return The next empty slot index, or first empty inventory slot if hotbar is full
     */
    public static int getNextBestSlot(final PlayerInventory inventory, final int currentSlot) {
        for (int i = 1; i <= 9; i++) {
            int checkSlot = (currentSlot + i) % 9;
            if (checkSlot == currentSlot) return inventory.firstEmpty();
            ItemStack item = inventory.getItem(checkSlot);
            if (item == null || item.getType() == Material.AIR) {
                return checkSlot;
            }
        }
        return inventory.firstEmpty();
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
    public static @Nonnull ItemStack getHandItem(final @Nonnull Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9") && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return player.getInventory().getItemInMainHand();
        } else if (ServerUtils.hasSpecificUpdate("1_9") && player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return new ItemStack(Material.AIR);
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
    public static @Nonnull ItemStack getPerfectHandItem(final @Nonnull Player player, final @Nullable String type) {
        if (ServerUtils.hasSpecificUpdate("1_9") && type != null && type.equalsIgnoreCase("HAND")) {
            return player.getInventory().getItemInMainHand();
        } else if (ServerUtils.hasSpecificUpdate("1_9") && type != null && type.equalsIgnoreCase("OFF_HAND")) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Gets the current ItemStack in the players Main Hand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @return The current ItemStack in the players hand.
     */
    public static @Nonnull ItemStack getMainHandItem(final @Nonnull Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            return player.getInventory().getItemInMainHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Gets the current ItemStack in the players Offhand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to be checked.
     * @return The current ItemStack in the players hand.
     */
    public static @Nonnull ItemStack getOffHandItem(final @Nonnull Player player) {
        if (ServerUtils.hasSpecificUpdate("1_9")) {
            return player.getInventory().getItemInOffHand();
        } else if (!ServerUtils.hasSpecificUpdate("1_9")) {
            return LegacyAPI.getInHandItem(player);
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Sets the specified ItemStack to the players Main Hand.
     * If the server version is below MC 1.9 it will use the
     * legacy hand method to get the single hand.
     *
     * @param player - The player to have the item set.
     * @param item   - The ItemStack to be set.
     */
    public static void setMainHandItem(final @Nonnull Player player, final @Nonnull ItemStack item) {
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
    public static void setOffHandItem(final @Nonnull Player player, final @Nonnull ItemStack item) {
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
    public static void updateExperienceLevels(final @Nonnull Player player) {
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
    public static void updateInventory(final @Nonnull Player player) {
        updateInventory(player, null, 0L);
    }

    /**
     * Updates the specified players inventory.
     *
     * @param player - The player to have their inventory updated.
     * @param delay  - The ticks to wait before updating the inventory.
     */
    public static void updateInventory(final @Nonnull Player player, final long delay) {
        updateInventory(player, null, delay);
    }

    /**
     * Updates the specified players inventory.
     * <p>
     * Notes;
     * WindowId: 0 - Player Inventory Instance.
     *
     * @param player - The player to have their inventory updated.
     * @param item   - The item expected to be updated.
     * @param delay  - The ticks to wait before updating the inventory.
     */
    public static void updateInventory(final @Nonnull Player player, final @Nullable ItemStack item, final long delay) {
        SchedulerUtils.runAsyncLater(delay, () -> {
            try {
                /* Updates Main Inventory Slot(s) */
                for (int i = 0; i < 36; i++) {
                    final ItemStack invItem = player.getInventory().getItem(i);
                    if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                        ReflectionUtils.sendPacketPlayOutSetSlot(player, invItem, (i < 9 ? (i + 36) : i), 0);
                    }
                }
                /* Updates Offhand Slot. */
                if (ServerUtils.hasSpecificUpdate("1_9")) {
                    if (item == null || (getOffHandItem(player).clone().isSimilar(item))) {
                        ReflectionUtils.sendPacketPlayOutSetSlot(player, getOffHandItem(player), 45, 0);
                    }
                }
                if (isCraftingInv(player)) {
                    /* Updates Crafting Slot(s) */
                    for (int i = 4; i >= 0; i--) {
                        final ItemStack invItem = CompatUtils.getTopInventory(player).getItem(i);
                        if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                            ReflectionUtils.sendPacketPlayOutSetSlot(player, invItem, i, 0);
                        }
                    }
                    /* Updates Armor Slot(s) */
                    for (int i = 0; i <= 3; i++) {
                        final ItemStack invItem = player.getInventory().getItem(i);
                        if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                            ReflectionUtils.sendPacketPlayOutSetSlot(player, player.getInventory().getItem(i + 36), (8 - i), 0);
                        }
                    }
                } else {
                    /* Update Top Inventory. */
                    try {
                        final Object handle = ReflectionUtils.getEntity(player);
                        if (handle != null) {
                            final Object container = handle.getClass().getField(ReflectionUtils.MinecraftField.ActiveContainer.getField()).get(handle);
                            if (container != null) {
                                final int windowId = (int) container.getClass().getField(ReflectionUtils.MinecraftField.windowId.getField()).get(container);
                                for (int i = CompatUtils.getTopInventory(player).getSize() - 1; i >= 0; i--) {
                                    final ItemStack invItem = CompatUtils.getTopInventory(player).getItem(i);
                                    if (item == null || (invItem != null && invItem.clone().isSimilar(item))) {
                                        ReflectionUtils.sendPacketPlayOutSetSlot(player, invItem, i, windowId);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        ServerUtils.sendDebugTrace(e);
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
    public static @Nonnull String getSkullOwner(final @Nonnull ItemStack item) {
        if (item.hasItemMeta()) {
            final ItemMeta itemMeta = item.getItemMeta();
            if (ServerUtils.hasSpecificUpdate("1_12") && itemMeta != null && ItemHandler.isSkull(item.getType())
                    && ((SkullMeta) itemMeta).hasOwner() && ItemHandler.usesOwningPlayer()) {
                final OfflinePlayer owner = ((SkullMeta) itemMeta).getOwningPlayer();
                if (owner != null && owner.getName() != null) {
                    return owner.getName();
                }
            } else if (itemMeta != null && ItemHandler.isSkull(item.getType()) && ((SkullMeta) itemMeta).hasOwner()) {
                String owner = LegacyAPI.getSkullOwner(((SkullMeta) itemMeta));
                if (owner != null) {
                    return owner;
                }
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
    public static @Nullable Player getPlayerString(final @Nonnull String playerName) {
        Player args = null;
        try {
            args = Bukkit.getPlayer(UUID.fromString(playerName));
        } catch (Exception ignored) {}
        if (Core.getCore().getDependencies().nickAPIEnabled()) {
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
     * Gets the Player instance from their String name.
     *
     * @param playerName - The player name to be transformed.
     * @return The fetched Player instance.
     */
    public static @Nullable OfflinePlayer getOfflinePlayer(final @Nonnull String playerName) {
        OfflinePlayer args = null;
        try {
            args = Bukkit.getOfflinePlayer(UUID.fromString(playerName));
        } catch (Exception ignored) {}
        if (args == null) {
            final OfflinePlayer[] result = { null };
            PlayerHandler.forOfflinePlayers(player -> {
                if (getOfflinePlayerID(player).equals(playerName)) {
                    result[0] = player;
                }
            });
            args = result[0];
        }
        return args;
    }

    /**
     * Gets the String Name of the Player.
     *
     * @param player - The player to have their String Name fetched.
     * @return The String Name of the player.
     */
    public static @Nonnull String getPlayerName(final @Nonnull Player player) {
        try {
            if (Core.getCore().getDependencies().nickAPIEnabled()) {
                if (xyz.haoshoku.nick.api.NickAPI.isNicked(player)) {
                    return xyz.haoshoku.nick.api.NickAPI.getOriginalName(player);
                } else {
                    return player.getName();
                }
            } else {
                return player.getName();
            }
        } catch (Exception e) {
            return player.getName();
        }
    }

    /**
     * Gets the UUID of the Player.
     * If the UUID does not exist it will fetch their String name.
     *
     * @param player - The player to have their UUID fetched.
     * @return The UUID of the player or if not found, their String name.
     */
    public static @Nonnull String getPlayerID(final @Nullable Player player) {
        if (player == null) {
            return "";
        }
        try {
            if (Core.getCore().getDependencies().nickAPIEnabled()) {
                if (xyz.haoshoku.nick.api.NickAPI.isNicked(player)) {
                    return xyz.haoshoku.nick.api.NickAPI.getOriginalName(player);
                } else {
                    return player.getName();
                }
            } else {
                return player.getUniqueId().toString();
            }
        } catch (Exception e) {
            return player.getName();
        }
    }

    /**
     * Gets the UUID of the OfflinePlayer.
     * If the UUID does not exist it will fetch their String name.
     *
     * @param player - The OfflinePlayer instance to have their UUID fetched.
     * @return The UUID of the player or if not found, their String name.
     */
    public static @Nonnull String getOfflinePlayerID(final @Nullable OfflinePlayer player) {
        if (player == null) {
            return "";
        }
        final String playerName = player.getName();
        if (playerName != null) {
            try {
                if (Core.getCore().getDependencies().nickAPIEnabled()) {
                    if (xyz.haoshoku.nick.api.NickAPI.isNickedName(playerName)) {
                        return xyz.haoshoku.nick.api.NickAPI.getOriginalName(xyz.haoshoku.nick.api.NickAPI.getPlayerOfNickedName(playerName));
                    } else {
                        return playerName;
                    }
                } else {
                    return player.getUniqueId().toString();
                }
            } catch (Exception e) {
                return playerName;
            }
        }
        return "";
    }

    /**
     * Quick saves the current inventories crafting items.
     *
     * @param player - The player having their crafting items saved.
     */
    public static void quickCraftSave(final @Nonnull Player player) {
        if (PlayerHandler.isCraftingInv(player)) {
            ItemStack[] contents = new ItemStack[5];
            for (int i = 0; i <= 4; i++) {
                contents[i] = CompatUtils.getTopInventory(player).getContents()[i].clone();
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
    public static void dropItem(final @Nonnull Player player, final @Nonnull ItemStack item) {
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
    public static @Nonnull HashMap<String, ItemStack[]> getCraftItems() {
        return craftingItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addCraftItems(final @Nonnull Player player, final @Nonnull ItemStack[] items) {
        craftingItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Gets the crafting items HashMap of players prior to creative crafting contents.
     *
     * @return The HashMap of players and their prior to creative crafting contents.
     */
    public static @Nonnull HashMap<String, ItemStack[]> getCreativeCraftItems() {
        return creativeCraftingItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addCreativeCraftItems(final @Nonnull Player player, final @Nonnull ItemStack[] items) {
        creativeCraftingItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Removes the Player and their Crafting items from a HashMap.
     *
     * @param player - The player being referenced.
     */
    public static void removeCreativeCraftItems(final @Nonnull Player player) {
        creativeCraftingItems.remove(PlayerHandler.getPlayerID(player));
    }

    /**
     * Gets the crafting items HashMap of players prior to opened inventory crafting contents.
     *
     * @return The HashMap of players and their prior to opened inventory crafting contents.
     */
    public static @Nonnull HashMap<String, ItemStack[]> getOpenCraftItems() {
        return craftingOpenItems;
    }

    /**
     * Adds the Player and their Crafting items to a HashMap.
     *
     * @param player - The player being referenced.
     * @param items  - THe items to be added.
     */
    public static void addOpenCraftItems(final @Nonnull Player player, final @Nonnull ItemStack[] items) {
        craftingOpenItems.put(PlayerHandler.getPlayerID(player), items);
    }

    /**
     * Removes the Player and their Crafting items from a HashMap.
     *
     * @param player - The player being referenced.
     */
    public static void removeOpenCraftItems(final @Nonnull Player player) {
        craftingOpenItems.remove(PlayerHandler.getPlayerID(player));
    }

    /**
     * Constantly cycles through the players crafting slots saving them to a HashMap for later use.
     */
    public static void cycleCrafting() {
        SchedulerUtils.runAsyncAtInterval(15L, 0L, () -> forOnlinePlayers(player -> {
            if (player.isOnline() && PlayerHandler.isCraftingInv(player)) {
                ItemStack[] contents = getTopContents(player);
                craftingItems.put(PlayerHandler.getPlayerID(player), contents);
            } else {
                craftingItems.remove(PlayerHandler.getPlayerID(player));
            }
        }));
    }

    /**
     * Attempts to get the Block that the Player is targeting.
     * Excludes transparent.
     *
     * @param player - The Player targeting the block.
     * @param range  - How far the block is allowed to be.
     * @return If the targeted block (if any).
     */
    public static @Nullable Block getTargetBlock(final @Nonnull Player player, final int range) {
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
    public static @Nonnull String getNearbyPlayer(final @Nonnull Player player, final int range) {
        if (SchedulerUtils.isMainThread()) {
            if (ServerUtils.hasSpecificUpdate("1_14")) { // raytrace doesn't exist in versions below 1.14.
                final Location eyeLocation = player.getEyeLocation();
                final org.bukkit.util.Vector direction = eyeLocation.getDirection().normalize();
                double closestDistanceSquared = Double.MAX_VALUE;
                Player targetPlayer = null;
                for (Entity entity : player.getNearbyEntities(range, range, range)) {
                    if (entity instanceof Player) {
                        Player nearbyPlayer = (Player) entity;
                        if (nearbyPlayer.equals(player)) continue;
                        final org.bukkit.util.RayTraceResult result = nearbyPlayer.getBoundingBox().rayTrace(eyeLocation.toVector(), direction, range);
                        if (result != null) {
                            double distanceSquared = eyeLocation.distanceSquared(result.getHitPosition().toLocation(player.getWorld()));
                            if (distanceSquared < closestDistanceSquared) {
                                closestDistanceSquared = distanceSquared;
                                targetPlayer = nearbyPlayer;
                            }
                        }
                    }
                }
                if (targetPlayer != null) return targetPlayer.getName();
            } else {
                final List<Block> lineOfSight = player.getLineOfSight(null, range);
                for (final Block block : lineOfSight) {
                    final Location blockLocation = block.getLocation();
                    for (Entity entity : player.getWorld().getNearbyEntities(blockLocation, 0.5, 0.5, 0.5)) {
                        if (entity instanceof Player) {
                            final Player nearbyPlayer = (Player) entity;
                            if (!nearbyPlayer.equals(player)) return nearbyPlayer.getName();
                        }
                    }
                }
            }
        }
        return (!Core.getCore().getLang().getLangMessage("placeholders.PLAYER_INTERACT").isEmpty() ? Core.getCore().getLang().getLangMessage("placeholders.PLAYER_INTERACT") : "INVALID");
    }

    /**
     * Executes an input of methods for the selected entities.
     *
     * @param sender - The sender who selected the entities.
     * @param entities - The entities to be fetched.
     * @param input - The methods to be executed.
     */
    public static void forSelectedEntities(final @Nonnull CommandSender sender, final @Nullable String entities, final @Nonnull Consumer<Player> input) {
        try {
            if (entities == null) {
                input.accept(null);
            } else {
                final List<Entity> targets = Bukkit.selectEntities(sender, entities);
                if (targets.isEmpty()) {
                    input.accept(null);
                } else {
                    for (final Entity target : targets) {
                        if (target instanceof Player) {
                            input.accept((Player) target);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
            input.accept(null);
        }
    }

    /**
     * Executes an input of methods for the currently online players.
     *
     * @param input - The methods to be executed.
     */
    public static void forOnlinePlayers(final @Nonnull Consumer<Player> input) {
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                input.accept(player);
            }
        }
    }

    /**
     * Executes an input of methods for the currently offline players.
     *
     * @param input - The methods to be executed.
     */
    public static void forOfflinePlayers(final @Nonnull Consumer<OfflinePlayer> input) {
        try {
		  /* New method for getting the current offline players.
			 This is for MC 1.12+
			*/
            if (Bukkit.class.getMethod("getOfflinePlayers").getReturnType() == Collection.class) {
                for (Object objPlayer : ((Collection<?>) Bukkit.class.getMethod("getOfflinePlayers").invoke(null, new Object[0]))) {
                    input.accept(((OfflinePlayer) objPlayer));
                }
            }
		  /* New old for getting the current offline players.
			 This is for MC versions below 1.12.

			 @deprecated Legacy version of getting offline players.
			*/
            else {
                for (OfflinePlayer player : ((OfflinePlayer[]) Bukkit.class.getMethod("getOfflinePlayers").invoke(null, new Object[0]))) {
                    input.accept(player);
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }
}