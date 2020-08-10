package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ObjectInputStream;
import java.util.List;

public class CommandDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.printf("bytes to read %s%n", in.readableBytes());
        ByteBufInputStream bbis = new ByteBufInputStream(in);
        ObjectInputStream ois = new ObjectInputStream(bbis);
        out.add(ois.readObject());
    }
}
