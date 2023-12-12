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