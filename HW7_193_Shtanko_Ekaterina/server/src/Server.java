import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// Tread per connection approach
public class Server {

    /**
     * Максимальный размер файла для скачивания — 128GiB.
     */
    static final double MAX_SIZE = 1.374e+11;

    /**
     * Список файлов, доступных для скачивания,
     * в виде строки для передачи клиенту.
     */
    static final StringBuilder FILES_LIST = new StringBuilder();

    static final private int SERVER_PORT = 3456;

    /**
     * Список файлов, доступных для скачивания.
     */
    static List<File> filesInFolder = new ArrayList<>();

    /**
     * Имя директории, ИЗ которой происходит скачивание.
     */
    static String directoryName;

    public static void main(String[] args) {
        try {
            if (isDirectoryCorrectlySpecified(args)) {
                makeFileList();
                ServerSocket fileServer = new ServerSocket(SERVER_PORT);
                while (true) {
                    Socket socket = fileServer.accept();
                    TorrentThread thread = new TorrentThread(socket);
                    thread.start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Метод для считывания и проверки корректности директори,
     * ИЗ которой будет осуществляться загрузка.
     * Позволяет получить путь как из args, так и из консоли.
     *
     * @param args - аргументы при запуске
     * @return корректна ли задана директория
     */
    private static boolean isDirectoryCorrectlySpecified(String[] args) {
        // директория с файлами, доступными для скачивания клиентам
        Path path;
        // директория указывается при запуске сервера
        if (args.length > 0) {
            directoryName = args[0];
            if (directoryName == null || directoryName.isEmpty()) {
                System.out.println("Указана некорректная директория!");
                return false;
            }
            path = Paths.get(directoryName);
            if (isNotCorrectDirectory(path)) return false;
        } else {
            directoryName = getDirectoryFromConsole();
            path = Paths.get(directoryName);
            while (isNotCorrectDirectory(path)) {
                System.out.println("Повторите ввод");
                directoryName = getDirectoryFromConsole();
                path = Paths.get(directoryName);
            }
        }
        System.out.println("С директорией все отлично! Сервер готов к работе.");
        return true;
    }

    /**
     * Для получения директории из консоли.
     *
     * @return директория
     */
    private static String getDirectoryFromConsole() {
        // директория с файлами, доступными для скачивания клиентам
        String directoryName;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите дерикторию с файлами," +
                " доступными для скачивания клиентам");
        directoryName = scanner.nextLine().trim();
        while (directoryName == null || directoryName.isEmpty()) {
            System.out.println("Введена некорректная диерктория! Повторите ввод.");
            directoryName = scanner.nextLine();
        }
        return directoryName;
    }

    /**
     * Вспомогательный метод для проверки корректности директори,
     * ИЗ которой будет осуществляться загрузка.
     *
     * @param path директория
     * @return корректна ли директория
     */
    static boolean isNotCorrectDirectory(Path path) {
        if (!Files.exists(path)) {
            System.out.println("Указанного пути не существует!");
            return true;
        }
        if (!Files.isDirectory(path)) {
            System.out.println("По указанному пути располагается НЕ директория!");
            return true;
        }

        filesInFolder = collectInfoAboutFilesInDirectory(path);

        if (filesInFolder.size() == 0) {
            System.out.println("В указанной директории нет файлов доступных для скачивания!");
            return true;
        }
        return false;
    }

    /**
     * Метод для считывания файлов в выбранной директории.
     *
     * @param path путь до выбранной диреткори
     * @return список файлов в диреткории
     */
    static List<File> collectInfoAboutFilesInDirectory(Path path) {
        return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(File::isFile)
                .filter(f -> f.length() < MAX_SIZE)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Метод, формирующий сообщение со списком доступных файлов для пользователя.
     */
    private static void makeFileList() {
        FILES_LIST.append("Введите номер желаемого для скачивания файла или 'exit' для выхода." +
                "\nСписок доступных файлов:\n");
        for (int i = 0; i < filesInFolder.size(); i++) {
            FILES_LIST.append((i + 1)).append(".) ")
                    .append(filesInFolder.get(i).getName())
                    .append("\n");
        }
    }
}