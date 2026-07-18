package dev.stack64taker.mixin;

import java.util.concurrent.TimeUnit;

import dev.stack64taker.ContainerClickBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiPlayerGameMode.class, priority = 2000)
public abstract class ClientContainerClickGuardMixin {
    @Unique
    private static final Logger STACK64TAKER_LOGGER = LoggerFactory.getLogger("Stack64Taker/ClientClickGuard");
    @Unique
    private static final long STACK64TAKER_LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30);

    @Unique
    private boolean stack64taker$hasLoggedInvalidOutgoingClick;
    @Unique
    private long stack64taker$lastInvalidOutgoingClickLogNanos;

    @Inject(method = "m_171799_", at = @At("HEAD"), cancellable = true, remap = false)
    private void stack64taker$guardInvalidOutgoingClick(
            int containerId,
            int slotId,
            int button,
            ClickType clickType,
            Player player,
            CallbackInfo ci) {
        if (slotId < 0 || player == null) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        AbstractContainerMenu menu = containerScreen.getMenu();
        if (menu == null
                || player.containerMenu != menu
                || containerId != menu.containerId
                || !ContainerClickBounds.isInvalid(slotId, menu.slots.size())) {
            return;
        }

        // クライアント自身が範囲外と分かる場合だけ遮断し、MOD独自GUIの処理には触れない。
        ci.cancel();
        long now = System.nanoTime();
        if (stack64taker$hasLoggedInvalidOutgoingClick
                && now - stack64taker$lastInvalidOutgoingClickLogNanos < STACK64TAKER_LOG_INTERVAL_NANOS) {
            return;
        }

        STACK64TAKER_LOGGER.warn(
                "Blocked invalid outgoing container click: screen={} menu={} containerId={} "
                        + "slotId={} slotCount={} button={} clickType={}",
                screen.getClass().getName(),
                menu.getClass().getName(),
                containerId,
                slotId,
                menu.slots.size(),
                button,
                clickType);
        stack64taker$hasLoggedInvalidOutgoingClick = true;
        stack64taker$lastInvalidOutgoingClickLogNanos = now;
    }
}
