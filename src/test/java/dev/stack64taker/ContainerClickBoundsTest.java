package dev.stack64taker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContainerClickBoundsTest {
    @Test
    void negativeVanillaSentinelsRemainAllowed() {
        assertFalse(ContainerClickBounds.isInvalid(-1, 51));
        assertFalse(ContainerClickBounds.isInvalid(-999, 51));
    }

    @Test
    void lastRealSlotRemainsAllowed() {
        assertFalse(ContainerClickBounds.isInvalid(50, 51));
    }

    @Test
    void firstOutOfRangeSlotIsRejected() {
        assertTrue(ContainerClickBounds.isInvalid(51, 51));
        assertTrue(ContainerClickBounds.isInvalid(102, 51));
    }

    @Test
    void anyNonNegativeSlotIsRejectedForAnEmptyMenu() {
        assertTrue(ContainerClickBounds.isInvalid(0, 0));
    }
}
