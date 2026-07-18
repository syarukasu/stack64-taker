package dev.stack64taker.mixin;

import java.util.concurrent.TimeUnit;

import dev.stack64taker.ContainerClickBounds;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 2000)
public abstract class ServerClickGuardMixin {
    @Unique
    private static final Logger STACK64TAKER_LOGGER = LoggerFactory.getLogger("Stack64Taker/ClickGuard");
    @Unique
    private static final long STACK64TAKER_LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30);
    @Unique
    private static final long STACK64TAKER_RESYNC_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

    @Unique
    private boolean stack64taker$hasLoggedInvalidClick;
    @Unique
    private long stack64taker$lastInvalidClickLogNanos;
    @Unique
    private int stack64taker$suppressedInvalidClickLogs;
    @Unique
    private boolean stack64taker$hasResyncedInvalidClick;
    @Unique
    private long stack64taker$lastInvalidClickResyncNanos;

    @Inject(method = "m_5914_", at = @At("HEAD"), cancellable = true, remap = false)
    private void stack64taker$guardInvalidContainerClick(
            ServerboundContainerClickPacket packet,
            CallbackInfo ci) {
        int slotId = packet.getSlotNum();
        if (slotId < 0) {
            return;
        }

        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        ServerPlayer player = listener.player;
        if (player == null || player.containerMenu == null) {
            return;
        }

        MinecraftServer server = player.server;
        if (server == null || !server.isSameThread()) {
            // Nettyスレッドではメニューへ触らず、バニラの通常処理にサーバースレッド移送を任せる。
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (packet.getContainerId() != menu.containerId
                || !ContainerClickBounds.isInvalid(slotId, menu.slots.size())) {
            return;
        }

        long now = System.nanoTime();
        stack64taker$logInvalidClick(player, menu, packet, slotId, now);
        stack64taker$resyncAtMostOncePerSecond(menu, now);
        ci.cancel();
    }

    @Unique
    private void stack64taker$logInvalidClick(
            ServerPlayer player,
            AbstractContainerMenu menu,
            ServerboundContainerClickPacket packet,
            int slotId,
            long now) {
        if (stack64taker$hasLoggedInvalidClick
                && now - stack64taker$lastInvalidClickLogNanos < STACK64TAKER_LOG_INTERVAL_NANOS) {
            stack64taker$suppressedInvalidClickLogs++;
            return;
        }

        STACK64TAKER_LOGGER.warn(
                "Blocked invalid container click: player={} menu={} containerId={} slotId={} "
                        + "slotCount={} button={} clickType={} suppressedSinceLastLog={}",
                player.getScoreboardName(),
                menu.getClass().getName(),
                menu.containerId,
                slotId,
                menu.slots.size(),
                packet.getButtonNum(),
                packet.getClickType(),
                stack64taker$suppressedInvalidClickLogs);
        stack64taker$hasLoggedInvalidClick = true;
        stack64taker$lastInvalidClickLogNanos = now;
        stack64taker$suppressedInvalidClickLogs = 0;
    }

    @Unique
    private void stack64taker$resyncAtMostOncePerSecond(AbstractContainerMenu menu, long now) {
        if (stack64taker$hasResyncedInvalidClick
                && now - stack64taker$lastInvalidClickResyncNanos < STACK64TAKER_RESYNC_INTERVAL_NANOS) {
            return;
        }

        // 不正パケットは破棄し、正しいスロット一覧とカーソルだけを低頻度で再送する。
        menu.broadcastFullState();
        stack64taker$hasResyncedInvalidClick = true;
        stack64taker$lastInvalidClickResyncNanos = now;
    }
}
