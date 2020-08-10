package server.netty;

import common.Command;
import common.FileUtility;
import common.UserData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import netty.CommandDecoder;
import netty.CommandEncoder;
import server.AuthService;
import server.BaseAuthService;
import server.LogService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private static final int DEFAULT_PORT = 8189;
    private static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_DATA = "server/data/";

    private int port;
    private final AuthService authService;
    private final List<ChannelHandlerContext> clients = new ArrayList<>();

    public Server(int port) {
        this.port = port;
        this.authService = new BaseAuthService();
    }

    public Server init() throws IOException {
        FileUtility.createDirectory(Server.DEFAULT_DATA);
        return this;
    }

    public Server runAuthService() throws IOException {
        authService.run();
        return this;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new CommandEncoder(),
                                    new CommandDecoder(),
                                    new ServerHandler(init().runAuthService()));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(DEFAULT_HOST, port).sync();
            LogService.info("server is started at %s", f.channel().localAddress());
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private synchronized void updateUsersListMessage(ChannelHandlerContext ctx) throws IOException {
        List<UserData> users = new ArrayList<>();
        users.add(new UserData("all",null, null));
        /*
        for (ChannelHandlerContext client : clients) {
            users.add(client.getUserData());
        }*/
        for (ChannelHandlerContext client : clients) {
            //if(client != ctx) {
                sendCommand(client, Command.updateUsersListCommand(users));
            //}
        }
    }

    public synchronized void readCommand(ChannelHandlerContext ctx, Command command) {
        LogService.info("read: %s: %s", ctx, command);
    }

    public synchronized void sendCommand(ChannelHandlerContext ctx, Command command)  {
        ctx.writeAndFlush(command);
        LogService.info("send: %s: %s", ctx, command);
    }

    public synchronized void subscribe(ChannelHandlerContext ctx) throws IOException {
        clients.add(ctx);
        updateUsersListMessage(ctx);
    }

    public synchronized void unsubscribe(ChannelHandlerContext ctx) throws IOException {
        updateUsersListMessage(ctx);
        clients.remove(ctx);
    }

    public AuthService getAuthService() {
        return authService;
    }

    private static int getServerPort(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                LogService.error("invalid port number: %s", args[0]);
            }
        }
        return port;
    }
    public static void main(String[] args) throws Exception {
        new Server(getServerPort(args)).start();
    }
}

