package nl.asrr.microservice.webutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

    private IdGenerator idGenerator;

    @BeforeEach
    void init() {
        idGenerator = new IdGenerator("1".getBytes());
    }

    @Test
    void partialHashMaximum() {
        byte[] machineHash = new byte[]{(byte) 0xFF, (byte) 0xFF};
        int partialHash = idGenerator.getPartialMachineHash(machineHash);

        assertThat(partialHash).isEqualTo(0xFFF);
    }

    @Test
    void randomPartialHash() {
        byte[] machineHash = new byte[]{(byte) 10, (byte) 20};
        int partialHash = idGenerator.getPartialMachineHash(machineHash);

        assertThat(partialHash).isEqualTo(0b1010_0001_0100);
    }

    @Test
    void generateId() {
        long localTime = 0xBEEF9;
        byte[] machineHash = new byte[]{(byte) 0x22, (byte) 0x33};
        int counterBits = 0x6688;
        byte randomBits = 0x11;

        ByteBuffer idBuffer = ByteBuffer.allocate(96 / Byte.SIZE);
        idBuffer.putLong(0x00_00_00_00_00_0B_EE_F9L);
        idBuffer.putInt(0x2_33_6_88_11);

        String calculatedId = Base64.getUrlEncoder().encodeToString(idBuffer.array());
        String id = idGenerator.generate(localTime, machineHash, randomBits, counterBits);
        assertThat(id).isEqualTo(calculatedId);
    }

}
