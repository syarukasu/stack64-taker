package dev.stack64taker.client;

import dev.stack64taker.Stack64Taker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class Stack64TakerAmountScreen extends Screen {
  private final Screen parent;
  private EditBox amountBox;

  public Stack64TakerAmountScreen(Screen parent) {
    super(Component.translatable("screen.stack64_taker.amount.title"));
    this.parent = parent;
  }

  public static void open(Screen parent) {
    Minecraft.getInstance().setScreen(new Stack64TakerAmountScreen(parent));
  }

  @Override
  protected void init() {
    int centerX = this.width / 2;
    int top = this.height / 2 - 52;

    this.amountBox = new EditBox(
        this.font,
        centerX - 80,
        top + 22,
        160,
        20,
        Component.translatable("screen.stack64_taker.amount.input")
    );
    this.amountBox.setFilter(value -> value.isEmpty() || value.matches("[0-9]{0,7}"));
    this.amountBox.insertText(Integer.toString(Stack64TakerClientConfig.getTakeAmount()));
    this.amountBox.deleteWords(7);
    this.amountBox.setFocused(true);
    this.amountBox.moveCursorToEnd();
    this.amountBox.setHighlightPos(0);
    this.addRenderableWidget(this.amountBox);

    int[] presets = {1, 16, 32, 64, 128, 256, 1024};
    int buttonWidth = 42;
    int startX = centerX - ((buttonWidth + 4) * presets.length - 4) / 2;
    for (int i = 0; i < presets.length; i++) {
      int value = presets[i];
      this.addRenderableWidget(Button.builder(
              Component.literal(Integer.toString(value)),
              button -> this.amountBox.insertText(Integer.toString(value))
          )
          .bounds(startX + i * (buttonWidth + 4), top + 50, buttonWidth, 20)
          .build());
    }

    this.addRenderableWidget(Button.builder(
            Component.translatable("screen.stack64_taker.amount.save"),
            button -> saveAndClose()
        )
        .bounds(centerX - 82, top + 82, 80, 20)
        .build());

    this.addRenderableWidget(Button.builder(
            Component.translatable("gui.cancel"),
            button -> Minecraft.getInstance().setScreen(this.parent)
        )
        .bounds(centerX + 2, top + 82, 80, 20)
        .build());
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      Minecraft.getInstance().setScreen(this.parent);
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_ENTER
        || keyCode == GLFW.GLFW_KEY_KP_ENTER
        || Stack64TakerKeyMappings.isSetAmountKeyAction(keyCode, scanCode)
        || Stack64TakerKeyMappings.isTakeAmountKeyAction(keyCode, scanCode)) {
      saveAndClose();
      return true;
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (Stack64TakerKeyMappings.isSetAmountMouseAction(button)
        || Stack64TakerKeyMappings.isTakeAmountMouseAction(button)) {
      saveAndClose();
      return true;
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public void onClose() {
    Minecraft.getInstance().setScreen(this.parent);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);

    int centerX = this.width / 2;
    int top = this.height / 2 - 52;
    graphics.drawString(this.font, this.title, centerX, top, 0xFFFFFF, true);
    graphics.drawString(
        this.font,
        Component.translatable("screen.stack64_taker.amount.hint", Stack64Taker.MAX_TAKE_AMOUNT),
        centerX - 80,
        top + 110,
        0xA0A0A0
    );
  }

  private void saveAndClose() {
    Stack64TakerClientConfig.setTakeAmount(Stack64TakerClientConfig.parseAmount(this.amountBox.getValue()));
    Minecraft.getInstance().setScreen(this.parent);
  }
}
