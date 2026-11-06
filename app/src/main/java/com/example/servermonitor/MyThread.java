package com.example.servermonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyThread extends Thread{
    private Server server;
    private Monitor monitor;
    public ServerCallback result;
    public MyThread(Server server, Monitor monitor, ServerCallback result) {
        this.server = server;
        this.monitor = monitor;
        this.result = result;
    }
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                boolean rezultat = monitor.CheckServer(server);
                String ora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                result.peRezultatPrimit(ora, rezultat);
                Thread.sleep(server.getFreq() * 1000);

            } catch (InterruptedException e) {
                System.out.println("Monitorizarea pentru " + server.getName() + " s-a oprit.");
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
