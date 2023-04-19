package com.example.ktor_4878

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.ktor_4878.ui.theme.KTOR4878Theme
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class MainActivity : ComponentActivity() {
    private var server: ApplicationEngine? = null

    private val serverRunning = mutableStateOf(false)

    private fun startServer() {
        val environment = applicationEngineEnvironment {
            val keystore = generateCertificate(keyAlias = "myKey")
            sslConnector(
                keyStore = keystore,
                keyAlias = "myKey",
                keyStorePassword = { charArrayOf() },
                privateKeyPassword = { charArrayOf() }) {
                port = 9000
            }
            watchPaths = emptyList()
            module {

                routing {
                    get("/") {
                        call.respondText { "OK" }
                    }
                }
            }
        }

        server = embeddedServer(Netty, environment).also {
            lifecycleScope.launch(Dispatchers.Default) {
                it.start(wait = false)
            }
        }
        serverRunning.value = true
    }

    private fun stopServer() {
        server?.stop()
        serverRunning.value = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KTOR4878Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Button(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = {
                                if (serverRunning.value) stopServer() else startServer()
                        }) {
                            Text(if (serverRunning.value)  "Stop server" else "Start server")
                        }
                    }

                }
            }
        }
    }
    companion object {
        init {
            Security.removeProvider("BC")
            Security.addProvider(BouncyCastleProvider())
        }
    }
}