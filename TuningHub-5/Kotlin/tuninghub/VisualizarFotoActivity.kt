package com.example.tuninghub

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisualizarFotoActivity : AppCompatActivity() {

    // ImageView para exibir a foto em tela cheia
    private lateinit var imageView: ImageView

    // URL da foto a ser exibida
    private var fotoUrl: String? = null

    // ID da foto para operações como excluir ou editar
    private var fotoId: Int = -1

    // Nome do filtro aplicado à foto (padrão "Nenhum")
    private var filtroAplicado: String = "Nenhum"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout da activity
        setContentView(R.layout.activity_visualizar_foto)

        // Inicializa views do layout
        val imageView = findViewById<ImageView>(R.id.imageViewFull)
        val textFiltro = findViewById<TextView>(R.id.textFiltro)
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)

        // Define ação para o botão de voltar (voltar para a tela anterior)
        btnVoltar.setOnClickListener { onBackPressed() }

        // Recebe os dados passados pela Intent (URL, ID da foto e filtro aplicado)
        fotoUrl = intent.getStringExtra("FOTO_URL")
        fotoId = intent.getIntExtra("FOTO_ID", -1)
        filtroAplicado = intent.getStringExtra("FILTRO_APLICADO") ?: "Nenhum"

        // Usa a biblioteca Glide para carregar a imagem a partir da URL na ImageView
        Glide.with(this)
            .load(fotoUrl)
            .into(imageView)

        // Exibe o filtro aplicado na TextView
        textFiltro.text = "Filtro: $filtroAplicado"

        // Configura ação do botão de excluir foto
        findViewById<ImageView>(R.id.btnExcluir).setOnClickListener {
            if (fotoId != -1) deletarFoto(fotoId) // Só tenta deletar se ID válido
            else Toast.makeText(this, "ID da foto inválido", Toast.LENGTH_SHORT).show()
        }

        // Configura ação do botão para editar filtro da foto
        findViewById<ImageView>(R.id.btnEditarFiltro).setOnClickListener {
            if (fotoId != -1) editarFiltro(fotoId) // Só tenta editar se ID válido
            else Toast.makeText(this, "ID da foto inválido", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para tratar o clique na seta de navegação (voltar)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // volta para a tela anterior
        return true
    }

    // Função para deletar a foto com o ID passado
    private fun deletarFoto(idPostagem: Int) {
        // Cria um diálogo de confirmação para exclusão
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Excluir Foto")
            .setMessage("Tem certeza que deseja excluir esta foto?")
            .setPositiveButton("Sim") { _, _ ->
                // Ao confirmar, chama o Retrofit para deletar a imagem no backend
                val retrofit = RetrofitClient.instance
                val call = retrofit.deletarImagem(idPostagem)

                // Envia requisição assíncrona
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            // Se deletar com sucesso, mostra toast e fecha activity
                            Toast.makeText(
                                this@VisualizarFotoActivity,
                                "Foto excluída com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            // Se resposta com erro, mostra mensagem de erro
                            Toast.makeText(
                                this@VisualizarFotoActivity,
                                "Erro ao excluir foto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Em caso de falha na conexão, mostra mensagem com erro
                        Toast.makeText(
                            this@VisualizarFotoActivity,
                            "Erro de conexão: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null) // botão para cancelar exclusão
            .create()

        // Exibe o diálogo para o usuário
        alertDialog.show()
    }

    // Função para editar o filtro da foto
    private fun editarFiltro(idPostagem: Int) {
        // Converte lista de filtros em array para mostrar no diálogo
        val filtros = FiltroUtils.listaFiltros.toTypedArray()
        var filtroSelecionado = filtros[0] // filtro padrão selecionado inicialmente

        // Cria diálogo para escolha do filtro (radio buttons)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha o novo filtro")
            .setSingleChoiceItems(filtros, 0) { _, which ->
                // Atualiza filtroSelecionado conforme opção escolhida pelo usuário
                filtroSelecionado = filtros[which]
            }
            .setPositiveButton("Salvar") { _, _ ->
                // Ao salvar, chama o backend para atualizar o filtro da imagem
                val retrofit = RetrofitClient.instance
                val call = retrofit.editarFiltroImagem(idPostagem, filtroSelecionado)

                // Envia requisição assíncrona para atualizar filtro
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            // Se sucesso, mostra toast e atualiza texto do filtro na tela
                            Toast.makeText(
                                this@VisualizarFotoActivity,
                                "Filtro atualizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findViewById<TextView>(R.id.textFiltro).text = "Filtro: $filtroSelecionado"
                        } else {
                            // Caso resposta com erro, mostra mensagem de erro
                            Toast.makeText(
                                this@VisualizarFotoActivity,
                                "Erro ao atualizar filtro",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Em caso de falha na conexão, loga erro e mostra toast
                        Log.e("ErroFiltro", "Erro de conexão", t)
                        Toast.makeText(
                            this@VisualizarFotoActivity,
                            "Erro de conexão: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null) // botão para cancelar edição
            .show() // exibe o diálogo
    }
}
