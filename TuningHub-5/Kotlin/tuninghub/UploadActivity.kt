package com.example.tuninghub

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream

// Activity responsável por permitir o upload de imagens com filtros aplicados
class UploadActivity : AppCompatActivity() {

    // Views da interface
    private lateinit var imageView: ImageView
    private lateinit var btnSelecionar: Button
    private lateinit var btnEnviar: Button
    private lateinit var recyclerViewFiltros: RecyclerView

    // Variável para armazenar o filtro escolhido pelo usuário
    private var filtroSelecionado: String = ""

    // Uri da imagem selecionada pelo usuário
    private var imagemUri: Uri? = null

    // Código para identificar a ação de selecionar imagem na galeria
    private val PICK_IMAGE_REQUEST = 1

    // ID do usuário, recebido via Intent
    private var idUsuario: Int = -1

    // Método chamado na criação da Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload) // Define o layout da tela

        // Recupera o ID do usuário passado pela activity anterior
        idUsuario = intent.getIntExtra("idUsuario", -1)

        // Liga as variáveis às views do layout
        imageView = findViewById(R.id.imageView)
        btnSelecionar = findViewById(R.id.btnSelecionar)
        btnEnviar = findViewById(R.id.btnEnviar)
        recyclerViewFiltros = findViewById(R.id.recyclerFiltros)

        // Configura o RecyclerView para mostrar os filtros disponíveis
        val filtros = FiltroUtils.listaFiltros // Lista de filtros disponíveis (externa)
        val adapter = FiltroAdapter(this, filtros) { filtro ->
            filtroSelecionado = filtro // Atualiza filtro selecionado quando o usuário escolhe um
        }

        // Layout do RecyclerView em grade (3 colunas)
        recyclerViewFiltros.layoutManager = GridLayoutManager(this, 3)
        recyclerViewFiltros.adapter = adapter

        // Configura o clique no botão Selecionar para abrir a galeria, se tiver permissão
        btnSelecionar.setOnClickListener {
            if (temPermissao()) {
                abrirGaleria()
            } else {
                solicitarPermissao()
            }
        }

        // Configura o clique no botão Enviar para subir a imagem junto com o filtro e usuário
        btnEnviar.setOnClickListener {
            imagemUri?.let {
                if (filtroSelecionado.isNotBlank()) {
                    enviarImagem(it, idUsuario)
                } else {
                    Toast.makeText(this, "Selecione um filtro antes de enviar", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Selecione uma imagem primeiro", Toast.LENGTH_SHORT).show()
        }

        // Configuração do menu inferior (Bottom Navigation)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Define que o item Upload está selecionado no menu
        bottomNavigationView.selectedItemId = R.id.btnAbrirUpload

        // Define o que acontece quando o usuário clica nos itens do menu
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnAbrirUpload -> {
                    // Já está na tela Upload, não faz nada
                    true
                }
                R.id.nav_profile -> {
                    // Abre a tela de perfil, passando o id do usuário
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("idUsuario", idUsuario)
                    startActivity(intent)
                    true
                }
                // Itens com funcionalidades futuras, por enquanto mostram um Toast
                R.id.nav_feed, R.id.nav_curtida, R.id.nav_notificacoes -> {
                    Toast.makeText(this, "Função disponível em breve", Toast.LENGTH_SHORT).show()
                    false
                }
                else -> false
            }
        }
    }

    // Função que abre a galeria para selecionar uma imagem
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Verifica se a permissão para ler imagens está concedida
    private fun temPermissao(): Boolean {
        val permissao = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES // Permissão para Android 13 ou superior
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // Permissão para versões anteriores
        }
        return ContextCompat.checkSelfPermission(this, permissao) == PackageManager.PERMISSION_GRANTED
    }

    // Solicita a permissão necessária para acessar a galeria
    private fun solicitarPermissao() {
        val permissao = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permissao, 100)
    }

    // Resultado da solicitação de permissão
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria() // Se permitiu, abre a galeria
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
        }
    }

    // Método chamado após o usuário selecionar a imagem da galeria
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imagemUri = data.data // Salva o URI da imagem selecionada
            imageView.setImageURI(imagemUri) // Exibe a imagem na ImageView
        }
    }

    // Converte o Uri da imagem para um arquivo temporário no cache para upload
    private fun getFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg") // Nome temporário
        file.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream) // Copia dados do inputStream para o arquivo
        }
        return file
    }

    // Realiza o upload da imagem usando Retrofit para comunicação HTTP
    private fun enviarImagem(uri: Uri, idUsuario: Int) {
        val file = getFileFromUri(uri) // Pega arquivo a partir do Uri
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull()) // Define o tipo MIME
        val imagemPart = MultipartBody.Part.createFormData("imagem", file.name, requestFile) // Cria parte multipart da imagem
        val idUsuarioPart = idUsuario.toString().toRequestBody("text/plain".toMediaTypeOrNull()) // Parte do idUsuario
        val filtroPart = filtroSelecionado.toRequestBody("text/plain".toMediaTypeOrNull()) // Parte do filtro selecionado

        // Configura Retrofit com URL base e conversor JSON
        val retrofit = Retrofit.Builder()
            .baseUrl("http://truulcorreio1.hospedagemdesites.ws/tuninghub/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        // Chama o endpoint para upload da imagem e trata a resposta assincronamente
        api.uploadImagem(imagemPart, idUsuarioPart, filtroPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UploadActivity, "Imagem enviada com sucesso!", Toast.LENGTH_LONG).show()
                        finish() // Fecha a activity ao terminar o upload com sucesso
                    } else {
                        Log.e("UPLOAD_ERROR", "Código HTTP: ${response.code()}")
                        Toast.makeText(this@UploadActivity, "Erro no upload: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("UPLOAD_FAILURE", "Erro: ${t.message}", t)
                    Toast.makeText(this@UploadActivity, "Falha: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
