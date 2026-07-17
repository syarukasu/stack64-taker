package dev.stack64taker.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerMenu.class, priority = 2000)
public abstract class AbstractContainerMenuClickGuardMixin {
  private static final Logger LOGGER = LoggerFactory.getLogger("Stack64Taker/MenuClickGuard");
  private static final Map<String, Long> LAST_LOG_BY_CLICK = new ConcurrentHashMap<>();
  private static final long LOG_INTERVAL_MS = 30000L;

  @Inject(method = "m_150399_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$guardInvalidMenuClick(
      int slotId,
      int button,
      ClickType clickType,
      Player player,
      CallbackInfo ci
  ) {
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
    if (slotId < 0 || slotId < menu.slots.size()) {
      return;
    }

    String menuClass = this.getClass().getName();
    String playerName = player == null ? "unknown" : player.getScoreboardName();
    String key = playerName + "|" + menuClass + "|" + menu.containerId + "|" + slotId + "|" + menu.slots.size();
    long now = System.currentTimeMillis();
    Long last = LAST_LOG_BY_CLICK.get(key);
    if (last == null || now - last >= LOG_INTERVAL_MS) {
      LAST_LOG_BY_CLICK.put(key, now);
      LOGGER.warn(
          "Blocked invalid menu click at final boundary: player={} menu={} containerId={} slotId={} slotCount={} button={} clickType={}",
          playerName,
          menuClass,
          menu.containerId,
          slotId,
          menu.slots.size(),
          button,
          clickType
      );
    }

    ci.cancel();
    stack64taker$resyncAuthoritativeMenu(player, menu);
  }

  private static void stack64taker$resyncAuthoritativeMenu(Player player, AbstractContainerMenu menu) {
    if (!(player instanceof ServerPlayer serverPlayer)) {
      return;
    }

    int containerId = menu.containerId;
    serverPlayer.getServer().execute(() -> {
      if (serverPlayer.containerMenu == menu && menu.containerId == containerId) {
        menu.sendAllDataToRemote();
      }
    });
  }
}
