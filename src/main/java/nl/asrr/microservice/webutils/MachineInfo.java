package nl.asrr.microservice.webutils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;

@Log4j2
public class MachineInfo {

    public static String getIdAsHex() {
        var machineBytes = concatByteArray(getSystemInfo(), getMacAddresses());
        return DigestUtils.sha256Hex(machineBytes);
    }

    public static byte[] getId() {
        var machineBytes = concatByteArray(getSystemInfo(), getMacAddresses());
        return DigestUtils.sha256(machineBytes);
    }

    private static byte[] concatByteArray(byte[] systemInfo, byte[] macAddresses) {
        var machineBytes = new byte[systemInfo.length + macAddresses.length];
        System.arraycopy(systemInfo, 0, machineBytes, 0, systemInfo.length);
        System.arraycopy(macAddresses, 0, machineBytes, systemInfo.length, macAddresses.length);
        return machineBytes;
    }

    private static byte[] getSystemInfo() {
        var systemInfo = System.getProperty("os.name") +
                System.getProperty("os.version") +
                System.getProperty("os.arch") +
                Runtime.getRuntime().availableProcessors();
        return systemInfo.getBytes();
    }

    private static byte[] getMacAddresses() {
        var macAddresses = new ByteArrayOutputStream();
        try {
            var networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                var networkInterface = networkInterfaces.nextElement();
                var mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    macAddresses.write(mac);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        return macAddresses.toByteArray();
    }

}
