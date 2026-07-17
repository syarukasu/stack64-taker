package dev.stack64taker.client;

import dev.stack64taker.Stack64Taker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraftforge.fml.loading.FMLPaths;

public final class Stack64TakerClientConfig {
  private static final String KEY_AMOUNT = "takeAmount";
  private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("stack64_taker-client.properties");
  private static boolean loaded;
  private static int takeAmount = Stack64Taker.DEFAULT_TAKE_AMOUNT;

  private Stack64TakerClientConfig() {
  }

  public static int getTakeAmount() {
    load();
    return takeAmount;
  }

  public static void setTakeAmount(int amount) {
    load();
    takeAmount = Stack64Taker.sanitizeTakeAmount(amount);
    save();
  }

  public static int parseAmount(String value) {
    if (value == null) {
      return Stack64Taker.DEFAULT_TAKE_AMOUNT;
    }

    try {
      return Stack64Taker.sanitizeTakeAmount(Integer.parseInt(value.trim()));
    } catch (NumberFormatException ignored) {
      return Stack64Taker.DEFAULT_TAKE_AMOUNT;
    }
  }

  private static void load() {
    if (loaded) {
      return;
    }

    loaded = true;
    if (!Files.isRegularFile(CONFIG_PATH)) {
      save();
      return;
    }

    Properties properties = new Properties();
    try (InputStream stream = Files.newInputStream(CONFIG_PATH)) {
      properties.load(stream);
      takeAmount = parseAmount(properties.getProperty(KEY_AMOUNT));
    } catch (IOException ignored) {
      takeAmount = Stack64Taker.DEFAULT_TAKE_AMOUNT;
    }
  }

  private static void save() {
    Properties properties = new Properties();
    properties.setProperty(KEY_AMOUNT, Integer.toString(takeAmount));
    try {
      Files.createDirectories(CONFIG_PATH.getParent());
      try (OutputStream stream = Files.newOutputStream(CONFIG_PATH)) {
        properties.store(stream, "Stack64 Taker client settings");
      }
    } catch (IOException ignored) {
      // Settings are convenience only. Failed writes must not break gameplay.
    }
  }
}
