package badasintended.slotlink.block.entity

import badasintended.slotlink.block.ModBlock
import badasintended.slotlink.common.util.*
import badasintended.slotlink.gui.screen.TransferScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

abstract class TransferCableBlockEntity(type: BlockEntityType<out BlockEntity>) : ConnectorCableBlockEntity(type),
    ExtendedScreenHandlerFactory {

    var isBlackList = false
    var filter: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)

    abstract var side: Direction

    abstract fun transfer(world: World, master: MasterBlockEntity)

    protected fun ItemStack.isValid(): Boolean {
        if (isEmpty) return false
        if (filter.all { it.isEmpty }) return true
        if (isBlackList) {
            if (filter.any { it.item == item }) return false
        } else {
            if (filter.none { it.item == item }) return false
        }
        return true
    }

    override fun WorldAccess.isBlockIgnored(block: Block) = block is ModBlock

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)

        side = Direction.byId(tag.getInt("side"))
        isBlackList = tag.getBoolean("isBlacklist")
        Inventories.fromTag(tag.getCompound("filter"), filter)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)

        tag.putInt("side", side.id)
        tag.putBoolean("isBlacklist", isBlackList)
        tag.put("filter", Inventories.toTag(CompoundTag(), filter))

        return tag
    }

    override fun markDirty() {
        super.markDirty()

        if (hasMaster) {
            val master = world?.getBlockEntity(masterPos.toPos())

            if (master is MasterBlockEntity) {
                master.transferCables.add(pos.toTag())
                master.markDirty()
            }
        }
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        return TransferScreenHandler(syncId, inv, pos, side, isBlackList, filter)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeVarInt(side.id)
        buf.writeBoolean(isBlackList)
        buf.writeInventory(filter)
    }

    override fun getDisplayName() = TranslatableText("container.slotlink.transfer")

}
