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

    /**
     * A random number generator.
     */
    private final SecureRandom random;

    /**
     * The local counter.
     */
    private AtomicInteger counter;

    /**
     * Contains a cached instance of the machine hash.
     */
    private AtomicReference<byte[]> cachedMachineHash = new AtomicReference<>();

    /**
     * Constructs an {@link IdGenerator} with predictable output.
     *
     * @param seed the seed for the random number generator
     */
    IdGenerator(byte[] seed) {
        random = new SecureRandom(seed);
        counter = new AtomicInteger(random.nextInt());
    }

    /**
     * Constructs an {@link IdGenerator}.
     */
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
        byte[] randomByteArray = new byte[1];
        random.nextBytes(randomByteArray);

        long localTime = System.currentTimeMillis();
        byte[] machineHash = getMachineHash();
        byte randomByte = randomByteArray[0];
        int counterValue = counter.getAndIncrement();

        return generate(localTime, machineHash, randomByte, counterValue);
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
     * Lazily returns the machine hash.
     *
     * @return the machine hash
     */
    private byte[] getMachineHash() {
        byte[] machineHash = cachedMachineHash.get();
        if (machineHash == null) {
            cachedMachineHash.set(generateMachineHash());
            machineHash = cachedMachineHash.get();
        }
        return machineHash;
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
    private byte[] generateMachineHash() {
        ByteArrayOutputStream macAddresses = new ByteArrayOutputStream();
        try {
            Enumeration<NetworkInterface> networkInterfaces
                    = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    macAddresses.write(mac);
                }
            }
            return DigestUtils.sha256(macAddresses.toByteArray());
        } catch (Exception e) {
            byte[] machineHash = new byte[32];
            random.nextBytes(machineHash);
            return machineHash;
        }
    }

}
