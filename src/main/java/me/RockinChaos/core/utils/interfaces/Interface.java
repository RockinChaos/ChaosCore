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
package me.RockinChaos.core.utils.interfaces;

import me.RockinChaos.core.Core;
import me.RockinChaos.core.handlers.ItemHandler;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import me.RockinChaos.core.utils.interfaces.types.Button;
import me.RockinChaos.core.utils.interfaces.types.Page;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Interface implements InventoryHolder {

    private final Inventory inventory;
    private final int pageSize;
    private final Player panePlayer;
    private final SortedMap<Integer, Page> pages = new TreeMap<>();
    private final boolean isPaged;
    private int activeButton = -1;
    private int currentIndex;
    private boolean canChat = false;
    private boolean pendingChat = false;
    private boolean pendingClick = false;
    private Button controlBack;
    private Button controlNext;
    private Button controlExit;
    private Button exitButton;

    /**
     * Creates a new interface instance.
     *
     * @param isPaged    - If the inventory has multiple pages.
     * @param rows       - Number of rows for the inventory.
     * @param exitButton - The action to be taken when exiting the menu.
     * @param title      - Title to be displayed on the inventory.
     * @param player     - The player interacting with the interface.
     */
    public Interface(final boolean isPaged, final int rows, final @Nonnull Button exitButton, final @Nonnull String title, final @Nonnull Player player) {
        this.panePlayer = player;
        this.isPaged = isPaged;
        if (this.isPaged) {
            this.pageSize = rows - 1;
            this.exitButton = exitButton;
        } else {
            this.pageSize = rows * 9;
        }
        this.inventory = Bukkit.createInventory(this, rows * 9, StringUtils.colorFormat(title));
        this.inventory.setMaxStackSize(128);
        this.pages.put(0, new Page(this.pageSize));
        this.createControls(this.inventory);
    }

    /**
     * Called on player inventory click.
     * Handles the player click event for the button method execution.
     *
     * @param event - InventoryClickEvent
     */
    public void onClick(final @Nonnull InventoryClickEvent event) {
        if (this.panePlayer.equals(event.getWhoClicked()) && !(this.pendingClick && event.getSlot() <= event.getWhoClicked().getInventory().getSize() && event.getSlot() >= 0 && this.clickInventory(event))) {
            try {
                if (this.isPaged && event.getSlot() == this.inventory.getSize() - 8 && this.getCurrentPage() > 1) {
                    if (this.controlBack != null) {
                        this.controlBack.onClick(event);
                    }
                } else if (this.isPaged && event.getSlot() == this.inventory.getSize() - 2 && this.getCurrentPage() < this.getPageAmount()) {
                    if (this.controlNext != null) {
                        this.controlNext.onClick(event);
                    }
                } else if (this.isPaged && (event.getSlot() == this.inventory.getSize() - 1 || event.getSlot() == this.inventory.getSize() - 9)) {
                    if (this.controlExit != null) {
                        this.controlExit.onClick(event);
                    }
                } else if (event.getCurrentItem() != null) {
                    this.pages.get(this.currentIndex).handleClick(event);
                    this.activeButton = event.getSlot();
                    if (this.pages.get(this.currentIndex).chatEvent(event.getSlot())) {
                        this.pendingChat = true;
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
            event.setCancelled(true);
        }
    }

    /**
     * Called on player chat.
     * Handles the player chat event for the button clicked.
     *
     * @param event - AsyncPlayerChatEvent
     */
    public void onChat(final @Nonnull AsyncPlayerChatEvent event) {
        if (this.panePlayer.equals(event.getPlayer()) && this.activeButton != -1) {
            this.pages.get(this.currentIndex).handleChat(event, this.activeButton);
            this.pendingChat = false;
            event.setCancelled(true);
        } else if (!canChat) {
            this.pendingChat = false;
        }
    }

    /**
     * Called on player chat.
     * Handles the player chat event for the button clicked.
     *
     * @param player - the player involved in the typing event.
     */
    public void onTyping(final @Nonnull Player player) {
        SchedulerUtils.run(() -> {
            if (this.panePlayer.equals(player) && this.activeButton != -1) {
                this.pages.get(this.currentIndex).handleTyping(player, this.activeButton);
                this.pendingChat = false;
            } else if (!canChat) {
                this.pendingChat = false;
            }
        });
    }

    /**
     * Attempts to close the active player query.
     *
     * @param player - the player involved in the typing event.
     */
    public void closeQuery(final @Nonnull Player player) {
        if (this.panePlayer.equals(player) && this.activeButton != -1) {
            this.pages.get(this.currentIndex).closeQuery(player, this.activeButton);
        }
    }

    /**
     * Allows the button to be clicked.
     *
     * @param bool - Allows the button to be clicked.
     */
    public void allowClick(final boolean bool) {
        this.pendingClick = bool;
    }

    /**
     * Allows the button to be clicked without a chat requirement.
     *
     * @param bool - Allows the button to be clicked without a chat requirement.
     */
    public void allowChat(final boolean bool) {
        this.canChat = bool;
    }

    /**
     * Adds a button to the current page.
     *
     * @param button - The button to be added.
     */
    public void addButton(final @Nonnull Button button) {
        for (Entry<Integer, Page> entry : this.pages.entrySet()) {
            if (entry.getValue().addButton(button)) {
                if (entry.getKey() == this.currentIndex) {
                    this.renderPage();
                }
                return;
            }
        }
        Page page = new Page(this.pageSize);
        page.addButton(button);
        this.pages.put(this.pages.lastKey() + 1, page);
        this.renderPage();
    }

    /**
     * Adds a button to the current page.
     *
     * @param button - The button to be added.
     * @param amount - The number of buttons to be added.
     */
    public void addButton(final @Nonnull Button button, final int amount) {
        if (amount == 0 || amount == 1) {
            this.addButton(button);
        } else {
            for (int i = 0; i < amount; i++) {
                this.addButton(button);
            }
        }
    }

    /**
     * Removes a button from the current page.
     *
     * @param button - The button to be removed.
     */
    public void removeButton(final @Nonnull Button button) {
        for (Iterator<Entry<Integer, Page>> iterator = pages.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<Integer, Page> entry = iterator.next();
            if (entry.getValue().removeButton(button)) {
                if (entry.getValue().isEmpty()) {
                    if (this.pages.size() > 1) {
                        iterator.remove();
                    }
                    if (this.currentIndex >= this.pages.size()) {
                        this.currentIndex--;
                    }
                }
                if (entry.getKey() >= this.currentIndex) {
                    this.renderPage();
                }
                return;
            }
        }
    }

    /**
     * Sets a custom return button.
     *
     * @param button - The button to be set as the return button.
     */
    public void setReturnButton(final @Nonnull Button button) {
        if (this.isPaged) {
            this.controlExit = button;
            this.inventory.setItem(inventory.getSize() - 9, button.getItemStack());
            this.inventory.setItem(inventory.getSize() - 1, button.getItemStack());
        }
    }

    /**
     * Creates the controls for the existing inventory page.
     *
     * @param inventory - The inventory to have the controls added.
     */
    private void createControls(final @Nonnull Inventory inventory) {
        if (this.isPaged) {
            if (this.getCurrentPage() > 1) {
                ItemStack backItem;
                backItem = ItemHandler.getItem("ARROW", 1, false, false, "&3" + (!Core.getCore().getLang().getString("menus.general.items.prevPage.name").isEmpty() ? Core.getCore().getLang().getString("menus.general.items.prevPage.name").replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount())) : "&n&lPrevious Page"),
                        Stream.concat(Core.getCore().getLang().getStringList("menus.general.items.prevPage.lore").stream().filter(s -> !s.isEmpty()).map(lore -> lore.replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount()))),
                        Stream.of(Core.getCore().getLang().getStringList("menus.general.items.prevPage.lore").isEmpty() || Core.getCore().getLang().getStringList("menus.general.items.prevPage.lore").stream().allMatch(String::isEmpty) ? "&7*Previous page &a&l" + (this.getCurrentPage() + 1) + "&7 / &c&l" + this.getPageAmount() : "")).toArray(String[]::new));
                this.controlBack = new Button(backItem, event -> this.selectPage(this.currentIndex - 1));
                inventory.setItem(inventory.getSize() - 8, backItem);
            } else {
                ItemStack backItem;
                backItem = ItemHandler.getItem("LEVER", 1, false, false, "&c" + (!Core.getCore().getLang().getString("menus.general.items.prevPage.name").isEmpty() ? Core.getCore().getLang().getString("menus.general.items.prevPage.name").replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount())) : "&n&lPrevious Page"),
                        Stream.concat(Core.getCore().getLang().getStringList("menus.general.items.prevPage.firstLore").stream().filter(s -> !s.isEmpty()).map(lore -> lore.replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount()))),
                        Stream.of(Core.getCore().getLang().getStringList("menus.general.items.prevPage.firstLore").isEmpty() || Core.getCore().getLang().getStringList("menus.general.items.prevPage.firstLore").stream().allMatch(String::isEmpty) ? "&7*You are already at the first page." : "")).toArray(String[]::new));
                inventory.setItem(inventory.getSize() - 8, backItem);
            }
            if (this.getCurrentPage() < this.getPageAmount()) {
                ItemStack nextItem;
                nextItem = ItemHandler.getItem("ARROW", 1, false, false, "&3" + (!Core.getCore().getLang().getString("menus.general.items.nextPage.name").isEmpty() ? Core.getCore().getLang().getString("menus.general.items.nextPage.name").replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount())) : "&n&lNext Page"),
                        Stream.concat(Core.getCore().getLang().getStringList("menus.general.items.nextPage.lore").stream().filter(s -> !s.isEmpty()).map(lore -> lore.replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount()))),
                        Stream.of(Core.getCore().getLang().getStringList("menus.general.items.nextPage.lore").isEmpty() || Core.getCore().getLang().getStringList("menus.general.items.nextPage.lore").stream().allMatch(String::isEmpty) ? "&7*Next page &a&l" + (this.getCurrentPage() + 1) + "&7 / &c&l" + this.getPageAmount() : "")).toArray(String[]::new));
                this.controlNext = new Button(nextItem, event -> this.selectPage(this.getCurrentPage()));
                inventory.setItem(inventory.getSize() - 2, nextItem);
            } else {
                ItemStack nextItem;
                nextItem = ItemHandler.getItem("LEVER", 1, false, false, "&c" + (!Core.getCore().getLang().getString("menus.general.items.nextPage.name").isEmpty() ? Core.getCore().getLang().getString("menus.general.items.nextPage.name").replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount())) : "&n&lNext Page"),
                        Stream.concat(Core.getCore().getLang().getStringList("menus.general.items.nextPage.lastLore").stream().filter(s -> !s.isEmpty()).map(lore -> lore.replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount()))),
                        Stream.of(Core.getCore().getLang().getStringList("menus.general.items.nextPage.lastLore").isEmpty() || Core.getCore().getLang().getStringList("menus.general.items.nextPage.lastLore").stream().allMatch(String::isEmpty) ? "&7*You are already at the last page." : "")).toArray(String[]::new));
                inventory.setItem(inventory.getSize() - 2, nextItem);
            }
            inventory.setItem(inventory.getSize() - 5, ItemHandler.getItem("BOOK", 1, false, false, !Core.getCore().getLang().getString("menus.general.items.page.name").isEmpty() ? Core.getCore().getLang().getString("menus.general.items.page.name").replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount())) : "&3&lPage &a&l" + this.getCurrentPage() + "&7 / &c&l" + this.getPageAmount(),
                         Stream.concat(Core.getCore().getLang().getStringList("menus.general.items.page.lore").stream().filter(s -> !s.isEmpty()).map(lore -> lore.replace("%page%", String.valueOf(this.getCurrentPage())).replace("%total_pages%", String.valueOf(this.getPageAmount()))),
                         Stream.of(Core.getCore().getLang().getStringList("menus.general.items.page.lore").isEmpty() || Core.getCore().getLang().getStringList("menus.general.items.page.lore").stream().allMatch(String::isEmpty) ? "&7You are on page &a&l" + this.getCurrentPage() + "&7 / &c&l" + this.getPageAmount() : "")).toArray(String[]::new)));
            ItemStack exitItem = ItemHandler.getItem("BARRIER", 1, false, false, "&c&l&nMain Menu", "&7", "&7*Returns you to the main menu.");
            if (this.controlExit == null) {
                this.controlExit = this.exitButton;
            } else {
                exitItem = controlExit.getItemStack();
            }
            inventory.setItem(inventory.getSize() - 9, exitItem);
            inventory.setItem(inventory.getSize() - 1, exitItem);
            ItemStack blackPane = ItemHandler.getItem((ServerUtils.hasSpecificUpdate("1_13") ? "BLACK_STAINED_GLASS_PANE" : "STAINED_GLASS_PANE:15"), 1, false, false, "&f", "");
            inventory.setItem(inventory.getSize() - 3, blackPane);
            inventory.setItem(inventory.getSize() - 4, blackPane);
            inventory.setItem(inventory.getSize() - 6, blackPane);
            inventory.setItem(inventory.getSize() - 7, blackPane);
        }
    }

    /**
     * Renders the current page.
     */
    private void renderPage() {
        this.inventory.clear();
        this.pages.get(this.currentIndex).render(this.inventory);
        this.createControls(this.inventory);
    }

    /**
     * Gets the total number of existing pages.
     *
     * @return The current number of pages.
     */
    private int getPageAmount() {
        return this.pages.size();
    }

    /**
     * Gets the current inventory page.
     *
     * @return The current inventory page number.
     */
    private int getCurrentPage() {
        return (this.currentIndex + 1);
    }

    /**
     * Changes the current inventory page to the specified page number
     *
     * @param index - The page to become the current page.
     */
    private void selectPage(final int index) {
        if (index == this.currentIndex || index >= this.pages.size()) {
            return;
        }
        this.currentIndex = index;
        this.renderPage();
    }

    /**
     * This is not called, rather it handles the onClick event to check
     * if the clicked inventory and button is valid.
     *
     * @param event - InventoryClickEvent
     * @return If the inventory clicked is the same as the current inventory page.
     */
    public boolean clickInventory(final @Nonnull InventoryClickEvent event) {
        if (ServerUtils.hasSpecificUpdate("1_14")) {
            return (event.getSlot() == -999 || event.getSlot() == -1 || event.getClickedInventory() == event.getWhoClicked().getInventory());
        } else {
            final ItemStack clickItem = event.getCurrentItem();
            final int slot = event.getSlot();
            return (slot == -999 || slot == -1 || (clickItem != null && (clickItem.equals(event.getWhoClicked().getInventory().getItem(slot)) || clickItem.getType() == org.bukkit.Material.AIR)));
        }
    }

    /**
     * Checks if the current page is expecting a player chat event before continuing.
     *
     * @return If there is a pending chat event.
     */
    public boolean chatPending() {
        return this.pendingChat;
    }

    /**
     * Opens the current inventory page for the player to view.
     *
     * @param player - The player to have the current inventory page opened.
     */
    public void open(final @Nonnull Player player) {
        if (Bukkit.isPrimaryThread()) {
            this.renderPage();
            player.openInventory(this.getInventory());
        } else {
        SchedulerUtils.run(() -> {
            this.renderPage();
            player.openInventory(this.getInventory());
        });
        }
    }

    /**
     * Gets the inventory.
     *
     * @return The inventory for the interface.
     */
    @Override
    public @Nonnull Inventory getInventory() {
        return this.inventory;
    }
}