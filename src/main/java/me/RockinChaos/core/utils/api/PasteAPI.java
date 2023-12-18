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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class PasteAPI {
    private final String pasteData;

    /**
     * Creates a new PasteAPI instance.
     *
     * @param pasteData - The data to be pasted.
     */
    public PasteAPI(final @Nonnull String pasteData) {
        this.pasteData = pasteData;
    }

    /**
     * Attempts to get a successful paste connection.
     *
     * @return The successful paste connection.
     */
    private static @Nonnull HttpURLConnection getHttpURLConnection(@Nonnull final String postOptions) throws IOException {
        final byte[] postBytes = postOptions.getBytes(StandardCharsets.UTF_8);
        final HttpURLConnection connection = (HttpURLConnection) new URL("https://paste.craftationgaming.com/documents").openConnection();
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(postBytes.length);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.connect();
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postBytes);
        }
        return connection;
    }

    /**
     * Attempts to get a successful paste URL.
     *
     * @return The successful paste URL.
     */
    public @Nonnull String getPaste() throws IOException, ParseException {
        final HttpURLConnection connection = getHttpURLConnection(this.pasteData);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        connection.disconnect();
        final JSONObject objectReader = (JSONObject) JSONValue.parseWithException(response.toString());
        final String pasteKey = objectReader.get("key").toString();
        return "https://paste.craftationgaming.com/" + pasteKey;
    }
}