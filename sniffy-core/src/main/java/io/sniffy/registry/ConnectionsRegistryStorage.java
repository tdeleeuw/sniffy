package io.sniffy.registry;

import io.sniffy.util.IOUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by bedrin on 02.11.2016.
 */
public enum ConnectionsRegistryStorage {
    INSTANCE;

    private File file = new File(IOUtil.getApplicationSniffyFolder(), "connectionsRegistry.json");

    public void loadConnectionsRegistry(ConnectionsRegistry connectionsRegistry) throws IOException {
        FileReader reader = null;
        try {
            if (file.exists()) {
                connectionsRegistry.readFrom(reader = new FileReader(file));
            }
        } finally {
            IOUtil.closeSilently(reader);
            file.delete();
        }
    }

    public void storeConnectionsRegistry(ConnectionsRegistry connectionsRegistry) throws IOException {
        FileWriter writer = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            connectionsRegistry.writeTo(writer = new FileWriter(file));
        } finally {
            IOUtil.closeSilently(writer);
        }
    }

}