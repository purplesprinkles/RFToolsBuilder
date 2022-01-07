package mcjty.rftoolsbuilder.modules.builder.network;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This is a packet that can be used to update the NBT on the held item of a player.
 */
public class PacketUpdateNBTShapeCard {
    private TypedMap args;

    public void toBytes(FriendlyByteBuf buf) {
        TypedMapTools.writeArguments(buf, args);
    }

    public PacketUpdateNBTShapeCard() {
    }

    public PacketUpdateNBTShapeCard(FriendlyByteBuf buf) {
        args = TypedMapTools.readArguments(buf);
    }

    public PacketUpdateNBTShapeCard(TypedMap arguments) {
        this.args = arguments;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer playerEntity = ctx.getSender();
            ItemStack heldItem = playerEntity.getItemInHand(InteractionHand.MAIN_HAND);
            if (heldItem.isEmpty()) {
                return;
            }
            CompoundTag tagCompound = heldItem.getTag();
            if (tagCompound == null) {
                tagCompound = new CompoundTag();
                heldItem.setTag(tagCompound);
            }
            for (Key<?> akey : args.getKeys()) {
                String key = akey.name();
                if (Type.STRING.equals(akey.type())) {
                    tagCompound.putString(key, (String) args.get(akey));
                } else if (Type.INTEGER.equals(akey.type())) {
                    tagCompound.putInt(key, (Integer) args.get(akey));
                } else if (Type.DOUBLE.equals(akey.type())) {
                    tagCompound.putDouble(key, (Double) args.get(akey));
                } else if (Type.BOOLEAN.equals(akey.type())) {
                    tagCompound.putBoolean(key, (Boolean) args.get(akey));
                } else if (Type.BLOCKPOS.equals(akey.type())) {
                    throw new RuntimeException("BlockPos not supported for PacketUpdateNBTItem!");
                } else if (Type.ITEMSTACK.equals(akey.type())) {
                    throw new RuntimeException("ItemStack not supported for PacketUpdateNBTItem!");
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}