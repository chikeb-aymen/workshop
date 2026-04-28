package com.workshop.tcp.state.concrete;

import com.workshop.tcp.context.TCPConnection;
import com.workshop.tcp.state.TCPState;

public final class TCPListen extends TCPState {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPListen.class);

    private static final TCPListen INSTANCE = new TCPListen();

    private TCPListen() {
    }

    public static TCPListen instance() {
        return INSTANCE;
    }

    @Override
    public void send(TCPConnection connection) {
        LOGGER.info("[TCPListen] send (SYN path) → TCPEstablished");
        changeState(connection, TCPEstablished.instance());
    }
}
