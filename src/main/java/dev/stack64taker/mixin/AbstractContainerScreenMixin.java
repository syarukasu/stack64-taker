package dev.stack64taker.mixin;

import dev.stack64taker.Stack64Taker;
import dev.stack64taker.client.Stack64TakerKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
  @Shadow protected Slot hoveredSlot;

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void stack64taker$take64WithModifier(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (button != 0 || !Stack64TakerKeyMappings.TAKE_64_MODIFIER.isDown()) {
      return;
    }

    Slot slot = this.hoveredSlot;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null || slot == null || !slot.hasItem() || slot.getItem().getCount() <= 64) {
      return;
    }

    if (!minecraft.player.containerMenu.getCarried().isEmpty()) {
      return;
    }

    Stack64Taker.sendTake64Request(slot.index);
    cir.setReturnValue(true);
  }
}
