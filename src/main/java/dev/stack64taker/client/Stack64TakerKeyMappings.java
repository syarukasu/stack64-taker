package dev.stack64taker.client;

import com.mojang.blaze3d.platform.InputConstants;
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

  public static final KeyMapping TAKE_AMOUNT_ACTION = new KeyMapping(
      "key.stack64_taker.take_amount_action",
      InputConstants.Type.MOUSE,
      GLFW.GLFW_MOUSE_BUTTON_4,
      CATEGORY
  );
  public static final KeyMapping SET_AMOUNT_ACTION = new KeyMapping(
      "key.stack64_taker.set_amount_action",
      InputConstants.Type.MOUSE,
      GLFW.GLFW_MOUSE_BUTTON_5,
      CATEGORY
  );

  private Stack64TakerKeyMappings() {
  }

  public static boolean isTakeAmountMouseAction(int button) {
    InputConstants.Key key = TAKE_AMOUNT_ACTION.getKey();
    return key.getType() == InputConstants.Type.MOUSE && key.getValue() == button;
  }

  public static boolean isTakeAmountKeyAction(int keyCode, int scanCode) {
    return isKeyAction(TAKE_AMOUNT_ACTION, keyCode, scanCode);
  }

  public static boolean isSetAmountMouseAction(int button) {
    InputConstants.Key key = SET_AMOUNT_ACTION.getKey();
    return key.getType() == InputConstants.Type.MOUSE && key.getValue() == button;
  }

  public static boolean isSetAmountKeyAction(int keyCode, int scanCode) {
    return isKeyAction(SET_AMOUNT_ACTION, keyCode, scanCode);
  }

  private static boolean isKeyAction(KeyMapping mapping, int keyCode, int scanCode) {
    InputConstants.Key key = mapping.getKey();
    if (key == InputConstants.UNKNOWN) {
      return false;
    }

    if (key.getType() == InputConstants.Type.KEYSYM) {
      return key.getValue() == keyCode;
    }

    if (key.getType() == InputConstants.Type.SCANCODE) {
      return key.getValue() == scanCode;
    }

    return false;
  }

  @SubscribeEvent
  public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(TAKE_AMOUNT_ACTION);
    event.register(SET_AMOUNT_ACTION);
  }
}
