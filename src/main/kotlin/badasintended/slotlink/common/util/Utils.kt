package badasintended.slotlink.common.util

import badasintended.slotlink.Slotlink
import badasintended.slotlink.inventory.DummyInventory
import com.google.common.collect.ImmutableMap
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import spinnery.common.handler.BaseScreenHandler
import spinnery.common.registry.NetworkRegistry
import spinnery.common.utility.StackUtilities
import spinnery.widget.api.*

fun spinneryId(id: String) = Identifier("spinnery", id)

/**
 * I just want ints on my gui
 */
@Environment(EnvType.CLIENT)
fun positionOf(x: Int, y: Int, z: Int): Position = Position.of(x.toFloat(), y.toFloat(), z.toFloat())

@Environment(EnvType.CLIENT)
fun positionOf(anchor: WPositioned, x: Int, y: Int, z: Int = 0): Position {
    return Position.of(anchor, x.toFloat(), y.toFloat(), z.toFloat())
}

@Environment(EnvType.CLIENT)
fun sizeOf(x: Int, y: Int): Size = Size.of(x.toFloat(), y.toFloat())

@Environment(EnvType.CLIENT)
fun sizeOf(s: Int): Size = Size.of(s.toFloat())

@Environment(EnvType.CLIENT)
fun slotAction(container: BaseScreenHandler, slotN: Int, invN: Int, button: Int, action: Action, player: PlayerEntity) {
    container.onSlotAction(slotN, invN, button, action, player)
    ClientSidePacketRegistry.INSTANCE.sendToServer(
        NetworkRegistry.SLOT_CLICK_PACKET,
        NetworkRegistry.createSlotClickPacket(container.syncId, slotN, invN, button, action)
    )
}

fun BlockPos.toTag(): CompoundTag {
    val tag = CompoundTag()
    tag.putInt("x", x)
    tag.putInt("y", y)
    tag.putInt("z", z)
    return tag
}

fun CompoundTag.toPos(): BlockPos {
    return BlockPos(getInt("x"), getInt("y"), getInt("z"))
}

fun PlayerEntity.actionBar(key: String, vararg args: Any) {
    if (this is ServerPlayerEntity) sendMessage(
        TranslatableText(key, *args), true
    )
}

fun PlayerEntity.chat(key: String, vararg args: Any) {
    if (this is ServerPlayerEntity) sendMessage(
        TranslatableText(key, *args), false
    )
}

fun buf(): PacketByteBuf {
    return PacketByteBuf(Unpooled.buffer())
}

/**
 * Generates [VoxelShape] based on the position that shows on [Blockbench](https://blockbench.net).
 * No thinking required!
 */
fun bbCuboid(xPos: Int, yPos: Int, zPos: Int, xSize: Int, ySize: Int, zSize: Int): VoxelShape {
    val xMin = xPos / 16.0
    val yMin = yPos / 16.0
    val zMin = zPos / 16.0
    val xMax = (xPos + xSize) / 16.0
    val yMax = (yPos + ySize) / 16.0
    val zMax = (zPos + zSize) / 16.0
    return VoxelShapes.cuboid(xMin, yMin, zMin, xMax, yMax, zMax)
}

fun BlockPos.around(): ImmutableMap<Direction, BlockPos> {
    return ImmutableMap
        .builder<Direction, BlockPos>()
        .put(Direction.NORTH, north())
        .put(Direction.SOUTH, south())
        .put(Direction.EAST, east())
        .put(Direction.WEST, west())
        .put(Direction.UP, up())
        .put(Direction.DOWN, down())
        .build()
}

@Environment(EnvType.CLIENT)
fun Direction.texture(): Identifier {
    return Slotlink.id("textures/gui/side_${asString()}.png")
}

fun Direction.next(): Direction {
    return Direction.byId(id + 1)
}

fun Inventory.mergeStack(slot: Int, source: ItemStack) {
    var target = getStack(slot)
    for (i in target.count until target.maxCount) {
        if (!isValid(slot, source)) return
        if (target.isEmpty) {
            val stack = source.copy()
            stack.count = 1
            setStack(slot, stack)
            source.decrement(1)
            target = getStack(slot)
        } else {
            if (!StackUtilities.equalItemAndTag(source, target)) return
            target.increment(1)
            source.decrement(1)
        }
        if (target.count == target.maxCount) return
    }
}

fun PacketByteBuf.writeInventorySet(inventories: Set<Inventory>) {
    writeVarInt(inventories.size)
    inventories.forEach {
        writeVarInt(it.size())
    }
}

fun PacketByteBuf.readInventorySet(): LinkedHashSet<Inventory> {
    val set = linkedSetOf<Inventory>()
    for (i in 0 until readVarInt()) {
        set.add(DummyInventory(readVarInt()))
    }
    return set
}

fun PacketByteBuf.writeInventory(stacks: DefaultedList<ItemStack>) {
    writeVarInt(stacks.size)
    stacks.forEach { writeItemStack(it) }
}

fun PacketByteBuf.readInventory(): DefaultedList<ItemStack> {
    val stack = DefaultedList.ofSize(readVarInt(), ItemStack.EMPTY)
    for (i in 0 until stack.size) {
        stack[i] = readItemStack()
    }
    return stack
}
