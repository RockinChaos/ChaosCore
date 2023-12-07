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
import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.api.LegacyAPI;
import me.RockinChaos.core.utils.interfaces.types.Container;
import me.RockinChaos.core.utils.protocol.ProtocolManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Query {

    private final Player player;
    private final Executor mainThreadExecutor;
    private final Object titleComponent;
    private final ItemStack[] initialContents;
    private final boolean preventClose;
    private final Set<Integer> interactableSlots;
    private final Consumer<StateSnapshot> closeListener;
    private final boolean concurrentClickHandlerExecution;
    private final ClickHandler clickHandler;
    private final AnvilInventory inventoryListener = new AnvilInventory();
    private final PrepareAnvil typingListener = new PrepareAnvil();
    private final PrepareAnvil_LEGACY typingListener_LEGACY = new PrepareAnvil_LEGACY();
    private Inventory inventory;
    private boolean open;
    private Container container;

    /**
     * Create a Query
     *
     * @param player                          The {@link Player} to open the inventory for.
     * @param mainThreadExecutor              An {@link Executor} that executes on the main server thread.
     * @param titleComponent                  What to have the text already set to.
     * @param initialContents                 The initial contents of the inventory.
     * @param preventClose                    Whether to prevent the inventory from closing.
     * @param closeListener                   A {@link Consumer} when the inventory closes.
     * @param concurrentClickHandlerExecution Flag to allow concurrent execution of the click handler.
     * @param clickHandler                    A {@link ClickHandler} that is called when the player clicks a slot.
     */
    private Query(
            Player player,
            Executor mainThreadExecutor,
            Object titleComponent,
            ItemStack[] initialContents,
            boolean preventClose,
            Set<Integer> interactableSlots,
            Consumer<StateSnapshot> closeListener,
            boolean concurrentClickHandlerExecution,
            ClickHandler clickHandler) {
        this.player = player;
        this.mainThreadExecutor = mainThreadExecutor;
        this.titleComponent = titleComponent;
        this.initialContents = initialContents;
        this.preventClose = preventClose;
        this.interactableSlots = Collections.unmodifiableSet(interactableSlots);
        this.closeListener = closeListener;
        this.concurrentClickHandlerExecution = concurrentClickHandlerExecution;
        this.clickHandler = clickHandler;
    }

    /**
     * Opens the container
     */
    private void openInventory() {
        Bukkit.getPluginManager().registerEvents(this.inventoryListener, Core.getCore().getPlugin());
        if (ServerUtils.hasSpecificUpdate("1_11")) {
            Bukkit.getPluginManager().registerEvents(this.typingListener, Core.getCore().getPlugin());
        } else {
            if (ProtocolManager.isDead()) {
                ProtocolManager.handleProtocols();
            }
            Bukkit.getPluginManager().registerEvents(this.typingListener_LEGACY, Core.getCore().getPlugin());
        }
        this.container = new Container(this.player, this.titleComponent, this.initialContents[this.initialContents.length - 1]);
        this.container.handleInventoryCloseEvent(this.player);
        this.container.setActiveContainerDefault(this.player);
        this.inventory = this.container.getBukkitInventory();
        for (int i = 0; i < this.initialContents.length; i++) {
            this.inventory.setItem(i, this.initialContents[i]);
        }
        this.container.sendPacketOpenWindow(this.player, this.titleComponent);
        this.container.setActiveContainer(this.player);
        this.container.setActiveContainerId();
        this.container.addActiveContainerSlotListener(this.player);
        this.open = true;
    }

    /**
     * Closes the inventory if it's open.
     */
    public void closeInventory() {
        closeInventory(true);
    }

    /**
     * Closes the inventory if it's open, only sending the close inventory packets if the arg is true.
     *
     * @param sendClosePacket Whether to send the close inventory event, packet, etc.
     */
    private void closeInventory(final boolean sendClosePacket) {
        if (!this.open) {
            return;
        }
        this.open = false;
        HandlerList.unregisterAll(this.inventoryListener);
        if (ServerUtils.hasSpecificUpdate("1_11")) {
            HandlerList.unregisterAll(this.typingListener);
        } else {
            HandlerList.unregisterAll(this.typingListener_LEGACY);
        }
        if (sendClosePacket) {
            this.container.handleInventoryCloseEvent(this.player);
            this.container.setActiveContainerDefault(this.player);
            this.container.sendPacketCloseWindow(this.player);
        }
        if (this.closeListener != null) {
            this.closeListener.accept(StateSnapshot.fromQuery(this));
        }
    }

    /**
     * Updates the title of the Query to the new one.
     *
     * @param literalTitle       The title to use as literal text.
     * @param preserveRenameText Whether to preserve the entered rename text.
     * @throws IllegalArgumentException when literalTitle is null.
     * @see Builder#title(String)
     */
    public void setTitle(final String literalTitle, final boolean preserveRenameText) {
        Validate.notNull(literalTitle, "literalTitle cannot be null");
        setTitle(ReflectionUtils.literalChatComponent(literalTitle), preserveRenameText);
    }

    /**
     * Updates the title of the Query to the new one.
     *
     * @param json               The json used to parse into a rich chat component.
     * @param preserveRenameText Whether to preserve the entered rename text.
     * @throws IllegalArgumentException when json is null.
     * @see Builder#jsonTitle(String).
     */
    public void setJsonTitle(final String json, final boolean preserveRenameText) {
        Validate.notNull(json, "json cannot be null");
        setTitle(ReflectionUtils.jsonChatComponent(json), preserveRenameText);
    }

    /**
     * Updates the title of the Query to the new one.
     *
     * @param title              The title as a NMS ChatComponent.
     * @param preserveRenameText Whether to preserve the entered rename text.
     */
    private void setTitle(final Object title, final boolean preserveRenameText) {
        if (!ServerUtils.hasSpecificUpdate("1_14")) {
            return;
        }
        String renameText = this.container.getRenameText();
        this.container.sendPacketOpenWindow(this.player, title);
        if (preserveRenameText) {
            this.container.setRenameText(renameText == null ? "" : renameText);
        }
    }

    /**
     * Returns the Bukkit inventory for this container.
     *
     * @return the {@link Inventory} for this container.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * A handler that is called when the user clicks a slot. The
     * {@link Integer} is the slot number corresponding to {@link Slot}, the
     * {@link StateSnapshot} contains information about the current state of the anvil,
     * and the response is a {@link CompletableFuture} that will eventually return a
     * list of {@link ResponseAction} to execute in the order that they are supplied.
     */
    @FunctionalInterface
    public interface ClickHandler extends BiFunction<Integer, StateSnapshot, CompletableFuture<List<ResponseAction>>> {
    }

    /**
     * An action to run in response to a player clicking the output slot in the GUI. This interface is public
     * and permits you, the developer, to add additional response features easily to your custom Query's.
     */
    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface ResponseAction extends BiConsumer<Query, Player> {

        /**
         * Replace the input text box value with the provided text value.
         * <p>
         * Before using this method, it must be verified by the caller that items are either in
         * {@link Slot#INPUT_LEFT} or {@link Slot#OUTPUT} present.
         *
         * @param text The text to write in the input box.
         * @return The {@link ResponseAction} to achieve the text replacement.
         * @throws IllegalArgumentException when the text is null.
         * @throws IllegalStateException    when the slots {@link Slot#INPUT_LEFT} and {@link Slot#OUTPUT} are <code>null</code>.
         */
        static ResponseAction replaceInputText(final String text) {
            Validate.notNull(text, "text cannot be null");
            final String inputText = "-->" + text;
            return (query, player) -> {
                query.container.setLeftText(text);
                ItemStack item = query.getInventory().getItem(Slot.INPUT_LEFT);
                if (item == null) {
                    item = query.getInventory().getItem(Slot.OUTPUT);
                }
                if (item == null) {
                    throw new IllegalStateException("replaceInputText can only be used if slots OUTPUT or INPUT_LEFT are not empty");
                }
                final ItemStack cloned = item.clone();
                final ItemMeta meta = cloned.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(inputText);
                    cloned.setItemMeta(meta);
                }
                query.container.setAction(true);
                {
                    query.getInventory().setItem(Slot.INPUT_LEFT, cloned);
                }
            };
        }

        /**
         * Replace the input text box and output item text value with the provided text values.
         * <p>
         * Before using this method, it must be verified by the caller that items are either in
         * {@link Slot#INPUT_LEFT} or {@link Slot#OUTPUT} present.
         *
         * @param inputText - The text to write in the input box.
         * @param outText   - The text to write on the output item.
         * @return The {@link ResponseAction} to achieve the text replacement.
         * @throws IllegalArgumentException when the text is null.
         * @throws IllegalStateException    when the slots {@link Slot#INPUT_LEFT} and {@link Slot#OUTPUT} are <code>null</code>.
         */
        static ResponseAction replaceInputText(final String inputText, final String outText) {
            Validate.notNull(inputText, "inputText cannot be null");
            Validate.notNull(outText, "outText cannot be null");
            final String leftText = "-->" + inputText;
            return (query, player) -> {
                query.container.setLeftText(inputText);
                query.container.setOutText(outText);
                ItemStack leftItem = query.getInventory().getItem(Slot.INPUT_LEFT);
                ItemStack outItem = query.getInventory().getItem(Slot.OUTPUT);
                if (leftItem == null) {
                    leftItem = query.getInventory().getItem(Slot.OUTPUT);
                }
                if (outItem == null) {
                    outItem = query.getInventory().getItem(Slot.INPUT_LEFT);
                }
                if (leftItem == null) {
                    throw new IllegalStateException("replaceInputText can only be used if slots OUTPUT or INPUT_LEFT are not empty");
                }
                final ItemStack leftItemCloned = leftItem.clone();
                final ItemMeta leftMeta = leftItemCloned.getItemMeta();
                if (leftMeta != null) {
                    leftMeta.setDisplayName(leftText);
                    leftItemCloned.setItemMeta(leftMeta);
                }
                query.container.setAction(true);
                {
                    query.getInventory().setItem(Slot.INPUT_LEFT, leftItemCloned);
                    /*
                        Prevents the output from ghosting when double-clicking.
                     */
                    if (!ServerUtils.hasSpecificUpdate("1_11") && outItem != null) {
                        final ItemStack outItemCloned = outItem.clone();
                        final ItemMeta outMeta = outItemCloned.getItemMeta();
                        if (outMeta != null) {
                            outMeta.setDisplayName(outText + query.container.getSpacers());
                            outItemCloned.setItemMeta(outMeta);
                        }
                        query.getInventory().setItem(Slot.OUTPUT, outItemCloned);
                    }
                }
            };
        }

        /**
         * Updates the title of the Query to the new one.
         *
         * @param literalTitle       The title to use as literal text.
         * @param preserveRenameText Whether to preserve the entered rename text.
         * @throws IllegalArgumentException when literalTitle is null.
         * @see Builder#title(String).
         */
        static ResponseAction updateTitle(final String literalTitle, final boolean preserveRenameText) {
            Validate.notNull(literalTitle, "literalTitle cannot be null");
            return (query, player) -> query.setTitle(literalTitle, preserveRenameText);
        }

        /**
         * Updates the title of the Query to the new one.
         *
         * @param json               The json used to parse into a rich chat component.
         * @param preserveRenameText Whether to preserve the entered rename text.
         * @throws IllegalArgumentException when json is null.
         * @see Builder#jsonTitle(String).
         */
        static ResponseAction updateJsonTitle(final String json, final boolean preserveRenameText) {
            Validate.notNull(json, "json cannot be null");
            return (query, player) -> query.setJsonTitle(json, preserveRenameText);
        }

        /**
         * Open another inventory.
         *
         * @param otherInventory The inventory to open.
         * @return The {@link ResponseAction} to achieve the inventory open.
         * @throws IllegalArgumentException when the otherInventory is null.
         */
        static ResponseAction openInventory(final Inventory otherInventory) {
            Validate.notNull(otherInventory, "otherInventory cannot be null");
            return (query, player) -> player.openInventory(otherInventory);
        }

        /**
         * Close the Query.
         *
         * @return The {@link ResponseAction} to achieve closing the Query.
         */
        static ResponseAction close() {
            return (query, player) -> query.closeInventory();
        }

        /**
         * Run the provided runnable.
         *
         * @param runnable The runnable to run.
         * @return The {@link ResponseAction} to achieve running the runnable.
         * @throws IllegalArgumentException when the runnable is null.
         */
        static ResponseAction run(final Runnable runnable) {
            Validate.notNull(runnable, "runnable cannot be null");
            return (query, player) -> runnable.run();
        }
    }


    /**
     * A builder class for an {@link Query} object.
     */
    @SuppressWarnings("unused")
    public static class Builder {

        private Executor mainThreadExecutor;
        private Consumer<StateSnapshot> closeListener;
        private boolean concurrentClickHandlerExecution = false;
        private ClickHandler clickHandler;
        private boolean preventClose = false;
        private Set<Integer> interactableSlots = Collections.emptySet();
        private Object titleComponent = ReflectionUtils.literalChatComponent("Repair & Name");
        private String itemText;
        private ItemStack itemLeft;
        private ItemStack itemRight;
        private ItemStack itemOutput;

        /**
         * Set a custom main server thread executor.
         *
         * @param executor The executor to run tasks on.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException when the executor is null.
         */
        public Builder mainThreadExecutor(final Executor executor) {
            Validate.notNull(executor, "Executor cannot be null");
            this.mainThreadExecutor = executor;
            return this;
        }

        /**
         * Prevents the closing of the container by the user.
         *
         * @return The {@link Builder} instance.
         */
        public Builder preventClose() {
            this.preventClose = true;
            return this;
        }

        /**
         * Permit the user to modify (take items in and out) the slot numbers provided.
         *
         * @param slots A param for the slot numbers. You can avoid relying on magic constants by using the {@link Query.Slot} class.
         * @return The {@link Builder} instance.
         */
        public Builder interactableSlots(final int... slots) {
            final Set<Integer> newValue = new HashSet<>();
            for (int slot : slots) {
                newValue.add(slot);
            }
            this.interactableSlots = newValue;
            return this;
        }

        /**
         * Listens for when the inventory is closed.
         *
         * @param closeListener An {@link Consumer} that is called when the container is closed.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException when the closeListener is null.
         */
        public Builder onClose(final Consumer<StateSnapshot> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        /**
         * Do an action when a slot is clicked in the inventory.
         * <p>
         * The ClickHandler is only called when the previous execution of the ClickHandler has finished.
         * To alter this behaviour use {@link #allowConcurrentClickHandlerExecution()}.
         *
         * @param clickHandler A {@link ClickHandler} that is called when the user clicks a slot. The
         *                     {@link Integer} is the slot number corresponding to {@link Slot}, the
         *                     {@link StateSnapshot} contains information about the current state of the anvil,
         *                     and the response is a {@link CompletableFuture} that will eventually return a
         *                     list of {@link ResponseAction} to execute in the order that they are supplied.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException when the function supplied is null.
         */
        public Builder onClickAsync(final ClickHandler clickHandler) {
            Validate.notNull(clickHandler, "click function cannot be null");
            this.clickHandler = clickHandler;
            return this;
        }

        /**
         * By default, the {@link #onClickAsync(ClickHandler) async click handler} will not run concurrently
         * and instead wait for the previous {@link CompletableFuture} to finish before executing it again.
         * <p>
         * If this trait is desired, it can be enabled by calling this method but may lead to inconsistent
         * behaviour if not handled properly.
         *
         * @return The {@link Builder} instance.
         */
        public Builder allowConcurrentClickHandlerExecution() {
            this.concurrentClickHandlerExecution = true;
            return this;
        }

        /**
         * Do an action when a slot is clicked in the inventory.
         *
         * @param clickHandler A {@link BiFunction} that is called when the user clicks a slot. The
         *                     {@link Integer} is the slot number corresponding to {@link Slot}, the
         *                     {@link StateSnapshot} contains information about the current state of the anvil,
         *                     and the response is a list of {@link ResponseAction} to execute in the order
         *                     that they are supplied.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException when the function supplied is null.
         */
        public Builder onClick(final BiFunction<Integer, StateSnapshot, List<ResponseAction>> clickHandler) {
            Validate.notNull(clickHandler, "click function cannot be null");
            this.clickHandler = (slot, stateSnapshot) -> CompletableFuture.completedFuture(clickHandler.apply(slot, stateSnapshot));
            return this;
        }

        /**
         * Sets the initial item-text that is displayed to the user.
         * <br><br>
         * If the usage of Adventure Components is desired, you must create an item, set the display name of it
         * and put it into the Query via {@link #itemLeft(ItemStack)} manually.
         *
         * @param text The initial name of the item in the anvil.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException if the text is null.
         */
        public Builder text(final String text) {
            Validate.notNull(text, "Text cannot be null");
            this.itemText = "->" + text;
            return this;
        }

        /**
         * Sets the Query title that is to be displayed to the user.
         * <br>
         * The provided title will be treated as literal text.
         *
         * @param title The title that is to be displayed to the user.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException if the title is null.
         */
        public Builder title(final String title) {
            Validate.notNull(title, "title cannot be null");
            this.titleComponent = ReflectionUtils.literalChatComponent(title);
            return this;
        }

        /**
         * Sets the Query title that is to be displayed to the user.
         * <br>
         * The provided json will be parsed into rich chat components.
         *
         * @param json The title that is to be displayed to the user.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException if the title is null.
         */
        public Builder jsonTitle(final String json) {
            Validate.notNull(json, "json cannot be null");
            this.titleComponent = ReflectionUtils.jsonChatComponent(json);
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the first slot.
         *
         * @param item The {@link ItemStack} to be put in the first slot.
         * @return The {@link Builder} instance.
         * @throws IllegalArgumentException if the {@link ItemStack} is null.
         */
        public Builder itemLeft(final ItemStack item) {
            Validate.notNull(item, "item cannot be null");
            this.itemLeft = item;
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the second slot.
         *
         * @param item The {@link ItemStack} to be put in the second slot.
         * @return The {@link Builder} instance.
         */
        public Builder itemRight(final ItemStack item) {
            this.itemRight = item;
            return this;
        }

        /**
         * Sets the {@link ItemStack} to be put in the output slot.
         *
         * @param item The {@link ItemStack} to be put in the output slot.
         * @return The {@link Builder} instance.
         */
        public Builder itemOutput(final ItemStack item) {
            this.itemOutput = item;
            return this;
        }

        /**
         * Creates the container and opens it for the player.
         *
         * @param player The {@link Player} the container should open for.
         * @return The {@link Query} instance from this builder.
         * @throws IllegalArgumentException when the onClick function, plugin, or player is null.
         */
        public Query open(final Player player) {
            Validate.notNull(this.clickHandler, "click handler cannot be null");
            Validate.notNull(player, "Player cannot be null");
            if (this.itemText != null) {
                if (this.itemLeft == null) {
                    this.itemLeft = new ItemStack(Material.PAPER);
                }
                ItemMeta paperMeta = this.itemLeft.getItemMeta();
                if (paperMeta != null) {
                    paperMeta.setDisplayName(this.itemText);
                    this.itemLeft.setItemMeta(paperMeta);
                }
            }
            if (this.mainThreadExecutor == null) {
                this.mainThreadExecutor = task -> Bukkit.getScheduler().runTask(Core.getCore().getPlugin(), task);
            }
            final Query query = new Query(
                    player,
                    this.mainThreadExecutor,
                    this.titleComponent,
                    new ItemStack[]{this.itemLeft, this.itemRight, this.itemOutput},
                    this.preventClose,
                    this.interactableSlots,
                    this.closeListener,
                    this.concurrentClickHandlerExecution,
                    this.clickHandler);
            query.openInventory();
            return query;
        }
    }

    /**
     * Represents a response when the player clicks the output item in the container.
     *
     * @deprecated Since 1.6.2, use {@link ResponseAction}.
     */
    @Deprecated
    public static class Response {
        /**
         * Returns an {@link Response} object for when the container is too close.
         *
         * @return An {@link Response} object for when the container is to display text to the user.
         * @deprecated Since 1.6.2, use {@link ResponseAction#close()}.
         */
        @Deprecated
        public static List<ResponseAction> close() {
            return Collections.singletonList(ResponseAction.close());
        }

        /**
         * Returns an {@link Response} object for when the container is to display text to the user.
         *
         * @param text The text that is to be displayed to the user.
         * @return A list containing the {@link ResponseAction} for legacy compat.
         * @deprecated Since 1.6.2, use {@link ResponseAction#replaceInputText(String)}.
         */
        @Deprecated
        public static List<ResponseAction> text(final String text) {
            return Collections.singletonList(ResponseAction.replaceInputText(text));
        }

        /**
         * Returns an {@link Response} object for when the GUI should open the provided inventory.
         *
         * @param inventory The inventory to open.
         * @return A list containing the {@link ResponseAction} for legacy compat.
         * @deprecated Since 1.6.2, use {@link ResponseAction#openInventory(Inventory)}.
         */
        @Deprecated
        public static List<ResponseAction> openInventory(final Inventory inventory) {
            return Collections.singletonList(ResponseAction.openInventory(inventory));
        }
    }

    /**
     * Class wrapping the magic constants of slot numbers in a container.
     */
    public static class Slot {

        /**
         * The slot on the far left, where the first input is inserted. An {@link ItemStack} is always inserted here to be renamed.
         */
        public static final int INPUT_LEFT = 0;
        /**
         * Not used, but in a real anvil you are able to put the second item you want to combine here.
         */
        public static final int INPUT_RIGHT = 1;
        /**
         * The output slot, where an item is put when two items are combined from {@link #INPUT_LEFT} and {@link #INPUT_RIGHT} or {@link #INPUT_LEFT} is renamed.
         */
        public static final int OUTPUT = 2;
        private static final int[] values = new int[]{Slot.INPUT_LEFT, Slot.INPUT_RIGHT, Slot.OUTPUT};

        /**
         * Get all anvil slot values.
         *
         * @return The array containing all possible anvil slots.
         */
        public static int[] values() {
            return values;
        }
    }

    /**
     * Represents a snapshot of the state of a Query.
     */
    @SuppressWarnings("unused")
    public static final class StateSnapshot {

        /**
         * The {@link ItemStack} in the Query slots.
         */
        private final ItemStack leftItem, rightItem, outputItem;
        /**
         * The {@link Player} that clicked the output slot.
         */
        private final Player player;

        /**
         * The event parameter constructor.
         *
         * @param leftItem   The left item in the combine slot of the Query.
         * @param rightItem  The right item in the combine slot of the Query.
         * @param outputItem The item that would have been outputted, when the items would have been combined.
         * @param player     The player that clicked the output slot.
         */
        public StateSnapshot(final ItemStack leftItem, final ItemStack rightItem, final ItemStack outputItem, final Player player) {
            this.leftItem = leftItem;
            this.rightItem = rightItem;
            this.outputItem = outputItem;
            this.player = player;
        }

        /**
         * Create an {@link StateSnapshot} from the current state of an {@link Query}.
         *
         * @param query The instance to take the snapshot of.
         * @return The snapshot.
         */
        private static StateSnapshot fromQuery(final Query query) {
            final Inventory inventory = query.getInventory();
            return new StateSnapshot(
                    ItemHandler.itemNotNull(inventory.getItem(Slot.INPUT_LEFT)).clone(),
                    ItemHandler.itemNotNull(inventory.getItem(Slot.INPUT_RIGHT)).clone(),
                    ItemHandler.itemNotNull(inventory.getItem(Slot.OUTPUT)).clone(),
                    query.player);
        }

        /**
         * It returns the item in the left combine slot of the gui.
         *
         * @return The leftItem.
         */
        public ItemStack getLeftItem() {
            return this.leftItem;
        }

        /**
         * It returns the item in the right combine slot of the gui.
         *
         * @return The rightItem.
         */
        public ItemStack getRightItem() {
            return this.rightItem;
        }

        /**
         * It returns the output item that would have been the result by combining the left and right one.
         *
         * @return The outputItem.
         */
        public ItemStack getOutputItem() {
            return this.outputItem;
        }

        /**
         * It returns the player that clicked onto the output slot.
         *
         * @return The player.
         */
        public Player getPlayer() {
            return this.player;
        }

        /**
         * It returns the text the player typed into the rename field.
         *
         * @return The text of the rename field.
         */
        public String getText() {
            String displayName = "";
            final ItemStack stackFetch = this.leftItem;
            if (stackFetch.hasItemMeta()) {
                final ItemMeta itemMeta = stackFetch.getItemMeta();
                if (itemMeta != null) {
                    displayName = itemMeta.getDisplayName();
                    displayName = displayName.substring((displayName.startsWith("--> ") ? 4 : displayName.startsWith("-->") ? 3 : displayName.startsWith("-> ") ? 3 : displayName.startsWith("->") ? 2 : displayName.startsWith("--") ? 2 : displayName.startsWith("-") ? 1 : 0));
                    displayName = displayName.trim();
                }
            }
            return displayName;
        }
    }

    /**
     * Simply holds the listener(s) for the typing actions inside the anvil.
     */
    private class PrepareAnvil implements Listener {

        /**
         * Handles the anvil input event for the virtualInventory.
         *
         * @param event - {@link PrepareAnvilEvent}.
         */
        @EventHandler
        public final void onPrepareAnvil(final PrepareAnvilEvent event) {
            if (event.getInventory().equals(inventory)) {
                final String renameText = event.getInventory().getRenameText();
                container.handleTyping(renameText);
                {
                    event.setResult(container.getResult((Player)event.getView().getPlayer(), renameText));
                    {
                        container.removeCost(event.getInventory());
                        container.setAction(false);
                    }
                }
            }
        }
    }

    /**
     * Simply holds the listener(s) for the typing actions inside the anvil.
     * This uses packets so the use is limited to server versions below 1.9.
     *
     */
    private class PrepareAnvil_LEGACY implements Listener {

        /**
         * Handles the anvil input event for the virtualInventory.
         * This uses packets so the use is limited to server versions 1.8 - 1.10.
         * <p>
         * SchedulerUtils#runAsync is required to prevent item ghosting .
         *
         * @param event - {@link me.RockinChaos.core.utils.protocol.events.PrepareAnvilEvent}.
         */
        @EventHandler
        public final void onPrepareAnvil_LEGACY(final me.RockinChaos.core.utils.protocol.events.PrepareAnvilEvent event) {
            if (event.getInventory().equals(inventory)) {
                final String renameText = event.getRenameText();
                container.handleTyping(renameText);
                {
                    SchedulerUtils.runAsync(() -> {
                        event.setResult(container.getResult(event.getPlayer(), renameText));
                        {
                            container.setAction(false);
                            LegacyAPI.updateInventory(event.getPlayer());
                        }
                    });
                }
            }
        }
    }

    /**
     * Simply holds the listeners for the GUI.
     */
    private class AnvilInventory implements Listener {

        /**
         * Boolean storing the running status of the latest click handler to prevent double execution.
         * All accesses to this boolean will be from the main server thread, except for the rare event
         * that the plugin is disabled and the mainThreadExecutor throws an exception.
         */
        private boolean clickHandlerRunning = false;

        /**
         * Handles the click action for the virtualInventory.
         *
         * @param event - {@link InventoryClickEvent}.
         */
        @EventHandler
        public void onInventoryClick(final InventoryClickEvent event) {
            if (!event.getInventory().equals(inventory)) {
                return;
            }
            final Player clicker = (Player) event.getWhoClicked();
            final Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory != null && clickedInventory.equals(clicker.getInventory()) && event.getClick().equals(ClickType.DOUBLE_CLICK)) {
                event.setCancelled(true);
                return;
            }
            final int rawSlot = event.getRawSlot();
            if (rawSlot < 3 || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                event.setCancelled(!interactableSlots.contains(rawSlot));
                if (this.clickHandlerRunning && !concurrentClickHandlerExecution) {
                    return;
                }
                final CompletableFuture<List<ResponseAction>> actionsFuture = clickHandler.apply(rawSlot, StateSnapshot.fromQuery(Query.this));
                final Consumer<List<ResponseAction>> actionsConsumer = actions -> {
                    for (final ResponseAction action : actions) {
                        action.accept(Query.this, clicker);
                    }
                };
                if (actionsFuture.isDone()) {
                    actionsFuture.thenAccept(actionsConsumer).join();
                } else {
                    this.clickHandlerRunning = true;
                    actionsFuture.thenAcceptAsync(actionsConsumer, mainThreadExecutor).handle((results, exception) -> {
                        if (exception != null) {
                            ServerUtils.logSevere("An exception occurred in the Query clickHandler");
                            ServerUtils.sendSevereThrowable(exception);
                        }
                        this.clickHandlerRunning = false;
                        return null;
                    });
                }
            }
        }

        /**
         * Handles the item drag action for the virtualInventory.
         *
         * @param event - {@link InventoryDragEvent}.
         */
        @EventHandler
        public void onInventoryDrag(final InventoryDragEvent event) {
            if (event.getInventory().equals(inventory)) {
                for (int slot : Slot.values()) {
                    if (event.getRawSlots().contains(slot)) {
                        event.setCancelled(!interactableSlots.contains(slot));
                        break;
                    }
                }
            }
        }

        /**
         * Handles the close inventory action for the virtualInventory.
         *
         * @param event - {@link InventoryCloseEvent}.
         */
        @EventHandler
        public void onInventoryClose(final InventoryCloseEvent event) {
            if (open && event.getInventory().equals(inventory)) {
                closeInventory(false);
                {
                    for (ItemStack item : event.getInventory().getContents()) {
                        event.getInventory().remove(item);
                    }
                }
                {
                    if (preventClose) {
                        mainThreadExecutor.execute(Query.this::openInventory);
                    }
                }
            }
        }
    }
}