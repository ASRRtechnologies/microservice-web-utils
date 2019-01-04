package nl.asrr.microservice.webutils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayOutputStream;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A threadsafe id generator to generate unique ids with.
 */
public class IdGenerator {

    private final SecureRandom random;

    private AtomicInteger counter;

    private AtomicReference<byte[]> cachedMachineHash = new AtomicReference<>();

    IdGenerator(byte[] seed) {
        random = new SecureRandom(seed);
        counter = new AtomicInteger(random.nextInt());
    }

    public IdGenerator() {
        random = new SecureRandom();
        counter = new AtomicInteger(random.nextInt());
    }

    /**
     * Generates an unique id.
     *
     * @return an unique id
     */
    public String generate() {
        byte[] randomByte = new byte[1];
        random.nextBytes(randomByte);

        byte[] machineHash = cachedMachineHash.get();
        if (machineHash == null) {
            cachedMachineHash.set(getMachineHash());
            machineHash = cachedMachineHash.get();
        }

        return generate(
                System.currentTimeMillis(),
                machineHash,
                randomByte[0],
                counter.getAndIncrement()
        );
    }

    /**
     * Generates an unique id.
     *
     * @param localTime   the local time of the machine represented in milliseconds
     * @param machineHash a hash that can identify this machine
     * @param randomBits  a random byte that represents random data
     * @param counterBits the id counter on this machine
     * @return an unique id
     */
    String generate(long localTime, byte[] machineHash, byte randomBits, int counterBits) {
        // take 63 bits of local time and reserve the last bit
        long timeBits = localTime;
        timeBits &= 0x7F_FF_FF_FF_FF_FF_FF_FFL;

        // 12 bits for the machine id
        int machineBits = getPartialMachineHash(machineHash);

        // combine the 3 parts into 1 32-bit integer
        int machineCounterRandomInfo = machineBits & 0xFFF;
        machineCounterRandomInfo <<= 12;
        machineCounterRandomInfo |= counterBits & 0xFFF;
        machineCounterRandomInfo <<= 8;
        machineCounterRandomInfo |= randomBits & 0xFF;

        int idSize = 96;
        ByteBuffer idBuffer = ByteBuffer.allocate(idSize / Byte.SIZE);

        // add the 63 bits of local time and 1 reserved bit
        idBuffer.putLong(timeBits);

        // add the 12 machine bits, 12 counter bits and 8 the random bits
        idBuffer.putInt(machineCounterRandomInfo);

        return Base64.getUrlEncoder().encodeToString(idBuffer.array());
    }

    /**
     * Takes the first 12 bits from the {@code hash}.
     *
     * @param hash the hash of the machine
     * @return the first 12 bits from the {@code hash}
     */
    int getPartialMachineHash(byte[] hash) {
        int machineId = (int) hash[0] & 0x0F;
        machineId <<= 8;
        machineId |= (int) hash[1] & 0xFF;
        return machineId;
    }

    /**
     * Generates a sha256 hash from the network interfaces of this device.
     * A random byte array of length 32 will be generated if any error occurs
     * during this operation.
     *
     * @return a sha256 hash from the network interfaces of this device
     */
    private byte[] getMachineHash() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Enumeration<NetworkInterface> networkInterfaces
                    = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    outputStream.write(mac);
                }
            }
            return DigestUtils.sha256(outputStream.toByteArray());
        } catch (Exception e) {
            byte[] machineHash = new byte[32];
            random.nextBytes(machineHash);
            return machineHash;
        }
    }

}
