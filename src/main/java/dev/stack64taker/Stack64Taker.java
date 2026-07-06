package dev.stack64taker;

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
  private static final String PROTOCOL = "1";

  private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(MODID, "main"),
      () -> PROTOCOL,
      PROTOCOL::equals,
      PROTOCOL::equals
  );

  public Stack64Taker() {
    CHANNEL.registerMessage(
        0,
        Take64Packet.class,
        Take64Packet::encode,
        Take64Packet::decode,
        Take64Packet::handle,
        Optional.of(NetworkDirection.PLAY_TO_SERVER)
    );
  }

  public static void sendTake64Request(int menuSlotIndex) {
    CHANNEL.sendToServer(new Take64Packet(menuSlotIndex));
  }

  private record Take64Packet(int menuSlotIndex) {
    private static Take64Packet decode(FriendlyByteBuf buffer) {
      return new Take64Packet(buffer.readInt());
    }

    private static void encode(Take64Packet packet, FriendlyByteBuf buffer) {
      buffer.writeInt(packet.menuSlotIndex);
    }

    private static void handle(Take64Packet packet, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = contextSupplier.get();
      context.enqueueWork(() -> take64(context.getSender(), packet.menuSlotIndex));
      context.setPacketHandled(true);
    }

    private static void take64(ServerPlayer player, int menuSlotIndex) {
      if (player == null) {
        return;
      }

      AbstractContainerMenu menu = player.f_36096_;
      if (menuSlotIndex < 0 || menuSlotIndex >= menu.f_38839_.size()) {
        return;
      }

      if (!menu.m_142621_().m_41619_()) {
        return;
      }

      Slot slot = menu.m_38853_(menuSlotIndex);
      if (!slot.m_8010_(player) || !slot.m_6657_()) {
        return;
      }

      ItemStack source = slot.m_7993_();
      if (source.m_41613_() <= 64) {
        return;
      }

      ItemStack taken = slot.m_6201_(64);
      if (taken.m_41619_()) {
        return;
      }

      menu.m_142503_(taken);
      slot.m_6654_();
      menu.m_38946_();
    }
  }
}
