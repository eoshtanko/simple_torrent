import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    /**
     * Размер одной порции байтов при считывании файла
     */
    static final private int BYTE_SIZE = 1024 * 8;

    public static void main(String[] args) {
        try {
            Socket clientSocket = getSocket();
            Scanner scanner = new Scanner(System.in);
            PrintStream ps = new PrintStream(clientSocket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (executeProcess(scanner, ps, br, clientSocket)) {
                // Цикл для многократного скачивания клиентом файлов
            }

            clientSocket.close();
            ps.close();
            br.close();
        } catch (Exception ex) {
            System.out.print("Что-то полшо не так...\n");
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Инициализирует сокет для общения с сервером
     *
     * @return сокет
     * @throws IOException - при некорректном создании сокета
     */
    private static Socket getSocket() throws IOException {
        Scanner scanner = new Scanner(System.in);
        InetAddress serverHost;
        int serverPort;
        Socket socket;
        String ip;
        String port;
        while (true) {
            do {
                System.out.print("Введите IP-адрес (localhost): ");
            } while (!isCorrectIP(ip = scanner.nextLine()));
            serverHost = InetAddress.getByName(ip);

            do {
                System.out.print("Введите порт (3456): ");
            } while (!isCorrectPort(port = scanner.nextLine()));
            serverPort = Integer.parseInt(port);

            try {
                socket = new Socket(serverHost, serverPort);
            } catch (Exception e) {
                System.out.println("Ошибка подключения. Попробуйте вновь.");
                continue;
            }
            break;
        }
        return socket;
    }

    /**
     * Осуществляет проверку корректности введенного ip
     *
     * @param ip введенный ip
     * @return true, если ip корректен
     */
    static boolean isCorrectIP(String ip) {
        try {
            InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.out.println("Введен неверный IP-адрес. Попробуйте вновь.");
            return false;
        }
        return true;
    }

    /**
     * Осуществляет первичную проверку корректности порта ip,
     * а именно, было ли введено число.
     *
     * @param port введенный порт
     * @return true, если введено - число
     */
    static boolean isCorrectPort(String port) {
        try {
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.out.println("Введен неверный порт. Попробуйте вновь.");
            return false;
        }
        return true;
    }

    /**
     * Основной метод работы клиента, содержащий все выполняемые им операции.
     *
     * @param scanner      инструмент для считывания данных из консоли
     * @param ps           поток для передачи сообщений серверу
     * @param br           поток для получения сообщений от сервера
     * @param clientSocket сокет
     * @return true - если итерация работы с севрером завершилась успешно и пользователь
     * хочет повторить скачивание.
     * false - завершение работы клиента.
     */
    static boolean executeProcess(Scanner scanner, PrintStream ps, BufferedReader br, Socket clientSocket) {
        try {
            System.out.print("-----------------------<>-----------------------");
            getFileList(br);

            String fileToSaveName = makeChoice(scanner, ps, br);
            if (fileToSaveName == null) {
                return false;
            }

            long length = getWeightInfo(br);

            String dir = agreeToDownland(scanner, ps, fileToSaveName);
            if (dir.equals("no")) {
                return true;
            }

            if (!getFile(fileToSaveName, dir, clientSocket, length)) {
                return false;
            }

            receiveInfoAboutPrevDownlands(br, ps);

            return continueWork(scanner, ps);

        } catch (Exception ex) {
            System.out.print("Что-то пошло не так...\n");
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
     * Получение от сервера списка файлов, доступных для скачивания
     *
     * @param br поток для получения сообщений от сервера
     * @throws IOException - ошикба при работе с потоками
     */
    static void getFileList(BufferedReader br) throws IOException {
        String line;
        // Получаем список файлов доступных для скачивания
        while ((line = br.readLine()) != null && !line.equals("0")) {
            System.out.println(line);
        }
    }

    /**
     * Метод, предоставляющий возможность пользователю выбрать файл из
     * списка доступных.
     *
     * @param scanner инструмент для считывания данных из консоли
     * @param ps      поток для передачи сообщений серверу
     * @param br      поток для получения сообщений от сервера
     * @return файл, который пользователь выбрал для скачивания
     * @throws IOException - ошикба при работе с потоками
     */
    static String makeChoice(Scanner scanner, PrintStream ps, BufferedReader br) throws IOException {
        String line;
        String fileToSaveName = null;
        boolean isValidChoice = false;
        do {
            line = scanner.nextLine();
            ps.println(line);
            ps.flush();
            if (line.toLowerCase().equals("exit")) {
                return fileToSaveName;
            }
            if ((line = br.readLine()) != null) {
                isValidChoice = line.startsWith("ОК.");
                fileToSaveName = line.substring(3);
            }
            if (!isValidChoice) {
                System.out.print(line);
                System.out.print("\nПовторите ввод: ");
            }
        } while (!isValidChoice);
        return fileToSaveName;
    }

    /**
     * Из сообщения, пришедшего от сервера, извлекает размер файла.
     *
     * @param str сообщение сервера
     * @return размер файла
     */
    static long extractWeight(String str) {
        return Long.parseLong(str.substring(11, str.lastIndexOf(' ')));
    }

    /**
     * Получает от севрера информацию о весе файла.
     *
     * @param br поток для получения сообщений от сервера
     * @return вес файла
     * @throws IOException - ошикба при работе с потоками
     */
    static long getWeightInfo(BufferedReader br) throws IOException {
        String line;
        // Получаем информацию о весе
        line = br.readLine();
        System.out.println(line);
        return extractWeight(line);
    }

    /**
     * Метод, предоставляющий возможность пользователю приянть решение,
     * скачивать ли файл(указывает директорию) или отказаться от скачивания (no).
     *
     * @param scanner        инструмент для считывания данных из консоли
     * @param ps             поток для передачи сообщений серверу
     * @param fileToSaveName имя выбранного для скачивания файла
     * @return решение  пользователя: диеректория или "no"
     */
    static String agreeToDownland(Scanner scanner, PrintStream ps, String fileToSaveName) {
        String directoryOrNo;
        System.out.print("Если Вы согласны скачать файл - введите название директории.\n" +
                "Иначе - введите 'no'.\n");
        directoryOrNo = scanner.nextLine().trim().toLowerCase();
        while (!directoryOrNo.equals("no") && !isCorrectDirectory(directoryOrNo, fileToSaveName)) {
            System.out.print("Введите коррктный путь до директории или 'no' для отмены.\n");
            directoryOrNo = scanner.nextLine().trim().toLowerCase();
        }
        ps.println(directoryOrNo);
        ps.flush();
        return directoryOrNo;
    }

    /**
     * Метод, предоставляющий возможность пользователю приянть решение,
     * продолжить ли работу.
     *
     * @param scanner инструмент для считывания данных из консоли
     * @param ps      поток для передачи сообщений серверу
     * @return true - продолжить, false - заевршить
     */
    static boolean continueWork(Scanner scanner, PrintStream ps) {
        String line;
        System.out.println("Хотите продолжить? Если нет - введите 'exit', иначе 'yes'");
        line = scanner.nextLine().toLowerCase();
        while (!(line.equals("yes") || line.equals("exit"))) {
            System.out.println("Введите 'yes' или 'exit'");
            line = scanner.nextLine().toLowerCase();
        }
        ps.println(line);
        ps.flush();
        return line.equals("yes");
    }

    /**
     * Проверка корректности диретории, которую пользователь указал для
     * скачивания файла.
     * Важно заметить, что директория, в которой уже содержится файл
     * с таким именем, считается ошибочной
     *
     * @param dir      указанная пользователем диреткория
     * @param fileName файл, желаемый для скачивания
     * @return true - директория корректна, false - некорректна
     */
    static boolean isCorrectDirectory(String dir, String fileName) {
        File directory = Paths.get(dir).toFile();
        if (directory.exists() && directory.isDirectory()) {
            File file = new File(dir + "/" + fileName);
            if (file.exists()) {
                System.out.print("Файл с таким именем уже существует в данной директории.\n");
                return false;
            } else {
                return true;
            }
        } else {
            System.out.print("Введенный путь некорректен.\n");
            return false;
        }
    }

    /**
     * Метод, вычисляющий проценты.
     *
     * @param part  часть(скаченная часть файла)
     * @param whole целое(размер файла)
     * @return процент
     */
    static double getPercent(long part, long whole) {
        return ((double) part) / whole * 100;
    }

    /**
     * Метод, выводящий дизайнерский progress bar.
     *
     * @param downlandLength скаченная часть файла
     * @param fileLength     общий ращмер файла
     * @param count          вспомогательная переменная для дизайнерского вывода
     * @return вспомогательная переменная для дизайнерского вывода
     */
    private static int showProgressInfo(long downlandLength, long fileLength, int count) {
        double percent;
        if (fileLength != 0) {
            percent = getPercent(downlandLength, fileLength);
        } else {
            percent = 100;
        }
        if ((int) percent / 10 > count) {
            String lvl = ("*").repeat((int) percent / 10);
            System.out.println(lvl);
            System.out.println("Загрузка: " + (int) percent + "%");
            count = (int) percent / 10;
        }
        return count;
    }

    /**
     * Метод для получения файла с сервера.
     *
     * @param fileName     имя получаемого файла
     * @param dir          директория для размещения полученного файла
     * @param clientSocket сокет
     * @param fileLength   общая длина получаемого файла
     * @return true, если скачивание завершилось успешно, иначе - false
     * @throws IOException - ошикба при работе с потоками
     */
    static boolean getFile(String fileName, String dir, Socket clientSocket,
                           long fileLength)
            throws IOException {
        int length;
        int count = 0;
        long downlandLength = 0;
        InputStream inputStream = clientSocket.getInputStream();
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(new File(dir + "/" + fileName)));
        byte[] buff = new byte[BYTE_SIZE];
        while ((length = inputStream.read(buff)) > 0) {
            bos.write(buff, 0, length);
            downlandLength += length;
            count = showProgressInfo(downlandLength, fileLength, count);
            if (downlandLength >= fileLength) {
                break;
            }
        }
        bos.close();
        return checkIfDownlandOk(fileLength, downlandLength);
    }

    /**
     * Метод для проверки корректности скачивания файла.
     * (Если размер скаченного файла >= размера ожидаемого -
     * скачивание считается корректным)
     *
     * @param fileLength     - ожидаемый размер файла
     * @param downlandLength - размер скаченного файла
     * @return true, если скачивание завершилось успешно, иначе - false
     */
    static boolean checkIfDownlandOk(long fileLength, long downlandLength) {
        if (fileLength <= downlandLength) {
            System.out.print("\nЗагрузка выполнена успешно!\n");
            return true;
        } else {
            System.out.println("При загрузке файла произошла ошибка!");
            return false;
        }
    }

    /**
     * Метод для получения информации о ранее скаченных файлах.
     *
     * @param ps поток для передачи сообщений серверу
     * @param br поток для получения сообщений от сервера
     * @throws IOException - ошикба при работе с потоками
     */
    static void receiveInfoAboutPrevDownlands(BufferedReader br, PrintStream ps)
            throws IOException {
        String line = "";
        ps.println(line);
        ps.flush();
        while (!((line = br.readLine()) == null) && !line.equals("0")) {
            System.out.println(line);
        }
    }
}
