package thelm.packagingprovider.client.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.client.gui.GuiContainerTileBase;
import thelm.packagingprovider.container.ContainerPackagingProvider;
import thelm.packagingprovider.network.PacketHandler;
import thelm.packagingprovider.network.packet.PacketChangeBlocking;
import thelm.packagingprovider.network.packet.PacketChangeProviding;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class GuiPackagingProvider extends GuiContainerTileBase<ContainerPackagingProvider> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagingprovider:textures/gui/packaging_provider.png");

	public GuiPackagingProvider(ContainerPackagingProvider container) {
		super(container);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		super.initGui();
		addButton(new ButtonChangeBlocking(0, guiLeft+62, guiTop+34));
		addButton(new ButtonChangeProvideDirect(0, guiLeft+98, guiTop+34));
		addButton(new ButtonChangeProvidePackaging(0, guiLeft+116, guiTop+34));
		addButton(new ButtonChangeProvideUnpackaging(0, guiLeft+134, guiTop+34));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize/2 - fontRenderer.getStringWidth(s)/2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		for(GuiButton guibutton : buttonList) {
			if(guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof ButtonChangeBlocking) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeBlocking());
		}
		if(button instanceof ButtonChangeProvideDirect) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeProviding(TilePackagingProvider.Type.DIRECT));
		}
		if(button instanceof ButtonChangeProvidePackaging) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeProviding(TilePackagingProvider.Type.PACKAGING));
		}
		if(button instanceof ButtonChangeProvideUnpackaging) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeProviding(TilePackagingProvider.Type.UNPACKAGING));
		}
	}

	class ButtonChangeBlocking extends GuiButton {

		ButtonChangeBlocking(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, container.tile.blocking ? 14 : 0, 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagedauto.unpackager.blocking."+container.tile.blocking), mouseX, mouseY);
		}
	}

	class ButtonChangeProvideDirect extends GuiButton {

		ButtonChangeProvideDirect(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, container.tile.provideDirect ? 42 : 28, 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagingprovider.packaging_provider.direct."+container.tile.provideDirect), mouseX, mouseY);
		}
	}

	class ButtonChangeProvidePackaging extends GuiButton {

		ButtonChangeProvidePackaging(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, container.tile.providePackaging ? 70 : 56, 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagingprovider.packaging_provider.packaging."+container.tile.providePackaging), mouseX, mouseY);
		}
	}

	class ButtonChangeProvideUnpackaging extends GuiButton {

		ButtonChangeProvideUnpackaging(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, container.tile.provideUnpackaging ? 98 : 84, 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagingprovider.packaging_provider.unpackaging."+container.tile.provideUnpackaging), mouseX, mouseY);
		}
	}
}
