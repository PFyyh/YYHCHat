package com.pofengsystem.server;

import com.google.common.primitives.Bytes;
import com.pofengsystem.server.config.ChatConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class Server {

    private final ChatConfig chatConfig;

    private Selector selector;

    private String charset;

    public Server(ChatConfig chatConfig) {
        try {
            //开启未绑定的socket套接字通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //绑定端口
            serverSocketChannel.bind(new InetSocketAddress(chatConfig.getServerPort()));
            //设置非阻塞
            serverSocketChannel.configureBlocking(false);
            //开启多路复用
            selector = Selector.open();
            charset = chatConfig.getCharset();
            //将通道注册到selector里面
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            log.info("服务器启动成功,监听{}端口", chatConfig.getServerPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.chatConfig = chatConfig;
    }

    public void listen() {
        while (true) {
            try {
                int count = selector.select(2000);
                if (count == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        connectInfo(key);
                    }
                    if (key.isReadable()) {
                        readClientInfo(key);
                    }
                    //防止重复消费
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readClientInfo(SelectionKey key) {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if (socketChannel.read(buffer) > 0) {
                buffer.flip();
//                log.info("客户端发送消息：{}",Charset.forName(chatConfig.getCharset()).decode(buffer).toString());
                String msg = Charset.forName(chatConfig.getCharset()).decode(buffer).toString();
                log.info("ip：{},消息内容{}", socketChannel.getRemoteAddress(), msg);
                forwardMessages(msg, socketChannel);
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转发给其他的socketChannel
     *
     * @param msg
     * @param myself
     */
    private void forwardMessages(String msg, SocketChannel myself) {
        log.info("开始转发。。。");
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey selectionKey : selectionKeys) {
            Channel channel = selectionKey.channel();
            if (channel instanceof SocketChannel && channel != myself) {
                SocketChannel socketChannel = null;
                try {
                    socketChannel = (SocketChannel) channel;
                    log.info("转发信息给{}",socketChannel.getRemoteAddress());

                    ByteBuffer msgByteBuffer = ByteBuffer.wrap(msg.getBytes(charset));
                    socketChannel.write(msgByteBuffer);
                } catch (IOException e) {
                    try {
                        log.info("ip:{},下线", socketChannel.getRemoteAddress());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }

            }

        }
    }

    private void connectInfo(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.wrap("欢迎进入YYH聊天室".getBytes(chatConfig.getCharset()));
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
