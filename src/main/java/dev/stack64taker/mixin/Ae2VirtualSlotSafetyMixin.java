package dev.stack64taker.mixin;

import java.lang.reflect.Method;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class Ae2VirtualSlotSafetyMixin {
  private static final Logger LOGGER = LoggerFactory.getLogger("Stack64Taker/AE2SlotSafety");
  private static boolean stack64taker$loggedReflectionFailure;

  @Inject(method = "m_171799_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$protectAe2VirtualSlot(
      int containerId,
      int slotId,
      int mouseButton,
      ClickType clickType,
      Player player,
      CallbackInfo ci
  ) {
    if (player == null || slotId < 0) {
      return;
    }

    AbstractContainerMenu menu = player.containerMenu;
    if (menu == null || menu.containerId != containerId || slotId >= menu.slots.size()) {
      return;
    }

    Slot slot = menu.slots.get(slotId);
    if (!"appeng.client.gui.me.common.RepoSlot".equals(slot.getClass().getName())) {
      return;
    }

    // RepoSlot is client-only. It must never be simulated or sent as a vanilla menu slot.
    ci.cancel();

    // AE2 normally handles this before Minecraft reaches this method. If another GUI helper
    // bypasses AE2, translate only the safe carried-item insertion path back to AE2's protocol.
    if (clickType != ClickType.PICKUP || menu.getCarried().isEmpty() || (mouseButton != 0 && mouseButton != 1)) {
      return;
    }

    try {
      Object entry = slot.getClass().getMethod("getEntry").invoke(slot);
      long serial = -1L;
      if (entry != null) {
        Object value = entry.getClass().getMethod("getSerial").invoke(entry);
        if (value instanceof Long entrySerial) {
          serial = entrySerial;
        }
      }

      Class<?> inventoryActionClass = Class.forName("appeng.helpers.InventoryAction");
      @SuppressWarnings({"rawtypes", "unchecked"})
      Object action = Enum.valueOf(
          (Class<? extends Enum>) inventoryActionClass.asSubclass(Enum.class),
          mouseButton == 1 ? "SPLIT_OR_PLACE_SINGLE" : "PICKUP_OR_SET_DOWN"
      );
      Method handleInteraction = menu.getClass().getMethod("handleInteraction", long.class, inventoryActionClass);
      handleInteraction.invoke(menu, serial, action);
    } catch (ReflectiveOperationException | LinkageError error) {
      if (!stack64taker$loggedReflectionFailure) {
        stack64taker$loggedReflectionFailure = true;
        LOGGER.error("Blocked an unsafe AE2 virtual-slot click, but could not translate it to AE2's interaction packet", error);
      }
    }
  }
}
