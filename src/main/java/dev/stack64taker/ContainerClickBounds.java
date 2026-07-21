package dev.stack64taker;

/** バニラのコンテナクリックとして安全に参照できるスロット番号かを判定する。 */
public final class ContainerClickBounds {
    private static final String AE_MENU_PREFIX = "appeng.menu.";
    private static final String AE_ME_MENU_PREFIX = "appeng.menu.me.";
    private static final String AEWTLIB_MENU_PREFIX = "de.mari_023.ae2wtlib.";

    private ContainerClickBounds() {
    }

    public static boolean isInvalid(int slotId, int slotCount) {
        // -1や-999はカーソル外クリックなどに使われるため、負数はバニラへ委ねる。
        return slotId >= 0 && slotId >= Math.max(0, slotCount);
    }

    public static boolean isInvalid(int slotId, int slotCount, Object menu) {
        if (menu != null) {
            String menuClass = menu.getClass().getName();
            if (isAe2VirtualSlotMenu(menuClass) || isAe2wtlibMenu(menuClass)) {
                // AE2/AE2WTLib の仮想スロット処理を尊重し、既存の処理系へ委譲する。
                return false;
            }
        }
        return isInvalid(slotId, slotCount);
    }

    private static boolean isAe2VirtualSlotMenu(String menuClass) {
        // 具体的に仮想スロットを扱う AE2/AdvancedAE のメニュー群だけを対象化する。
        if (!menuClass.startsWith(AE_MENU_PREFIX)) {
            return false;
        }
        return menuClass.startsWith(AE_ME_MENU_PREFIX);
    }

    private static boolean isAe2wtlibMenu(String menuClass) {
        return menuClass.startsWith(AEWTLIB_MENU_PREFIX);
    }
}
