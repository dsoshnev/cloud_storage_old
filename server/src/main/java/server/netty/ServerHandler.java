package server.netty;

import common.AuthCommand;
import common.Command;
import common.UserData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.LogService;

import java.io.IOException;
import java.util.Date;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final Server server;

    public ServerHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Command) {
            Command command = (Command) msg;
            readMessages(ctx, command);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogService.info("client %s connected at %s", ctx.channel().remoteAddress(), new Date());
        // subscribe after authorization
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogService.info("client %s disconnected at %s", ctx.channel().remoteAddress(), new Date());
        server.unsubscribe(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        server.unsubscribe(ctx);
        ctx.close();

    }

    private boolean readMessages(ChannelHandlerContext ctx, Command command) throws IOException, InterruptedException {
        server.readCommand(ctx, command);
        if (command != null) {
            switch (command.getType()) {
                case END:
                    ctx.channel().close().sync();
                    return false;
                case AUTH:
                    AuthCommand authCommand = (AuthCommand) command;
                    UserData userData = server.getAuthService().AuthorizeUser(authCommand.getLogin(), authCommand.getPassword());
                    if (userData != null) {
                        //this.userData = userData;
                        userData.username = authCommand.getUsername();
                        server.getAuthService().setUsername(userData.login, userData.username);
                        server.sendCommand(ctx, command);
                        server.subscribe(ctx);
                        return true;
                    } else {
                        server.sendCommand(ctx, Command.errorCommand("user not found"));
                    }
                    break;
            }
        }
        return false;
    }
    private void init() throws IOException {
        //FileUtility.createDirectory(Server.DEFAULT_DATA + userData.homeDir);
    }


}
