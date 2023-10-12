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
package me.RockinChaos.core.utils.interfaces.types;

import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.ReflectionUtils.FieldAccessor;
import me.RockinChaos.core.utils.ReflectionUtils.MinecraftField;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import me.RockinChaos.core.utils.api.LegacyAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

public class Container {

    private final Class<?> baseComponent = ReflectionUtils.getMinecraftClass("IChatBaseComponent");
    private final Class<?> humanEntity = ReflectionUtils.getMinecraftClass("EntityHuman");
    private final Class<?> mineContainer = ReflectionUtils.getMinecraftClass("Container");
    private final FieldAccessor<?> activeContainer = ReflectionUtils.getField(this.humanEntity, MinecraftField.ActiveContainer.getField());
    private final FieldAccessor<?> defaultContainer = ReflectionUtils.getField(ReflectionUtils.getMinecraftClass("EntityHuman"), MinecraftField.DefaultContainer.getField());
    private final Class<?> playOpenWindow = ReflectionUtils.getMinecraftClass("PacketPlayOutOpenWindow");
    private final Class<?> playCloseWindow = ReflectionUtils.getMinecraftClass("PacketPlayOutCloseWindow");
    private ItemStack outItem;
    private String outText;
    private int outPreview = 0;
    private String leftText;
    private boolean isAction = false;
    private Object container = null;
    private int containerId = 0;

