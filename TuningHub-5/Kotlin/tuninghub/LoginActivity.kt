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
 * Tela de login da aplicação TuningHub.
 * Permite ao usuário autenticar com nome de usuário e senha.
 */
class LoginActivity : AppCompatActivity() {

    // Componentes da interface
    private lateinit var txtUserName: EditText
    private lateinit var txtSenha: EditText
    private lateinit var txtErroLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialização dos campos de texto e botões
        txtUserName = findViewById(R.id.txtUserName)
        txtSenha = findViewById(R.id.txtSenha)
        txtErroLogin = findViewById(R.id.txtErroLogin)

        // Ação do botão de login
        val loginButton: Button = findViewById(R.id.btnLogin)
        loginButton.setOnClickListener {
            blockLogin()
        }

        // Ação do texto "Cadastrar"
        val txtCadastrar: TextView = findViewById(R.id.txtCadastrar)
        txtCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        val btnToggleSenha: ImageButton = findViewById(R.id.btnToggleSenhaLogin)
        var isPasswordVisible = false

        btnToggleSenha.setOnClickListener {
            if (isPasswordVisible) {
                txtSenha.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.ic_olho_fechado)
            } else {
                txtSenha.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.ic_olho_aberto)
            }
            txtSenha.setSelection(txtSenha.text.length) // Move cursor para o final
            isPasswordVisible = !isPasswordVisible
        }

    }

    /**
     * Valida os campos e realiza a chamada à API para autenticação do usuário.
     */
    private fun blockLogin() {
        val usuario = txtUserName.text.toString().trim()
        val senha = txtSenha.text.toString().trim()

        // Verifica se os campos estão preenchidos
        if (usuario.isEmpty() || senha.isEmpty()) {
            txtErroLogin.text = "Preencha todos os campos"
            txtErroLogin.visibility = TextView.VISIBLE
            return
        }

        // Cria uma instância do Retrofit para comunicação com a API
        val retrofit = Retrofit.Builder()
            .baseUrl("http://truulcorreio1.hospedagemdesites.ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.login(usuario, senha)

        // Enfileira a chamada de login de forma assíncrona
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true) {
                        val idUsuario = loginResponse.idUsuario

                        // Salva o ID do usuário nas preferências do app
                        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
                        prefs.edit().putInt("idUsuario", idUsuario).apply()

                        // Redireciona o usuário para a tela de perfil
                        val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        txtErroLogin.text = "Usuário ou senha inválidos"
                        txtErroLogin.visibility = TextView.VISIBLE
                    }
                } else {
                    txtErroLogin.text = "Erro na resposta do servidor"
                    txtErroLogin.visibility = TextView.VISIBLE
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Trata erro de rede ou falha na requisição
                txtErroLogin.text = "Erro: ${t.message}"
                txtErroLogin.visibility = TextView.VISIBLE
            }
        })
    }

    /**
     * Interface da API usada para autenticar o usuário com Retrofit.
     */
    interface ApiService {
        @GET("tuninghub/loginTuningHub.php")
        fun login(
            @Query("usuario") usuario: String,
            @Query("senha") senha: String
        ): Call<LoginResponse>
    }

    /**
     * Classe de dados que representa a resposta da API de login.
     */
    data class LoginResponse(
        val success: Boolean, // Indica se o login foi bem-sucedido
        val idUsuario: Int,   // ID do usuário logado
        val nome: String,     // Nome do usuário
        val email: String     // E-mail do usuário
    )
}
