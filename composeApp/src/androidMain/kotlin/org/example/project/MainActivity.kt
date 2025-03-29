package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CepConsultaScreen()
            }
        }
    }
}

@Composable
fun CepConsultaScreen() {
    var cep by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<CepResult?>(null) }
    var isButtonEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var isInputEnabled by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = cep,
                onValueChange = { newCep -> cep = newCep },
                label = { Text("Digite o CEP") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                enabled = isInputEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            ElevatedButton(
                onClick = {

                    resultado = null
                    errorMessage = null

                    isLoading = true
                    isInputEnabled = false

                    coroutineScope.launch {
                        val response = consultarCep(cep)
                        if (response != null) {
                            resultado = response
                        } else {
                            errorMessage = "Erro ao consultar o CEP. Tente novamente."
                        }
                        isLoading = false
                        isInputEnabled = true
                        isButtonEnabled = true
                    }
                },
                enabled = isButtonEnabled && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colors.onPrimary
                    )
                } else {
                    Text("Consultar")
                }
            }

            resultado?.let {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("CEP: ${it.cep}")
                    Text("Logradouro: ${it.logradouro}")
                    Text("Bairro: ${it.bairro}")
                    Text("Localidade: ${it.localidade}")
                    Text("UF: ${it.uf}")
                }
            }
        }
    }
}

suspend fun consultarCep(cep: String): CepResult? {
    return try {
        withContext(Dispatchers.IO) {
            val service = RetrofitInstance.api
            service.getCep(cep)
        }
    } catch (e: Exception) {
        null
    }
}

interface ViaCepApi {
    @GET("ws/{cep}/json/")
    suspend fun getCep(@Path("cep") cep: String): CepResult
}

object RetrofitInstance {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://viacep.com.br/") // URL base da API
        .addConverterFactory(GsonConverterFactory.create()) // Conversor de JSON
        .build()

    val api: ViaCepApi by lazy {
        retrofit.create(ViaCepApi::class.java)
    }
}

data class CepResult(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String,
   // val ddd: String
)
