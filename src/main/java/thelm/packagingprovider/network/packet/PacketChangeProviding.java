package thelm.packagingprovider.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.network.ISelfHandleMessage;
import thelm.packagingprovider.container.ContainerPackagingProvider;
import thelm.packagingprovider.tile.TilePackagingProvider;

public class PacketChangeProviding implements ISelfHandleMessage<IMessage> {

	private TilePackagingProvider.Type type;

	public PacketChangeProviding() {}

	public PacketChangeProviding(TilePackagingProvider.Type type) {
		this.type = type;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(type.ordinal());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = TilePackagingProvider.Type.values()[buf.readByte()];
	}

	@Override
	public IMessage onMessage(MessageContext ctx) {
		EntityPlayerMP player = ctx.getServerHandler().player;
		WorldServer world = player.getServerWorld();
		world.addScheduledTask(()->{
			if(player.openContainer instanceof ContainerPackagingProvider) {
				ContainerPackagingProvider container = (ContainerPackagingProvider)player.openContainer;
				container.tile.changeProvideType(type);	
			}
		});
		return null;
	}
}
