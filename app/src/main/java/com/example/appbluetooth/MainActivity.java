package com.example.appbluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Timer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    MeowBottomNavigation bottomNavigation;

    BluetoothAdapter AdaptadorBluetooth;
    private static final int Solicita_Ativacao = 1;
    private static final int Solicita_Conexao = 2;
    private static final int MESSAGE_READ = 3;
    ConnectedThread connectedThread;

    Boolean Medicao = false;

    Button btnConectar;
    Button IniciarMedicao;

    TextView infoTextView;
    int estadoBtnMedir = 1;

    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;


    StringBuilder dadosBluetooth = new StringBuilder();

    Handler mHandler;

    boolean conexao = false;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (AdaptadorBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Seu dispositvo não possui bluetooth", Toast.LENGTH_LONG).show();
        } else if (!AdaptadorBluetooth.isEnabled()) {
            Intent intentAtivaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentAtivaBluetooth, Solicita_Ativacao);
            //Toast.makeText(getApplicationContext(), "Seu dispositvo possui bluetooth", Toast.LENGTH_LONG).show();

        }
        

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_devices));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_raio));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_settings));

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment fragment = null;

                switch (item.getId()) {
                    case 1:
                        fragment = new DevicesFragment();
                        break;
                    case 2:
                        fragment = new MedicaoFragment();

                        break;
                    case 3:
                        fragment = new SettingsFragment();
                        break;
                }
                loadFragment(fragment);
            }
        });

        // bottomNavigation.setCount(1, "5");

        bottomNavigation.show(2, true);

        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                Toast.makeText(getApplicationContext(), "Voce Clicou Em" + item.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
                //Toast.makeText(getApplicationContext(),"Voce Clicou Denovo Em" + item.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        /*
        btnConectar = (Button) findViewById(R.id.btnConexao);
        IniciarMedicao = (Button) findViewById(R.id.btnIniciar);

        infoTextView = (TextView) findViewById(R.id.informacoes);


        AdaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (AdaptadorBluetooth == null) {
            Toast.makeText(this, "Seu dispositvo não possui bluetooth", Toast.LENGTH_LONG).show();
        } else if (!AdaptadorBluetooth.isEnabled()) {
            Intent intentAtivaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentAtivaBluetooth, Solicita_Ativacao);

        }

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao) {

                    try {
                        meuSocket.close();
                        conexao = false;
                        btnConectar.setText("Conectar");
                        Toast.makeText(getApplicationContext(), "Bluetooth Desconectado ", Toast.LENGTH_SHORT).show();
                    } catch (IOException erro) {
                        Toast.makeText(getApplicationContext(), "Ocorreu um Erro: " + erro, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent abrelista = new Intent(MainActivity.this, ListaDispositvos.class);
                    startActivityForResult(abrelista, Solicita_Conexao);
                }
            }
        });


        IniciarMedicao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    if(estadoBtnMedir == 1 && Medicao == false){
                     connectedThread.enviar("m");

                    }else if(estadoBtnMedir == 2 && Medicao){
                        connectedThread.enviar("p");
                        
                    }

                }else {
                    Toast.makeText(getApplicationContext(), "Bluetooth não está Conectado", Toast.LENGTH_SHORT).show();
                }

            }
        });


        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String recebidos = (String) msg.obj;
                    dadosBluetooth.append(recebidos);

                    int finInformacao = dadosBluetooth.indexOf("}");
                    if (finInformacao > 0) {
                        String dadosCompletos = dadosBluetooth.substring(0, finInformacao);
                        int tamInformacao = dadosCompletos.length();
                        if (dadosBluetooth.charAt(0) == '{') {

                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);
                            Log.d("Recebidos", dadosFinais);
                           /* while(estadoBtnMedir == 1 && Medicao) {
                                infoTextView.setText(dadosFinais);
                                Log.d("Recebidos", dadosFinais);
                                dadosBluetooth.delete(0, dadosBluetooth.length());
                            }*/


        /*

                            infoTextView.setText(dadosFinais);
                            if(dadosFinais.contains("PARADO")){
                                IniciarMedicao.setText("INICIAR MEDIÇÃO");
                                estadoBtnMedir = 1;
                                Medicao = false;
                            }else{
                                Medicao = true;
                                estadoBtnMedir = 2;
                                IniciarMedicao.setText("PARAR MEDIÇÃO");
                            }

                            /*if (dadosFinais.contains("l1on")) {
                                btnLed1.setText("LED 1 LIGADO");
                            } else if (dadosFinais.contains("l1off")) {
                                btnLed1.setText("LED 1 DESLIGADO");
                            }
                            if (dadosFinais.contains("l2on")) {
                                btnLed2.setText("LED 2 LIGADO");
                            } else if (dadosFinais.contains("l2off")) {
                                btnLed2.setText("LED 2 DESLIGADO");
                            }*/

                        /*}


                        dadosBluetooth.delete(0, dadosBluetooth.length());

                    }
                }
            }
        };

        */

    }


    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Solicita_Ativacao:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "O bluetooth foi ativado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "O bluetooth não foi ativado, o app será encerrado", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case Solicita_Conexao:
                if (resultCode == Activity.RESULT_OK) {

                    MAC = data.getExtras().getString(ListaDispositvos.ENDERECO_MAC);
                    //Toast.makeText(this, "MAC FINAL: "+ MAC, Toast.LENGTH_LONG).show();
                    meuDevice = AdaptadorBluetooth.getRemoteDevice(MAC);

                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                        meuSocket.connect();
                        conexao = true;

                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();

                        btnConectar.setText("Desconectar");
                        Toast.makeText(this, "VOCE FOI CONECTADO COM: " + MAC, Toast.LENGTH_LONG).show();
                    } catch (IOException erro) {
                        conexao = false;
                        Toast.makeText(this, "OCORREU UM ERRO: " + erro, Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(this, "FALHA AO OBTER O MAC", Toast.LENGTH_LONG).show();
                }
        }

    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {

            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);

                    String dadosBT = new String(mmBuffer, 0, numBytes);
                    // Send the obtained bytes to the UI activity.
                    mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBT).sendToTarget();

                } catch (IOException e) {

                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);


            } catch (IOException e) {

            }
        }


        // Call this method from the main activity to shut down the connection.
        /*public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

         */
    }
}
