package com.algotrading.base.util;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

/**
 * Подключение к серверу через сокет.
 * Предполагается, что обмен информацией осуществляется с помощью строковых сообщений.
 */
public class SocketConnector implements AutoCloseable {

    private final String host;
    private final int port;
    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    /**
     * Конструктор.
     *
     * @param host хост
     * @param port порт
     */
    public SocketConnector(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void open(final Charset charset) throws IOException {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(1);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException ignored) {
            }
            reader = null;
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (final IOException ignored) {
            }
            writer = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (final IOException ignored) {
            }
            socket = null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed()
               && reader != null && writer != null;
    }

    /**
     * Отправить сообщение на сервер.
     *
     * @param message строка сообщения
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public void send(final String message) throws IOException {
        if (writer == null) {
            throw new IOException("Socket closed");
        }
        writer.write(message, 0, message.length());
        writer.newLine();
        writer.flush();
    }

    /**
     * Получить ответ от сервера.
     *
     * @return строка ответа или null, если ответа пока нет
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public String receive() throws IOException {
        if (reader == null) {
            throw new IOException("Socket closed");
        }
        try {
            return reader.readLine();
        } catch (final IOException e) {
            if (e instanceof SocketTimeoutException) {
                return null;
            } else {
                throw e;
            }
        }
    }
}
