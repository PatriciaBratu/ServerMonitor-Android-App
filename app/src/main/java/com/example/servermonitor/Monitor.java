package com.example.servermonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Monitor {
    public List<Server> servers = new ArrayList<>();
    public void readServers(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            Gson gson = new Gson();
            Type tipLista = new TypeToken<List<Server>>() {}.getType();
            List<Server> servereCitite = gson.fromJson(reader, tipLista);

            if (servereCitite != null) {
                this.servers = servereCitite;
            }

            reader.close();
            inputStream.close();

            System.out.println("Fișier importat cu succes!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Eroare la parsarea fișierului JSON ales!");
        }
        for(int i = 0; i < servers.size(); i++) {
            if(!servers.get(i).validate())
                System.out.println(servers.get(i).getName() + " is not ok");

        }
    }
    public boolean CheckServer(Server server) throws IOException, InterruptedException {
        switch (server.getUtils()){
            case "ping":
                int exitCode = -1;
                synchronized (this) {
                    String command = "ping -c 1 -W " + server.getTimeout_s() + " " + server.getAddress();
                    Process process = Runtime.getRuntime().exec(command);
                    exitCode = process.waitFor();
                }
                if (exitCode == 0) {
                    System.out.println("PING SUCCES: " + server.getAddress() + " este ONLINE.");
                    return true;
                } else {
                    System.out.println("PING EȘUAT: " + server.getAddress() + " este OFFLINE.");
                    return false;
                }
            case "curl":
            case "http_request":
                try {
                    String address = server.getAddress();
                    if (!address.startsWith("http://") && !address.startsWith("https://")) {
                        address = "https://" + address;
                    }
                    URL url = new URL(address);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(server.getTimeout_s() * 1000);
                    connection.setReadTimeout(server.getTimeout_s() * 1000);

                    connection.setRequestMethod("HEAD");

                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    System.out.println(responseCode);
                    if(responseCode >= 400)
                        return false;
                    if(responseCode >= 200)
                        return true;
                } catch (Exception e) {
                    System.out.println("Eroare severă la conectare: " + e.getMessage());
                    return false;
                }
            case "telnet":
                try{
                    if (!server.getAddress().contains(":")) {
                        System.out.println("adresa nu contine si portul");
                        return false;
                    }
                    String[] parts = server.getAddress().split(":");
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);

                    Socket socket = new Socket();

                    socket.connect(new InetSocketAddress(ip, port), server.getTimeout_s() * 1000);

                    socket.close();
                    return true;

                }catch (Exception e) {
                    System.out.println("Eroare severă la conectare: " + e.getMessage());
                    return false;
                }
            default:
                break;
        }
        return false;
    }
    public void trimiteAlertaDiscord(Server server) {
        String discordWebhookUrl = "https://discord.com/api/webhooks/1542457565370392666/N-hnO2Mx1ZxxV9hKTNmOcnlApArG1WSLMy2JHzLZH78BFu0XCczcRNfJouA2nft5OPEg";

        try {
            URL url = new URL(discordWebhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String mesaj = "{\"content\": \"ALERTA: Serverul `" + server.getName() + "` a picat!\"}";

            OutputStream os = connection.getOutputStream();
            os.write(mesaj.getBytes("UTF-8"));
            os.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == 204 || responseCode == 200) {
                System.out.println("Alerta trimisă cu succes pe Discord!");
            } else {
                System.out.println("Eroare la trimitere pe Discord. Cod: " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("Nu s-a putut trimite alerta pe Discord: " + e.getMessage());
        }
    }
    public void scrieInJurnal(Context context, String mesaj) {
        try {
            File fisier = new File(context.getFilesDir(), "istoric_servere.txt");
            FileWriter scriitor = new FileWriter(fisier, true);

            scriitor.write(mesaj + "\n");

            scriitor.close();

        } catch (IOException e) {
            System.out.println("Eroare la scrierea în fișier: " + e.getMessage());
        }
    }
    public String citesteJurnal(Context context) {
        try {
            File fisier = new File(context.getFilesDir(), "istoric_servere.txt");
            if (!fisier.exists()) {
                return "Jurnalul este gol.";
            }

            BufferedReader reader = new BufferedReader(new FileReader(fisier));
            StringBuilder textLog = new StringBuilder();
            String linie;

            while ((linie = reader.readLine()) != null) {
                textLog.append("->");
                textLog.append(linie).append("\n");
            }
            reader.close();

            return textLog.toString();
        } catch (Exception e) {
            return "Eroare la citirea jurnalului: " + e.getMessage();
        }
    }
    public void delete (Context context) throws IOException {
        File fisier = new File(context.getFilesDir(), "istoric_servere.txt");
        FileWriter s = new FileWriter(fisier, false);
    }

    public void trimiteNotificareSistem(Context context, String numeServer) {
        String CHANNEL_ID = "ServerMonitor_Alerts";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alerte Servere Picate",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Alertă: A picat un server!")
                .setContentText("Serverul " + numeServer + " nu mai răspunde.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }

}

