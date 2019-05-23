package ro.pub.cs.systems.eim.practicaltest02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PracticalTest02MainActivity extends AppCompatActivity {

    TextView result;
    EditText clientPort;
    Spinner type;
    Button connect, get;

    ServerThread serverThread = null;
    ClientThread clientThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        clientPort = findViewById(R.id.port);
        connect = findViewById(R.id.start);
        get = findViewById(R.id.get);
        result = findViewById(R.id.result);
        type = findViewById(R.id.information_type_spinner);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String svPort = "5000";
                if (svPort != null && !svPort.isEmpty()) {
                    Log.d("AICI", "start server on port: " + svPort);
                    serverThread = new ServerThread(Integer.parseInt(svPort));
                    serverThread.start();
                }
            }
        });

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = type.getSelectedItem().toString();
                Log.d("AICI", query);

                String clPort = clientPort.getText().toString();
                String clAddress = "127.0.0.1";

                clientThread = new ClientThread(clAddress, Integer.parseInt(clPort), query, result);
                clientThread.start();
            }
        });

    }

    @Override
    protected void onDestroy() {

        if (serverThread != null)
            serverThread.stopThread();
        super.onDestroy();


    }
}
