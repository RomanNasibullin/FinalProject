# Итоговый проект по Java 3 вариант
## 1. Создадим два класса для хранения данных извлеченных из csv - Regions и SportObjects
Классы Regions и SportObjects представляют собой используемые в приложении для представления структуры данных. Они служат для моделирования сущностей, с которыми приложение работает.
- Regions - Представляет регион и список спортивных объектов
- SportObjects - Описывает спортивный объект
### Regions
    import java.util.ArrayList;
    import java.util.List;
    
    // Класс, представляющий регион
    public class Regions {
        private final String region; // Название региона
        private final List<SportObjects> sportObjectsList = new ArrayList<>(); // Список объектов в регионе
    
        // Конструктор класса Regions
        public Regions(String region){
            this.region = region;
        }
    
        // Добавляет объект в регион
        public void addSportObject(SportObjects sportObject) {
            sportObjectsList.add(sportObject);
        }
    
        // Получает массив объектов в регионе
        public SportObjects[] getObjects() {
            SportObjects[] objectsArray = new SportObjects[sportObjectsList.size()];
            sportObjectsList.toArray(objectsArray);
            return objectsArray;
        }
    
        // Получает название региона
        public String getRegion() {
            return region;
        }
    }
### SportObjects
    // Класс, представляющий данные о спортивном объекте
    public class SportObjects {
        private final String name; // Название объекта
        private final String address; // Адрес
        private final String date; // Дата занесения в реестр
    
        public SportObjects(String name, String address, String date) {
            this.name = name;
            this.address = address;
            this.date = date;
        }
    
    
        // Получение названия объекта
        public String getName() {
            return name;
        }
    
        // Получение адреса
        public String getAddress() {
            return address;
        }
    
        // Получение даты занесения в реестр
        public String getDate() {
            return date;
        }
    }
