import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void isCorrectIP() {
      assertTrue(Client.isCorrectIP("localhost"));
      assertFalse(Client.isCorrectIP("abraCadabra"));
    }

    @Test
    void isCorrectPort() {
        assertTrue(Client.isCorrectPort("3456"));
        assertFalse(Client.isCorrectPort("abraCadabra"));
    }

    @Test
    void extractWeight() {
        String w = "Вес файла: " + 0 + " bytes\n";
        assertEquals(0, Client.extractWeight(w));
        String w1 = "Вес файла: " + 100000000 + " bytes\n";
        assertEquals(100000000, Client.extractWeight(w1));
    }

    @Test
    void isCorrectDirectory() {
        Path path1 = Paths.get(".").toAbsolutePath();
        assertFalse(Client.isCorrectDirectory(path1.toString(), ""));
        assertTrue(Client.isCorrectDirectory(path1.toString(), "s"));
        assertFalse(Client.isCorrectDirectory("abraCadabra", "blalala"));
        assertFalse(Client.isCorrectDirectory("abraCadabra", "blalala"));
    }

    @Test
    void getPercent() {
        assertEquals(40, Client.getPercent(12, 30));
        assertEquals(13, Client.getPercent(52, 400));
        assertEquals(0, Client.getPercent(0, 6));
    }

    @Test
    void checkIfDownlandOk() {
        long fileLength = 5;
        long downlandLength = 10;
        assertTrue(Client.checkIfDownlandOk(fileLength, downlandLength));
        long fileLength1 = 5;
        long downlandLength1 = 5;
        assertTrue(Client.checkIfDownlandOk(fileLength1, downlandLength1));
        long fileLength2 = 10;
        long downlandLength2 = 5;
        assertFalse(Client.checkIfDownlandOk(fileLength2, downlandLength2));
    }

    @Test
    void executeProcess() {
        assertFalse(Client.executeProcess(null, null, null, null));
    }

    @Test
    void getFileList() {
        assertThrows(NullPointerException.class, () -> Client.getFileList(null));
    }

    @Test
    void makeChoice() {
        assertThrows(NullPointerException.class, () -> Client.makeChoice(null, null, null));
    }

    @Test
    void getWeightInfo() {
        assertThrows(NullPointerException.class, () -> Client.getWeightInfo(null));
    }

    @Test
    void agreeToDownland() {
        assertThrows(NullPointerException.class, () -> Client.agreeToDownland(null, null, ""));
    }

    @Test
    void continueWork() {
        assertThrows(NullPointerException.class, () -> Client.continueWork(null, null));
    }

    @Test
    void getFile() {
        assertThrows(NullPointerException.class, () -> Client.getFile("", "", null, 10));
    }

    @Test
    void receiveInfoAboutPrevDownlands() {
        assertThrows(NullPointerException.class, () -> Client.receiveInfoAboutPrevDownlands(null, null));
    }
}