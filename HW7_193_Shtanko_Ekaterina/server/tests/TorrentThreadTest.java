import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TorrentThreadTest {
    private static TorrentThread torrentThread;

    @BeforeAll
    @Test
    static void init() {
        assertDoesNotThrow(() -> torrentThread = new TorrentThread(null));
    }

    @Test
    void isNumber() {
        assertTrue(torrentThread.isNumber("4"));
        assertFalse(torrentThread.isNumber("f"));
    }

    @Test
    void inRange() {
        List<File> lst = new ArrayList<>();
        lst.add(new File("test1"));
        lst.add(new File("test2"));

        assertTrue(torrentThread.inRange(2, lst));
        assertFalse(torrentThread.inRange(0, lst));
        assertFalse(torrentThread.inRange(3, lst));
    }

    @Test
    void fileStillExist() {
        Path path = Paths.get("").toAbsolutePath();
        assertFalse(torrentThread.fileStillExist(path.toFile()));
        Path path1 = Paths.get("blablabla");
        assertFalse(torrentThread.fileStillExist(path1.toFile()));
    }

    @Test
    void sendWeightOfFile() {
        Path path = Paths.get("").toAbsolutePath();
        assertThrows(NullPointerException.class, () -> torrentThread.sendWeightOfFile(path.toFile()));
    }

    @Test
    void run() {
        assertDoesNotThrow(() -> torrentThread.run());
    }

    @Test
    void executeProcess() {
        assertThrows(NullPointerException.class, () -> torrentThread.executeProcess());
    }

    @Test
    void sendListOfFilesToClient() {
        assertThrows(NullPointerException.class, () -> torrentThread.sendListOfFilesToClient());
    }

    @Test
    void getOrderNumberOfChoice() {
        assertThrows(NullPointerException.class, () -> torrentThread.getOrderNumberOfChoice());
    }

    @Test
    void isValidChoice() {
        assertThrows(NullPointerException.class, () -> torrentThread.isValidChoice("4"));
        assertThrows(NullPointerException.class, () -> torrentThread.isValidChoice("h"));
    }

    @Test
    void isUserAgreeToDownland() {
        assertThrows(NullPointerException.class, () -> torrentThread.isUserAgreeToDownland());
    }

    @Test
    void sendFile() {
        assertThrows(NullPointerException.class, () -> torrentThread.sendFile(null));
    }

    @Test
    void sendInfoAboutDownlands() {
        assertThrows(NullPointerException.class, () -> torrentThread.sendInfoAboutDownlands());
    }
}