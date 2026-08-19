package dev.stack64taker.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.minecraft.world.inventory.ClickType;
import org.junit.jupiter.api.Test;

class Ae2ClientClickBridgeTest {
    @Test
    void emptyCraftableEntryOpensAutocrafting() {
        assertEquals(
                "AUTO_CRAFT",
                Ae2ClientClickBridge.actionName(ClickType.PICKUP, 0, true, 0L, null));
    }

    @Test
    void storedEntryKeepsNormalPickupActions() {
        assertEquals(
                "PICKUP_OR_SET_DOWN",
                Ae2ClientClickBridge.actionName(ClickType.PICKUP, 0, true, 1L, null));
        assertEquals(
                "SPLIT_OR_PLACE_SINGLE",
                Ae2ClientClickBridge.actionName(ClickType.PICKUP, 1, true, 1L, null));
    }

    @Test
    void quickMoveKeepsAe2ButtonMapping() {
        assertEquals(
                "SHIFT_CLICK",
                Ae2ClientClickBridge.actionName(ClickType.QUICK_MOVE, 0, false, 1L, null));
        assertEquals(
                "PICKUP_SINGLE",
                Ae2ClientClickBridge.actionName(ClickType.QUICK_MOVE, 1, false, 1L, null));
    }

    @Test
    void unsupportedContainerClickIsNotTranslated() {
        assertNull(Ae2ClientClickBridge.actionName(ClickType.THROW, 0, false, 1L, null));
    }
}
