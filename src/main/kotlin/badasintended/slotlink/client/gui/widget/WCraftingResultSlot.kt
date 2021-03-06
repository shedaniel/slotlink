package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.common.registry.NetworkRegistry.CRAFT_ONCE
import badasintended.slotlink.common.registry.NetworkRegistry.CRAFT_STACK
import badasintended.slotlink.common.util.buf
import badasintended.slotlink.gui.screen.RequestScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry.INSTANCE
import net.minecraft.client.gui.screen.Screen
import spinnery.common.registry.NetworkRegistry.SLOT_CLICK_PACKET
import spinnery.common.registry.NetworkRegistry.createSlotClickPacket
import spinnery.common.utility.StackUtilities.equalItemAndTag
import spinnery.widget.WSlot
import spinnery.widget.api.Action.CLONE

@Environment(EnvType.CLIENT)
class WCraftingResultSlot : WSlot() {

    override fun onMouseReleased(mouseX: Float, mouseY: Float, button: Int) {
        //sort.invoke()
    }

    override fun onMouseDragged(mouseX: Float, mouseY: Float, button: Int, deltaX: Double, deltaY: Double) {}

    override fun onMouseClicked(mouseX: Float, mouseY: Float, button: Int) {
        if (!isFocused) return

        val container = `interface`.handler as RequestScreenHandler
        val player = container.player

        val cursorStack = player.inventory.cursorStack

        if (Screen.hasShiftDown()) {
            container.craftStack()
            if (button == LEFT) INSTANCE.sendToServer(
                CRAFT_STACK, buf()
            )
        } else {
            if ((button == LEFT) or (button == RIGHT)) {
                if ((!equalItemAndTag(
                        cursorStack, stack
                    ) and !cursorStack.isEmpty) or ((cursorStack.count + stack.count) > cursorStack.maxCount)
                ) return
                container.craftOnce()
                INSTANCE.sendToServer(CRAFT_ONCE, buf())
            } else if (button == MIDDLE) {
                container.onSlotAction(slotNumber, inventoryNumber, button, CLONE, player)
                INSTANCE.sendToServer(
                    SLOT_CLICK_PACKET,
                    createSlotClickPacket(container.syncId, slotNumber, inventoryNumber, button, CLONE)
                )
            }
        }
    }

}
