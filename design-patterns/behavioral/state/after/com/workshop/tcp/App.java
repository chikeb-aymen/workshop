package com.workshop.tcp;

import com.workshop.tcp.context.TCPConnection;
import com.workshop.tcp.context.TCPOctetStream;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        TCPConnection connection = new TCPConnection();
        LOGGER.info("start: " + connection.getState().getClass().getSimpleName());

        connection.passiveOpen();
        LOGGER.info("after passiveOpen: " + connection.getState().getClass().getSimpleName());

        connection.send();
        LOGGER.info("after send: " + connection.getState().getClass().getSimpleName());

        connection.transmit(new TCPOctetStream(new byte[] { 0x01, 0x02 }));
        LOGGER.info("still: " + connection.getState().getClass().getSimpleName());

        connection.close();
        LOGGER.info("after close: " + connection.getState().getClass().getSimpleName());

        TCPConnection active = new TCPConnection();
        active.activeOpen();
        LOGGER.info("activeOpen from closed: " + active.getState().getClass().getSimpleName());
    }
}