## 2. Создаём класс CSVParser, который будет служить для обработки данных в формате CSV
### CSVParser
    import java.io.IOException;
    import java.nio.charset.Charset;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    
    //Класс для парсера
    public class CSVParser {
        public static Map<String, Regions> readFile(String path) {
            Map<String, Regions> regions = new HashMap<>();
    
            try {
                List<String> lines = Files.readAllLines(Paths.get(path), Charset.forName("windows-1251"));
                lines = lines.subList(1, lines.size()); // Пропускаем первую строку с заголовками
    
                for (String line : lines) {
                    String[] data = line.split(",", 2); // Отделяем номер, так есть пустые строки
                    data = data[1].toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Разделяем строку
                    String region = data[1].replaceAll("г. ", ""); // Убираем лишние символы в регионе
                    if (data.length == 4  && !data[0].isEmpty() && !data[1].isEmpty() && !data[2].isEmpty() && !data[3].isEmpty()) {
                        Regions regionObject = regions.computeIfAbsent(region, Regions::new);
                        regionObject.addSportObject(new SportObjects(data[0], data[2], data[3])); // Добавляем объект в регион
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return regions;
        }
    }
## 3. Создаём класс SQLLiteHandler, который будет выполнять роль взаимодействия с базой данных SQLite и предоставляет методы для выполнения различных операций с данными. Он используется для создания, чтения, обновления и удаления данных в базе данных, а также для выполнения запросов, которые выполнят наши задания
### SQLLiteHandler
    import org.jfree.data.category.CategoryDataset;
    import org.jfree.data.category.DefaultCategoryDataset;
    import org.sqlite.SQLiteConfig;
    
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;
    
    // Для создания БД и работы с ней
    public class SQLLiteHandler {
        private final Connection connection;
    
        // Конструктор класса
    
        public SQLLiteHandler() throws SQLException, ClassNotFoundException {
            Class.forName("org.sqlite.JDBC");
    
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
    
            connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Роман\\IdeaProjects\\FinalProject\\DataBase.db", config.toProperties());
    
            createTables();
        }
    
        // Для создания таблиц в базе данных, если они не существуют
        private void createTables() {
            try {
                connection.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS Regions" +
                                "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);");
    
                connection.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS SportObjects ("
                                + "objectId INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "name TEXT NOT NULL,"
                                + "address TEXT NOT NULL,"
                                + "date TEXT NOT NULL,"
                                + "regionId INTEGER,"
                                + "FOREIGN KEY (regionId) REFERENCES Regions(id));");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        // Для получения всех данных из базы
        public List<String> getAllData() throws SQLException {
            List<String> allData = new ArrayList<>();
    
            String sql = """
                    SELECT
                    	SportObjects.name, Regions.name AS region,
                    	SportObjects.address, SportObjects.date
                    FROM `SportObjects`
                    	LEFT JOIN `Regions`
                    		ON Regions.id = SportObjects.regionId
                    """;
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet data = preparedStatement.executeQuery()) {
                while (data.next()) {
                    String dbLine = data.getString("name") + ", " +
                            data.getString("region") + ", " +
                            data.getString("address") + ", " +
                            data.getString("date");
    
                    allData.add(dbLine);
                }
            }
            return allData;
        }
    
        // Для вставки региона в таблицу
        public void insertRegion(Regions region) throws SQLException {
            if (!regionExists(region.getRegion())) {
                String sqlInsertTeam = "INSERT INTO Regions(name) VALUES(?)";
    
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertTeam)) {
                    preparedStatement.setString(1, region.getRegion());
                    preparedStatement.executeUpdate();
                }
            }
    
            for (SportObjects sportObjects : region.getObjects()) {
                insertObject(sportObjects, getRegionId(region.getRegion()));
            }
        }
    
        // Проверка на существование региона
        private boolean regionExists(String region) throws SQLException {
            String sql = "SELECT COUNT(*) FROM Regions WHERE name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.getInt(1) > 0;
            }
        }
    
        // Для получения Id региона
        private int getRegionId(String region) throws SQLException {
            String sql = "SELECT id FROM Regions WHERE name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, region);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.getInt("id");
            }
        }
        
        // Для вставки объекта в таблицу
        public void insertObject(SportObjects sportObjects, int regionId) throws SQLException {
            String sql = "INSERT INTO SportObjects(name, address, date, regionId) VALUES (?, ?, ?, ?)";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, sportObjects.getName());
                preparedStatement.setString(2,sportObjects.getAddress());
                preparedStatement.setString(3, sportObjects.getDate());
                preparedStatement.setInt(4, regionId);
                preparedStatement.executeUpdate();
            }
        }
## Задание 3
        // Для получения топ-3 региона с наибольшим кол-вом спортивных объектов
        public String[] getCountObj() throws SQLException {
            String sql = "SELECT Regions.name as region, COUNT(SportObjects.name) as count\n" +
                    "FROM SportObjects\n" +
                    "JOIN Regions ON Regions.id = SportObjects.regionId\n" +
                    "GROUP BY region\n" +
                    "ORDER BY count DESC\n" +
                    "LIMIT 3;";
            ResultSet countObj = connection.createStatement().executeQuery(sql);
    
            List<String> regions = new ArrayList<>();
            while (countObj.next()) {
                regions.add(countObj.getString(1));
            }
            return regions.toArray(new String[0]);
        }
## Задание 2
        // Для получения среднего числа объектов в регионах
        public double getCountAvg() throws SQLException {
            String sql = "SELECT ROUND(CAST((SELECT COUNT(*) FROM 'SportObjects') AS FLOAT) / (SELECT COUNT(*) FROM 'Regions'), 2) as result";
            try (ResultSet avgObj = connection.createStatement().executeQuery(sql)) {
                return avgObj.getDouble(1);
            }
        }
