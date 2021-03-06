package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.Slotlink
import badasintended.slotlink.common.util.spinneryId
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import spinnery.client.render.BaseRenderer
import spinnery.common.registry.ThemeRegistry
import spinnery.widget.WAbstractWidget
import spinnery.widget.api.Style
import kotlin.math.floor

@Environment(EnvType.CLIENT)
class WCraftingArrow : WAbstractWidget() {

    private val texture = Slotlink.id("textures/gui/arrow.png")

    override fun draw(matrices: MatrixStack, provider: VertexConsumerProvider) {
        val x = floor(x)
        val y = floor(y)

        val slotStyle = Style.of(
            ThemeRegistry.getStyle(
                theme, spinneryId("slot")
            )
        )
        val tint = slotStyle.asColor("background.unfocused")

        BaseRenderer.drawTexturedQuad(matrices, provider, x, y, z, 22f, 15f, tint, texture)
    }

}
