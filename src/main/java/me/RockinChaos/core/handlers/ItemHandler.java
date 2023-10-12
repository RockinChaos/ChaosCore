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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.ReflectionUtils.MinecraftMethod;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import me.RockinChaos.core.utils.api.LegacyAPI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionType;

import java.io.EOFException;
import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class ItemHandler {

    /**
     * Adds a list of lores to the specified ItemStack.
     *
     * @param item  - The ItemStack to be modified.
     * @param lores - The list of lores to be added to the item.
     * @return The ItemStack with its newly added lores.
     */
    public static ItemStack addLore(final ItemStack item, final String... lores) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            List<String> newLore = new ArrayList<>();
            if (meta != null) {
                if (meta.hasLore()) {
                    newLore = meta.getLore();
                }
                for (String lore : lores) {
                    if (newLore != null) {
                        newLore.add(StringUtils.colorFormat(lore));
                    }
                }
                meta.setLore(newLore);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    /**
     * Checks if the ItemStack is similar to the defined ItemMap.
     *
     * @param item1 - The ItemStack being checked.
     * @param item2 - The ItemStack being checked.
     * @return If the ItemStack is similar.
     */
    public static boolean isSimilar(final ItemStack item1, final ItemStack item2) {
        return item1 != null && item2 != null && item1.getType() != Material.AIR && item2.getType() != Material.AIR && item1.getType() == item2.getType() && item1.hasItemMeta() && item2.hasItemMeta()
                && Objects.requireNonNull(item1.getItemMeta()).hasDisplayName() && Objects.requireNonNull(item2.getItemMeta()).hasDisplayName() && item1.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName());
    }

    /**
     * Gets the exact name of the ItemStack Material with normal case and no underlines.
     *
     * @param item - The ItemStack to have its Material name fetched.
     * @return A friendly String version of the Material name with normal case and no underlines.
     */
    public static String getMaterialName(final ItemStack item) {
        try {
            return WordUtils.capitalizeFully(item.getType().name().toLowerCase().replace('_', ' '));
        } catch (NullPointerException e) {
            ServerUtils.sendDebugTrace(e);
        }
        return null;
    }

    /**
     * Gets the String name of the Bukkit Enchantment.
     *
     * @param enchant - The Enchantment to have its String name found.
     * @return The String name of the Bukkit Enchantment.
     */
    public static String getEnchantName(final Enchantment enchant) {
        if (!ServerUtils.hasSpecificUpdate("1_13")) {
            return LegacyAPI.getEnchantName(enchant);
        } else {
            return enchant.getKey().getKey();
        }
    }

    /**
     * Gets the exact Bukkit Enchantment provided its String name.
     *
     * @param name - Name of the Bukkit Enchantment.
     * @return The proper Bukkit Enchantment instance.
     */
    public static Enchantment getEnchantByName(final String name) {
        if (!ServerUtils.hasSpecificUpdate("1_13")) {
            return LegacyAPI.getEnchant(name);
        } else {
            try {
                Enchantment enchantName = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(name.toLowerCase()));
                if (enchantName != null) {
                    return enchantName;
                } else {
                    return LegacyAPI.getEnchant(name);
                }
            } catch (Exception e) {
                ServerUtils.sendDebugTrace(e);
            }
        }
        return null;
    }

    /**
     * Gets the current Durability Value of the specified ItemStack.
     *
     * @param item - The ItemStack to have its Durability found.
     * @return The Durability value of the ItemStack.
     */
    public static short getDurability(final ItemStack item) {
        if (!ServerUtils.hasSpecificUpdate("1_13")) {
            return LegacyAPI.getDurability(item);
        } else {
            return ((short) ((org.bukkit.inventory.meta.Damageable) Objects.requireNonNull(item.getItemMeta())).getDamage());
        }
    }

    /**
     * Sets the specified Durability to the specified ItemStack.
     *
     * @param item       - The ItemStack to have its Durability changed.
     * @param durability - The Durability to be set to the ItemStack.
     * @return The ItemStack with its new Durability.
     */
    public static ItemStack setDurability(final ItemStack item, final int durability) {
        if (item.getType().getMaxDurability() != 0 && durability != 0) {
            if (ServerUtils.hasSpecificUpdate("1_13")) {
                ItemMeta tempMeta = item.getItemMeta();
                if (tempMeta != null) {
                    ((org.bukkit.inventory.meta.Damageable) tempMeta).setDamage(durability);
                    item.setItemMeta(tempMeta);
                    return item;
                }
            } else {
                return LegacyAPI.setDurability(item, (short) durability);
            }
        }
        return item;
    }

    /**
     * Modifies the ItemStack.
     *
     * @param itemCopy - The copy of the ItemStack to be modified.
     * @param allItems - If the item should not have its amount changed.
     * @param amount   - The intended stack size.
     * @return The newly Modified ItemStack.
     */
    public static ItemStack modifyItem(final ItemStack itemCopy, final boolean allItems, final int amount) {
        ItemStack item = new ItemStack(itemCopy);
        if (((item.getAmount() > amount && item.getAmount() != amount) || item.getAmount() < amount) && !allItems) {
            item.setAmount(item.getAmount() - amount);
        } else {
            item = new ItemStack(Material.AIR);
        }
        return item;
    }

    /**
     * If the given ItemStack is null, return an air ItemStack, otherwise return the given ItemStack.
     *
     * @param stack The ItemStack to check.
     * @return air or the given ItemStack.
     */
    public static ItemStack itemNotNull(ItemStack stack) {
        return stack == null ? new ItemStack(Material.AIR) : stack;
    }

    /**
     * Creates a new ItemStack with the specified material, count,
     * adding an invisible glowing enchant, custom name, and lore.
     *
     * @param material - The material name and data value of the ItemStack, Example: "WOOL:14".
     * @param count    - The stack size of the ItemStack.
     * @param glowing  - If the ItemStack should visually glow.
     * @param name     - The custom name to be added to the ItemStack.
     * @param lores    - The custom lore to be added to the ItemStack.
     */
    public static ItemStack getItem(String material, final int count, final boolean glowing, boolean hideAttributes, String name, final String... lores) {
        ItemStack tempItem;
        String refMat = "";
        if (!ServerUtils.hasSpecificUpdate("1_8") && material.equals("BARRIER")) {
            material = "WOOL:14";
        }
        if (material.equalsIgnoreCase("AIR") || material.equalsIgnoreCase("AIR:0")) {
            material = "GLASS_PANE";
        }
        if (material.equalsIgnoreCase("WATER_BOTTLE")) {
            refMat = material;
            material = "POTION";
        }
        if (getMaterial(material, null) == null) {
            material = "STONE";
        }
        if (ServerUtils.hasSpecificUpdate("1_13")) {
            tempItem = new ItemStack(Objects.requireNonNull(getMaterial(material, null)), count);
        } else {
            short dataValue = 0;
            if (material.contains(":")) {
                String[] parts = material.split(":");
                material = parts[0];
                dataValue = (short) Integer.parseInt(parts[1]);
            }
            tempItem = LegacyAPI.newItemStack(getMaterial(material, null), count, dataValue);
        }
        if (glowing) {
            tempItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        }
        ItemMeta tempMeta = tempItem.getItemMeta();
        if (ServerUtils.hasSpecificUpdate("1_8") && tempMeta != null) {
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        if (name != null && tempMeta != null) {
            name = StringUtils.colorFormat(name);
            tempMeta.setDisplayName(name);
        }
        if (lores != null && lores.length != 0 && tempMeta != null) {
            ArrayList<String> loreList = new ArrayList<>();
            for (String loreString : lores) {
                if (!loreString.isEmpty()) {
                    if (loreString.contains("/n")) {
                        String[] loreSplit = loreString.split(" /n ");
                        for (String loreStringSplit : loreSplit) {
                            loreList.add(StringUtils.colorFormat(loreStringSplit));
                        }
                    } else {
                        loreList.add(StringUtils.colorFormat(loreString));
                    }
                }
            }
            tempMeta.setLore(loreList);
        }
        if (ServerUtils.hasSpecificUpdate("1_8") && hideAttributes && tempMeta != null) {
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        }
        if (ServerUtils.hasSpecificUpdate("1_9") && refMat.equalsIgnoreCase("WATER_BOTTLE") && tempMeta != null) {
            ((PotionMeta) tempMeta).setBasePotionData(new org.bukkit.potion.PotionData(PotionType.WATER));
        }
        tempItem.setItemMeta(tempMeta);
        return tempItem;
    }

    /**
     * Checks the Players Inventory for an item in the Specified slot,
     * and returns the item if it exists.
     *
     * @param player    - The Player having their Inventory checked.
     * @param checkSlot - The slot being checked.
     * @return The existing ItemStack from the Players Inventory.
     */
    public static ItemStack getItem(final Player player, final String checkSlot) {
        int craftSlot = StringUtils.getSlotConversion(checkSlot);
        final EntityEquipment equipment = player.getEquipment();
        ItemStack existingItem = null;
        if (StringUtils.isInt(checkSlot)) {
            existingItem = player.getInventory().getItem(Integer.parseInt(checkSlot));
        } else if (checkSlot.contains("%")) {
            String slot = StringUtils.translateLayout(checkSlot, player);
            if (StringUtils.isInt(slot)) {
                existingItem = player.getInventory().getItem(Integer.parseInt(slot));
            }
        } else if (CustomSlot.HELMET.isSlot(checkSlot) && equipment != null) {
            existingItem = equipment.getHelmet();
        } else if (CustomSlot.CHESTPLATE.isSlot(checkSlot) && equipment != null) {
            existingItem = equipment.getChestplate();
        } else if (CustomSlot.LEGGINGS.isSlot(checkSlot) && equipment != null) {
            existingItem = equipment.getLeggings();
        } else if (CustomSlot.BOOTS.isSlot(checkSlot) && equipment != null) {
            existingItem = equipment.getBoots();
        } else if (ServerUtils.hasSpecificUpdate("1_9") && CustomSlot.OFFHAND.isSlot(checkSlot) && equipment != null) {
            existingItem = equipment.getItemInOffHand();
        } else if (craftSlot != -1) {
            existingItem = player.getOpenInventory().getTopInventory().getItem(craftSlot);
        }
        return (existingItem != null && existingItem.getType() != Material.AIR ? existingItem : null);
    }

    /**
     * Gets the Bukkit Material instance of the specified String material name and data value.
     *
     * @param material - The item ID or Bukkit Material String name.
     * @param data     - The data value of the item, usually this is zero.
     * @return The proper Bukkit Material instance.
     */
    public static Material getMaterial(String material, String data) {
        try {
            boolean isLegacy = (data != null && Integer.parseInt(data) > 0);
            if (material.contains(":")) {
                String[] parts = material.split(":");
                material = parts[0];
                if (!parts[1].equalsIgnoreCase("0")) {
                    data = parts[1];
                    isLegacy = true;
                }
            }
            if (StringUtils.isInt(material) && !ServerUtils.hasSpecificUpdate("1_13")) {
                return LegacyAPI.findMaterial(Integer.parseInt(material));
            } else if (StringUtils.isInt(material) && ServerUtils.hasSpecificUpdate("1_13") || isLegacy && ServerUtils.hasSpecificUpdate("1_13")) {
                int dataValue;
                if (!StringUtils.isInt(material)) {
                    material = "LEGACY_" + material;
                }
                if (data != null) {
                    dataValue = Integer.parseInt(data);
                } else {
                    dataValue = 0;
                }
                if (!StringUtils.isInt(material)) {
                    return LegacyAPI.getMaterial(Material.getMaterial(material.toUpperCase()), (byte) dataValue);
                } else {
                    return LegacyAPI.getMaterial(Integer.parseInt(material), (byte) dataValue);
                }
            } else if (!ServerUtils.hasSpecificUpdate("1_13")) {
                return Material.getMaterial(material.toUpperCase());
            } else {
                return Material.matchMaterial(material.toUpperCase());
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return null;
    }

    /**
     * Checks if the ItemStack List contains the Material.
     *
     * @param itemStacks - The ItemStacks being checked.
     * @param material   - The Material expected.
     * @return If the List contains the Material
     */
    public static boolean containsMaterial(final Collection<ItemStack> itemStacks, final Material material) {
        for (ItemStack item : itemStacks) {
            if (item.getType().equals(material)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the Skull Texture to the ItemStack.
     *
     * @param item         - The ItemStack to have its Skull Texture changed.
     * @param skullTexture - The Skull Texture to be added to the ItemStack.
     */
    public static ItemStack setSkullTexture(final ItemStack item, final String skullTexture) {
        try {
            if (ServerUtils.hasSpecificUpdate("1_8")) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    final UUID uuid = UUID.randomUUID();
                    GameProfile gameProfile = new GameProfile(uuid, uuid.toString().replaceAll("_", "").replaceAll("-", ""));
                    gameProfile.getProperties().put("textures", new Property("textures", skullTexture));
                    Field declaredField = itemMeta.getClass().getDeclaredField("profile");
                    declaredField.setAccessible(true);
                    declaredField.set(itemMeta, gameProfile);
                    item.setItemMeta(itemMeta);
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return item;
    }

    /**
     * Sets the Skull Texture to the ItemStack.
     *
     * @param itemMeta     - The ItemMeta to have its Skull Texture changed.
     * @param skullTexture - The Skull Texture to be added to the ItemStack.
     */
    public static ItemMeta setSkullTexture(final ItemMeta itemMeta, final String skullTexture) {
        try {
            if (ServerUtils.hasSpecificUpdate("1_8")) {
                final UUID uuid = UUID.randomUUID();
                GameProfile gameProfile = new GameProfile(uuid, uuid.toString().replaceAll("_", "").replaceAll("-", ""));
                gameProfile.getProperties().put("textures", new Property("textures", skullTexture));
                Field declaredField = itemMeta.getClass().getDeclaredField("profile");
                declaredField.setAccessible(true);
                declaredField.set(itemMeta, gameProfile);
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return itemMeta;
    }

    /**
     * Gets the current Skull Texture of the ItemMeta.
     *
     * @param meta - The ItemMeta to have its Skull Texture found.
     * @return The found Skull Texture String value.
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static String getSkullTexture(final ItemMeta meta) {
        Class<?> propertyClass = Property.class;
        try {
            final Class<?> cls = ReflectionUtils.getCraftBukkitClass("inventory.CraftMetaSkull");
            final Object real = cls.cast(meta);
            final Field field = real.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            final GameProfile profile = (GameProfile) field.get(real);
            final Collection<Property> props = profile.getProperties().get("textures");
            for (final Property property : props) {
                try {
                    if (propertyClass.getMethod("getName").invoke(property).equals("textures")) {
                        return ((String) propertyClass.getMethod("getValue").invoke(property));
                    }
                } catch (Exception e) {
                    try {
                        if (propertyClass.getMethod("name").invoke(property).equals("textures")) {
                            return ((String) propertyClass.getMethod("value").invoke(property));
                        }
                    } catch (Exception e2) {
                        ServerUtils.sendSevereTrace(e);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * Gets the current Skull Texture of the ItemMeta.
     *
     * @param skull - The Skull to have its Skull Texture found.
     * @return The found Skull Texture String value.
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static String getSkullTexture(final Skull skull) {
        Class<?> propertyClass = Property.class;
        try {
            final Field field = skull.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            final GameProfile profile = (GameProfile) field.get(skull);
            final Collection<Property> props = profile.getProperties().get("textures");
            for (final Property property : props) {
                try {
                    if (propertyClass.getMethod("getName").invoke(property).equals("textures")) {
                        return ((String) propertyClass.getMethod("getValue").invoke(property));
                    }
                } catch (Exception e) {
                    try {
                        if (propertyClass.getMethod("name").invoke(property).equals("textures")) {
                            return ((String) propertyClass.getMethod("value").invoke(property));
                        }
                    } catch (Exception e2) {
                        ServerUtils.sendSevereTrace(e);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * Sets the Skull Owner name to the ItemMeta.
     *
     * @param meta  - The ItemMeta to have its Skull Owner changed.
     * @param owner - The String name of the Skull Owner to be set.
     * @return The ItemMeta with the new Skull Owner.
     */
    public static ItemMeta setSkullOwner(final ItemMeta meta, final String owner) {
        if (!ServerUtils.hasSpecificUpdate("1_8")) {
            ServerUtils.logDebug("{ItemHandler} Minecraft does not support offline player heads below Version 1.8.");
            ServerUtils.logDebug("{ItemHandler} Player heads will only be given a skin if the player has previously joined the sever.");
        }
        setStoredSkull(meta, owner);
        return meta;
    }

    /**
     * Sets the locale stored skull owner.
     *
     * @param meta  - The referenced ItemMeta.
     * @param owner - The referenced Skull Owner
     */
    public static void setStoredSkull(final ItemMeta meta, final String owner) {
        if (!owner.isEmpty()) {
            SkullMeta skullMeta = (SkullMeta) meta;
            OfflinePlayer player = LegacyAPI.getOfflinePlayer(owner);
            if (Core.getCore().getDependencies().skinsRestorerEnabled()) {
                final String textureValue = Core.getCore().getDependencies().getSkinValue(owner);
                if (textureValue != null) {
                    setSkullTexture(meta, textureValue);
                } else {
                    try {
                        skullMeta.setOwningPlayer(player);
                    } catch (Throwable t) {
                        LegacyAPI.setSkullOwner(skullMeta, player.getName());
                    }
                }
            } else {
                try {
                    skullMeta.setOwningPlayer(player);
                } catch (Throwable t) {
                    LegacyAPI.setSkullOwner(skullMeta, player.getName());
                }
            }
        }
    }

    /**
     * Stacks two items together.
     *
     * @param player - The player being referenced.
     * @param item1  - The main item being removed.
     * @param item2  - The secondary item being stacked.
     * @param slot   - The new event slot of the main item.
     * @return The Remaining amount to be set (if any).
     */
    public static int stackItems(final Player player, final ItemStack item1, final ItemStack item2, final int slot, final boolean topInventory) {
        int MINECRAFT_STACK_MAX = 64;
        int DESIRED_STACK_SIZE = item1.getAmount() + item2.getAmount();
        int REMAINING_STACK_SIZE = 0;
        if (DESIRED_STACK_SIZE > MINECRAFT_STACK_MAX) {
            item2.setAmount(MINECRAFT_STACK_MAX);
            item1.setAmount(DESIRED_STACK_SIZE - MINECRAFT_STACK_MAX);
            REMAINING_STACK_SIZE = item1.getAmount();
        } else {
            item2.setAmount(item2.getAmount() + item1.getAmount());
            if (slot == -1) {
                player.getOpenInventory().setCursor(new ItemStack(Material.AIR));
            } else if (slot != -2) {
                if (topInventory) {
                    player.getOpenInventory().getTopInventory().setItem(slot, new ItemStack(Material.AIR));
                } else {
                    player.getInventory().setItem(slot, new ItemStack(Material.AIR));
                }
            }
        }
        return REMAINING_STACK_SIZE;
    }

    /**
     * Gets the existing MapView for the image id.
     *
     * @param id - that will receive the items.
     * @return The existing MapView.
     */
    public static MapView existingView(final int id) {
        MapView view = LegacyAPI.getMapView(id);
        if (view == null) {
            view = LegacyAPI.createMapView();
        }
        return view;
    }

    /**
     * Removes all crafting items from the players inventory.
     *
     * @param player - The Player to have their crafting items removed.
     */
    public static void removeCraftItems(final Player player) {
        ItemStack[] craftingContents = player.getOpenInventory().getTopInventory().getContents();
        Inventory craftView = player.getOpenInventory().getTopInventory();
        if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
            for (int k = 0; k < craftingContents.length; k++) {
                craftView.setItem(k, new ItemStack(Material.AIR));
            }
        }
    }

    /**
     * Returns the custom crafting item to the player after the specified delay.
     *
     * @param player - the Player having their item returned.
     * @param slot   - the slot to return the crafting item to.
     * @param item   - the item to be returned.
     * @param delay  - the delay to wait before returning the item.
     */
    public static void returnCraftingItem(final Player player, final int slot, final ItemStack item, long delay) {
        if (item == null) {
            return;
        }
        if (slot == 0) {
            delay += 1L;
        }
        SchedulerUtils.runLater(delay, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (PlayerHandler.isCraftingInv(player.getOpenInventory()) && player.getGameMode() != GameMode.CREATIVE) {
                player.getOpenInventory().getTopInventory().setItem(slot, item);
                PlayerHandler.updateInventory(player, 1L);
            } else {
                returnCraftingItem(player, slot, item, 10L);
            }
        });
    }

    /**
     * Saves the players current crafting items for later retrieval.
     *
     * @param player - The Player to have its crafting items saved.
     */
    public static Inventory getCraftInventory(final Player player) {
        final Inventory inv = Bukkit.createInventory(null, 9);
        boolean notNull = false;
        if (PlayerHandler.getCreativeCraftItems().containsKey(PlayerHandler.getPlayerID(player))) {
            ItemStack[] craftingContents = PlayerHandler.getCreativeCraftItems().get(PlayerHandler.getPlayerID(player));
            for (int k = 0; k <= 4; k++) {
                if (craftingContents != null && craftingContents[k] != null) {
                    inv.setItem(k, craftingContents[k]);
                    if (craftingContents[k] != null && craftingContents[k].getType() != Material.AIR) {
                        notNull = true;
                    }
                }
            }
        } else if (PlayerHandler.getOpenCraftItems().containsKey(PlayerHandler.getPlayerID(player))) {
            ItemStack[] craftingContents = PlayerHandler.getOpenCraftItems().get(PlayerHandler.getPlayerID(player));
            for (int k = 0; k <= 4; k++) {
                if (craftingContents != null && craftingContents[k] != null) {
                    inv.setItem(k, craftingContents[k]);
                    if (craftingContents[k] != null && craftingContents[k].getType() != Material.AIR) {
                        notNull = true;
                    }
                }
            }
        } else if (PlayerHandler.getCraftItems().containsKey(PlayerHandler.getPlayerID(player))) {
            ItemStack[] craftingContents = PlayerHandler.getCraftItems().get(PlayerHandler.getPlayerID(player));
            for (int k = 0; k <= 4; k++) {
                if (craftingContents != null && craftingContents[k] != null) {
                    inv.setItem(k, craftingContents[k]);
                    if (craftingContents[k] != null && craftingContents[k].getType() != Material.AIR) {
                        notNull = true;
                    }
                }
            }
        }
        return (notNull ? inv : null);
    }

    /**
     * Restores the players crafting items to their inventory if they were previously saved.
     *
     * @param player - The Player to have its crafting items restored.
     */
    public static boolean restoreCraftItems(final Player player, final Inventory inventory) {
        Inventory craftView = player.getOpenInventory().getTopInventory();
        if (inventory != null && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
            for (int k = 4; k >= 0; k--) {
                final ItemStack item = inventory.getItem(k);
                if (item != null && item.getType() != Material.AIR) {
                    craftView.setItem(k, item.clone());
                }
            }
            PlayerHandler.updateInventory(player, 1L);
            return true;
        } else if (!PlayerHandler.isCraftingInv(player.getOpenInventory())) {
            SchedulerUtils.runLater(60L, () -> restoreCraftItems(player, inventory));
        }
        return false;
    }

    /**
     * Copies the specified ItemStack contents.
     *
     * @param contents - The ItemStack contents to be copied.
     * @return The copied ItemStack contents.
     */
    public static ItemStack[] cloneContents(final ItemStack[] contents) {
        int itr = 0;
        for (ItemStack itemStack : contents) {
            if (contents[itr] != null) {
                contents[itr] = itemStack.clone();
            }
            itr++;
        }
        return contents;
    }

    /**
     * Checks if the ItemStack contents are NULL or empty.
     *
     * @param contents - The ItemStack contents to be checked.
     * @return If the contents do not exist.
     */
    public static boolean isContentsEmpty(final ItemStack[] contents) {
        int size = 0;
        for (ItemStack itemStack : contents) {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                size++;
            }
        }
        return (size != contents.length);
    }

    /**
     * Converts the Inventory to a Base64 String.
     * This is a way of encrypting an Inventory to be decrypted and referenced later.
     *
     * @param inventory - The Inventory to be converted.
     * @return The Base64 String of the Inventory.
     */
    public static String serializeInventory(final Inventory inventory) {
        try {
            java.io.ByteArrayOutputStream str = new java.io.ByteArrayOutputStream();
            org.bukkit.util.io.BukkitObjectOutputStream data = new org.bukkit.util.io.BukkitObjectOutputStream(str);
            data.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                data.writeObject(inventory.getItem(i));
            }
            data.close();
            return Base64.getEncoder().encodeToString(str.toByteArray());
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return "";
    }

    /**
     * Converts the Base64 String to an Inventory.
     * This is a way of decrypting an encrypted Inventory to be referenced.
     *
     * @param inventoryData - The Base64 String to be converted to an Inventory.
     * @return The Inventory instance that has been deserialized.
     */
    public static Inventory deserializeInventory(final String inventoryData) {
        try {
            java.io.ByteArrayInputStream stream = new java.io.ByteArrayInputStream(Base64.getDecoder().decode(inventoryData));
            org.bukkit.util.io.BukkitObjectInputStream data = new org.bukkit.util.io.BukkitObjectInputStream(stream);
            Inventory inventory = Bukkit.createInventory(null, data.readInt());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) data.readObject());
            }
            data.close();
            return inventory;
        } catch (EOFException e) {
            return null;
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return null;
    }

    /**
     * Gets the custom NBTData of the ItemStack.
     *
     * @param item     - The ItemStack to have its custom NBTData found.
     * @param dataList - The list of data expected on the ItemStack.
     * @return The String of NBTData found on the ItemStack.
     */
    public static String getNBTData(final ItemStack item, final List<String> dataList) {
        synchronized ("CC_NBT") {
            if (Core.getCore().getData().dataTagsEnabled() && item != null && item.getType() != Material.AIR) {
                try {
                    final ItemStack itemCopy = item.clone();
                    Object nms = ReflectionUtils.getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, itemCopy);
                    Class<?> itemClass = ReflectionUtils.getMinecraftClass("ItemStack");
                    Object cacheTag = itemClass.getMethod(MinecraftMethod.getTag.getMethod(itemClass)).invoke(nms);
                    if (cacheTag != null) {
                        StringBuilder returnData = new StringBuilder();
                        for (String dataString : dataList) {
                            String data = (String) cacheTag.getClass().getMethod(MinecraftMethod.getString.getMethod(cacheTag, String.class), String.class).invoke(cacheTag, dataString);
                            if (data != null && !data.isEmpty()) {
                                returnData.append(data).append(" ");
                            }
                        }
                        return returnData.toString().trim();
                    }
                } catch (ConcurrentModificationException ignored) {
                } catch (Exception e) {
                    ServerUtils.logSevere("{ItemHandler} An error has occurred when getting NBTData to an item, reason: " + e.getCause() + ".");
                    ServerUtils.sendSevereThrowable(e.getCause());
                    ServerUtils.sendSevereTrace(e);
                }
            }
        }
        return null;
    }

    /**
     * Checks if the ItemStack contains plugin specific NBTData.
     *
     * @param item     - The ItemStack to be checked for custom NBTData.
     * @param dataList - The list of data expected on the ItemStack.
     * @return If the ItemStack has plugin specific NBTData.
     */
    public static boolean containsNBTData(final ItemStack item, final List<String> dataList) {
        if (Core.getCore().getData().dataTagsEnabled() && item != null && item.getType() != Material.AIR && getNBTData(item, dataList) != null) {
            return true;
        } else if (!Core.getCore().getData().dataTagsEnabled()) {
            return item != null && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName()
                    && StringUtils.colorDecode(item) != null && !Objects.requireNonNull(StringUtils.colorDecode(item)).isEmpty();
        }
        return false;
    }

    /**
     * Attempts to fetch the designated slot of an item.
     *
     * @param material - The ItemStack to be checked for a designated slot.
     * @return The proper designated slot name.
     */
    public static String getDesignatedSlot(final Material material) {
        String name = material.name().contains("_") ? material.name().split("_")[1] : material.name();
        String hand = (ServerUtils.hasSpecificUpdate("1_13") ? "hand" : "mainhand");
        return (name != null ? (name.equalsIgnoreCase("HELMET") ? "head" : name.equalsIgnoreCase("CHESTPLATE") ? "chest" : name.equalsIgnoreCase("LEGGINGS") ? "legs" : name.equalsIgnoreCase("BOOTS") ? "feet" :
                name.equalsIgnoreCase("HOE") ? hand : name.equalsIgnoreCase("SWORD") ? hand : name.equalsIgnoreCase("SHOVEL") ? hand : name.equalsIgnoreCase("AXE") ? hand : name.equalsIgnoreCase("PICKAXE") ? hand : "noslot") : "noslot");
    }

    /**
     * Checks if the server is using the new skull method.
     *
     * @return If the server is using the new skull method.
     */
    public static boolean usesOwningPlayer() {
        try {
            Class.forName("org.bukkit.inventory.meta.SkullMeta");
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Gets the Integer Delay of the specified String.
     *
     * @param context - The String to have the Delay found.
     * @return The Delay of the String as an Integer.
     */
    public static int getDelay(final String context) {
        try {
            if (StringUtils.returnInteger(context) != null) {
                return StringUtils.returnInteger(context);
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return 0;
    }

    /**
     * Gets the delay format including the proper delay Integer of the String.
     *
     * @param context - The String to have the Delay found.
     * @return The Delay Format of the String with the proper Integer value.
     */
    public static String getDelayFormat(final String context) {
        if (StringUtils.containsIgnoreCase(context, "<delay:" + StringUtils.returnInteger(context) + ">")
                || StringUtils.containsIgnoreCase(context, "delay:" + StringUtils.returnInteger(context))
                || StringUtils.containsIgnoreCase(context, "<delay: " + StringUtils.returnInteger(context) + ">")
                || StringUtils.containsIgnoreCase(context, "delay: " + StringUtils.returnInteger(context))) {
            return ("<delay:" + StringUtils.returnInteger(context) + ">");
        }
        return null;
    }

    /**
     * Removes the delay formatting from the specified String.
     *
     * @param context - The String to have the Delay Formatting removed.
     * @return The String with the removed Delay Formatting.
     */
    public static String cutDelay(final String context) {
        if (getDelayFormat(context) != null) {
            return context.replace(Objects.requireNonNull(getDelayFormat(context)), "");
        }
        return context;
    }

    /**
     * Removes the delay formatting from the specified String List.
     *
     * @param context - The String List to have the Delay Formatting removed.
     * @return The String List with the removed Delay Formatting.
     */
    public static List<String> cutDelay(final List<String> context) {
        List<String> newContext = new ArrayList<>();
        for (String minorContext : context) {
            newContext.add(cutDelay(minorContext));
        }
        return newContext;
    }

    /**
     * Checks if the book pages contains a JSONEvent.
     *
     * @param formatPage - The page to be formatted.
     * @return If the book page contains a JSONEvent.
     */
    public static boolean containsJSONEvent(final String formatPage) {
        return formatPage.contains(JSONEvent.TEXT.matchType) || formatPage.contains(JSONEvent.SHOW_TEXT.matchType) || formatPage.contains(JSONEvent.OPEN_URL.matchType) || formatPage.contains(JSONEvent.RUN_COMMAND.matchType);
    }

    /**
     * Checks to see if the open_url is correctly defined with https or http.
     *
     * @param itemName    - The name of the item being modified.
     * @param type        - The JSONEvent type.
     * @param inputResult - The input for the JSONEvent.
     */
    public static void safetyCheckURL(final String itemName, final JSONEvent type, final String inputResult) {
        if (type.equals(JSONEvent.OPEN_URL)) {
            if (!StringUtils.containsIgnoreCase(inputResult, "https") && !StringUtils.containsIgnoreCase(inputResult, "http")) {
                ServerUtils.logSevere("{ItemHandler} The URL Specified for the clickable link in the book " + itemName + " is missing http or https and will not be clickable.");
                ServerUtils.logWarn("{ItemHandler} A URL designed for a clickable link should resemble this link structure: https://www.google.com/");
            }
        }
    }

    /**
     * Checks if the specified String slot is a Custom Slot.
     *
     * @param slot - The slot to be checked.
     * @return If the slot is a custom slot.
     */
    public static boolean isCustomSlot(final String slot) {
        return slot.equalsIgnoreCase("Offhand") || slot.equalsIgnoreCase("Arbitrary") || slot.equalsIgnoreCase("Helmet")
                || slot.equalsIgnoreCase("Chestplate") || slot.equalsIgnoreCase("Leggings") || slot.equalsIgnoreCase("Boots") || isCraftingSlot(slot) || slot.contains("%");
    }

    /**
     * Checks if the specified String slot is a Crafting Slot.
     *
     * @param slot - The slot to be checked.
     * @return If the slot is a crafting slot.
     */
    public static boolean isCraftingSlot(final String slot) {
        return slot.equalsIgnoreCase("CRAFTING[0]") || slot.equalsIgnoreCase("CRAFTING[1]")
                || slot.equalsIgnoreCase("CRAFTING[2]") || slot.equalsIgnoreCase("CRAFTING[3]") || slot.equalsIgnoreCase("CRAFTING[4]");
    }

    /**
     * Checks if the Material is a Legacy Skull Item or Player Skull.
     *
     * @param material - The Material to be checked.
     * @return If the Material is a Skull/Player Head.
     */
    public static boolean isSkull(final Material material) {
        return material.toString().equalsIgnoreCase("SKULL_ITEM") || material.toString().equalsIgnoreCase("PLAYER_HEAD");
    }

    /**
     * Checks if the ItemStack is a Writable Book.
     *
     * @param item - The ItemStack to be checked.
     * @return If the ItemStack is a Writable Book.
     */
    public static boolean isBookQuill(final ItemStack item) {
        if (item == null) return false;
        return item.getType().toString().equalsIgnoreCase("WRITABLE_BOOK") || item.getType().toString().equalsIgnoreCase("BOOK_AND_QUILL");
    }

    /**
     * Defines the JSONEvents for their action, event, and matchType.
     */
    public enum JSONEvent {
        TEXT("nullEvent", "text", "<text:"),
        SHOW_TEXT("hoverEvent", "show_text", "<show_text:"),
        OPEN_URL("clickEvent", "open_url", "<open_url:"),
        RUN_COMMAND("clickEvent", "run_command", "<run_command:"),
        CHANGE_PAGE("clickEvent", "change_page", "<change_page:");
        public final String event;
        public final String action;
        public final String matchType;

        JSONEvent(String Event, String Action, String MatchType) {
            this.event = Event;
            this.action = Action;
            this.matchType = MatchType;
        }
    }

    /**
     * Trigger types.
     */
    public enum CustomSlot {
        HELMET("Helmet"),
        CHESTPLATE("Chestplate"),
        LEGGINGS("Leggings"),
        BOOTS("Boots"),
        OFFHAND("Offhand"),
        CRAFTING("Crafting"),
        ARBITRARY("Arbitrary");
        private final String name;

        CustomSlot(String name) {
            this.name = name;
        }

        public boolean isSlot(String slot) {
            return this.name.equalsIgnoreCase(slot);
        }
    }
}