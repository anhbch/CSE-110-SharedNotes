package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;
    private NoteDao noteDao;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Note getByTitle(String title) {
        title = title.replace(" ", "%20");
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/note/" + title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("GET BY TITLE", body);
            return Note.fromJSON(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Note(title, "");
    }

    public void putByTitle(String title, String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20") ;
        String json = "{\"content\":\"" + msg + "\",\"updated_at\":\""
                + System.currentTimeMillis() + "\"}";

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("PUT", body)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var result = response.body().string();
            Log.i("PUT BY TITLE", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public LiveData<Note> getByTitle(String title) {
//        title = title.replace(" ", "%20");
//        //MutableLiveData<Note> noteMutableLiveData = new MutableLiveData<>();
//        var request = new Request.Builder()
//                .url("https://sharednotes.goto.ucsd.edu/note/" + title)
//                .method("GET BY TITLE", null)
//                .build();
//
//        try (var response = client.newCall(request).execute()) {
//            assert response.body() != null;
//            var body = response.body().string();
//            //Gson gson = new Gson();
//            //var result =  gson.fromJson(body, Note.class);
//            //noteMutableLiveData.postValue(result);
//            Log.i("GET BY TITLE", result.toString());
//            //return noteMutableLiveData;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


}
