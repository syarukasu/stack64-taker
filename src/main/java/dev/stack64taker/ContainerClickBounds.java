package dev.stack64taker;

/** バニラのコンテナクリックとして安全に参照できるスロット番号かを判定する。 */
public final class ContainerClickBounds {
    private ContainerClickBounds() {
    }

    public static boolean isInvalid(int slotId, int slotCount) {
        // -1や-999はカーソル外クリックなどに使われるため、負数はバニラへ委ねる。
        return slotId >= 0 && slotId >= Math.max(0, slotCount);
    }
}
