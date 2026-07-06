package dev.stack64taker.client;

import dev.stack64taker.Stack64Taker;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Stack64Taker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class Stack64TakerKeyMappings {
  public static final String CATEGORY = "key.categories.stack64_taker";

  public static final KeyMapping TAKE_64_MODIFIER = new KeyMapping(
      "key.stack64_taker.take_64_modifier",
      GLFW.GLFW_KEY_RIGHT_ALT,
      CATEGORY
  );

  private Stack64TakerKeyMappings() {
  }

  @SubscribeEvent
  public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(TAKE_64_MODIFIER);
  }
}
