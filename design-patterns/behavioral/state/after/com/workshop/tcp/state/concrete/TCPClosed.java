package com.workshop.tcp.state.concrete;

import com.workshop.tcp.context.TCPConnection;
import com.workshop.tcp.state.TCPState;

public final class TCPClosed extends TCPState {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPClosed.class);

    
    private static final TCPClosed INSTANCE = new TCPClosed();

    private TCPClosed() {
    }

    public static TCPClosed instance() {
        return INSTANCE;
    }

    @Override
    public void activeOpen(TCPConnection connection) {
        LOGGER.info("[TCPClosed] activeOpen → handshake → TCPEstablished");
        changeState(connection, TCPEstablished.instance());
    }

    @Override
    public void passiveOpen(TCPConnection connection) {
        LOGGER.info("[TCPClosed] passiveOpen → TCPListen");
        changeState(connection, TCPListen.instance());
    }
}
