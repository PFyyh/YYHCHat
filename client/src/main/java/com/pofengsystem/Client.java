package com.pofengsystem;

import com.pofengsystem.client.ChatConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;


@Component
@Slf4j
public class Client {

    private Selector selector;

    private SocketChannel socketChannel;
    private String username;
    private String charset;

    public Client(ChatConfig config) {
        try {
            charset = config.getCharset();
            //绑定端口
            socketChannel = SocketChannel.open(new InetSocketAddress(config.getSeverIp(), config.getServerPort()));
            socketChannel.configureBlocking(false);
            //打开多路复用
            selector = Selector.open();
            //通道注册上
            socketChannel.register(selector, SelectionKey.OP_READ);
            username = socketChannel.getLocalAddress().toString().toLowerCase(Locale.ROOT);
            log.info("客户端：{}，成功连接。", username);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendInfo(String info) {
        info = username + "说:" + info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes(charset)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readInfo() {
        try {
            int count = selector.select();
            if (count > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        //得到相关的通道
                        SocketChannel sc = (SocketChannel) key.channel();
                        //得到一个 Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //读取
                        sc.read(buffer);
                        //把读到的缓冲区的数据转成字符串
                        String msg = new String(buffer.array(), charset);
                        log.info(msg.trim());
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {

        }
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                readInfo();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            try {
                String msg = socketChannel.getLocalAddress() + " " + in.nextLine();
                socketChannel.write(ByteBuffer.wrap(msg.getBytes(charset)));
            } catch (IOException e) {
                try {
                    socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }

    }
}
