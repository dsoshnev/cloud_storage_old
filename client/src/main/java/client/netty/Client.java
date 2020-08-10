package client.netty;

import common.Command;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import netty.CommandDecoder;
import netty.CommandEncoder;

public class Client {

    private static final int DEFAULT_PORT = 8189;
    private static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) throws Exception {

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new CommandDecoder(), new CommandEncoder(), new ClientHandler());
                }
            });

            // Start the client.
            //ChannelFuture f = b.connect(DEFAULT_HOST, DEFAULT_PORT).sync();
            Channel channel = b.connect(DEFAULT_HOST, DEFAULT_PORT).sync().channel();
            System.out.printf("client connected to %s%n", channel.remoteAddress());
            channel.write(Command.authCommand("login1", "pass1", "Dmitry"));
            channel.write(Command.authCommand("login2", "pass1", "Dmitry"));
            channel.flush();
            // Wait until the connection is closed.
            // f.channel().closeFuture().sync()
            channel.close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
