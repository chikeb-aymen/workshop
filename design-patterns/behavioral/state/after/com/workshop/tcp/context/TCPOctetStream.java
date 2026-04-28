package com.workshop.tcp.context;

public class TCPOctetStream {

    private final byte[] data;

    public TCPOctetStream(byte[] data) {
        this.data = data != null ? data.clone() : new byte[0];
    }

    public byte[] getData() {
        return data.clone();
    }

    public int length() {
        return data.length;
    }
}
