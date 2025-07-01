package com.example.tuninghub

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Tela de perfil do usuário.
 * Exibe o nome do usuário e suas fotos postadas.
 */
class ProfileActivity : AppCompatActivity() {

    // Elementos da interface
    private lateinit var txtUsername: TextView
    private lateinit var recyclerFotos: RecyclerView
    private lateinit var fotoAdapter: FotoAdapter
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Vincula componentes do layout
        txtUsername = findViewById(R.id.txtUsername)
        recyclerFotos = findViewById(R.id.recyclerFotos)

        // Define o layout do RecyclerView como um grid de 3 colunas
        recyclerFotos.layoutManager = GridLayoutManager(this, 3)

        // Configura a navegação inferior
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile // Marca "Perfil" como selecionado

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> true // Já está na tela de perfil

                R.id.btnAbrirUpload -> {
                    // Abre a tela de upload passando o ID do usuário
                    val intent = Intent(this, UploadActivity::class.java)
                    intent.putExtra("idUsuario", idUsuario)
                    startActivity(intent)
                    false
                }

                R.id.nav_feed, R.id.nav_curtida, R.id.nav_notificacoes -> {
                    // Funções ainda não implementadas
                    Toast.makeText(this, "Função disponível em breve", Toast.LENGTH_SHORT).show()
                    false
                }

                else -> false
            }
        }

        // Recupera o ID do usuário salvo no login
        val prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE)
        idUsuario = prefs.getInt("idUsuario", -1)

        if (idUsuario == -1) {
            Toast.makeText(this, "Erro: usuário não logado", Toast.LENGTH_SHORT).show()
        }

        // Botão de logout: limpa preferências e volta para login
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega as informações do perfil sempre que a tela for exibida
        if (idUsuario != -1) {
            carregarPerfil(idUsuario)
        }
    }

    /**
     * Busca os dados do perfil do usuário e exibe na interface.
     */
    private fun carregarPerfil(idUsuario: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://truulcorreio1.hospedagemdesites.ws/tuninghub/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.getPerfil(idUsuario)

        call.enqueue(object : Callback<PerfilResponse> {
            override fun onResponse(
                call: Call<PerfilResponse>,
                response: Response<PerfilResponse>
            ) {
                if (response.isSuccessful) {
                    val perfil = response.body()
                    if (perfil != null && perfil.success) {
                        // Atualiza o nome do usuário na tela
                        txtUsername.text = perfil.username

                        // Exibe a quantidade de fotos (debug/log)
                        Log.d("ProfileActivity", "Fotos recebidas: ${perfil.fotos}")
                        Toast.makeText(
                            this@ProfileActivity,
                            "Qtd fotos: ${perfil.fotos.size}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Preenche o RecyclerView com as fotos
                        fotoAdapter = FotoAdapter(perfil.fotos)
                        recyclerFotos.adapter = fotoAdapter

                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Erro ao carregar perfil",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Erro no servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                // Trata falhas de conexão ou erro desconhecido
                Toast.makeText(this@ProfileActivity, "Falha: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Interface Retrofit para obter os dados do perfil via API.
     */
    interface ApiService {
        @GET("getPerfilUsuario.php")
        fun getPerfil(@Query("idUsuario") id: Int): Call<PerfilResponse>
    }

    /**
     * Classe de resposta da API de perfil.
     */
    data class PerfilResponse(
        val success: Boolean,
        val username: String,
        val fotos: List<Foto>
    )

    /**
     * Representa uma foto do usuário.
     */
    data class Foto(
        val idPostagem: Int,
        val urlImagem: String,
        val filtro: String?
    )
}
