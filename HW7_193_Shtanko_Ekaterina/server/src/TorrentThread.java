import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Tread per connection approach
class TorrentThread extends Thread {

    /**
     * Размер одной порции байтов при передачи файла
     */
    static final private int BYTE_SIZE = 1024 * 8;
    /**
     * Список ранее переданных клиенту файлов
     */
    private final StringBuilder infoAboutPrevDownloads = new StringBuilder();
    /**
     * То, сколько файлов уже было передано данному клиенту.
     */
    private int numberOfDownloadedFiles = 0;
    private final Socket clientSocket;
    private BufferedReader br;
    private PrintStream ps;
    /**
     * Диреткория, в которой будет сохранен файл(для записи истории скачиваний)
     */
    private String directoryToSaveAt;
    /**
     * Файл, который скачивается
     */
    private File fileToSave;

    TorrentThread(Socket s) {
        clientSocket = s;
    }

    /**
     * Запуск потока
     */
    public void run() {
        try {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ps = new PrintStream(clientSocket.getOutputStream());
            while (executeProcess()) {
                // Цикл для многократного скачивания клиентом файлов
            }
            br.close();
            ps.close();
            clientSocket.close();
        } catch (Exception ex) {
            System.out.println("Заевршение работы ...");
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Основной метод, содержащий все выполняемые операции.
     *
     * @return завершать ли работу с клиентом
     */
    boolean executeProcess() {
        String str;
        try {
            // отправляем клиенту список файлов доступных для скачивания
            sendListOfFilesToClient();

            // получаем от клиента порядковый номер файла в предложенном списке
            if (!getOrderNumberOfChoice()) {
                return false;
            }

            directoryToSaveAt = null;
            // отправляем информацию о весе
            sendWeightOfFile(fileToSave);

            if (isUserAgreeToDownland()) {
                if (fileStillExist(fileToSave)) {
                    sendFile(fileToSave);
                    if (!sendInfoAboutDownlands()) return false;
                    if ((str = br.readLine()) != null) {
                        return !str.toLowerCase().equals("exit");
                    }
                } else {
                    return false;
                }
                return false;
            }
            return directoryToSaveAt != null;
        } catch (Exception ex) {
            System.out.println("Заевршение работы с клиентом");
            System.out.println(ex.getMessage());
            ps.println("\n В процессе работы произошла ошибка. Попробуйте еще раз.\n");
            ps.flush();
            return false;
        }
    }

    /**
     * Отправляем список доступных для скачивания файлов клиенту.
     */
    void sendListOfFilesToClient() {
        ps.println("\nФайлы, которые Вы уже скачали:\n" +
                infoAboutPrevDownloads.toString() + "\n" +
                Server.FILES_LIST.toString() + "\nВаш выбор: ");
        ps.println(0);
        ps.flush();
    }

    /**
     * Получаем от клиента порядковый номер выбранного файла(или exit).
     *
     * @return продолжать ли работу с клиентом
     * @throws IOException - ошибка при работе с потокам
     */
    boolean getOrderNumberOfChoice() throws IOException {
        String input;
        do {
            input = br.readLine();
            // если клиент отключился
            if (input == null) {
                return false;
            }
            input = input.trim();
            if (input.toLowerCase().equals("exit")) {
                return false;
            }
        } while (!isValidChoice(input));
        return true;
    }

    /**
     * Проверяет, число ли
     *
     * @param str строка для проверки
     * @return true - если чило
     */
    boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Проверяет в границах ли массива с файлами выбранное пользователем чило.
     *
     * @param number число выбранное пользователем
     * @param lst    список файлов доступных для скачиваняи
     * @return true - если в границах
     */
    boolean inRange(int number, List<File> lst) {
        return number > 0 && number <= lst.size();
    }

    /**
     * Проверяет корректный ли выбор сделал пользователь.
     *
     * @param input выбор пользователя
     * @return true - если корректный
     */
    boolean isValidChoice(String input) {
        int numberOfFile;
        if (isNumber(input)) {
            numberOfFile = Integer.parseInt(input);
        } else {
            ps.println("Введено не число!");
            ps.flush();
            return false;
        }
        if (inRange(numberOfFile, Server.filesInFolder)) {
            fileToSave = Server.filesInFolder.get(numberOfFile - 1);
            if (!fileStillExist(fileToSave)) {
                ps.println("Запрашиваемый файл больше недоступен для скачивания.");
                ps.flush();
                return false;
            }
            ps.println("ОК." + fileToSave.getName());
            ps.flush();
            return true;
        } else {
            ps.println("Должно быть введено число от 1 до " + Server.filesInFolder.size());
            ps.flush();
            return false;
        }
    }

    /**
     * Проверяет, не был ли удален файл в процессе.
     *
     * @param file файл для скачивания
     * @return существует ли файл
     */
    boolean fileStillExist(File file) {
        return Files.exists(Path.of(Server.directoryName + "/" + file.getName()));
    }

    /**
     * Отправляет пользователю вес выбранного им файла.
     *
     * @param fileToSave файл для скачивания
     */
    void sendWeightOfFile(File fileToSave) {
        // Отправляем клиенту информацию о весе файла
        long weight = fileToSave.length();
        ps.println("Вес файла: " + weight + " bytes\n");
        ps.flush();
    }

    /**
     * Принимает решение пользователя, скачивать ли файл
     *
     * @return если польщзователь согласен скачать файл - true, иначе - false
     * @throws IOException - ошибка при работе с потоками
     */
    boolean isUserAgreeToDownland() throws IOException {
        directoryToSaveAt = br.readLine();
        return directoryToSaveAt != null &&
                !directoryToSaveAt.toLowerCase().equals("no") &&
                !directoryToSaveAt.isEmpty();
    }

    /**
     * Отправка файла клиенту.
     *
     * @param file файл для отправки
     * @throws IOException - при работе с потокам
     */
    void sendFile(final File file) throws IOException {
        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int length;
        byte[] buf = new byte[BYTE_SIZE];
        while ((length = bis.read(buf)) > 0) {
            outputStream.write(buf, 0, length);
        }
        bis.close();
        addInfoAboutDownland(file);
    }

    /**
     * Добавляет инфрмацию о счкаченном файле в "историю" скачиваний.
     *
     * @param file скчаенный файл
     */
    private void addInfoAboutDownland(File file) {
        numberOfDownloadedFiles++;
        int orderNumber = numberOfDownloadedFiles;
        String str = orderNumber + ".) " +
                file.getName() +
                " загружен в " +
                directoryToSaveAt + "\n";
        infoAboutPrevDownloads.append(str);
    }

    /**
     * Отправляет пользователю информацию о ранее скаченных файлах
     *
     * @return true - если отправка произошла успешно
     * @throws IOException - при работе с потоками
     */
    boolean sendInfoAboutDownlands() throws IOException {
        String input = br.readLine();
        // если клиент отключился
        if (input == null) {
            return false;
        }
        ps.println("Скаченные Вами файлы:\n" + infoAboutPrevDownloads);
        ps.println(0);
        ps.flush();
        return true;
    }
}