package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.json.JSONObject;
import org.w3c.dom.Node;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NoteRepository {
    private final NoteDao dao;
    private NoteAPI api;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture = null;

    public NoteRepository(NoteDao dao) {
        this.dao = dao;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.api = new NoteAPI();
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     *
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */
    public LiveData<Note> getSynced(String title) {
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            if (theirNote == null) return;
            var ourNote = note.getValue();
            if (ourNote == null || ourNote.version < theirNote.version)  {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();
    }

    public void upsertLocal(Note note) {
        //note.updatedAt = System.currentTimeMillis();
//        note.updatedAt = Instant.now().getEpochSecond();
        note.version = note.version + 1;
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============
    public LiveData<Note> getRemote(String title) {
        // TODO: Implement getRemote!
        // TODO: Set up polling background thread (MutableLiveData?)
        // TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.

        // Start by fetching the note from the server _once_ and feeding it into MutableLiveData.
        // Start by fetching the note from the server ONCE. call getByTittle
        // Then, set up a background thread that will poll the server every 3 seconds.

        // You may (but don't have to) want to cache the LiveData's for each title, so that
        // you don't create a new polling thread every time you call getRemote with the same title.
        // You don't need to worry about killing background threads.


//        public void registerTimeListener() {
//            var executor = Executors.newSingleThreadScheduledExecutor();
//            clockFuture = executor.scheduleAtFixedRate(() -> {
//                realTimeData.postValue(System.currentTimeMillis());
//            }, 0, 1000, TimeUnit.MILLISECONDS);
//        }
//        var api = new NoteAPI();
//        var note = api.getByTitle(title);

        MediatorLiveData<Note> remoteNote = new MediatorLiveData<>();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
           // String oldData = api.getByTitle(title).content;
            @Override
            public void run() {
                //Note currData = api.getByTitle(title);
                remoteNote.postValue(api.getByTitle(title));
                Log.i("GET REMOTE", api.getByTitle(title).content);
               //if (!Objects.equals(oldData, currData.content)) {
                   // oldData = currData.content;
              //}
            }
        }, 0, 3000, TimeUnit.MILLISECONDS);

        return remoteNote;
        //throw new UnsupportedOperationException("Not implemented yet");
    }

    public void upsertRemote(Note note) {
        // TODO: Implement upsertRemote!
       // throw new UnsupportedOperationException("Not implemented yet");
        new Thread(new Runnable() {
            @Override
            public void run() {
                api.putByTitle(note);
            }
        }).start();
//        if (note != null) {
//            api.putByTitle(note.title, note.content);
//        }
        //api.putByTitle(note.title, note.content);
    }
}
