package server.netty;

import common.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import server.LogService;

import java.util.Date;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command) msg;
        System.out.print(command);
        System.out.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogService.info("client %s connected at %s", ctx.channel().remoteAddress(), new Date());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogService.info("client %s disconnected at %s", ctx.channel().remoteAddress(), new Date());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();

    }
}
