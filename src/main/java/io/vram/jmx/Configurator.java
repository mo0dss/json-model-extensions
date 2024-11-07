/*
 * This file is part of JSON Model Extensions and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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

package io.vram.jmx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

import io.vram.jmx.client.JsonModelExtensions;
import net.fabricmc.loader.api.FabricLoader;

public class Configurator {
	public static boolean loadVanillaModels;
	public static boolean logResolutionErrors;

	static {
		final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("jmx.properties");
		final Properties properties = new Properties();

		if (Files.exists(configFile)) {
			try (var stream = Files.newInputStream(configFile)) {
				properties.load(stream);
			} catch (final IOException e) {
        JsonModelExtensions.LOG.warn("[JMX] Could not read property file '{}'", configFile, e);
			}
		}

		loadVanillaModels = ((String) properties.computeIfAbsent("load-vanilla-models", (a) -> "false")).toLowerCase(Locale.ROOT).equals("true");
		logResolutionErrors = ((String) properties.computeIfAbsent("log-resolution-errors", (a) -> "false")).toLowerCase(Locale.ROOT).equals("true");

		try (var stream = Files.newOutputStream(configFile)) {
			properties.store(stream, "JMX properties file");
		} catch (final IOException e) {
      JsonModelExtensions.LOG.warn("[JMX] Could not store property file '{}'", configFile, e);
		}
	}
}
