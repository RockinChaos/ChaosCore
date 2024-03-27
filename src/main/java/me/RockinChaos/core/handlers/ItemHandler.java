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

import com.google.common.collect.ImmutableList;
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
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ItemHandler {

    private static final Map<String, Object> gameProfiles = new HashMap<>();

    /**
     * Adds a list of lores to the specified ItemStack.
     *
     * @param item  - The ItemStack to be modified.
     * @param lores - The list of lores to be added to the item.
     * @return The ItemStack with its newly added lores.
     */
    public static @Nonnull ItemStack addLore(final @Nonnull ItemStack item, final @Nonnull String... lores) {
        if (item.getType() != Material.AIR) {
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
    public static boolean isSimilar(@Nullable final ItemStack item1, @Nullable final ItemStack item2) {
        return item1 != null && item2 != null && item1.getType() != Material.AIR && item2.getType() != Material.AIR && item1.getType() == item2.getType() && item1.hasItemMeta() && item2.hasItemMeta()
                && Objects.requireNonNull(item1.getItemMeta()).hasDisplayName() && Objects.requireNonNull(item2.getItemMeta()).hasDisplayName() && item1.getItemMeta().getDisplayName().equalsIgnoreCase(item2.getItemMeta().getDisplayName());
    }

    /**
     * Gets the exact name of the ItemStack Material with normal case and no underlines.
     *
     * @param item - The ItemStack to have its Material name fetched.
     * @return A friendly String version of the Material name with normal case and no underlines.
     */
    public static @Nonnull String getMaterialName(final @Nonnull ItemStack item) {
        final String name = item.getType().name().toLowerCase().replace('_', ' ');
        try {
            return WordUtils.capitalizeFully(name);
        } catch (NullPointerException e) {
            ServerUtils.sendDebugTrace(e);
        }
        return name;
    }

    /**
     * Gets a list of all registered Enchantments on the server.
     *
     * @return The list of all available Enchantments.
     */
    public static @Nonnull List<Enchantment> getEnchants() {
        return (ServerUtils.hasPreciseUpdate("1_20_3") ? ImmutableList.copyOf(Registry.ENCHANTMENT.iterator()) : LegacyAPI.getEnchants());
    }

    /**
     * Gets the String name of the Bukkit Enchantment.
     *
     * @param enchant - The Enchantment to have its String name found.
     * @return The String name of the Bukkit Enchantment.
     */
    public static @Nonnull String getEnchantName(final @Nonnull Enchantment enchant) {
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
    public static @Nullable Enchantment getEnchantByName(final @Nonnull String name) {
        if (ServerUtils.hasSpecificUpdate("1_13")) {
            try {
                Enchantment enchantName = LegacyAPI.getEnchantByKey(name);
                if (enchantName != null) {
                    return enchantName;
                }
            } catch (Exception e) {
                ServerUtils.sendDebugTrace(e);
            }
        }
        return LegacyAPI.getEnchant(name);
    }

    /**
     * Gets the exact Trim Material for the String.
     *
     * @param name - Name of the Trim Material.
     * @return The proper Trim Material instance.
     */
    public static @Nullable org.bukkit.inventory.meta.trim.TrimMaterial getTrimMaterial(final @Nonnull String name) {
        try {
            Field field = org.bukkit.inventory.meta.trim.TrimMaterial.class.getDeclaredField(name);
            Object value = field.get(name);
            return (org.bukkit.inventory.meta.trim.TrimMaterial) value;
        } catch (Exception e) {
            ServerUtils.logWarn("{ItemHandler} Failed to get the Trim Material " + name + ", check that you are using the proper name.");
            ServerUtils.sendDebugTrace(e);
        }
        return null;
    }

    /**
     * Gets the full list of available Armor Trim Materials.
     *
     * @return The full list of available armor trim materials.
     */
    public static @Nonnull List<org.bukkit.inventory.meta.trim.TrimMaterial> getTrimMaterials() {
        final List<org.bukkit.inventory.meta.trim.TrimMaterial> trimMaterials = new ArrayList<>();
        try {
            for (Field fieldMaterial : org.bukkit.inventory.meta.trim.TrimMaterial.class.getDeclaredFields()) {
                trimMaterials.add((org.bukkit.inventory.meta.trim.TrimMaterial) fieldMaterial.get(fieldMaterial.getName()));
            }
            return trimMaterials;
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return trimMaterials;
    }

    /**
     * Gets the exact Trim Pattern for the String.
     *
     * @param name - Name of the Trim Pattern.
     * @return The proper Trim Pattern instance.
     */
    public static @Nullable org.bukkit.inventory.meta.trim.TrimPattern getTrimPattern(final @Nonnull String name) {
        try {
            Field field = org.bukkit.inventory.meta.trim.TrimPattern.class.getDeclaredField(name);
            Object value = field.get(name);
            return (org.bukkit.inventory.meta.trim.TrimPattern) value;
        } catch (Exception e) {
            ServerUtils.logWarn("{ItemHandler} Failed to get the Trim Pattern " + name + ", check that you are using the proper name.");
            ServerUtils.sendDebugTrace(e);
        }
        return null;
    }

    /**
     * Gets the full list of available Armor Trim Patterns.
     *
     * @return The full list of available armor trim patterns.
     */
    public static @Nonnull List<org.bukkit.inventory.meta.trim.TrimPattern> getTrimPatterns() {
        final List<org.bukkit.inventory.meta.trim.TrimPattern> trimPatterns = new ArrayList<>();
        try {
            for (Field fieldPattern : org.bukkit.inventory.meta.trim.TrimPattern.class.getDeclaredFields()) {
                trimPatterns.add((org.bukkit.inventory.meta.trim.TrimPattern) fieldPattern.get(fieldPattern.getName()));
            }
            return trimPatterns;
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return trimPatterns;
    }

    /**
     * Sets the ItemStack Armor Trim Pattern
     */
    public static void setArmorTrim(final @Nonnull ItemStack item, final @Nonnull String material, final @Nonnull String pattern) {
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            final org.bukkit.inventory.meta.trim.TrimMaterial trimMaterial = getTrimMaterial(material);
            final org.bukkit.inventory.meta.trim.TrimPattern trimPattern = getTrimPattern(pattern);
            if (trimMaterial != null && trimPattern != null) {
                ((ArmorMeta) itemMeta).setTrim(new org.bukkit.inventory.meta.trim.ArmorTrim(trimMaterial, trimPattern));
                item.setItemMeta(itemMeta);
            }
        }
    }

    /**
     * Gets the current Durability Value of the specified ItemStack.
     *
     * @param item - The ItemStack to have its Durability found.
     * @return The Durability value of the ItemStack.
     */
    public static short getDurability(final @Nonnull ItemStack item) {
        if (!ServerUtils.hasSpecificUpdate("1_13")) {
            return LegacyAPI.getDurability(item);
        } else if (item.getItemMeta() != null) {
            return ((short) ((org.bukkit.inventory.meta.Damageable) item.getItemMeta()).getDamage());
        }
        return 0;
    }

    /**
     * Sets the specified Durability to the specified ItemStack.
     *
     * @param item       - The ItemStack to have its Durability changed.
     * @param durability - The Durability to be set to the ItemStack.
     * @return The ItemStack with its new Durability.
     */
    public static @Nonnull ItemStack setDurability(final @Nonnull ItemStack item, final int durability) {
        if (item.getType().getMaxDurability() != 0 && durability != 0) {
            if (ServerUtils.hasSpecificUpdate("1_13")) {
                final ItemMeta tempMeta = item.getItemMeta();
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
    public static @Nonnull ItemStack modifyItem(final @Nullable ItemStack itemCopy, final boolean allItems, final int amount) {
        if (itemCopy == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack item = new ItemStack(itemCopy);
        if (((item.getAmount() > amount && item.getAmount() != amount) || item.getAmount() < amount) && !allItems) {
            item.setAmount(item.getAmount() - amount);
        } else {
            item = new ItemStack(Material.AIR);
        }
        return item;
    }

    /**
     * Sets the ItemStack Model Data.
     *
     * @param itemMeta - The ItemMeta being modified.
     * @param data     - The model data being set.
     */
    public static void setData(final @Nonnull ItemMeta itemMeta, final int data) {
        itemMeta.setCustomModelData(data);
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
    public static @Nonnull ItemStack getItem(@Nonnull String material, final int count, final boolean glowing, boolean hideAttributes, @Nonnull String name, final @Nonnull String... lores) {
        ItemStack tempItem;
        String refMat = "";
        if (material.equalsIgnoreCase("AIR") || material.equalsIgnoreCase("AIR:0")) {
            material = "GLASS_PANE";
        }
        if (material.equalsIgnoreCase("WATER_BOTTLE")) {
            refMat = material;
            material = "POTION";
        }
        if (ServerUtils.hasSpecificUpdate("1_13")) {
            final Material bukkitMaterial = getMaterial(material, null);
            tempItem = new ItemStack(bukkitMaterial, count);
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
            setGlowing(tempItem);
        }
        ItemMeta tempMeta = tempItem.getItemMeta();
        if (tempMeta != null) {
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        if (tempMeta != null) {
            name = StringUtils.colorFormat(name);
            tempMeta.setDisplayName(name);
        }
        if (lores.length != 0 && tempMeta != null) {
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
        if (hideAttributes && tempMeta != null) {
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS);
            tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            if (ServerUtils.hasSpecificUpdate("1_20")) {
                tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM);
            }
            if (ServerUtils.hasSpecificUpdate("1_17")) {
                tempMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DYE);
            }
        }
        if (ServerUtils.hasSpecificUpdate("1_9") && refMat.equalsIgnoreCase("WATER_BOTTLE") && tempMeta != null) {
            if (ServerUtils.hasPreciseUpdate("1_20_3")) {
                ((PotionMeta) tempMeta).setBasePotionType(PotionType.WATER);
            } else {
                LegacyAPI.setPotionData(((PotionMeta) tempMeta), PotionType.WATER);
            }
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
    public static @Nonnull ItemStack getItem(final @Nonnull Player player, final @Nonnull String checkSlot) {
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
        return (existingItem != null && existingItem.getType() != Material.AIR ? existingItem : new ItemStack(Material.AIR));
    }

    /**
     * Gets the Bukkit Material instance of the specified String material name and data value.
     *
     * @param material - The item ID or Bukkit Material String name.
     * @param data     - The data value of the item, usually this is zero.
     * @return The proper Bukkit Material instance.
     */
    public static @Nonnull Material getMaterial(@Nonnull String material, @Nullable String data) {
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
                    final Material mat = Material.getMaterial(material.toUpperCase());
                    if (mat != null) {
                        return LegacyAPI.getMaterial(mat, (byte) dataValue);
                    } else {
                        return Material.AIR;
                    }
                } else {
                    return LegacyAPI.getMaterial(Integer.parseInt(material), (byte) dataValue);
                }
            } else if (!ServerUtils.hasSpecificUpdate("1_13")) {
                final Material mat = Material.getMaterial(material.toUpperCase());
                if (mat != null) {
                    return mat;
                }
            } else {
                final Material mat = Material.matchMaterial(material.toUpperCase());
                if (mat != null) {
                    return mat;
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        return Material.AIR;
    }

    /**
     * Checks if the ItemStack List contains the Material.
     *
     * @param itemStacks - The ItemStacks being checked.
     * @param material   - The Material expected.
     * @return If the List contains the Material
     */
    public static boolean containsMaterial(final @Nonnull Collection<ItemStack> itemStacks, final @Nonnull Material material) {
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
     * @param player       - The player being referenced.
     * @param item         - The ItemStack to have its Skull Texture changed.
     * @param skullTexture - The Skull Texture to be added to the ItemStack.
     */
    public static @Nonnull ItemStack setSkullTexture(final @Nonnull Player player, final @Nonnull ItemStack item, final @Nonnull String skullTexture) {
        if (item.getItemMeta() != null) {
            ItemMeta itemMeta = setSkullTexture(player, item.getItemMeta(), skullTexture);
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    /**
     * Sets the Skull Texture to the ItemStack.
     *
     * @param player       - The player being referenced.
     * @param itemMeta     - The ItemMeta to have its Skull Texture changed.
     * @param skullTexture - The Skull Texture to be added to the ItemStack.
     */
    public static @Nonnull ItemMeta setSkullTexture(final @Nonnull Player player, final @Nonnull ItemMeta itemMeta, final @Nonnull String skullTexture) {
        try {
            if (ServerUtils.hasPreciseUpdate("1_18_2")) {
                PlayerProfile playerProfile;
                if (!gameProfiles.containsKey(skullTexture)) {
                    final UUID uuid = UUID.randomUUID();
                    playerProfile = Bukkit.createPlayerProfile(uuid, uuid.toString().replaceAll("_", "").replaceAll("-", ""));
                    final PlayerTextures textures = playerProfile.getTextures();
                    final URI textureURI = StringUtils.toTextureURI(skullTexture);
                    if (textureURI != null) {
                        textures.setSkin(textureURI.toURL());
                        playerProfile.setTextures(textures);
                    } else {
                        ServerUtils.logSevere("{ItemHandler} The specified skull-texture is INVALID: " + skullTexture + ".");
                        ServerUtils.logSevere("{ItemHandler} The skull-texture will NOT be set!");
                    }
                } else {
                    playerProfile = (PlayerProfile) gameProfiles.get(skullTexture);
                }
                ((SkullMeta) itemMeta).setOwnerProfile(playerProfile);
            } else {
                GameProfile gameProfile;
                if (!gameProfiles.containsKey(skullTexture)) {
                    final UUID uuid = UUID.randomUUID();
                    gameProfile = new GameProfile(uuid, uuid.toString().replaceAll("_", "").replaceAll("-", ""));
                    gameProfile.getProperties().put("textures", new Property("textures", skullTexture));
                    gameProfiles.put(skullTexture, gameProfile);
                } else {
                    gameProfile = (GameProfile) gameProfiles.get(skullTexture);
                }
                final Field declaredField = itemMeta.getClass().getDeclaredField("profile");
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
    public static @Nonnull String getSkullTexture(final @Nonnull ItemMeta meta) {
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
    public static @Nonnull String getSkullTexture(final @Nonnull Skull skull) {
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
     * @param meta   - The ItemMeta to have its Skull Owner changed.
     * @param player - The Player being referenced.
     * @param owner  - The String name of the Skull Owner to be set.
     * @return The ItemMeta with the new Skull Owner.
     */
    public static @Nonnull ItemMeta setSkullOwner(final @Nonnull ItemMeta meta, final @Nonnull Player player, final @Nonnull String owner) {
        setStoredSkull(meta, player, owner);
        return meta;
    }

    /**
     * Sets the locale stored skull owner.
     *
     * @param meta   - The referenced ItemMeta.
     * @param player - The Player being referenced.
     * @param owner  - The referenced Skull Owner
     */
    public static void setStoredSkull(final @Nonnull ItemMeta meta, final @Nonnull Player player, final @Nonnull String owner) {
        if (!owner.isEmpty()) {
            SkullMeta skullMeta = (SkullMeta) meta;
            OfflinePlayer offPlayer = LegacyAPI.getOfflinePlayer(owner);
            if (Core.getCore().getDependencies().skinsRestorerEnabled()) {
                final String textureValue = Core.getCore().getDependencies().getSkinValue(player.getUniqueId(), owner);
                if (textureValue != null) {
                    setSkullTexture(player, meta, textureValue);
                } else {
                    try {
                        skullMeta.setOwningPlayer(offPlayer);
                    } catch (Throwable t) {
                        if (offPlayer.getName() != null) {
                            LegacyAPI.setSkullOwner(player, skullMeta, offPlayer.getName());
                        }
                    }
                }
            } else {
                try {
                    skullMeta.setOwningPlayer(offPlayer);
                } catch (Throwable t) {
                    if (offPlayer.getName() != null) {
                        LegacyAPI.setSkullOwner(player, skullMeta, offPlayer.getName());
                    }
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
    public static int stackItems(final @Nonnull Player player, final @Nonnull ItemStack item1, final @Nonnull ItemStack item2, final int slot, final boolean topInventory) {
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
    public static @Nonnull MapView existingView(final int id) {
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
    public static void removeCraftItems(final @Nonnull Player player) {
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
    public static void returnCraftingItem(final @Nonnull Player player, final int slot, final @Nonnull ItemStack item, long delay) {
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
    public static @Nullable Inventory getCraftInventory(final @Nonnull Player player) {
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
    public static boolean restoreCraftItems(final @Nonnull Player player, @Nullable final Inventory inventory) {
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
    public static @Nonnull ItemStack[] cloneContents(final @Nonnull ItemStack[] contents) {
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
    public static boolean isContentsEmpty(final @Nonnull ItemStack[] contents) {
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
    public static @Nonnull String serializeInventory(final @Nonnull Inventory inventory) {
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
    public static @Nullable Inventory deserializeInventory(final @Nonnull String inventoryData) {
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
    public static @Nullable String getNBTData(final @Nullable ItemStack item, final @Nonnull List<String> dataList) {
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
                    if (e.getCause() != null) {
                        ServerUtils.logSevere("{ItemHandler} An error has occurred when getting NBTData to an item, reason: " + e.getCause() + ".");
                        ServerUtils.sendSevereThrowable(e.getCause());
                    } else {
                        ServerUtils.logSevere("{ItemHandler} An error has occurred when getting NBTData to an item, reason: " + e.getMessage() + ".");
                    }
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
    public static boolean containsNBTData(final @Nonnull ItemStack item, final @Nonnull List<String> dataList) {
        if (Core.getCore().getData().dataTagsEnabled() && item.getType() != Material.AIR && getNBTData(item, dataList) != null) {
            return true;
        } else if (!Core.getCore().getData().dataTagsEnabled()) {
            final String colorDecode = StringUtils.colorDecode(item);
            return !colorDecode.isEmpty();
        }
        return false;
    }

    /**
     * Attempts to fetch the designated slot of an item.
     *
     * @param material - The ItemStack to be checked for a designated slot.
     * @return The proper designated slot name.
     */
    public static @Nonnull String getDesignatedSlot(final @Nonnull Material material) {
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
     * Sets the ItemStack to glow.
     *
     * @return The ItemStack instance.
     */
    public static @Nonnull ItemStack setGlowing(final @Nonnull ItemStack item) {
        Enchantment enchant;
        if (item.getType().name().equalsIgnoreCase("ENCHANTED_BOOK")) {
            enchant = getEnchantByName("BINDING_CURSE");
        } else if (item.getType().name().equalsIgnoreCase("BOW")) {
            enchant = getEnchantByName("LUCK");
        } else {
            enchant = getEnchantByName("ARROW_INFINITE");
        }
        if (enchant != null) {
            item.addUnsafeEnchantment(enchant, 1);
        }
        return item;
    }

    /**
     * Gets the Integer Delay of the specified String.
     *
     * @param context - The String to have the Delay found.
     * @return The Delay of the String as an Integer.
     */
    public static int getDelay(final @Nonnull String context) {
        try {
            final Integer returnInt = StringUtils.returnInteger(context);
            if (returnInt != null) {
                return returnInt;
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
    public static @Nullable String getDelayFormat(final @Nonnull String context) {
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
    public static @Nonnull String cutDelay(final @Nullable String context) {
        if (context == null) {
            return "";
        }
        final String delayFormat = getDelayFormat(context);
        if (delayFormat != null) {
            return context.replace(delayFormat, "");
        }
        return context;
    }

    /**
     * Removes the delay formatting from the specified String List.
     *
     * @param context - The String List to have the Delay Formatting removed.
     * @return The String List with the removed Delay Formatting.
     */
    public static @Nonnull List<String> cutDelay(final @Nonnull List<String> context) {
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
    public static boolean containsJSONEvent(final @Nonnull String formatPage) {
        return formatPage.contains(JSONEvent.TEXT.matchType) || formatPage.contains(JSONEvent.COLOR.matchType) || formatPage.contains(JSONEvent.SHOW_TEXT.matchType) || formatPage.contains(JSONEvent.OPEN_URL.matchType) || formatPage.contains(JSONEvent.RUN_COMMAND.matchType);
    }

    /**
     * Checks to see if the open_url is correctly defined with https or http.
     *
     * @param itemName    - The name of the item being modified.
     * @param type        - The JSONEvent type.
     * @param inputResult - The input for the JSONEvent.
     */
    public static void safetyCheckURL(final @Nonnull String itemName, final @Nonnull JSONEvent type, final @Nonnull String inputResult) {
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
    public static boolean isCustomSlot(final @Nonnull String slot) {
        return slot.equalsIgnoreCase("OFFHAND") || slot.equalsIgnoreCase("ARBITRARY") || isArmor(slot) || isCraftingSlot(slot) || slot.contains("%");
    }

    /**
     * Checks if the specified String is an armor type.
     *
     * @param context - The string to be checked.
     * @return If the string is an armor type.
     */
    public static boolean isArmor(final @Nonnull String context) {
        return StringUtils.containsIgnoreCase(context, "HELMET") || StringUtils.containsIgnoreCase(context, "CHESTPLATE") || StringUtils.containsIgnoreCase(context, "LEGGINGS") || StringUtils.containsIgnoreCase(context, "BOOTS");
    }

    /**
     * Checks if the specified String slot is a Crafting Slot.
     *
     * @param slot - The slot to be checked.
     * @return If the slot is a crafting slot.
     */
    public static boolean isCraftingSlot(final @Nonnull String slot) {
        return slot.equalsIgnoreCase("CRAFTING[0]") || slot.equalsIgnoreCase("CRAFTING[1]")
                || slot.equalsIgnoreCase("CRAFTING[2]") || slot.equalsIgnoreCase("CRAFTING[3]") || slot.equalsIgnoreCase("CRAFTING[4]");
    }

    /**
     * Checks if the Material is a Legacy Skull Item or Player Skull.
     *
     * @param material - The Material to be checked.
     * @return If the Material is a Skull/Player Head.
     */
    public static boolean isSkull(final @Nonnull Material material) {
        return material.toString().equalsIgnoreCase("SKULL_ITEM") || material.toString().equalsIgnoreCase("PLAYER_HEAD");
    }

    /**
     * Checks if the ItemStack is a Writable Book.
     *
     * @param item - The ItemStack to be checked.
     * @return If the ItemStack is a Writable Book.
     */
    public static boolean isBookQuill(final @Nonnull ItemStack item) {
        return item.getType().toString().equalsIgnoreCase("WRITABLE_BOOK") || item.getType().toString().equalsIgnoreCase("BOOK_AND_QUILL");
    }

    /**
     * Defines the JSONEvents for their action, event, and matchType.
     */
    public enum JSONEvent {
        TEXT("nullEvent", "text", "<text:"),
        COLOR("nullEvent", "color", "<color:"),
        SHOW_TEXT("hoverEvent", "show_text", "<show_text:"),
        OPEN_URL("clickEvent", "open_url", "<open_url:"),
        RUN_COMMAND("clickEvent", "run_command", "<run_command:"),
        CHANGE_PAGE("clickEvent", "change_page", "<change_page:");
        public final String event;
        public final String action;
        public final String matchType;

        JSONEvent(@Nonnull String Event, @Nonnull String Action, @Nonnull String MatchType) {
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

        CustomSlot(@Nonnull String name) {
            this.name = name;
        }

        public boolean isSlot(@Nonnull String slot) {
            return this.name.equalsIgnoreCase(slot);
        }
    }

    /**
     * Trim Material types.
     */
    public enum TrimMaterial {
        AMETHYST("AMETHYST_SHARD"),
        COPPER("COPPER_INGOT"),
        DIAMOND("DIAMOND"),
        EMERALD("EMERALD"),
        GOLD("GOLD_INGOT"),
        IRON("IRON_INGOT"),
        LAPIS("LAPIS_LAZULI"),
        NETHERITE("NETHERITE_INGOT"),
        QUARTZ("QUARTZ"),
        REDSTONE("REDSTONE");
        private final String name;

        TrimMaterial(@Nonnull String name) {
            this.name = name;
        }

        public Material getMaterial() {
            return ItemHandler.getMaterial(this.name, null);
        }
    }

    /**
     * Trim Pattern types.
     */
    public enum TrimPattern {
        COAST("COAST_ARMOR_TRIM_SMITHING_TEMPLATE"),
        DUNE("DUNE_ARMOR_TRIM_SMITHING_TEMPLATE"),
        EYE("EYE_ARMOR_TRIM_SMITHING_TEMPLATE"),
        HOST("HOST_ARMOR_TRIM_SMITHING_TEMPLATE"),
        RAISER("RAISER_ARMOR_TRIM_SMITHING_TEMPLATE"),
        RIB("RIB_ARMOR_TRIM_SMITHING_TEMPLATE"),
        SENTRY("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE"),
        SHAPER("SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE"),
        SILENCE("SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE"),
        SNOUT("SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE"),
        SPIRE("SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE"),
        TIDE("TIDE_ARMOR_TRIM_SMITHING_TEMPLATE"),
        VEX("VEX_ARMOR_TRIM_SMITHING_TEMPLATE"),
        WARD("WARD_ARMOR_TRIM_SMITHING_TEMPLATE"),
        WAYFINDER("WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE"),
        WILD("WILD_ARMOR_TRIM_SMITHING_TEMPLATE");
        private final String name;

        TrimPattern(@Nonnull String name) {
            this.name = name;
        }

        public Material getMaterial() {
            return ItemHandler.getMaterial(this.name, null);
        }
    }
}