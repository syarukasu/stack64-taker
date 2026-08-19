package dev.stack64taker.client;

import java.lang.reflect.Method;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** AE2のRepoSlotだけを、AE2本来のMEInteraction経路へ戻すクライアントBridge。 */
public final class Ae2ClientClickBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("Stack64Taker/AE2ClickBridge");
    private static final String REPO_SLOT_CLASS = "appeng.client.gui.me.common.RepoSlot";
    private static final String ME_STORAGE_MENU_CLASS = "appeng.menu.me.common.MEStorageMenu";
    private static final String INVENTORY_ACTION_CLASS = "appeng.helpers.InventoryAction";

    private Ae2ClientClickBridge() {
    }

    /**
     * 範囲外container clickが実際にはAE2 RepoSlot操作なら専用Actionとして処理する。
     * 変換できない操作はfalseを返し、呼出側の通常境界ガードで遮断する。
     */
    public static boolean handleVirtualSlot(
            AbstractContainerScreen<?> screen,
            AbstractContainerMenu menu,
            int button,
            ClickType clickType,
            Player player) {
        // 対象Menu以外へAE2クラスを反射適用しない。
        if (screen == null || menu == null || !isTypeOrSubtype(menu.getClass(), ME_STORAGE_MENU_CLASS)) {
            return false;
        }

        Slot slot = screen.getSlotUnderMouse();
        // 実際にマウス下にあるRepoSlotだけを仮想操作として認める。
        if (slot == null || !isTypeOrSubtype(slot.getClass(), REPO_SLOT_CLASS)) {
            return false;
        }

        try {
            Object entry = slot.getClass().getMethod("getEntry").invoke(slot);
            // 表示更新直後の空RepoSlotには送信対象がない。
            if (entry == null) {
                return false;
            }

            long serial = ((Number) entry.getClass().getMethod("getSerial").invoke(entry)).longValue();
            boolean craftable = (Boolean) entry.getClass().getMethod("isCraftable").invoke(entry);
            long storedAmount = ((Number) entry.getClass().getMethod("getStoredAmount").invoke(entry)).longValue();
            String actionName = actionName(clickType, button, craftable, storedAmount, player);
            // AE2本来の画面が意味を定義していないClickTypeは変換しない。
            if (actionName == null) {
                return false;
            }

            Class<?> actionClass = Class.forName(INVENTORY_ACTION_CLASS);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object action = Enum.valueOf((Class<? extends Enum>) actionClass.asSubclass(Enum.class), actionName);
            Method handleInteraction = findMethod(menu.getClass(), "handleInteraction", long.class, actionClass);
            // 互換アドオンがMenu階層を変更していても、実メソッドを確認できた時だけ実行する。
            if (handleInteraction == null) {
                return false;
            }
            handleInteraction.invoke(menu, serial, action);
            return true;
        } catch (ReflectiveOperationException | ClassCastException | LinkageError exception) {
            LOGGER.warn("Could not route an AE2 RepoSlot click through MEInteraction", exception);
            return false;
        }
    }

    static String actionName(
            ClickType clickType,
            int button,
            boolean craftable,
            long storedAmount,
            Player player) {
        // 左クリックで在庫0かつクラフト可能なら、AE2標準と同じく自動クラフトを開く。
        if (clickType == ClickType.PICKUP && button == 0 && craftable && storedAmount == 0L) {
            return "AUTO_CRAFT";
        }
        // PICKUPは右ボタンだけ単数操作、左ボタンは通常の取得・設置操作になる。
        if (clickType == ClickType.PICKUP) {
            return button == 1 ? "SPLIT_OR_PLACE_SINGLE" : "PICKUP_OR_SET_DOWN";
        }
        // QUICK_MOVEはAE2標準のボタン割当をそのまま維持する。
        if (clickType == ClickType.QUICK_MOVE) {
            return button == 1 ? "PICKUP_SINGLE" : "SHIFT_CLICK";
        }
        // 中クリックはクラフト可能表示を優先し、それ以外はCreative複製だけを許可する。
        if (clickType == ClickType.CLONE && craftable) {
            return "AUTO_CRAFT";
        }
        if (clickType == ClickType.CLONE && player != null && player.getAbilities().instabuild) {
            return "CREATIVE_DUPLICATE";
        }
        return null;
    }

    private static boolean isTypeOrSubtype(Class<?> type, String expectedName) {
        // アドオンの派生Screen/Menuも、実際の継承階層に基底AE2型がある場合だけ認める。
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (expectedName.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Method findMethod(
            Class<?> type,
            String methodName,
            Class<?> firstParameter,
            Class<?> secondParameter) {
        // 公開メソッドを親型まで検索し、推測した記述子で直接呼ばない。
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(methodName, firstParameter, secondParameter);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // 次の親型を確認する。
            }
        }
        return null;
    }
}
