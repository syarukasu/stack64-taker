package dev.stack64taker.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 2000)
public abstract class ServerClickGuardMixin {
  private static final Logger LOGGER = LoggerFactory.getLogger("Stack64Taker/ClickGuard");
  private static final Map<String, Long> LAST_LOG_BY_CLICK = new ConcurrentHashMap<>();
  private static final long LOG_INTERVAL_MS = 30000L;

  @Inject(method = "m_5914_", at = @At("HEAD"), cancellable = true, remap = false)
  private void stack64taker$guardInvalidContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
    int slotId = packet.getSlotNum();
    if (slotId < 0) {
      return;
    }

    ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
    ServerPlayer player = listener.player;
    if (player == null || player.containerMenu == null) {
      return;
    }

    AbstractContainerMenu menu = player.containerMenu;
    if (packet.getContainerId() != menu.containerId) {
      return;
    }

    int slotCount = menu.slots.size();
    if (slotId < slotCount) {
      return;
    }

    stack64taker$logInvalidClick(player, menu, packet, slotId, slotCount);
    ci.cancel();
    stack64taker$resyncAuthoritativeMenu(player, menu, packet.getContainerId());
  }

  private static void stack64taker$resyncAuthoritativeMenu(
      ServerPlayer player,
      AbstractContainerMenu menu,
      int containerId
  ) {
    player.getServer().execute(() -> {
      if (player.containerMenu == menu && menu.containerId == containerId) {
        menu.sendAllDataToRemote();
      }
    });
  }

  private static void stack64taker$logInvalidClick(
      ServerPlayer player,
      AbstractContainerMenu menu,
      ServerboundContainerClickPacket packet,
      int slotId,
      int slotCount
  ) {
    String menuClass = menu.getClass().getName();
    String playerName = player.getScoreboardName();
    String key = playerName + "|" + menuClass + "|" + packet.getContainerId() + "|" + slotId + "|" + slotCount;
    long now = System.currentTimeMillis();
    Long last = LAST_LOG_BY_CLICK.get(key);
    if (last != null && now - last < LOG_INTERVAL_MS) {
      return;
    }

    LAST_LOG_BY_CLICK.put(key, now);
    LOGGER.warn(
        "Blocked invalid container click: player={} menu={} packetContainerId={} serverContainerId={} slotId={} serverSlotCount={} button={} clickType={}",
        playerName,
        menuClass,
        packet.getContainerId(),
        menu.containerId,
        slotId,
        slotCount,
        packet.getButtonNum(),
        packet.getClickType()
    );
  }
}
