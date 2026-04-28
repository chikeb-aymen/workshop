package com.workshop.tcp.state.concrete;

import com.workshop.tcp.context.TCPConnection;
import com.workshop.tcp.context.TCPOctetStream;
import com.workshop.tcp.state.TCPState;

/** ConcreteState: ESTABLISHED — flyweight singleton. */
public final class TCPEstablished extends TCPState {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TCPEstablished.class);

    private static final TCPEstablished INSTANCE = new TCPEstablished();

    private TCPEstablished() {
    }

    public static TCPEstablished instance() {
        return INSTANCE;
    }

    @Override
    public void transmit(TCPConnection connection, TCPOctetStream stream) {
        connection.processOctet(stream);
    }

    @Override
    public void close(TCPConnection connection) {
        LOGGER.info("[TCPEstablished] close → TCPListen");
        changeState(connection, TCPListen.instance());
    }
}
