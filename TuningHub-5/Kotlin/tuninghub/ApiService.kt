package com.example.tuninghub

// Importações necessárias para lidar com requisições HTTP e o Retrofit
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Interface que define os endpoints da API usados no app TuningHub.
 * O Retrofit utiliza essa interface para gerar automaticamente o código necessário
 * para fazer as chamadas HTTP.
 */
interface ApiService {

    /**
     * Envia uma imagem para o servidor com dados adicionais (id do usuário e filtro aplicado).
     * Utiliza multipart/form-data para enviar o arquivo.
     */
    @Multipart
    @POST("uploadFotoTuningHub.php")
    fun uploadImagem(
        @Part imagem: MultipartBody.Part,
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("filtro") filtro: RequestBody
    ): Call<ResponseBody>


    // Deleta uma imagem do servidor com base no ID da postagem.

    @FormUrlEncoded
    @POST("deleteFotoTuningHub.php")
    fun deletarImagem(
        @Field("id_postagem") idPostagem: Int
    ): Call<ResponseBody>


    // Edita o filtro aplicado a uma imagem já postada.

    @FormUrlEncoded
    @POST("editarFiltroTuningHub.php")
    fun editarFiltroImagem(
        @Field("id_postagem") idPostagem: Int,
        @Field("novo_filtro") novoFiltro: String
    ): Call<ResponseBody>
}
