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
package me.RockinChaos.core.utils.enchants;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;

import me.RockinChaos.core.utils.ServerUtils;

public class Enchantments {
	
	private boolean glowRegistered = false;
	
	private static Enchantments enchant;
	
   /**
    * Registers the glow enchantment.
    * 
    */
    public void registerGlow() {
    	if (ServerUtils.hasSpecificUpdate("1_13") && !this.glowRegistered) {
	    	try {
	    		Field f = Enchantment.class.getDeclaredField("acceptingNew");
	    		f.setAccessible(true);
	    		f.set(null, true);
	    		Glow glow = new Glow();
	    		Enchantment.registerEnchantment(glow);
	    		glowRegistered = true;
	    	} catch (IllegalArgumentException e) { } catch (Exception e) { ServerUtils.sendDebugTrace(e); }
    	}
    }
    
   /**
    * Gets the instance of the Enchantments.
    * 
    * @return The Enchantments instance.
    */
    public static Enchantments getEnchants() { 
        if (enchant == null) {
        	enchant = new Enchantments();
        }
        return enchant; 
    }
}