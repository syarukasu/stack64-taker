package dev.stack64taker.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.stack64taker.Stack64Taker;
import net.minecraft.client.Minecraft;
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
  public static final KeyMapping TAKE_64_ACTION = new KeyMapping(
      "key.stack64_taker.take_64_action",
      InputConstants.Type.MOUSE,
      GLFW.GLFW_MOUSE_BUTTON_4,
      CATEGORY
  );

  private Stack64TakerKeyMappings() {
  }

  public static boolean isTake64ModifierDown() {
    InputConstants.Key key = TAKE_64_MODIFIER.m_90861_();
    if (key == InputConstants.f_84822_) {
      return false;
    }

    long window = Minecraft.m_91087_().m_91268_().m_85439_();
    if (key.m_84868_() == InputConstants.Type.MOUSE) {
      return GLFW.glfwGetMouseButton(window, key.m_84873_()) == GLFW.GLFW_PRESS;
    }

    if (key.m_84868_() == InputConstants.Type.KEYSYM) {
      return InputConstants.m_84830_(window, key.m_84873_());
    }

    return TAKE_64_MODIFIER.m_90857_();
  }

  public static boolean isTake64MouseAction(int button) {
    InputConstants.Key key = TAKE_64_ACTION.m_90861_();
    return key.m_84868_() == InputConstants.Type.MOUSE && key.m_84873_() == button;
  }

  public static boolean isTake64KeyAction(int keyCode, int scanCode) {
    InputConstants.Key key = TAKE_64_ACTION.m_90861_();
    if (key == InputConstants.f_84822_) {
      return false;
    }

    if (key.m_84868_() == InputConstants.Type.KEYSYM) {
      return key.m_84873_() == keyCode;
    }

    if (key.m_84868_() == InputConstants.Type.SCANCODE) {
      return key.m_84873_() == scanCode;
    }

    return false;
  }

  @SubscribeEvent
  public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(TAKE_64_MODIFIER);
    event.register(TAKE_64_ACTION);
  }
}
