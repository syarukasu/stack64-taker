package dev.stack64taker.mixin;

import dev.stack64taker.Stack64Taker;
import dev.stack64taker.client.Stack64TakerAmountScreen;
import dev.stack64taker.client.Stack64TakerClientConfig;
import dev.stack64taker.client.Stack64TakerKeyMappings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
  @Shadow(remap = false)
  protected Slot f_97734_;

  @Inject(method = "m_6375_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$takeAmountWithMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    if (Stack64TakerKeyMappings.isSetAmountMouseAction(button)) {
      Stack64TakerAmountScreen.open((Screen) (Object) this);
      cir.setReturnValue(true);
      return;
    }

    if (!Stack64TakerKeyMappings.isTakeAmountMouseAction(button)) {
      return;
    }

    if (stack64taker$sendTakeAmount(this.f_97734_)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "m_7933_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$takeAmountWithKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
    if (Stack64TakerKeyMappings.isSetAmountKeyAction(keyCode, scanCode)) {
      Stack64TakerAmountScreen.open((Screen) (Object) this);
      cir.setReturnValue(true);
      return;
    }

    if (!Stack64TakerKeyMappings.isTakeAmountKeyAction(keyCode, scanCode)) {
      return;
    }

    if (stack64taker$sendTakeAmount(this.f_97734_)) {
      cir.setReturnValue(true);
    }
  }

  private boolean stack64taker$sendTakeAmount(Slot slot) {
    if (slot == null) {
      return false;
    }

    AbstractContainerMenu menu = ((AbstractContainerScreen<?>) (Object) this).getMenu();
    if (menu == null) {
      return false;
    }

    boolean realMenuSlot = menu.slots.contains(slot);
    boolean ae2StorageMenu = stack64taker$isAe2StorageMenu(menu);
    Long ae2Serial = stack64taker$getAe2RepoSerial(slot);
    if (ae2Serial != null) {
      Stack64Taker.sendTakeAmountAe2Request(ae2Serial, Stack64TakerClientConfig.getTakeAmount());
      return true;
    }

    // AE2の端末表示スロットはサーバーメニューの実スロットではない。
    // serialが取れない場合も通常クリックへ落とすと不正slotIdを送るので、Stack64キーだけここで消費する。
    if (ae2StorageMenu && !realMenuSlot) {
      return true;
    }

    if (!slot.hasItem() || slot.getItem().getCount() <= 0) {
      return false;
    }

    int menuSlotIndex = menu.slots.indexOf(slot);
    if (menuSlotIndex < 0 || menuSlotIndex >= menu.slots.size()) {
      return false;
    }

    Stack64Taker.sendTakeAmountSlotRequest(menuSlotIndex, Stack64TakerClientConfig.getTakeAmount());
    return true;
  }

  private boolean stack64taker$isAe2StorageMenu(AbstractContainerMenu menu) {
    Class<?> type = menu.getClass();
    while (type != null) {
      if ("appeng.menu.me.common.MEStorageMenu".equals(type.getName())) {
        return true;
      }

      type = type.getSuperclass();
    }

    return false;
  }

  private Long stack64taker$getAe2RepoSerial(Slot slot) {
    try {
      Object entry = slot.getClass().getMethod("getEntry").invoke(slot);
      if (entry == null) {
        return null;
      }

      Object storedAmount = entry.getClass().getMethod("getStoredAmount").invoke(entry);
      if (!(storedAmount instanceof Long amount) || amount <= 0L) {
        return null;
      }

      Object serial = entry.getClass().getMethod("getSerial").invoke(entry);
      return serial instanceof Long value ? value : null;
    } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ignored) {
      return null;
    }
  }
}
