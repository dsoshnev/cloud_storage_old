package netty;

import common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectOutputStream;

public class CommandEncoder extends MessageToByteEncoder<Command> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        System.out.printf("write message: %s%n", msg);
        ByteBufOutputStream bbos = new ByteBufOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(bbos);
        oos.writeObject(msg);
    }
}
