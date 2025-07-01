package com.example.tuninghub

// Importações necessárias para trabalhar com RecyclerView e views Android
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter personalizado para exibir uma lista de filtros em um RecyclerView.
 * Permite ao usuário selecionar um filtro visualmente.
 *
 * @param context Contexto da aplicação
 * @param filtros Lista de filtros disponíveis
 * @param onFiltroSelecionado Callback chamado quando um filtro é selecionado
 */
class FiltroAdapter(
    private val context: Context,
    private val filtros: List<String>,
    private val onFiltroSelecionado: (String) -> Unit
) : RecyclerView.Adapter<FiltroAdapter.FiltroViewHolder>() {

    // Posição do item atualmente selecionado. -1 significa nenhum selecionado.
    private var selectedPosition = -1

    /**
     * ViewHolder que representa o item da lista.
     * Cada item é um TextView que exibe o nome do filtro.
     */
    inner class FiltroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFiltro: TextView = itemView.findViewById(R.id.textFiltro)
    }

    /**
     * Cria e retorna um novo ViewHolder para um item da lista.
     * Infla o layout item_filtro.xml.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiltroViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_filtro, parent, false)
        return FiltroViewHolder(view)
    }

    /**
     * Associa dados a um ViewHolder específico (atualiza o conteúdo visual).
     * Também trata a seleção de itens.
     */
    override fun onBindViewHolder(holder: FiltroViewHolder, position: Int) {
        val filtro = filtros[position]
        holder.textFiltro.text = filtro

        // Define o fundo do item com base na seleção
        if (selectedPosition == position) {
            holder.textFiltro.setBackgroundResource(R.drawable.filtro_background_selected)
        } else {
            holder.textFiltro.setBackgroundResource(R.drawable.filtro_background_unselected)
        }

        // Define o comportamento de clique do item
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition) // Atualiza visual do item anterior
            notifyItemChanged(selectedPosition) // Atualiza visual do novo item
            onFiltroSelecionado(filtro) // Chama o callback com o filtro selecionado
        }
    }

    /**
     * Retorna a quantidade total de itens na lista.
     */
    override fun getItemCount(): Int = filtros.size
}
