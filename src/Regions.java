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
