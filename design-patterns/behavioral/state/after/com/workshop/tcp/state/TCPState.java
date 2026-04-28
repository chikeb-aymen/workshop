package com.workshop.tcp.state;

import com.workshop.tcp.context.TCPConnection;
import com.workshop.tcp.context.TCPOctetStream;

public abstract class TCPState {

    protected final void changeState(TCPConnection connection, TCPState next) {
        connection.changeState(next);
    }

    public void transmit(TCPConnection connection, TCPOctetStream stream) {
    }

    public void activeOpen(TCPConnection connection) {
    }

    public void passiveOpen(TCPConnection connection) {
    }

    public void close(TCPConnection connection) {
    }

    public void synchronize(TCPConnection connection) {
    }

    public void acknowledge(TCPConnection connection) {
    }

    public void send(TCPConnection connection) {
    }
}
