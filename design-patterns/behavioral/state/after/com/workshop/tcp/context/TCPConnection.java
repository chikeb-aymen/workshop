package com.workshop.tcp.context;

import com.workshop.tcp.state.TCPState;
import com.workshop.tcp.state.concrete.TCPClosed;

public class TCPConnection {

    private TCPState state;

    public TCPConnection() {
        this.state = TCPClosed.instance();
    }

    public void changeState(TCPState next) {
        this.state = next;
    }

    public TCPState getState() {
        return state;
    }

    public void activeOpen() {
        state.activeOpen(this);
    }

    public void passiveOpen() {
        state.passiveOpen(this);
    }

    public void close() {
        state.close(this);
    }

    public void acknowledge() {
        state.acknowledge(this);
    }

    public void synchronize() {
        state.synchronize(this);
    }

    public void send() {
        state.send(this);
    }

    public void transmit(TCPOctetStream stream) {
        state.transmit(this, stream);
    }

    public void processOctet(TCPOctetStream stream) {
        System.out.println("[TCPEstablished] processOctet: " + stream.length() + " byte(s)");
    }
}
