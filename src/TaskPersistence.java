import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskPersistence {
    private static final String TASKS_FILE = "tasks.json";
    private Gson gson;

    public TaskPersistence() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void saveTasks(List<Task> tasks) throws IOException {
        try (FileWriter writer = new FileWriter(TASKS_FILE)) {
            gson.toJson(tasks, writer);
        }
    }

    public List<Task> loadTasks() throws IOException {
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            return List.of(); // Return empty list if file doesn't exist
        }

        try (FileReader reader = new FileReader(TASKS_FILE)) {
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            return tasks != null ? tasks : List.of();
        }
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}