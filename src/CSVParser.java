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