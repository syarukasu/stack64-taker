package dev.stack64taker.mixin;

import dev.stack64taker.Stack64Taker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class AbstractContainerScreenMixin {
  @Shadow(remap = false) protected Slot f_97734_;

  @Inject(method = "m_6375_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$take64WithCtrlShift(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (button != 0 || !Screen.m_96637_() || !Screen.m_96638_()) {
      return;
    }

    Slot slot = this.f_97734_;
    Minecraft minecraft = Minecraft.m_91087_();
    if (minecraft.f_91074_ == null || slot == null || !slot.m_6657_() || slot.m_7993_().m_41613_() <= 64) {
      return;
    }

    if (!minecraft.f_91074_.f_36096_.m_142621_().m_41619_()) {
      return;
    }

    Stack64Taker.sendTake64Request(slot.f_40219_);
    cir.setReturnValue(true);
  }
}
