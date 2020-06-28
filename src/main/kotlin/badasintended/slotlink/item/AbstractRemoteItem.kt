package badasintended.slotlink.item

import badasintended.slotlink.block.MasterBlock
import badasintended.slotlink.common.*
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Hand.MAIN_HAND
import net.minecraft.util.Hand.OFF_HAND
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

abstract class AbstractRemoteItem(id: String) : ModItem(id) {

    protected val baseTlKey = "item.slotlink.remote"

    protected open fun use(
        world: World,
        player: PlayerEntity,
        stack: ItemStack,
        hand: Hand,
        masterPos: BlockPos,
        masterDim: DimensionType?
    ) {
        if (masterDim == null) {
            addChat(world, player, "${baseTlKey}.invalidDimension")
        } else if (masterDim != world.dimension.type) {
            // multi dimension remote is not really possible with my knowledge
            addChat(world, player, "${baseTlKey}.differentDimension")
        } else {
            if (!world.isClient) {
                openScreen("remote", player) { buf ->
                    buf.writeBlockPos(BlockPos(player.pos))
                    buf.writeInt(stack.orCreateTag.getInt("lastSort"))
                    writeRequestData(buf, world, masterPos)
                    buf.writeBoolean(hand == OFF_HAND)
                }
            }
        }
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = when (hand) {
            MAIN_HAND -> player.mainHandStack
            OFF_HAND -> player.offHandStack
        }

        val masterPosTag = stack.orCreateTag.getCompound("masterPos")
        val masterDim = DimensionType.byId(Identifier(stack.orCreateTag.getString("masterDim")))

        if (masterPosTag == CompoundTag()) {
            addChat(world, player, "${baseTlKey}.hasNoMaster")
        } else use(world, player, stack, hand, tag2Pos(masterPosTag), masterDim)

        return TypedActionResult.fail(stack)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player
        val stack = context.stack
        val world = context.world
        val pos = context.blockPos
        val dimId = DimensionType.getId(world.dimension.type).toString()

        val block = world.getBlockState(pos).block
        if (block is MasterBlock) {
            if (player != null) if (player.isSneaking) {
                stack.orCreateTag.put("masterPos", pos2Tag(pos))
                stack.orCreateTag.putString("masterDim", dimId)
                addChat(world, player, "${baseTlKey}.linked", pos.x, pos.y, pos.z, dimId)
                return ActionResult.CONSUME
            }
        }

        return ActionResult.PASS
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)

        val masterPosTag = stack.orCreateTag.getCompound("masterPos")
        if (masterPosTag != CompoundTag()) {
            val masterPos = tag2Pos(masterPosTag)
            val masterDim = Identifier(stack.orCreateTag.getString("masterDim"))
            tooltip.add(
                LiteralText("§5").append(
                    TranslatableText("${baseTlKey}.info", masterPos.x, masterPos.y, masterPos.z, masterDim)
                )
            )
        }
    }

}