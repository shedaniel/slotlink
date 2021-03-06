package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.common.util.spinneryId
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText
import spinnery.client.render.TextRenderer
import spinnery.common.registry.ThemeRegistry
import spinnery.widget.WStaticText
import spinnery.widget.api.Style

@Environment(EnvType.CLIENT)
class WTranslatableLabel(key: String, vararg args: Any) : WStaticText() {

    init {
        setText<WStaticText>(TranslatableText(key, *args))
    }

    override fun draw(matrices: MatrixStack, provider: VertexConsumerProvider) {
        if (isHidden) return

        val panelStyle = Style.of(
            ThemeRegistry.getStyle(
                theme, spinneryId("panel")
            )
        )

        TextRenderer
            .pass()
            .text(text)
            .at(x, y, z)
            .shadow(false)
            .color(panelStyle.asColor("label.color"))
            .render(matrices, provider)
    }

}
