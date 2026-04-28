import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPConnection.class);

    public enum Phase {
        CLOSED,
        LISTEN,
        ESTABLISHED
    }

    private Phase phase = Phase.CLOSED;

    public Phase getPhase() {
        return phase;
    }

    public void activeOpen() {
        switch (phase) {
            case CLOSED:
                // send SYN, receive SYN-ACK, etc.
                LOGGER.info("[CLOSED] activeOpen → handshake → ESTABLISHED");
                phase = Phase.ESTABLISHED;
                break;
            case LISTEN:
            case ESTABLISHED:
            default:
                break;
        }
    }

    public void passiveOpen() {
        switch (phase) {
            case CLOSED:
                LOGGER.info("[CLOSED] passiveOpen → LISTEN");
                phase = Phase.LISTEN;
                break;
            case LISTEN:
            case ESTABLISHED:
            default:
                break;
        }
    }

    public void close() {
        switch (phase) {
            case ESTABLISHED:
                // send FIN, wait for ACK, etc.
                LOGGER.info("[ESTABLISHED] close → LISTEN");
                phase = Phase.LISTEN;
                break;
            case CLOSED:
            case LISTEN:
            default:
                break;
        }
    }

    public void send() {
        switch (phase) {
            case LISTEN:
                LOGGER.info("[LISTEN] send (SYN path) → ESTABLISHED");
                phase = Phase.ESTABLISHED;
                break;
            case CLOSED:
            case ESTABLISHED:
            default:
                break;
        }
    }

    public void acknowledge() {
    }

    public void synchronize() {
    }

    public void transmit(TCPOctetStream stream) {
        switch (phase) {
            case ESTABLISHED:
                processOctet(stream);
                break;
            case CLOSED:
            case LISTEN:
            default:
                break;
        }
    }

    private void processOctet(TCPOctetStream stream) {
        LOGGER.info("[ESTABLISHED] processOctet: " + stream.length() + " byte(s)");
    }
}
