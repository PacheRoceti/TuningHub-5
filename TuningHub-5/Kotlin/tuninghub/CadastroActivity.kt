package com.example.tuninghub

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Tela de cadastro de novo usuário.
 * Permite que o usuário insira email, nome de usuário e senha para se cadastrar.
 */
class CadastroActivity : AppCompatActivity() {

    // Declaração dos componentes da interface
    private lateinit var edtEmail: EditText
    private lateinit var edtUsername: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnCadastrar: Button
    private lateinit var txtJaTemConta: TextView
    private lateinit var txtErroCadastro: TextView // Campo para exibir erros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Inicializa os componentes da interface
        edtEmail = findViewById(R.id.edtEmail)
        edtUsername = findViewById(R.id.edtUsername)
        edtSenha = findViewById(R.id.edtSenha)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        txtJaTemConta = findViewById(R.id.txtJaTemConta)
        txtErroCadastro = findViewById(R.id.txtErroCadastro) // NOVO

        // Define ação do botão de cadastro
        btnCadastrar.setOnClickListener {
            cadastrarUsuario()
        }

        // Define ação do texto "Já tem conta?" para ir à tela de login
        txtJaTemConta.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnToggleSenha: ImageButton = findViewById(R.id.btnToggleSenhaCadastro)
        var isPasswordVisible = false

        btnToggleSenha.setOnClickListener {
            if (isPasswordVisible) {
                edtSenha.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.ic_olho_fechado)
            } else {
                edtSenha.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.ic_olho_aberto)
            }
            edtSenha.setSelection(edtSenha.text.length)
            isPasswordVisible = !isPasswordVisible
        }

    }

    /**
     * Função responsável por realizar o cadastro do usuário
     * utilizando uma requisição GET via Retrofit.
     */
    private fun cadastrarUsuario() {
        // Captura os dados digitados pelo usuário
        val email = edtEmail.text.toString().trim()
        val usuario = edtUsername.text.toString().trim()
        val senha = edtSenha.text.toString().trim()

        // Cria a instância do Retrofit para comunicação com o servidor
        val retrofit = Retrofit.Builder()
            .baseUrl("http://truulcorreio1.hospedagemdesites.ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Cria a implementação da interface da API
        val service = retrofit.create(ApiService::class.java)

        // Faz a chamada para o endpoint de cadastro
        val call = service.cadastrarUsuario(email, usuario, senha)
        call.enqueue(object : Callback<ApiResponse> {
            // Resposta do servidor recebida com sucesso (mesmo que seja erro de negócio)
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        // Cadastro bem-sucedido
                        txtErroCadastro.visibility = TextView.GONE
                        Toast.makeText(
                            this@CadastroActivity,
                            "Cadastro realizado com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()
                        // Redireciona para a tela de login
                        val intent = Intent(this@CadastroActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Erro retornado pela API
                        txtErroCadastro.text = apiResponse?.message ?: "Erro ao cadastrar"
                        txtErroCadastro.visibility = TextView.VISIBLE
                    }
                } else {
                    // Erro no protocolo da resposta HTTP
                    txtErroCadastro.text = "Erro no servidor"
                    txtErroCadastro.visibility = TextView.VISIBLE
                }
            }

            // Falha na requisição (problema de rede ou exceção)
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                txtErroCadastro.text = "Erro: ${t.message}"
                txtErroCadastro.visibility = TextView.VISIBLE
            }
        })
    }

    /**
     * Interface interna com o endpoint da API de cadastro.
     * Usa método GET com parâmetros via URL.
     */
    interface ApiService {
        @GET("tuninghub/cadastroTuningHub.php")
        fun cadastrarUsuario(
            @Query("email") email: String,
            @Query("usuario") usuario: String,
            @Query("senha") senha: String
        ): Call<ApiResponse>
    }

    /**
     * Classe de modelo para receber a resposta da API.
     * Indica se o cadastro foi bem-sucedido e traz uma mensagem opcional.
     */
    data class ApiResponse(
        val success: Boolean,
        val message: String?
    )
}
