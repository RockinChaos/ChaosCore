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

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.interfaces.Query;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Button {

    private final ItemStack itemStack;
    private int counter = 0;
    private final int ID = this.counter++;
    private Consumer<InventoryClickEvent> clickAction;
    private Consumer<AsyncPlayerChatEvent> chatAction;
    private Consumer<Query.Builder> typingAction;
    private Query activeQuery;

    /**
     * Creates a new button instance.
     * There is no click event or chat event.
     *
     * @param itemStack - The ItemStack the button is to be placed as.
     */
    public Button(final @Nonnull ItemStack itemStack) {
        this(itemStack, event -> {
        });
    }

    /**
     * Creates a new button instance.
     * There is no chat event.
     *
     * @param itemStack   - The ItemStack the button is to be placed as.
     * @param clickAction - The method to be executed upon clicking the button.
     */
    public Button(final @Nonnull ItemStack itemStack, final @Nonnull Consumer<InventoryClickEvent> clickAction) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
    }

    /**
     * Creates a new button instance.
     *
     * @param itemStack   - The ItemStack the button is to be placed as.
     * @param clickAction - The method to be executed upon clicking the button.
     * @param chatAction  - The method to be executed upon chatting after clicking the button.
     */
    public Button(final @Nonnull ItemStack itemStack, final @Nonnull Consumer<InventoryClickEvent> clickAction, @Nonnull final Consumer<AsyncPlayerChatEvent> chatAction) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
        this.chatAction = chatAction;
    }

    /**
     * Creates a new button instance.
     *
     * @param itemStack    - The ItemStack the button is to be placed as.
     * @param clickAction  - The method to be executed upon clicking the button.
     * @param typingAction - The method to be executed upon typing in an anvil after clicking the button.
     */
    public Button(final @Nonnull ItemStack itemStack, final @Nonnull Consumer<InventoryClickEvent> clickAction, final @Nonnull Consumer<Query.Builder> typingAction, final int placeholder) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
        this.typingAction = typingAction;
    }

    /**
     * Creates a new button instance.
     *
     * @param itemStack   - The ItemStack the button is to be placed as.
     * @param clickAction - The method to be executed upon clicking the button.
     */
    public Button(final @Nonnull ItemStack itemStack, final boolean tempIdentifier, final @Nonnull Consumer<InventoryClickEvent> clickAction) {
        this.itemStack = itemStack;
        this.clickAction = clickAction;
    }

    /**
     * Gets the ItemStack for the button.
     *
     * @return The buttons ItemStack.
     */
    public @Nonnull ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Sets the click action method to be executed.
     *
     * @param clickAction - The click action method to be executed.
     */
    public void setClickAction(final @Nonnull Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
    }

    /**
     * Sets the chat action method to be executed.
     *
     * @param chatAction - The chat action method to be executed.
     */
    public void setChatAction(final @Nonnull Consumer<AsyncPlayerChatEvent> chatAction) {
        this.chatAction = chatAction;
    }

    /**
     * Sets the typing action method to be executed.
     *
     * @param typingAction - The typing action method to be executed.
     */
    public void setTypingAction(final @Nonnull Consumer<Query.Builder> typingAction) {
        this.typingAction = typingAction;
    }

    /**
     * Called on player inventory click.
     * Executes the pending click actions.
     *
     * @param event - InventoryClickEvent
     */
    public void onClick(final @Nonnull InventoryClickEvent event) {
        if (Core.getCore().getPlugin().isEnabled()) {
            SchedulerUtils.run(() -> this.clickAction.accept(event));
        }
    }

    /**
     * Called on player chat.
     * Executes the pending chat actions.
     *
     * @param event - AsyncPlayerChatEvent
     */
    public void onChat(final @Nonnull AsyncPlayerChatEvent event) {
        if (Core.getCore().getPlugin().isEnabled()) {
            SchedulerUtils.run(() -> this.chatAction.accept(event));
        }
    }

    /**
     * Called on player typing.
     * Executes the pending typing actions.
     *
     * @param player - the player involved in the typing event.
     */
    public void onTyping(final @Nonnull Player player) {
        if (Core.getCore().getPlugin().isEnabled()) {
            final Query.Builder builder = new Query.Builder().text(" ");
            SchedulerUtils.run(() -> {
                this.typingAction.accept(builder);
                {
                    this.activeQuery = builder.open(player);
                }
            });
        }
    }

    /**
     * Attempts to force close any active queries.
     */
    public void closeQuery() {
        this.activeQuery.closeInventory();
    }

    /**
     * Checks if the button is waits for a chat event.
     *
     * @return If the button is listening for a chat event.
     */
    public boolean chatEvent() {
        return this.chatAction != null;
    }

    /**
     * Checks if the button is waits for a typing event.
     *
     * @return If the button is listening for a typing event.
     */
    public boolean typingEvent() {
        return this.typingAction != null;
    }

    /**
     * Checks if the current button instance is similar to the referenced object.
     *
     * @param obj - The object being compared.
     */
    @Override
    public boolean equals(final @Nonnull Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Button)) {
            return false;
        }
        Button button = (Button) obj;
        return this.ID == button.ID;
    }

    /**
     * Gets the hash of the current button id.
     *
     * @return The hash of the current button id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.ID);
    }
}