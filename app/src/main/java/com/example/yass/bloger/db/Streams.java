package com.example.yass.bloger.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yass on 3/22/17.
 */

public class Streams {

    private static Streams ourStreams;

    private static HashMap<String, String> streams = new HashMap<String, String>();
    private static HashMap<String, String> requests = new HashMap<String, String>();

    public Streams() {
        streams.put("JAZZ","http://jazz128instr.streamr.ru");
        streams.put("ROCK","http://jfm1.hostingradio.ru:14536/rock80.mp3");


        requests.put("JAZZ", "http://radiopleer.com/info/ijstream.txt");
        requests.put("ROCK", "http://radiopleer.com/info/rock80.txt");
    }

    public static Streams getInstance(){

        if (ourStreams == null){
            ourStreams = new Streams();
        }
        return ourStreams;
    }

    public HashMap<String, String> getStreams(){
        return streams;
    }

    public HashMap<String, String> getRequests(){
        return requests;
    }
}
