package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }


    @Override
    public void run() {
        if (socket == null) {
            Log.e("AICI", "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        Log.d("AICI", "Started Communication Thread");
        try {
            BufferedReader bufferedReader = getReader(socket);
            PrintWriter printWriter = getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e("AICI", "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i("AICI", "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");


            // We read the first query sent in the ClientThread
            String query1 = bufferedReader.readLine();


            if (query1 == null || query1.isEmpty()) {
                Log.e("AICI", "[COMMUNICATION THREAD] Error receiving parameters from client (query1 / information type!");
                return;
            }

            HashMap<String, ModelBitcoin> dataServer = serverThread.getData();
            ModelBitcoin responseData;
            String result;

            if (dataServer.containsKey(query1)) {
                Log.i("AICI", "[COMMUNICATION THREAD] Getting the information from the cache...");
                responseData = dataServer.get(query1);

            } else {
                Log.i("AICI", "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                // In case of POST change to HttpPost and remover the arghuments from the urkl
                HttpGet httpPost = new HttpGet("https://api.coindesk.com/v1/bpi/currentprice/EUR.json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpPost, responseHandler);
                if (pageSourceCode == null) {
                    Log.e("AICI", "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }
                /*
                Document document = Jsoup.parse(pageSourceCode);
                Element element = document.child(0);
                Elements elements = element.getElementsByTag("WordDefinition");
                Log.d("abc", elements.text());
                String querryData = elements.get(0).text();
                Log.d("abc", query1);
                */
                if (query1.equals("EUR")) {
                    int st = pageSourceCode.indexOf("\"rate\":\"", pageSourceCode.indexOf("\"rate\":\"") + 1);
                    int end = pageSourceCode.indexOf("\",\"", st + 1);
                    String querryData = pageSourceCode.substring(st + "\"rate\":\"".length(), end);
                    responseData = new ModelBitcoin(querryData);
                } else {
                    int st = pageSourceCode.indexOf("\"rate\":\"");
                    int end = pageSourceCode.indexOf("\",\"", st + 1);
                    String querryData = pageSourceCode.substring(st + "\"rate\":\"".length(), end);
                    responseData = new ModelBitcoin(querryData);
                }

                serverThread.setData(query1, responseData);


            }
            result = responseData.queryResponse1;

            // Send the data to the client
            printWriter.println(result);
            printWriter.flush();

            socket.close();
        }catch (Exception e){
            Log.d("AICI", "Exceptie: + " + e);
        }
    }
}
