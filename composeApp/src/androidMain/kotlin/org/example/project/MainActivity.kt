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

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = cep,
            onValueChange = { newCep ->
                cep = newCep
            },
            label = { Text("Digite o CEP") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            enabled = isInputEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        ElevatedButton(
            onClick = {
                // Limpa resultado anterior
                resultado = null

                isLoading = true
                isInputEnabled = false

                coroutineScope.launch {
                    val response = consultarCep(cep)
                    resultado = response
                    isLoading = false
                    isInputEnabled = true
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
            Text("Rua: ${it.logradouro}")
            Text("Bairro: ${it.bairro}")
            Text("Cidade: ${it.localidade}")
            Text("Estado: ${it.uf}")
            Text("DDD: ${it.ddd}")
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
        .baseUrl("https://viacep.com.br/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ViaCepApi by lazy {
        retrofit.create(ViaCepApi::class.java)
    }
}

data class CepResult(
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String,
    val ddd: String
)
