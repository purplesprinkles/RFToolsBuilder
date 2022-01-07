package mcjty.rftoolsbuilder.modules.scanner.network;

import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.shapes.IFormula;
import mcjty.rftoolsbuilder.shapes.Shape;
import mcjty.rftoolsbuilder.shapes.ShapeDataManagerServer;
import mcjty.rftoolsbuilder.shapes.ShapeID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestShapeData {
    private final ItemStack card;
    private final ShapeID id;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(card);
        id.toBytes(buf);
    }

    public PacketRequestShapeData(FriendlyByteBuf buf) {
        card = buf.readItem();
        id = new ShapeID(buf);
    }

    public PacketRequestShapeData(ItemStack card, ShapeID id) {
        this.card = card;
        this.id = id;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Shape shape = ShapeCardItem.getShape(card);
            boolean solid = ShapeCardItem.isSolid(card);
            BlockPos dimension = ShapeCardItem.getDimension(card);

            BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
            int dy = clamped.getY();
            ItemStack copy = card.copy();

            IFormula formula = shape.getFormulaFactory().get();
            formula = formula.correctFormula(solid);
            formula.setup(ctx.getSender().getLevel(), new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), copy.getTag());

            for (int y = 0 ; y < dy ; y++) {
                ShapeDataManagerServer.pushWork(id, copy, y, formula, ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
