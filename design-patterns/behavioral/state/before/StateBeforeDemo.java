public class StateBeforeDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateBeforeDemo.class);
    
    public static void main(String[] args) {
        TCPConnection c = new TCPConnection();
        LOGGER.info("start: " + c.getPhase());

        c.passiveOpen();
        LOGGER.info("after passiveOpen: " + c.getPhase());

        c.send();
        LOGGER.info("after send: " + c.getPhase());

        c.transmit(new TCPOctetStream(new byte[]{0x01, 0x02}));
        LOGGER.info("still: " + c.getPhase());

        c.close();
        LOGGER.info("after close: " + c.getPhase());

        TCPConnection c2 = new TCPConnection();
        c2.activeOpen();
        LOGGER.info("activeOpen from CLOSED: " + c2.getPhase());
    }
}
