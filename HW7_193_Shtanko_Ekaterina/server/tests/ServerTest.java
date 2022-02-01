import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

// Tread per connection approach
class ServerTest {
    @Test
    void isNotCorrectDirectoryTest(){
        Path path = Paths.get("blablabla");
        assertTrue(Server.isNotCorrectDirectory(path));

        path =  Paths.get(new File("test.txt").getAbsolutePath());
        assertTrue(Server.isNotCorrectDirectory(path));

        path = Paths.get(".").toAbsolutePath();
        assertFalse(Server.isNotCorrectDirectory(path));
    }

    @Test
    void collectInfoAboutFilesInDirectoryTest(){
        Path path = Paths.get(new File("test.txt").getAbsolutePath());
        assertThrows(NullPointerException.class, () -> Server.collectInfoAboutFilesInDirectory(path));
        Path path1 = Paths.get(".").toAbsolutePath();
        assertEquals(1, Server.collectInfoAboutFilesInDirectory(path1).size());
    }

    @Test
    void main(){
        String[] args = new String[1];
        args[0] = "dir";
        assertDoesNotThrow(() -> Server.main(args));

        String[] args1 = new String[1];
        args1[0] = "";
        assertDoesNotThrow(() -> Server.main(args1));
    }


}