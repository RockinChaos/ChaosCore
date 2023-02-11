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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import me.RockinChaos.core.Core;

public class PasteAPI {
	private final String devKey;
	private final String pasteData;
	
	private int pasteState = 1;
	private String pasteFormat = "yaml";
	private String pasteExpire = "10M";
	private String pasteName = Core.getCore().getPlugin().getName() + " Plugin by RockinChaos";
	
   /**
    * Creates a new PasteAPI instance.
    * 
    * @param devKey - The key for Pastebin.
    * @param pasteData - The data to be pasted.
    */
	public PasteAPI(final String devKey, final String pasteData) {
		this.devKey = devKey;
		this.pasteData = pasteData;
	}
	
   /**
    * Sets the name of the pastebin.
    * 
    * @param pasteName - The name to be set.
    */
	public void setPasteName(final String pasteName) {
		this.pasteName = pasteName;
	}
	
   /**
    * Sets the format of the pastebin.
    * 
    * https://pastebin.com/doc_api#5
    * Default is yaml.
    * 
    * @param pasteFormat - The format to be set.
    */
	public void setPasteFormat(final String pasteFormat) {
		this.pasteFormat = pasteFormat;
	}
	
   /**
    * Sets the state of the pastebin.
    * 
    * 0 = Public
    * 1 = Unlisted
    * 2 = Private (only allowed in combination with api_user_key, as you have to be logged into your account to access the paste)
    * 
    * @param pasteState - The state to be set.
    */
	public void setPasteState(final int pasteState) {
		this.pasteState = pasteState;
	}
	
   /**
    * Sets the expire duration of the pastebin.
    * 
    * N = Never
    * 10M = 10 Minutes
    * 1H = 1 Hour
    * 1D = 1 Day
    * 1W = 1 Week
    * 2W = 2 Weeks
    * 1M = 1 Month
    * 6M = 6 Months
    * 1Y = 1 Year
    * 
    * @param pasteExpire - The expire duration to be set.
    */
	public void setPasteExpire(final String pasteExpire) {
		this.pasteExpire = pasteExpire;
	}
	
   /**
    * Gets the option set of the pastebin URI.
    * 
    * @return The option set as a String.
    */
	private String getOptions() throws UnsupportedEncodingException {
		final Map < String, String > options = new HashMap < > ();
		options.put("api_option", "paste");
		options.put("api_dev_key", this.devKey);
		options.put("api_paste_code", this.pasteData);
		options.put("api_paste_name", this.pasteName);
		options.put("api_paste_format", this.pasteFormat);
		options.put("api_paste_private", String.valueOf(this.pasteState));
		options.put("api_paste_expire_date", this.pasteExpire);
		final StringJoiner stringJoiner = new StringJoiner("&");
		for (Entry < String, String > entry: options.entrySet()) {
			stringJoiner.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return stringJoiner.toString();
	}
	
   /**
    * Attempts to get a successful pastebin URL.
    * 
    * @return The successful pastebin URL.
    */
	public String getPaste() throws IOException {
		final String postOptions = this.getOptions();
		final byte[] postBytes = postOptions.getBytes(StandardCharsets.UTF_8);
		final HttpURLConnection connection = (HttpURLConnection) new URL("https://pastebin.com/api/api_post.php").openConnection();
		connection.setDoOutput(true);
		connection.setFixedLengthStreamingMode(postBytes.length);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		connection.connect();
		try (OutputStream os = connection.getOutputStream()) {
			os.write(postBytes);
		}
		final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		final StringBuilder response = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		} 
		in.close();
		connection.disconnect();
		return response.toString();
	}
}