## Задание 1
        // Для получения всех объектов по регионам (Москва и Московская область объединены)
        public CategoryDataset getGistObj() throws SQLException {
            String sql = "SELECT \n" +
                    "    CASE \n" +
                    "        WHEN Regions.name = '\"Москва\"' OR Regions.name = '\"Московская область\"' THEN '\"Москва и Московская область\"'\n" +
                    "        ELSE Regions.name \n" +
                    "    END as region,\n" +
                    "    COUNT(SportObjects.name) as count\n" +
                    "FROM \n" +
                    "    SportObjects\n" +
                    "JOIN \n" +
                    "    Regions ON Regions.id = SportObjects.regionId\n" +
                    "GROUP BY \n" +
                    "    region\n" +
                    "ORDER BY \n" +
                    "    count DESC;";
            ResultSet gistObj = connection.createStatement().executeQuery(sql);
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            while (gistObj.next()) {
                String region = gistObj.getString("region");
                int count = gistObj.getInt("count");
                dataset.addValue(count, region, region);
            }
            return dataset;
        }
    }
## 3. Создаем приложение для выполнения наших задач
### AnalyticsApp
    import org.jfree.chart.ChartFactory;
    import org.jfree.chart.ChartUtils;
    import org.jfree.chart.JFreeChart;
    import org.jfree.chart.axis.NumberAxis;
    import org.jfree.chart.plot.CategoryPlot;
    
    import java.io.File;
    import java.io.IOException;
    import java.sql.SQLException;
    import java.text.NumberFormat;
    
    public class AnalyticsApp {
        private static SQLLiteHandler dataBase;
    
        public static void main(String[] args) {
            try {
                // Импорт данных из CSV и вставка в базу данных
                importDataFromCSV("Объекты спорта.csv");
    
                // Вывод всех данных
                //displayDatabaseData();
    
                // Задание 1: Построить гистрограмму объектов по регионам
                System.out.println("Задание №1");
                buildGist();
                System.out.println();
    
                // Задание 2: Вывести среднее количество объектов спорта в регионах в консоль
                System.out.println("Задание №2");
                displayCountAvg();
                System.out.println();
    
                // Задание 3: Вывести топ-3 региона по спортивным объектам
                System.out.println("Задание №3");
                displayCountObj();
    
    
            } catch (ClassNotFoundException | SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    
        //Для импорта и чтения файла
        private static void importDataFromCSV(String csvFilePath) throws ClassNotFoundException, SQLException, IOException {
            var regions = CSVParser.readFile(csvFilePath);
            dataBase = new SQLLiteHandler();
    
            try {
                regions.values().forEach(region -> {
                    try {
                        dataBase.insertRegion(region);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        // Для вывода построчно всех объектов спорта
        private static void displayDatabaseData() throws SQLException {
            System.out.println("Спортивные объекты РФ:");
            System.out.println(String.join("\n", dataBase.getAllData()));
            System.out.println();
        }
    
        // Для построения гистрограммы
        private static void buildGist() throws SQLException, IOException {
            JFreeChart chart = ChartFactory.createBarChart("Количество объектов спорта по регионам", "Регион", "Количество объектов", dataBase.getGistObj());
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
            ChartUtils.saveChartAsPNG(new File("graphic.png"), chart, 1920, 1080);
            System.out.println("Гистрограмма построена !");
        }
    
        // Для вывода среднего кол-ва объектов в регионах
        private static void displayCountAvg() throws SQLException {
            System.out.println("Среднее кол-во объектов спорта в регионах: " + dataBase.getCountAvg());
        }
    
        // Для вывода топ-3 регионов по кол-ву спортивных объектов
        private static void displayCountObj() throws SQLException {
            System.out.println("Топ-3 региона с наибольшим количеством спортивных объектов: ");
            System.out.println(String.join(", ", dataBase.getCountObj()));
        }
    }
## 4. Вывод программы
Гистограмма, выполненная с помощью Java
![image](https://github.com/RomanNasibullin/FinalProject/assets/103939443/6da7b8ab-06c4-4d19-a05b-f3424c8793b7)
Гистограмма, выполненная с помощью Excel
![image](https://github.com/RomanNasibullin/FinalProject/assets/103939443/35b04067-9cdc-42db-870b-5574f36d156a)
![image](https://github.com/RomanNasibullin/FinalProject/assets/103939443/7361927c-58c0-40de-953c-aa604310c563)
