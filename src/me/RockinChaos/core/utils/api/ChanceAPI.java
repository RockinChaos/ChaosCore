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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;

public class ChanceAPI {
	private List < Chance > chances;
	private int sum;
	private Random random;
	
	private Map < Object, Integer > probabilityItems = new HashMap < Object, Integer > ();
	
	private static ChanceAPI chance;
	
   /**
    * Creates a new Chances instance.
    * 
    */
	public ChanceAPI() { }
	
   /**
    * Initializes the Chances instance.
    * 
    */
	public void newChance() {
		this.random = new Random();
		this.chances = new ArrayList < > ();
		this.sum = 0;
	}
	
   /**
    * Initializes the Chances instance.
    * 
    * @param seed - The random seed.
    */
	public void newChance(final long seed) {
		this.random = new Random(seed);
		this.chances = new ArrayList < > ();
		this.sum = 0;
	}
	
   /**
    * Adds an Object and its Chance to the pool of Objects to be randomly selected.
    * 
    * @param element - The Object to be selected.
    * @param chance - The Integer chance the Object has to be selected.
    */
	public void addChance(final Object element, final int chance) {
		if (!this.chances.contains(element)) {
			this.chances.add(new Chance(element, this.sum, this.sum + chance));
			this.sum += chance;
		}
	}
	
   /**
    * Gets a randomly selected Object.
    * 
    * @return The randomly selected Object.
    */
	public Object getRandomElement() {
		final int index = this.random.nextInt(this.sum);
		final List < Chance > chanceList = this.chances;
		final Iterator < Chance > itr = chanceList.iterator();
		while (itr.hasNext()) {
			final Chance chanceFound = itr.next();
			if (chanceFound != null && chanceFound.getLowerLimit() <= index && chanceFound.getUpperLimit() > index) {
				return chanceFound.getElement();
			}
		}
		return null;
	}
	
   /**
    * Gets the sum.
    * 
    * @return The options of the sum.
    */
	public int getOptions() {
		return this.sum;
	}
	
   /**
    * Gets the probabilityMap and the Probability HashMap.
    * 
    * @return The probabilityMaps and their Probabilities as a HashMap.
    */
	public Map<Object, Integer> getItems() {
		return this.probabilityItems;
	}
	
   /**
    * Adds an probabilityMap and its Probability to the HashMap pool.
    * 
    * @param probabilityMap - The probabilityMap to be selected.
    * @param i - The chance the probabilityMap has to be selected.
    */
	public void putItem(final Object probabilityMap, final int i) {
		this.probabilityItems.put(probabilityMap, i);
	}
	
   /**
    * Selects a Random probabilityMap from the list of Probability items.
    * 
    * @param player - The Player to have its Probability Item chosen.
    * @return The randomly selected probability.
    */
	public Object getRandom(final Player player) {
		this.newChance();
		if (this.probabilityItems != null && !this.probabilityItems.isEmpty()) {
			for (Object probabilityMap: this.probabilityItems.keySet()) {
				if (this.probabilityItems.get(probabilityMap) != null) { 
					this.addChance(probabilityMap, this.probabilityItems.get(probabilityMap)); 
				}
			}
			return this.getRandomElement();
		}
		return null;
	}
	
   /**
    * Gets the instance of the Chances.
    * 
    * @return The Chances instance.
    */
    public static ChanceAPI getChances() { 
        if (chance == null) {
        	chance = new ChanceAPI(); 
        }
        return chance; 
    } 
	
   /**
    * The Chances class.
    * 
    */
	private class Chance {
		private int upperLimit;
		private int lowerLimit;
		private Object element;
		
	   /**
    	* Creates a new Chance instance.
    	* 
    	* @param element - The Object to be chosen.
    	* @param lowerLimit - The lowest probability. 
    	* @param upperLimit - The highest probability.
    	*/
		private Chance(final Object element, final int lowerLimit, final int upperLimit) {
			this.element = element;
			this.upperLimit = upperLimit;
			this.lowerLimit = lowerLimit;
		}
		
	   /**
    	* Gets the highest probability.
    	* 
    	* @return The highest probability.
    	*/
		private int getUpperLimit() { return this.upperLimit; }
		
	   /**
    	* Gets the lowest probability.
    	* 
    	* @return The lowest probability.
    	*/
		private int getLowerLimit() { return this.lowerLimit; }
		
	   /**
    	* Gets the Object to be chosen.
    	* 
    	* @return The Object to be chosen.
    	*/
		private Object getElement() { return this.element; }
		
	   /**
    	* Handles the toString of a Map Element.
    	* 
    	* @return The newly formatted Map Element as a String instance.
    	*/
		@Override
		public String toString() { return "[" + Integer.toString(this.lowerLimit) + "|" + Integer.toString(this.upperLimit) + "]: " + this.element.toString(); }
	}
}