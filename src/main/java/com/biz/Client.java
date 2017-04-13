package com.biz;

import com.rabbitmq.client.*;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/4/12.
 */
public class Client {
    private static final String EXCHANGE_NAME = "chat";
    private static final String Host = "localhost";
    private static String nickname = null;//用户昵称
    private static String QUIT = "-q";//退出指令

    public static void main(String[] argv)
            throws java.io.IOException, TimeoutException, InterruptedException {
        System.out.println("Welcome to ChatRoom");
        System.out.println("Type " + QUIT + " to exit...");
        Connection connection = getConnection();
        Channel channel = getChannel(connection);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        receive(channel);
        send(channel);
        connection.close();
    }

    /**
     * 通过ConnectionFactory获取连接
     *
     * @return com.rabbitmq.client.Connection
     * @throws IOException
     * @throws TimeoutException
     */
    private static Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Host);
        Connection connection = factory.newConnection();
        return connection;
    }

    /**
     * 建立一个通道
     *
     * @param connection com.rabbitmq.client.Connection
     * @return com.rabbitmq.client.Channel
     * @throws IOException
     */
    private static Channel getChannel(Connection connection) throws IOException {
        return connection.createChannel();
    }

    /**
     * 发送消息
     *
     * @param channel com.rabbitmq.client.Channel
     * @throws IOException
     * @throws TimeoutException
     */
    private static void send(Channel channel) throws IOException, TimeoutException {
        Scanner scanner = new Scanner(System.in);
        boolean flag = true;
        System.out.println("Please input your nickname：");
        nickname = StringUtils.trim(scanner.nextLine());
        while (StringUtils.isEmpty(nickname)) {
            System.out.println("Your nickname is empty,please enter again:");
            nickname = StringUtils.trim(scanner.nextLine());
        }
        System.out.println("Hello " + nickname + ",you can chat from now, enjoy it");
        while (flag) {
            String message = scanner.nextLine();
            if (StringUtils.equals(QUIT, message)) {
                nickname = null;
                System.out.println("GoodBye");
                flag = false;
            } else if (StringUtils.isNotEmpty(message)) {
                message = nickname + " said:" + message;
                channel.basicPublish(EXCHANGE_NAME, "", null, SerializationUtils.serialize(message));
            }
        }

    }

    /**
     * 接收队列中消息
     *
     * @param channel com.rabbitmq.client.Channel
     * @throws IOException
     */
    private static void receive(Channel channel) throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = SerializationUtils.deserialize(body);
                System.out.println(message);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
