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

    // Для получения среднего числа объектов в регионах
    public double getCountAvg() throws SQLException {
        String sql = "SELECT ROUND(CAST((SELECT COUNT(*) FROM 'SportObjects') AS FLOAT) / (SELECT COUNT(*) FROM 'Regions'), 2) as result";
        try (ResultSet avgObj = connection.createStatement().executeQuery(sql)) {
            return avgObj.getDouble(1);
        }
    }

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