    /**
     * Creates a new Container (Anvil).
     *
     * @param player         - The player the Container is assigned to.
     * @param inventoryTitle - The Title (BaseComponent) to set to the Container.
     * @param outItem        - The expected result item of the Container.
     */
    public Container(final Player player, final Object inventoryTitle, final ItemStack outItem) {
        try {
            final Class<?> blockPosition = ReflectionUtils.getMinecraftClass("BlockPosition");
            final Class<?> containerAnvil = ReflectionUtils.getMinecraftClass("ContainerAnvil");
            final Object world = player.getWorld().getClass().getMethod("getHandle").invoke(player.getWorld());
            final Object playerInventory;
            if (ServerUtils.hasSpecificUpdate("1_17")) {
                playerInventory = Objects.requireNonNull(ReflectionUtils.getEntity(player)).getClass().getMethod(MinecraftField.Inventory.getField()).invoke(ReflectionUtils.getEntity(player));
            } else {
                playerInventory = Objects.requireNonNull(ReflectionUtils.getEntity(player)).getClass().getField(MinecraftField.Inventory.getField()).get(ReflectionUtils.getEntity(player));
            }
            this.outItem = outItem;
            this.containerId = this.getRealNextContainerId(player);
            if (ServerUtils.hasSpecificUpdate("1_14")) {
                final Class<?> containerAccess = ReflectionUtils.getMinecraftClass("ContainerAccess");
                final ReflectionUtils.MethodInvoker CAM = ReflectionUtils.getMethod(containerAccess, MinecraftField.At.getField(), ReflectionUtils.getMinecraftClass("World"), blockPosition);
                final Object accessContainer = CAM.invoke(containerAccess, world, blockPosition.getConstructor(int.class, int.class, int.class).newInstance(0, 0, 0));
                this.container = containerAnvil.getConstructor(int.class, ReflectionUtils.getMinecraftClass("PlayerInventory"), containerAccess).newInstance(this.containerId, playerInventory, accessContainer);
            } else {
                this.container = containerAnvil.getConstructor(ReflectionUtils.getMinecraftClass("PlayerInventory"), ReflectionUtils.getMinecraftClass("World"), blockPosition, this.humanEntity)
                        .newInstance(playerInventory, world, blockPosition.getConstructor(int.class, int.class, int.class).newInstance(0, 0, 0), ReflectionUtils.getEntity(player));
            }
            ReflectionUtils.getField(this.mineContainer, "checkReachable").set(this.container, false);
            if (ServerUtils.hasSpecificUpdate("1_14")) {
                this.container.getClass().getMethod("setTitle", this.baseComponent).invoke(this.container, inventoryTitle);
            }
            ReflectionUtils.getField(containerAnvil, (ServerUtils.hasSpecificUpdate("1_13") ? "maximumRepairCost" : ServerUtils.hasSpecificUpdate("1_12") ? "levelCost" : "a"), int.class).set(this.container, 0);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Gets the next available NMS container id for the player.
     *
     * @param player The player to get the next container id of.
     * @return The next available NMS container id.
     */
    private int getRealNextContainerId(final Player player) {
        return (int) ReflectionUtils.invokeMethod("nextContainerCounter", ReflectionUtils.getEntity(player));
    }

    /**
     * Closes the current inventory for the player.
     *
     * @param player The player that needs their current inventory closed.
     */
    public void handleInventoryCloseEvent(final Player player) {
        try {
            ReflectionUtils.getCraftBukkitClass("event.CraftEventFactory").getMethod("handleInventoryCloseEvent", this.humanEntity).invoke(null, ReflectionUtils.getEntity(player));
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Sends PacketPlayOutOpenWindow to the player with the container id and window title.
     *
     * @param player         The player to send the packet to.
     * @param inventoryTitle The title of the inventory to be opened (only works in Minecraft 1.14 and above).
     */
    public void sendPacketOpenWindow(final Player player, final Object inventoryTitle) {
        try {
            Object packets;
            if (ServerUtils.hasSpecificUpdate("1_14")) {
                final Class<?> mineContainers = ReflectionUtils.getMinecraftClass("Containers");
                FieldAccessor<?> anvilContainers = ReflectionUtils.getField(mineContainers, MinecraftField.Anvil.getField());
                Constructor<?> packetConstructor = this.playOpenWindow.getConstructor(int.class, mineContainers, this.baseComponent);
                packets = packetConstructor.newInstance(this.containerId, anvilContainers.get(mineContainers), inventoryTitle);
            } else {
                Constructor<?> packetConstructor = this.playOpenWindow.getConstructor(int.class, String.class, this.baseComponent);
                packets = packetConstructor.newInstance(this.containerId, "minecraft:anvil", inventoryTitle);
            }
            ReflectionUtils.sendPacket(player, packets);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Sends PacketPlayOutCloseWindow to the player with the container id.
     *
     * @param player The player to send the packet to.
     */
    public void sendPacketCloseWindow(final Player player) {
        try {
            Object packets = this.playCloseWindow.getConstructor(int.class).newInstance(this.containerId);
            ReflectionUtils.sendPacket(player, packets);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Sets the NMS player's active container to the default one.
     *
     * @param player The player to set the active container of.
     */
    public void setActiveContainerDefault(final Player player) {
        this.activeContainer.set(ReflectionUtils.getEntity(player), this.defaultContainer.get(ReflectionUtils.getEntity(player)));
    }

    /**
     * Sets the NMS player's active container to the one supplied.
     *
     * @param player The player to set the active container of.
     */
    public void setActiveContainer(final Player player) {
        this.activeContainer.set(ReflectionUtils.getEntity(player), this.container);
    }

    /**
     * Sets the supplied windowId of the supplied Container.
     */
    public void setActiveContainerId() {
        if (!ServerUtils.hasSpecificUpdate("1_14")) {
            ReflectionUtils.getField(this.mineContainer, "windowId").set(this.container, this.containerId);
        }
    }

    /**
     * Adds a slot listener to the supplied container for the player.
     *
     * @param player The player to have as a listener.
     */
    public void addActiveContainerSlotListener(final Player player) {
        try {
            if (ServerUtils.hasSpecificUpdate("1_17")) {
                Objects.requireNonNull(ReflectionUtils.getEntity(player)).getClass().getMethod(MinecraftField.AddSlotListener.getField(), this.mineContainer).invoke(ReflectionUtils.getEntity(player), this.container);
            } else {
                this.mineContainer.getMethod(MinecraftField.AddSlotListener.getField(), ReflectionUtils.getMinecraftClass("ICrafting")).invoke(this.container, ReflectionUtils.getEntity(player));
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Gets the text of the input field for the Container.
     *
     * @return The current input field text.
     */
    public String getRenameText() {
        try {
            return (String) this.mineContainer.getMethod(MinecraftField.RenameText.getField()).invoke(this.container);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return null;
    }

    /**
     * Sets the text of the input field for the Container.
     *
     * @param text - The text to be set in the input field.
     */
    public void setRenameText(final String text) {
        try {
            Object inputLeft = this.mineContainer.getMethod(MinecraftField.GetSlot.getField(), int.class).invoke(this.container, 0);
            boolean inputLeftF = (boolean) inputLeft.getClass().getMethod(MinecraftField.HasItem.getField()).invoke(inputLeft);
            if (inputLeftF) {
                Object inputLeftE = inputLeft.getClass().getMethod(MinecraftField.GetItem.getField()).invoke(inputLeft);
                if (ServerUtils.hasSpecificUpdate("1_13")) {
                    inputLeftE.getClass().getMethod("a", this.baseComponent).invoke(inputLeftE, ReflectionUtils.literalChatComponent(text));
                } else {
                    inputLeftE.getClass().getMethod((ServerUtils.hasSpecificUpdate("1_11") ? "g" : "c"), String.class).invoke(inputLeftE,text);
                }
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Gets the result item for the Container.
     *
     * @param player - the player being referenced.
     * @param inventory - The AnvilInventory instance.
     * @return The newly formatted result item.
     */
    public ItemStack getResult(final Player player, final String entryText) {
        final ItemStack item = this.outItem.clone();
        String renameText = entryText;
        try {
            final ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && renameText != null) {
                boolean isAction = renameText.startsWith("-->") || this.isAction;
                renameText = renameText.substring((renameText.startsWith("--> ") ? 4 : renameText.startsWith("-->") ? 3 : renameText.startsWith("-> ") ? 3 : renameText.startsWith("->") ? 2 : renameText.startsWith("--") ? 2 : renameText.startsWith("-") ? 1 : 0));
                if (renameText.isEmpty() && isAction && this.outText != null && !this.outText.isEmpty()) {
                    Map.Entry<StringBuilder, Integer> rSpaces = StringUtils.getSpacers(0, 5, this.outPreview).entrySet().iterator().next();
                    this.outPreview = rSpaces.getValue();
                    renameText = this.outText + rSpaces.getKey();
                } else if (renameText.isEmpty()) {
                    Map.Entry<StringBuilder, Integer> rSpaces = StringUtils.getSpacers(0, 5, this.outPreview).entrySet().iterator().next();
                    this.outPreview = rSpaces.getValue();
                    renameText = itemMeta.getDisplayName() + rSpaces.getKey();
                }
                itemMeta.setDisplayName(StringUtils.translateLayout(renameText, player));
                item.setItemMeta(itemMeta);
                if (!ServerUtils.hasSpecificUpdate("1_12")) {
                    LegacyAPI.updateInventory(player);
                }
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return item;
    }

    /**
     * Handles the typing action in the AnvilInventory to be reflected in the left item and input field.
     *
     * @param inventory - The AnvilInventory instance.
     */
    public void handleTyping(final String entryText) {
        String renameText = entryText;
        try {
            if (renameText != null) {
                boolean isAction = renameText.startsWith("-->") || this.isAction;
                renameText = renameText.substring((renameText.startsWith("-->") ? 3 : renameText.startsWith("--> ") ? 4 : renameText.startsWith("->") ? 2 : renameText.startsWith("-> ") ? 3 : 0));
                renameText = renameText.trim();
                if (!renameText.isEmpty() && !this.isAction) {
                    this.setRenameText(renameText);
                } else if (isAction) {
                    if (this.leftText != null && !this.leftText.isEmpty()) {
                        this.setRenameText("-->" + this.leftText);
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Removes the repair cost of the AnvilInventory Container.
     *
     * @param inventory - The AnvilInventory instance.
     */
    public void removeCost(final AnvilInventory inventory) {
        if (ServerUtils.hasSpecificUpdate("1_11")) {
            inventory.setRepairCost(0);
        }
    }

    /**
     * If the Container is currently in a retreat actionable resting state.
     *
     * @param action - If the container should be in a retreat actionable resting state.
     */
    public void setAction(final boolean action) {
        this.isAction = action;
    }

    /**
     * Sets the specified text of the left item/input field.
     *
     * @param leftText - The text to be set to the left item/input field.
     */
    public void setLeftText(final String leftText) {
        this.leftText = leftText;
    }

    /**
     * Sets the specified text of the output item.
     *
     * @param outText - The text to be set to the output item.
     */
    public void setOutText(final String outText) {
        this.outText = outText;
    }

    /**
     * Gets the Inventory (Bukkit) of the Container.
     *
     * @return The Inventory (Bukkit) instance of the Minecraft Inventory Container.
     */
    public Inventory getBukkitInventory() {
        return (Inventory) ReflectionUtils.invokeMethod("getTopInventory", ReflectionUtils.invokeMethod("getBukkitView", this.container));
    }
}