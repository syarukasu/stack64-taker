package dev.stack64taker;

/** バニラのコンテナクリックとして安全に参照できるスロット番号かを判定する。 */
public final class ContainerClickBounds {
    private static final String AE_MENU_PREFIX = "appeng.menu.";
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
            if (menuClass.startsWith(AE_MENU_PREFIX) || menuClass.startsWith(AEWTLIB_MENU_PREFIX)) {
                // AE2/AE2WTLib の仮想スロット処理を尊重し、既存の AE2 処理系へ委譲する。
                return false;
            }
        }
        return isInvalid(slotId, slotCount);
    }
}
