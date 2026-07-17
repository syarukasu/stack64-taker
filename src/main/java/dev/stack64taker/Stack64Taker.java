package dev.stack64taker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Stack64Taker.MODID)
public final class Stack64Taker {
  public static final String MODID = "stack64_taker";
  public static final int DEFAULT_TAKE_AMOUNT = 64;
  public static final int MAX_TAKE_AMOUNT = 1_048_576;
  private static final String PROTOCOL = "12";

  private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(MODID, "main"),
      () -> PROTOCOL,
      PROTOCOL::equals,
      PROTOCOL::equals
  );

  public Stack64Taker() {
    CHANNEL.registerMessage(
        0,
        TakeAmountSlotPacket.class,
        TakeAmountSlotPacket::encode,
        TakeAmountSlotPacket::decode,
        TakeAmountSlotPacket::handle,
        Optional.of(NetworkDirection.PLAY_TO_SERVER)
    );
    CHANNEL.registerMessage(
        1,
        TakeAmountAe2Packet.class,
        TakeAmountAe2Packet::encode,
        TakeAmountAe2Packet::decode,
        TakeAmountAe2Packet::handle,
        Optional.of(NetworkDirection.PLAY_TO_SERVER)
    );
  }

  public static int sanitizeTakeAmount(int amount) {
    if (amount < 1) {
      return 1;
    }

    return Math.min(amount, MAX_TAKE_AMOUNT);
  }

  public static void sendTakeAmountSlotRequest(int menuSlotIndex, int amount) {
    CHANNEL.sendToServer(new TakeAmountSlotPacket(menuSlotIndex, sanitizeTakeAmount(amount)));
  }

  public static void sendTakeAmountAe2Request(long serial, int amount) {
    CHANNEL.sendToServer(new TakeAmountAe2Packet(serial, sanitizeTakeAmount(amount)));
  }

  private static int remainingCursorRoom(AbstractContainerMenu menu, ItemStack incoming) {
    ItemStack carried = menu.getCarried();
    if (carried.isEmpty()) {
      return MAX_TAKE_AMOUNT;
    }

    if (!ItemStack.matches(carried, incoming)) {
      return 0;
    }

    return Math.max(0, MAX_TAKE_AMOUNT - carried.getCount());
  }

  private static boolean putOnCursor(AbstractContainerMenu menu, ItemStack incoming) {
    if (incoming.isEmpty()) {
      return false;
    }

    ItemStack carried = menu.getCarried();
    if (carried.isEmpty()) {
      menu.setCarried(incoming);
      return true;
    }

    if (!ItemStack.matches(carried, incoming)) {
      return false;
    }

    int room = Math.max(0, MAX_TAKE_AMOUNT - carried.getCount());
    int amount = Math.min(room, incoming.getCount());
    if (amount <= 0 || amount != incoming.getCount()) {
      return false;
    }

    carried.grow(amount);
    return true;
  }

  private static void restoreToSlot(Slot slot, ItemStack stack) {
    if (stack.isEmpty()) {
      return;
    }

    ItemStack current = slot.getItem();
    if (current.isEmpty()) {
      slot.set(stack);
      return;
    }

    if (ItemStack.matches(current, stack)) {
      current.grow(stack.getCount());
    }
  }

  private record TakeAmountSlotPacket(int menuSlotIndex, int amount) {
    private static TakeAmountSlotPacket decode(FriendlyByteBuf buffer) {
      return new TakeAmountSlotPacket(buffer.readInt(), buffer.readInt());
    }

    private static void encode(TakeAmountSlotPacket packet, FriendlyByteBuf buffer) {
      buffer.writeInt(packet.menuSlotIndex);
      buffer.writeInt(packet.amount);
    }

    private static void handle(TakeAmountSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = contextSupplier.get();
      context.enqueueWork(() -> takeAmountFromNormalSlot(context.getSender(), packet.menuSlotIndex, packet.amount));
      context.setPacketHandled(true);
    }

    private static void takeAmountFromNormalSlot(ServerPlayer player, int menuSlotIndex, int requestedAmount) {
      if (player == null) {
        return;
      }

      AbstractContainerMenu menu = player.containerMenu;
      if (menu == null) {
        return;
      }

      int amount = sanitizeTakeAmount(requestedAmount);
      takeAmountFromSlot(player, menu, menuSlotIndex, amount);
    }

    private static boolean takeAmountFromSlot(ServerPlayer player, AbstractContainerMenu menu, int menuSlotIndex, int requestedAmount) {
      if (menuSlotIndex < 0 || menuSlotIndex >= menu.slots.size()) {
        return false;
      }

      Slot slot = menu.getSlot(menuSlotIndex);
      if (!slot.mayPickup(player) || !slot.hasItem()) {
        return false;
      }

      ItemStack source = slot.getItem();
      int room = remainingCursorRoom(menu, source);
      int amount = Math.min(Math.min(requestedAmount, source.getCount()), room);
      if (amount <= 0) {
        return false;
      }

      ItemStack taken = slot.remove(amount);
      if (taken.isEmpty()) {
        return false;
      }

      if (!putOnCursor(menu, taken)) {
        restoreToSlot(slot, taken);
        slot.setChanged();
        menu.broadcastChanges();
        return false;
      }

      slot.setChanged();
      menu.broadcastChanges();
      return true;
    }
  }

  private record TakeAmountAe2Packet(long serial, int amount) {
    private static TakeAmountAe2Packet decode(FriendlyByteBuf buffer) {
      return new TakeAmountAe2Packet(buffer.readLong(), buffer.readInt());
    }

    private static void encode(TakeAmountAe2Packet packet, FriendlyByteBuf buffer) {
      buffer.writeLong(packet.serial);
      buffer.writeInt(packet.amount);
    }

    private static void handle(TakeAmountAe2Packet packet, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = contextSupplier.get();
      context.enqueueWork(() -> takeAmountFromAe2(context.getSender(), packet.serial, packet.amount));
      context.setPacketHandled(true);
    }

    private static void takeAmountFromAe2(ServerPlayer player, long serial, int requestedAmount) {
      if (player == null || serial < 0) {
        return;
      }

      AbstractContainerMenu menu = player.containerMenu;
      if (menu == null) {
        return;
      }

      try {
        Class<?> meStorageMenuClass = Class.forName("appeng.menu.me.common.MEStorageMenu");
        if (!meStorageMenuClass.isInstance(menu)) {
          return;
        }

        Method getStackBySerial = meStorageMenuClass.getDeclaredMethod("getStackBySerial", long.class);
        getStackBySerial.setAccessible(true);
        Object key = getStackBySerial.invoke(menu, serial);
        if (key == null) {
          return;
        }

        Class<?> aeItemKeyClass = Class.forName("appeng.api.stacks.AEItemKey");
        if (!aeItemKeyClass.isInstance(key)) {
          return;
        }

        Method toStack = aeItemKeyClass.getMethod("toStack", int.class);
        ItemStack probe = (ItemStack) toStack.invoke(key, 1);
        int room = remainingCursorRoom(menu, probe);
        int amount = Math.min(sanitizeTakeAmount(requestedAmount), room);
        if (amount <= 0) {
          return;
        }

        Field storageField = meStorageMenuClass.getDeclaredField("storage");
        storageField.setAccessible(true);
        Object storage = storageField.get(menu);
        Field powerSourceField = meStorageMenuClass.getDeclaredField("powerSource");
        powerSourceField.setAccessible(true);
        Object powerSource = powerSourceField.get(menu);
        Object actionSource = menu.getClass().getMethod("getActionSource").invoke(menu);
        if (storage == null || powerSource == null || actionSource == null) {
          return;
        }

        Class<?> storageHelperClass = Class.forName("appeng.api.storage.StorageHelper");
        Class<?> energySourceClass = Class.forName("appeng.api.networking.energy.IEnergySource");
        Class<?> storageClass = Class.forName("appeng.api.storage.MEStorage");
        Class<?> aeKeyClass = Class.forName("appeng.api.stacks.AEKey");
        Class<?> actionSourceClass = Class.forName("appeng.api.networking.security.IActionSource");
        Method poweredExtraction = storageHelperClass.getMethod(
            "poweredExtraction",
            energySourceClass,
            storageClass,
            aeKeyClass,
            long.class,
            actionSourceClass
        );

        Object extractedResult = poweredExtraction.invoke(null, powerSource, storage, key, (long) amount, actionSource);
        long extracted = extractedResult instanceof Long value ? value : 0L;
        if (extracted <= 0L) {
          return;
        }

        int carriedAmount = (int) Math.min((long) amount, extracted);
        ItemStack carried = (ItemStack) toStack.invoke(key, carriedAmount);
        if (carried.isEmpty()) {
          return;
        }

        if (!putOnCursor(menu, carried)) {
          return;
        }

        menu.broadcastChanges();
      } catch (ReflectiveOperationException | ClassCastException | IllegalArgumentException | SecurityException ignored) {
        // AE2 is optional; if its API changes, ignore only the AE2 shortcut path.
      }
    }
  }
}
