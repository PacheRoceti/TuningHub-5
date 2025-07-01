package com.example.tuninghub

// Importações necessárias
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * Adapter para exibir uma galeria de fotos em um RecyclerView.
 * Usa a biblioteca Glide para carregar imagens a partir de URLs.
 *
 * @param fotos Lista de objetos Foto (definido dentro de ProfileActivity)
 */
class FotoAdapter(private var fotos: List<ProfileActivity.Foto>) :
    RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    /**
     * ViewHolder que representa cada item de foto na lista.
     */
    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFoto: ImageView = view.findViewById(R.id.imgFoto)
    }

    /**
     * Cria a visualização do item (ViewHolder) inflando o layout item_foto.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false) // Layout personalizado para cada foto
        return FotoViewHolder(view)
    }

    /**
     * Associa os dados da foto à view (ViewHolder).
     * Carrega a imagem usando Glide e define comportamento ao clicar.
     */
    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]

        // Carrega a imagem na ImageView com Glide a partir da URL
        Glide.with(holder.itemView.context)
            .load(foto.urlImagem)
            .into(holder.imgFoto)

        // Define o clique no item da foto para abrir a VisualizarFotoActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VisualizarFotoActivity::class.java).apply {
                putExtra("FOTO_URL", foto.urlImagem)             // URL da imagem
                putExtra("FOTO_ID", foto.idPostagem)             // ID da postagem
                putExtra("FILTRO_APLICADO", foto.filtro ?: "Nenhum") // Filtro aplicado (ou "Nenhum" se for nulo)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Retorna a quantidade de fotos que o adapter irá exibir.
     */
    override fun getItemCount(): Int = fotos.size

    /**
     * Atualiza a lista de fotos exibida e notifica o RecyclerView que os dados mudaram.
     *
     * @param novasFotos Lista atualizada de fotos
     */
    fun atualizarFotos(novasFotos: List<ProfileActivity.Foto>) {
        fotos = novasFotos
        notifyDataSetChanged() // Atualiza a interface com os novos dados
    }
}
