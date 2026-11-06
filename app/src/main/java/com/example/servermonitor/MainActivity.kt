package com.example.servermonitor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.servermonitor.ui.theme.ServerMonitorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val monitor = Monitor()
        SQLiteDatabase.loadLibs(this)
        monitor.delete(this)     // aceasta functie sterge datele de la rularile anterioare
//        monitor.readServers(this)
//        val listaDeServere = monitor.servers

        enableEdgeToEdge()
        setContent {
            ServerMonitorTheme {
                var listaDeServere by remember { mutableStateOf<List<Server>>(emptyList()) }
                var ecranCurent by remember { mutableStateOf("Servere") }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current
                    val filePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            monitor.readServers(context, uri)
                            listaDeServere = monitor.servers
                        }
                    }
                    if (ecranCurent == "Servere") {
                        Row {Button(onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "*/*"))
                        }) {
                            Text("Start")
                        }
                            Button(onClick = { ecranCurent = "Loguri" }) {
                                Text("Loguri")
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Greeting(
                                modifier = Modifier.padding(innerPadding)

                            )
                            for (server in listaDeServere) {
                                PresentServers(server, monitor)
                            }
                        }
                    } else {
                        EcranLoguri(
                            monitor = monitor,
                            onInapoi = { ecranCurent = "Servere" }
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun EcranLoguri(monitor: Monitor, onInapoi: () -> Unit) {
    val context = LocalContext.current
    var continutLoguri by remember { mutableStateOf("Se încarcă...") }
    LaunchedEffect(Unit) {
        while (true) {
            continutLoguri = monitor.citesteJurnal(context)
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column() {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = onInapoi) {
                    Text("Inapoi la Servere")
                }
            }

            Text(
                text = continutLoguri,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = "Monitor servers!",
        modifier = modifier,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresentServers(server : Server, monitor: Monitor){
    val context = LocalContext.current
    val db = Database(context)
    db.stergeDatabase()
    val coroutineScope = rememberCoroutineScope()
    var lastCheckTime by remember { mutableStateOf("Not checked") }
    var serverColor by remember { mutableStateOf(Color.Yellow) }
    var state by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = server.name) {

        val actiuneLaFinal = object : ServerCallback {
            override fun peRezultatPrimit(oraExacta: String, stare: Boolean) {
                lastCheckTime = oraExacta
                state = stare
                if(!state) {
                    monitor.trimiteAlertaDiscord(server)
                    val msg = oraExacta + " / " + server.name + " / "+server.utils + " / DOWN / connection timeout"
                    monitor.scrieInJurnal(context, msg)
                    db.insereazaLog(oraExacta, server.name, "DOWN")
                } else {
                    val msg = oraExacta + " / " + server.name + " / "+server.utils + " / UP"
                    monitor.scrieInJurnal(context, msg)
                    db.insereazaLog(oraExacta, server.name, "UP")
                }
                serverColor = if (state) Color.Green else Color.Red
            }
        }

        val thread = MyThread(server, monitor, actiuneLaFinal)

        thread.start()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                Text(text = server.name, color = serverColor)
                Text(text = lastCheckTime)
            }

            Button(onClick = {serverColor = Color.Yellow
                coroutineScope.launch(Dispatchers.IO) {

                println("Verific în fundal pentru: ${server.name}")
                if (monitor.CheckServer(server)) {
                    serverColor = Color.Green
                    val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    lastCheckTime = format.format(Date())
                    val msg = format.format(Date()) + " / " + server.name + " / "+server.utils + " / UP"
                    monitor.scrieInJurnal(context, msg)
                    db.insereazaLog(lastCheckTime, server.name, "UP")
                } else {
                    serverColor = Color.Red
                    monitor.trimiteAlertaDiscord(server)
                    val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    lastCheckTime = format.format(Date())
                    val msg = format.format(Date()) + " / " + server.name + " / "+server.utils + " / DOWN / connection timeout"
                    monitor.scrieInJurnal(context, msg)
                    monitor.trimiteNotificareSistem(context, server.name)
                    db.insereazaLog(lastCheckTime, server.name, "DOWN")
                }
            }}) {
                Text("Check")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ServerMonitorTheme {
        Greeting()
    }
}

