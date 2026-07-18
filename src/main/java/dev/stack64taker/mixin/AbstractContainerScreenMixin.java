package dev.stack64taker.mixin;

import dev.stack64taker.ContainerClickBounds;
import dev.stack64taker.Stack64Taker;
import dev.stack64taker.client.Stack64TakerAmountScreen;
import dev.stack64taker.client.Stack64TakerClientConfig;
import dev.stack64taker.client.Stack64TakerKeyMappings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "m_6375_", at = @At("HEAD"), cancellable = true, remap = false)
    private void stack64taker$takeAmountWithMouse(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir) {
        if (Stack64TakerKeyMappings.isSetAmountMouseAction(button)) {
            Stack64TakerAmountScreen.open((Screen) (Object) this);
            cir.setReturnValue(true);
            return;
        }
        if (!Stack64TakerKeyMappings.isTakeAmountMouseAction(button)) {
            return;
        }
        if (stack64taker$sendTakeAmount(stack64taker$getSlotUnderMouse())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "m_7933_", at = @At("HEAD"), cancellable = true, remap = false)
    private void stack64taker$takeAmountWithKey(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir) {
        if (Stack64TakerKeyMappings.isSetAmountKeyAction(keyCode, scanCode)) {
            Stack64TakerAmountScreen.open((Screen) (Object) this);
            cir.setReturnValue(true);
            return;
        }
        if (!Stack64TakerKeyMappings.isTakeAmountKeyAction(keyCode, scanCode)) {
            return;
        }
        if (stack64taker$sendTakeAmount(stack64taker$getSlotUnderMouse())) {
            cir.setReturnValue(true);
        }
    }

    private Slot stack64taker$getSlotUnderMouse() {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        return screen.getSlotUnderMouse();
    }

    private boolean stack64taker$sendTakeAmount(Slot slot) {
        if (slot == null) {
            return false;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerMenu menu = screen.getMenu();
        if (menu == null) {
            return false;
        }

        boolean realMenuSlot = menu.slots.contains(slot);
        boolean ae2StorageMenu = stack64taker$isAe2StorageMenu(menu);
        Long ae2Serial = stack64taker$getAe2RepoSerial(slot);
        if (ae2Serial != null) {
            Stack64Taker.sendTakeAmountAe2Request(
                    menu.containerId,
                    ae2Serial,
                    Stack64TakerClientConfig.getTakeAmount());
            return true;
        }

        // AE2の表示専用仮想スロットをバニラのslotIdとして送信しない。
        if (ae2StorageMenu && !realMenuSlot) {
            return true;
        }
        if (!slot.hasItem() || slot.getItem().getCount() <= 0) {
            return false;
        }

        int menuSlotIndex = menu.slots.indexOf(slot);
        if (ContainerClickBounds.isInvalid(menuSlotIndex, menu.slots.size())) {
            return false;
        }

        Stack64Taker.sendTakeAmountSlotRequest(
                menu.containerId,
                menuSlotIndex,
                Stack64TakerClientConfig.getTakeAmount());
        return true;
    }

    private boolean stack64taker$isAe2StorageMenu(AbstractContainerMenu menu) {
        for (Class<?> type = menu.getClass(); type != null; type = type.getSuperclass()) {
            if ("appeng.menu.me.common.MEStorageMenu".equals(type.getName())) {
                return true;
            }
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
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }
}
