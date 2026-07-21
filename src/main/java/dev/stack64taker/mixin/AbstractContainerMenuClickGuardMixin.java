package dev.stack64taker.mixin;

import java.util.concurrent.TimeUnit;

import dev.stack64taker.ContainerClickBounds;
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

@Mixin(value = AbstractContainerMenu.class, priority = 2000)
public abstract class AbstractContainerMenuClickGuardMixin {
    @Unique
    private static final Logger STACK64TAKER_LOGGER = LoggerFactory.getLogger("Stack64Taker/MenuClickGuard");
    @Unique
    private static final long STACK64TAKER_LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30);

    @Unique
    private boolean stack64taker$hasLoggedInvalidMenuClick;
    @Unique
    private long stack64taker$lastInvalidMenuClickLogNanos;

    @Inject(method = "m_150399_", at = @At("HEAD"), cancellable = true, remap = false)
    private void stack64taker$guardInvalidMenuClick(
            int slotId,
            int button,
            ClickType clickType,
            Player player,
            CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!ContainerClickBounds.isInvalid(slotId, menu.slots.size(), menu)) {
            return;
        }

        // パケット以外からclickedが呼ばれた場合も、getSlotより前の最終境界で止める。
        ci.cancel();
        if (player == null || player.level().isClientSide) {
            return;
        }

        long now = System.nanoTime();
        if (stack64taker$hasLoggedInvalidMenuClick
                && now - stack64taker$lastInvalidMenuClickLogNanos < STACK64TAKER_LOG_INTERVAL_NANOS) {
            return;
        }

        STACK64TAKER_LOGGER.warn(
                "Blocked invalid menu click at final boundary: player={} menu={} containerId={} "
                        + "slotId={} slotCount={} button={} clickType={}",
                player.getScoreboardName(),
                menu.getClass().getName(),
                menu.containerId,
                slotId,
                menu.slots.size(),
                button,
                clickType);
        stack64taker$hasLoggedInvalidMenuClick = true;
        stack64taker$lastInvalidMenuClickLogNanos = now;
    }
}
