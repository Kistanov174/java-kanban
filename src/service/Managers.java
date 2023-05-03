package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;

public class Managers {

    public static TaskManager getDefault(URI uri) throws IOException, InterruptedException {
        return new HttpTaskManager(uri);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        return  gsonBuilder.create();
    }
}
