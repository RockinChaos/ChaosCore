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
package me.RockinChaos.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that simplifies reflection in Bukkit plugins.
 */
@SuppressWarnings("unused")
public final class ReflectionUtils {
    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    private static final String MC_PREFIX = "net.minecraft";
    private static final String VERSION = OBC_PREFIX.replace("org.bukkit.craftbukkit", "").replace(".", "");
    private static final boolean MC_REMAPPED = (Integer.parseInt(VERSION.replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "")) >= 117);
    private static final Pattern MATCH_VARIABLE = Pattern.compile("\\{([^}]+)}");

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target - the target type.
     * @param name   - the name of the field, or NULL to ignore.
     * @return The field accessor.
     */
    public static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull Class<?> target, final @Nonnull String name) {
        return getField(target, name, null, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target    - the target type.
     * @param name      - the name of the field, or NULL to ignore.
     * @param fieldType - a compatible field type.
     * @return The field accessor.
     */
    public static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull Class<?> target, final @Nullable String name, final @Nonnull Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param name      - the name of the field, or NULL to ignore.
     * @param fieldType - a compatible field type.
     * @return The field accessor.
     */
    public static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull String className, final @Nonnull String name, final @Nonnull Class<T> fieldType) {
        return getField(getClass(className), name, fieldType, 0);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target    - the target type.
     * @param fieldType - a compatible field type.
     * @param index     - the number of compatible fields to skip.
     * @return The field accessor.
     */
    public static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull Class<?> target, @Nonnull final Class<T> fieldType, final int index) {
        return getField(target, null, fieldType, index);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param fieldType - a compatible field type.
     * @param index     - the number of compatible fields to skip.
     * @return The field accessor.
     */
    public static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull String className, final @Nonnull Class<T> fieldType, final int index) {
        return getField(getClass(className), fieldType, index);
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target    - the targeted class.
     * @param fieldType - a compatible field type.
     * @param index     - the number of compatible fields to skip.
     * @return The field accessor.
     */
    private static @Nonnull <T> FieldAccessor<T> getField(final @Nonnull Class<?> target, final @Nullable String name, @Nullable Class<T> fieldType, int index) {
        for (final Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && (fieldType == null || fieldType.isAssignableFrom(field.getType())) && index-- <= 0) {
                field.setAccessible(true);
                return new FieldAccessor<T>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public T get(Object target) {
                        try {
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public void set(Object target, Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public boolean hasField(Object target) {
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }
                };
            }
        }
        if (target.getSuperclass() != null)
            return getField(target.getSuperclass(), name, fieldType, index);
        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    /**
     * Search for the first publicly and privately defined method of the given name and parameter count.
     *
     * @param className  - lookup name of the class, see {@link #getClass(String)}.
     * @param methodName - the method name, or NULL to skip.
     * @param params     - the expected parameters.
     * @return An object that invokes this specific method.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static @Nonnull MethodInvoker getMethod(final @Nonnull String className, final @Nonnull String methodName, final @Nonnull Class<?>... params) {
        return getTypedMethod(getClass(className), methodName, null, params);
    }

    /**
     * Search for the first publicly and privately defined method of the given name and parameter count.
     *
     * @param clazz      - a class to start with.
     * @param methodName - the method name, or NULL to skip.
     * @param params     - the expected parameters.
     * @return An object that invokes this specific method.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static @Nonnull MethodInvoker getMethod(final @Nonnull Class<?> clazz, final @Nonnull String methodName, final @Nonnull Class<?>... params) {
        return getTypedMethod(clazz, methodName, null, params);
    }

    /**
     * Search for the first publicly and privately defined method of the given name and parameter count.
     *
     * @param clazz      - a class to start with.
     * @param methodName - the method name, or NULL to skip.
     * @param returnType - the expected return type, or NULL to ignore.
     * @param params     - the expected parameters.
     * @return An object that invokes this specific method.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static @Nonnull MethodInvoker getTypedMethod(final @Nonnull Class<?> clazz, final @Nonnull String methodName, final @Nullable Class<?> returnType, final @Nonnull Class<?>... params) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && (returnType == null || method.getReturnType().equals(returnType)) && Arrays.equals(method.getParameterTypes(), params)) {
                method.setAccessible(true);
                return (target, arguments) -> {
                    try {
                        return method.invoke(target, arguments);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot invoke method " + method, e);
                    }
                };
            }
        }
        if (clazz.getSuperclass() != null)
            return getMethod(clazz.getSuperclass(), methodName, params);
        throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
    }

    /**
     * Attempts to get the PlayerField.
     *
     * @param player - the player being referenced.
     * @param field  - the field being fetched.
     * @return The PlayerField.
     */
    public static @Nonnull Object getPlayerField(final @Nonnull Player player, final @Nonnull String field) {
        try {
            final Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return craftPlayer.getClass().getField(field).get(craftPlayer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke player field " + field, e);
        }
    }

    /**
     * Attempts to invoke a specific method given parameters.
     *
     * @param methodName - the method name, or NULL to skip.
     * @param params     - the expected parameters.
     * @return The newly created Object.
     */
    public static @Nonnull Object invokeMethod(final @Nonnull String methodName, final @Nonnull Object params) {
        try {
            return getMethod(params.getClass(), methodName).invoke(params);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke method " + methodName, e);
        }
    }

    /**
     * Search for the first publicly and privately defined constructor of the given name and parameter count.
     *
     * @param className - lookup name of the class, see {@link #getClass(String)}.
     * @param params    - the expected parameters.
     * @return An object that invokes this constructor.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static @Nonnull ConstructorInvoker getConstructor(final @Nonnull String className, final @Nonnull Class<?>... params) {
        return getConstructor(getClass(className), params);
    }

    /**
     * Search for the first publicly and privately defined constructor of the given name and parameter count.
     *
     * @param clazz  - a class to start with.
     * @param params - the expected parameters.
     * @return An object that invokes this constructor.
     * @throws IllegalStateException If we cannot find this method.
     */
    public static @Nonnull ConstructorInvoker getConstructor(final @Nonnull Class<?> clazz, final @Nonnull Class<?>... params) {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return arguments -> {
                    try {
                        return constructor.newInstance(arguments);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot invoke constructor " + constructor, e);
                    }
                };
            }
        }
        throw new IllegalStateException(String.format("Unable to find constructor for %s (%s).", clazz, Arrays.asList(params)));
    }

    /**
     * Retrieve a class from its full name, without knowing its type on compile time.
     * <p>
     * This is useful when looking up fields by a NMS or OBC type.
     * <p>
     *
     * @param lookupName - the class name with variables.
     * @return The class.
     */
    @SuppressWarnings("unchecked")
    public static @Nonnull Class<Object> getUntypedClass(final @Nonnull String lookupName) {
        return (Class<Object>) getClass(lookupName);
    }

    /**
     * Retrieve a version referenced in the packages.
     */
    public static @Nonnull String getServerVersion() {
        return VERSION;
    }

    /**
     * Retrieve a class from its full name.
     * <p>
     * Strings enclosed with curly brackets - such as {TEXT} - will be replaced according to the following table:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Variable</th>
     * <th>Content</th>
     * </tr>
     * <tr>
     * <td>{nms}</td>
     * <td>Actual package name of net.minecraft.server.VERSION</td>
     * </tr>
     * <tr>
     * <td>{obc}</td>
     * <td>Actual package name of org.bukkit.craftbukkit.VERSION</td>
     * </tr>
     * <tr>
     * <td>{version}</td>
     * <td>The current Minecraft package VERSION, if any.</td>
     * </tr>
     * </table>
     *
     * @param lookupName - the class name with variables.
     * @return The looked up class.
     * @throws IllegalArgumentException If a variable or class could not be found.
     */
    public static @Nonnull Class<?> getClass(final @Nonnull String lookupName) {
        return getCanonicalClass(expandVariables(lookupName));
    }

    /**
     * Retrieve a class in the net.minecraft.server.VERSION.* package.
     *
     * @param name - the name of the class, excluding the package.
     * @throws IllegalArgumentException If the class doesn't exist.
     */
    public static @Nonnull Class<?> getMinecraftClass(final @Nonnull String name) {
        if (MC_REMAPPED) {
            try {
                return getMinecraftTag(name);
            } catch (Exception e) {
                return getCanonicalClass(NMS_PREFIX + "." + name);
            }
        } else {
            return getCanonicalClass(NMS_PREFIX + "." + name);
        }
    }

    /**
     * Retrieve a class in the org.bukkit.craftbukkit.VERSION.* package.
     *
     * @param name - the name of the class, excluding the package.
     * @throws IllegalArgumentException If the class doesn't exist.
     */
    public static @Nonnull Class<?> getCraftBukkitClass(final @Nonnull String name) {
        return getCanonicalClass(OBC_PREFIX + "." + name);
    }

    /**
     * Retrieve a class in the org.bukkit.* package.
     *
     * @param name - the name of the class, excluding the package.
     * @throws IllegalArgumentException If the class doesn't exist.
     */
    public static @Nonnull Class<?> getBukkitClass(final @Nonnull String name) {
        return getCanonicalClass("org.bukkit." + name);
    }

    /**
     * Retrieve a class by its canonical name.
     *
     * @param canonicalName - the canonical name.
     * @return The class.
     */
    public static @Nonnull Class<?> getCanonicalClass(final @Nonnull String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    /**
     * Sends a PacketPlayOutSetSlot Packet to the specified player.
     *
     * @param player - The player receiving the packet.
     * @param item   - The ItemStack to be sent to the slot.
     * @param index  - The slot to have the item sent.
     */
    public static void sendPacketPlayOutSetSlot(final @Nonnull Player player, final @Nullable ItemStack item, int index, int windowId) throws Exception {
        Class<?> itemStack = getMinecraftClass("ItemStack");
        Object nms = getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
        final Class<?> playOutSlot = ReflectionUtils.getMinecraftClass("PacketPlayOutSetSlot");
        Object packet;
        if (MC_REMAPPED) {
            try {
                packet = playOutSlot.getConstructor(int.class, int.class, int.class, itemStack).newInstance(windowId, 0, index, itemStack.cast(nms));
            } catch (NoSuchMethodException e) {
                packet = playOutSlot.getConstructor(int.class, int.class, itemStack).newInstance(0, index, itemStack.cast(nms));
            }
        } else {
            packet = playOutSlot.getConstructor(int.class, int.class, itemStack).newInstance(0, index, itemStack.cast(nms));
        }
        sendPacket(player, packet);
    }

    /**
     * Sends a Packet Object to the specified player.
     *
     * @param player - The player receiving the packet.
     * @param packet - The Packet Object being sent.
     */
    public static void sendPacket(final @Nonnull Player player, final @Nonnull Object packet) throws Exception {
        Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object playerHandle = nmsPlayer.getClass().getField(MinecraftField.PlayerConnection.getField()).get(nmsPlayer);
        Class<?> packetClass = getMinecraftClass("Packet");
        playerHandle.getClass().getMethod(MinecraftMethod.sendPacket.getMethod(playerHandle.getClass(), packetClass), packetClass).invoke(playerHandle, packet);
    }

    public static @Nullable Object literalChatComponent(final @Nonnull String content) {
        try {
            if (ServerUtils.hasSpecificUpdate("1_19")) {
                return getMinecraftClass("IChatBaseComponent").getMethod("b", String.class).invoke(null, content);
            } else {
                return getMinecraftClass("ChatComponentText").getConstructor(String.class).newInstance(content);
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return null;
    }

    public static @Nullable Object jsonChatComponent(final @Nonnull String json) {
        try {
            return getMinecraftClass("IChatBaseComponent").getMethod("a", String.class).invoke(null, json);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return null;
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    public static @Nullable Object getEntity(final @Nonnull Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
        return null;
    }

    /**
     * Expand variables such as "{nms}" and "{obc}" to their corresponding packages.
     *
     * @param name - the full name of the class.
     * @return The expanded string.
     */
    private static @Nonnull String expandVariables(final @Nonnull String name) {
        StringBuffer output = new StringBuffer();
        Matcher matcher = MATCH_VARIABLE.matcher(name);
        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement;
            if ("nms".equalsIgnoreCase(variable)) {
                if (MC_REMAPPED) {
                    try {
                        String forClass = ReflectionUtils.getMinecraftClass("PlayerConnection").getCanonicalName();
                        if (forClass != null) {
                            replacement = MC_PREFIX;
                        } else {
                            replacement = NMS_PREFIX;
                        }
                    } catch (Exception e) {
                        replacement = NMS_PREFIX;
                    }
                } else {
                    replacement = NMS_PREFIX;
                }
            } else if ("obc".equalsIgnoreCase(variable)) {
                replacement = OBC_PREFIX;
            } else if ("version".equalsIgnoreCase(variable)) {
                replacement = VERSION;
            } else {
                throw new IllegalArgumentException("Unknown variable: " + variable);
            }
            if (!replacement.isEmpty() && matcher.end() < name.length() && name.charAt(matcher.end()) != '.')
                replacement += ".";
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));

        }
        matcher.appendTail(output);
        return output.toString();
    }

    /**
     * Gets the correct location of the searchable tag.
     *
     * @param name - The Tag being located.
     * @return The located searchable tag.
     */
    public static @Nonnull Class<?> getMinecraftTag(final @Nonnull String name) {
        for (MinecraftTags tag : MinecraftTags.values()) {
            if (tag.name().equalsIgnoreCase(name)) {
                return getCanonicalClass(MC_PREFIX + tag.tag + "." + name);
            }
        }
        return getCanonicalClass(NMS_PREFIX + "." + name);
    }

    /**
     * Checks if the Server is running a remapped version of NBT.
     *
     * @return If the Server is remapped.
     */
    public static boolean remapped() {
        return MC_REMAPPED;
    }

    /**
     * Searchable methods that no longer require NBT Reflections.
     */
    public enum MinecraftMethod {
        add("add", (ServerUtils.hasSpecificUpdate("1_18") ? "c" : "add")),
        set("set", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "set")),
        setInt("setInt", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "setInt")),
        getPage("a", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "getPage")),
        getTag("getTag", (ServerUtils.hasSpecificUpdate("1_19") ? "v" : ServerUtils.hasPreciseUpdate("1_18_2") ? "t" : ServerUtils.hasSpecificUpdate("1_18") ? "s" : "getTag")),
        setTag("setTag", (ServerUtils.hasSpecificUpdate("1_18") ? "c" : "setTag")),
        setString("setString", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "setString")),
        getString("getString", (ServerUtils.hasSpecificUpdate("1_18") ? "l" : "getString")),
        setDouble("setDouble", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "setDouble")),
        sendPacket("sendPacket", (ServerUtils.hasPreciseUpdate("1_20_2") ? "b" : ServerUtils.hasSpecificUpdate("1_18") ? "a" : "sendPacket"));
        public final String original;
        public final String remapped;

        MinecraftMethod(final String original, final String remapped) {
            this.original = original;
            this.remapped = remapped;
        }

        public @Nonnull String getMethod(final @Nonnull Object objClass, final @Nonnull Class<?>... arguments) {
            try {
                Class<?> canonicalClass = (!(objClass instanceof Class<?>) ? objClass.getClass() : (Class<?>) objClass);
                return (ReflectionUtils.remapped() ? this.remapped : this.original);
            } catch (Exception e) {
                return this.original;
            }
        }
    }

    /**
     * Searchable tags that no longer require NBT Reflections.
     */
    public enum MinecraftField {
        PlayerConnection("playerConnection", (ServerUtils.hasSpecificUpdate("1_20") ? "c" : "b")),
        ActiveContainer("activeContainer", (ServerUtils.hasPreciseUpdate("1_20_2") ? "bS" : ServerUtils.hasSpecificUpdate("1_20") ? "bR" : ServerUtils.hasPreciseUpdate("1_19_3") ? "bP" :
                ServerUtils.hasSpecificUpdate("1_19") ? "bU" : ServerUtils.hasPreciseUpdate("1_18_2") ? "bV" : ServerUtils.hasSpecificUpdate("1_18") ? "bW" : "bV")),
        DefaultContainer("defaultContainer", (ServerUtils.hasPreciseUpdate("1_20_2") ? "bR" : ServerUtils.hasSpecificUpdate("1_20") ? "bQ" : ServerUtils.hasPreciseUpdate("1_19_3") ? "bO" :
                ServerUtils.hasSpecificUpdate("1_19") ? "bT" : ServerUtils.hasPreciseUpdate("1_18_2") ? "bU" : ServerUtils.hasSpecificUpdate("1_18") ? "bV" : "bU")),
        Inventory("inventory", (ServerUtils.hasPreciseUpdate("1_20_3") ? "fS" : ServerUtils.hasPreciseUpdate("1_20_2") ? "fR" : ServerUtils.hasSpecificUpdate("1_20") ? "fN" : ServerUtils.hasPreciseUpdate("1_19_3") ? "fJ" : ServerUtils.hasPreciseUpdate("1_19_3") ? "fE" :
                ServerUtils.hasSpecificUpdate("1_19") ? "fB" : ServerUtils.hasPreciseUpdate("1_18_2") ? "fr" : ServerUtils.hasSpecificUpdate("1_18") ? "fq" : "getInventory")),
        At("at", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "at")),
        Anvil("ANVIL", ServerUtils.hasPreciseUpdate("1_20_3") ? "i" : "h"),
        AddSlotListener("addSlotListener", (ServerUtils.hasSpecificUpdate("1_18") ? "a" : "initMenu")),
        RenameText("renameText", "v"),
        GetSlot("getSlot", (ServerUtils.hasPreciseUpdate("1_18_2") ? "b" : ServerUtils.hasSpecificUpdate("1_18") ? "a" : "getSlot")),
        HasItem("hasItem", (ServerUtils.hasPreciseUpdate("1_20_3") ? "h" : ServerUtils.hasSpecificUpdate("1_18") ? "f" : "hasItem")),
        GetItem("getItem", (ServerUtils.hasPreciseUpdate("1_20_3") ? "g" : ServerUtils.hasSpecificUpdate("1_18") ? "e" : "getItem")),
        NetworkManager("networkManager", (ServerUtils.hasSpecificUpdate("1_19") ? "b" : "a"));
        public final String original;
        public final String remapped;

        MinecraftField(final String original, final String remapped) {
            this.original = original;
            this.remapped = remapped;
        }

        public @Nonnull String getField() {
            try {
                return (ReflectionUtils.remapped() ? this.remapped : this.original);
            } catch (Exception e) {
                return this.original;
            }
        }
    }

    /**
     * Searchable tags that no longer require NBT Reflections.
     */
    public enum MinecraftTags {
        NBTTagCompound(".nbt"),
        NBTTagList(".nbt"),
        NBTTagString(".nbt"),
        NBTBase(".nbt"),
        ItemStack(".world.item"),
        Packet(".network.protocol"),
        PacketLoginInStart(".network.protocol.login"),
        PacketPlayOutSetSlot(".network.protocol.game"),
        PacketPlayOutOpenWindow(".network.protocol.game"),
        PacketPlayOutCloseWindow(".network.protocol.game"),
        PlayerConnection(".server.network"),
        EntityPlayer(".server.level"),
        NetworkManager(".network"),
        MinecraftServer(".server"),
        ServerConnection(".server.network"),
        IChatBaseComponent(".network.chat"),
        IChatBaseComponent$ChatSerializer(".network.chat"),
        PacketPlayOutChat(".network.protocol.game"),
        ClientboundSystemChatPacket(".network.protocol.game"),
        ChatMessageType(".network.chat"),
        ChatMessage(".server"),
        BlockPosition(".core"),
        ContainerAnvil(".world.inventory"),
        EntityHuman(".world.entity.player"),
        ICrafting(".world.inventory"),
        ContainerAccess(".world.inventory"),
        Containers(".world.inventory"),
        Container(".world.inventory"),
        ContainerProperty(".world.inventory"),
        ChatComponentText(".network.chat"),
        World(".world.level"),
        PlayerInventory(".world.entity.player");
        public final String tag;

        MinecraftTags(final String tag) {
            this.tag = tag;
        }
    }

    /**
     * An interface for invoking a specific constructor.
     */
    public interface ConstructorInvoker {
        /**
         * Invoke a constructor for a specific class.
         *
         * @param arguments - the arguments to pass to the constructor.
         * @return The constructed object.
         */
        Object invoke(final @Nonnull Object... arguments);
    }

    /**
     * An interface for invoking a specific method.
     */
    public interface MethodInvoker {
        /**
         * Invoke a method on a specific target object.
         *
         * @param target    - the target object, or NULL for a method.
         * @param arguments - the arguments to pass to the method.
         * @return The return value, or NULL if is void.
         */
        Object invoke(final @Nonnull Object target, final @Nonnull Object... arguments);
    }

    /**
     * An interface for retrieving the field content.
     *
     * @param <T> - field type.
     */
    public interface FieldAccessor<T> {
        /**
         * Retrieve the content of a field.
         *
         * @param target - the target object, or NULL for a field.
         * @return The value of the field.
         */
        T get(final Object target);

        /**
         * Set the content of a field.
         *
         * @param target - the target object, or NULL for a field.
         * @param value  - the new value of the field.
         */
        void set(final Object target, final Object value);

        /**
         * Determine if the given object has this field.
         *
         * @param target - the object to test.
         * @return TRUE if it does, FALSE otherwise.
         */
        boolean hasField(final Object target);
    }
}