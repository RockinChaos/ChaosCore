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
import me.RockinChaos.core.utils.ChatComponent;
import me.RockinChaos.core.utils.ChatComponent.ClickAction;
import me.RockinChaos.core.utils.ChatComponent.ClickEvent;
import me.RockinChaos.core.utils.ChatComponent.TextSection;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import me.RockinChaos.core.utils.types.PlaceHolder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class LanguageAPI {
    private static LanguageAPI lang;
    private Lang langType = Lang.ENGLISH;
    private String langPrefix = Core.getCore().getPlugin().getName();

    /**
     * Gets the instance of the Language.
     *
     * @return The Language instance.
     */
    public static @Nonnull LanguageAPI getLang() {
        if (lang == null) {
            lang = new LanguageAPI();
            lang.langFile();
        }
        return lang;
    }

    /**
     * Executes a Message to the Sender.
     *
     * @param sender      - The sender receiving the Message.
     * @param langMessage - The Message being sent.
     */
    public void dispatchMessage(final @Nonnull CommandSender sender, @Nonnull String langMessage) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        langMessage = StringUtils.translateLayout(langMessage, player);
        if (sender instanceof ConsoleCommandSender) {
            langMessage = ChatColor.stripColor(langMessage);
        }
        sender.sendMessage(langMessage);
    }

    /**
     * Executes a Message to the Sender.
     *
     * @param sender       - The sender receiving the Message.
     * @param langMessage  - The Message being sent.
     * @param hoverMessage - The Hoverable Message being attached to langMessage.
     * @param clickMessage - The Clicked Message being attached to langMessage.
     */
    public void dispatchMessage(final @Nonnull CommandSender sender, @Nonnull String langMessage, @Nonnull String hoverMessage, @Nonnull String clickMessage, final @Nonnull ClickAction action) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        langMessage = new PlaceHolder().setPlaceholders(langMessage);
        langMessage = StringUtils.translateLayout(langMessage, player);
        hoverMessage = StringUtils.translateLayout(hoverMessage, player);
        clickMessage = StringUtils.translateLayout(clickMessage, player);
        if (sender instanceof ConsoleCommandSender) {
            langMessage = ChatColor.stripColor(langMessage);
            sender.sendMessage(langMessage);
        } else if (ServerUtils.hasPreciseUpdate("1_8_8")) {
            TextSection textComponent = ChatComponent.of(langMessage);
            TextSection.HoverEvent hoverEvent = new TextSection.HoverEvent(ChatComponent.of(hoverMessage));
            textComponent.hoverEvent(hoverEvent);
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.action(action);
            clickEvent.click(clickMessage);
            textComponent.clickEvent(clickEvent);
            ChatComponent.sendTo(textComponent, player);
        } else {
            sender.sendMessage(langMessage);
        }
    }

    /**
     * Executes a Lang Message for the Sender.
     *
     * @param nodeLocation - The String location of the Language Message.
     * @param sender       - The Sender who will receive the Message.
     * @param placeHolder  - Placeholders to be placed into the Language Message.
     */
    public void sendLangMessage(final @Nonnull String nodeLocation, final @Nonnull CommandSender sender, final @Nonnull PlaceHolder... placeHolder) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        String langMessage = (sender.isPermissionSet(Core.getCore().getPlugin().getName().toLowerCase() + ".lang." + nodeLocation) ? sender.hasPermission(Core.getCore().getPlugin().getName().toLowerCase() + ".lang." + nodeLocation) ? this.getLangMessage(nodeLocation) : null : this.getLangMessage(nodeLocation));
        if (langMessage != null && !langMessage.isEmpty()) {
            langMessage = (placeHolder.length > 0 ? placeHolder[0].setPlaceholders(langMessage) : langMessage);
            langMessage = StringUtils.translateLayout(langMessage, player).replace(" \\n ", " \\n").replace(" /n ", " \\n").replace(" /n", " \\n");
            String[] langLines = langMessage.split(Pattern.quote(" \\" + "n"));
            for (String langLine : langLines) {
                String langStrip = langLine;
                if (sender instanceof ConsoleCommandSender) {
                    langStrip = ChatColor.stripColor(langStrip);
                }
                if (this.isConsoleMessage(nodeLocation)) {
                    ServerUtils.logInfo(ChatColor.stripColor(langLine));
                } else {
                    sender.sendMessage(langStrip);
                }
            }
        }
    }

    /**
     * Gets the lang message at the node location.
     *
     * @param nodeLocation - The String location of the Language Message.
     */
    public @Nonnull String getLangMessage(final @Nonnull String nodeLocation) {
        String message = Core.getCore().getConfig(this.langType.nodeLocation()).getString(nodeLocation);
        return (message != null && !message.isEmpty() ? message : "");
    }

    /**
     * Gets the string at the node location.
     *
     * @param nodeLocation - The String location of the Language Translation.
     */
    public @Nonnull String getString(final @Nonnull String nodeLocation) {
        return getLangMessage(nodeLocation);
    }

    /**
     * Gets the string list at the node location.
     *
     * @param nodeLocation - The String List location of the Language Translation.
     */
    public @Nonnull List<String> getStringList(final @Nonnull String nodeLocation) {
        final Object value = Core.getCore().getConfig(this.langType.nodeLocation()).get(nodeLocation);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> result = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String) {
                    result.add((String) obj);
                }
            }
            return result.isEmpty() ? Collections.singletonList("") : result;
        } else if (value instanceof String) {
            return Collections.singletonList((String) value);
        }
        return Collections.singletonList("");
    }

    /**
     * Checks if the Language Message is a Console Message.
     *
     * @param nodeLocation - The String location of the Language Message.
     * @return If the Language Message is a Console Message.
     */
    private boolean isConsoleMessage(final @Nonnull String nodeLocation) {
        return nodeLocation.equalsIgnoreCase("Commands.Updates.checking")
                || nodeLocation.equalsIgnoreCase("Commands.Updates.forcing");
    }

    /**
     * Gets the current Language that is being translated.
     *
     * @return The Language.
     */
    public @Nonnull String getLanguage() {
        return this.langType.name().substring(0, 1).toUpperCase() + this.langType.name().substring(1).toLowerCase();
    }

    /**
     * Sets the current Language.
     *
     * @param lang - The Language to be set.
     */
    public void setLanguage(final @Nonnull String lang) {
        if (lang.equalsIgnoreCase("tw")) {
            this.langType = Lang.TWCHINESE;
        } else if (lang.equalsIgnoreCase("cn")) {
            this.langType = Lang.CNCHINESE;
        } else if (lang.equalsIgnoreCase("en")) {
            this.langType = Lang.ENGLISH;
        } else if (lang.equalsIgnoreCase("es")) {
            this.langType = Lang.SPANISH;
        } else if (lang.equalsIgnoreCase("ru")) {
            this.langType = Lang.RUSSIAN;
        } else if (lang.equalsIgnoreCase("fr")) {
            this.langType = Lang.FRENCH;
        } else if (lang.equalsIgnoreCase("nl")) {
            this.langType = Lang.DUTCH;
        } else if (lang.equalsIgnoreCase("pt")) {
            this.langType = Lang.PORTUGUESE;
        } else if (lang.equalsIgnoreCase("pl")) {
            this.langType = Lang.POLISH;
        } else if (lang.equalsIgnoreCase("it")) {
            this.langType = Lang.ITALIAN;
        } else if (lang.equalsIgnoreCase("de")) {
            this.langType = Lang.GERMAN;
        }
    }

    /**
     * Gets the current Language File name.
     *
     * @return The Language File name.
     */
    public @Nonnull String getFile() {
        return this.langType.nodeLocation();
    }

    /**
     * Sets the Language Prefix
     */
    public void setPrefix() {
        final String prefix = Core.getCore().getConfig(this.langType.nodeLocation()).getString("Prefix");
        this.langPrefix = StringUtils.colorFormat(prefix == null ? "" : prefix);
    }

    /**
     * Gets the Plugin Prefix.
     *
     * @return The Plugin Prefix.
     */
    public String getPrefix() {
        return this.langPrefix;
    }

    /**
     * Gets the current Language from the config and saves it to memory.
     */
    public void langFile() {
        String lang = Core.getCore().getConfig("config.yml").getString("Language");
        if (lang != null) {
            lang = lang.replace(" ", "");
            if (StringUtils.containsValue(Core.getCore().getData().getLanguages(), lang)) {
                if (lang.equalsIgnoreCase("TraditionalChinese") || lang.equalsIgnoreCase("TwChinese")) {
                    this.setLanguage("tw");
                } else if (lang.equalsIgnoreCase("SimplifiedChinese") || lang.equalsIgnoreCase("CnChinese") || lang.equalsIgnoreCase("Chinese")) {
                    this.setLanguage("cn");
                } else if (StringUtils.containsIgnoreCase(lang, "Chinese")) {
                    this.setLanguage("cn");
                } else if (lang.equalsIgnoreCase("Spanish")) {
                    this.setLanguage("es");
                } else if (lang.equalsIgnoreCase("Russian")) {
                    this.setLanguage("ru");
                } else if (lang.equalsIgnoreCase("French")) {
                    this.setLanguage("fr");
                } else if (lang.equalsIgnoreCase("Dutch")) {
                    this.setLanguage("nl");
                } else if (lang.equalsIgnoreCase("Portuguese")) {
                    this.setLanguage("pt");
                } else if (lang.equalsIgnoreCase("Polish")) {
                    this.setLanguage("pl");
                } else if (lang.equalsIgnoreCase("Italian")) {
                    this.setLanguage("it");
                } else if (lang.equalsIgnoreCase("German")) {
                    this.setLanguage("de");
                } else {
                    this.setLanguage("en");
                }
            } else {
                ServerUtils.logSevere("{Lang} The language " + lang + " was specified but could not be found!");
                ServerUtils.logSevere("{Lang} Please check the language specified in the config.yml for proper spelling");
                ServerUtils.logSevere("{Lang} Defaulting to the English language ...");
                this.setLanguage("en");
            }
        } else {
            this.setLanguage("en");
        }
    }

    /**
     * Refreshes the LanguageAPI instanced cache.
     */
    public void refresh() {
        lang = new LanguageAPI();
        lang.langFile();
    }

    /**
     * Defines the Lang type for the Language.
     */
    private enum Lang {
        DEFAULT("en-lang.yml", 0),
        ENGLISH("en-lang.yml", 1),
        SPANISH("es-lang.yml", 2),
        TWCHINESE("tw-lang.yml", 3),
        CNCHINESE("cn-lang.yml", 4),
        RUSSIAN("ru-lang.yml", 5),
        FRENCH("fr-lang.yml", 6),
        DUTCH("nl-lang.yml", 7),
        PORTUGUESE("pt-lang.yml", 8),
        POLISH("pl-lang.yml", 9),
        ITALIAN("it-lang.yml", 10),
        GERMAN("de-lang.yml", 11);

        private final String nodeLocation;

        Lang(final String nodeLocation, final int i) {
            this.nodeLocation = nodeLocation;
        }

        private String nodeLocation() {
            return nodeLocation;
        }
    }
}