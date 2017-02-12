package io.sniffy.registry;

import io.sniffy.socket.BaseSocketTest;
import io.sniffy.socket.SnifferSocketImplFactory;
import org.junit.After;
import org.junit.Test;

import java.io.StringReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;

import static io.sniffy.registry.ConnectionsRegistry.ConnectionStatus.CLOSED;
import static io.sniffy.registry.ConnectionsRegistry.ConnectionStatus.OPEN;
import static org.junit.Assert.*;

public class ConnectionsRegistryTest extends BaseSocketTest {

    @After
    public void clearConnectionRules() {
        ConnectionsRegistry.INSTANCE.clear();
    }

    @Test
    public void testConnectionClosed() throws Exception {

        SnifferSocketImplFactory.uninstall();
        SnifferSocketImplFactory.install();

        ConnectionsRegistry.INSTANCE.setSocketAddressStatus(localhost.getHostName(), echoServerRule.getBoundPort(), CLOSED);

        Socket socket = null;

        try {
            socket = new Socket(localhost, echoServerRule.getBoundPort());
            fail("Should have failed since this connection is forbidden by sniffy");
        } catch (ConnectException e) {
            assertNotNull(e);
        } finally {
            if (null != socket) socket.close();
        }

    }

    @Test
    public void testIsNullConnectionOpened() {

        assertEquals(OPEN, ConnectionsRegistry.INSTANCE.resolveSocketAddressStatus(null));
        assertEquals(OPEN, ConnectionsRegistry.INSTANCE.resolveSocketAddressStatus(new InetSocketAddress((InetAddress) null, 5555)));

    }

    @Test
    public void testConnectionOpened() throws Exception {

        SnifferSocketImplFactory.uninstall();
        SnifferSocketImplFactory.install();

        Socket socket = new Socket(localhost, echoServerRule.getBoundPort());

        assertTrue(socket.isConnected());

        socket.close();

    }

    @Test
    public void testLoadFromReader() throws Exception {

        String json = "{\"sockets\":[{\"host\":\"google.com\",\"port\":\"42\",\"status\":\"OPEN\"}]," +
                "\"dataSources\":[{\"url\":\"jdbc:h2:mem:test\",\"userName\":\"sa\",\"status\":\"CLOSED\"}]}";

        ConnectionsRegistry.INSTANCE.readFrom(new StringReader(json));

        Map<Map.Entry<String, Integer>, ConnectionsRegistry.ConnectionStatus> discoveredAddresses =
                ConnectionsRegistry.INSTANCE.getDiscoveredAddresses();

        assertEquals(1, discoveredAddresses.size());
        assertEquals(OPEN, discoveredAddresses.get(new AbstractMap.SimpleEntry<>("google.com", 42)));

        Map<Map.Entry<String, String>, ConnectionsRegistry.ConnectionStatus> discoveredDataSources =
                ConnectionsRegistry.INSTANCE.getDiscoveredDataSources();

        assertEquals(1, discoveredDataSources.size());
        assertEquals(CLOSED, discoveredDataSources.get(new AbstractMap.SimpleEntry<>("jdbc:h2:mem:test", "sa")));

    }
}