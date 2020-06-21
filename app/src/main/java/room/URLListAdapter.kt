package room

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flightmobileapp.R


class URLListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<URLListAdapter.URLViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var urls = emptyList<URLItem>() // Cached copy of words

    inner class URLViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urlItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): URLViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return URLViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: URLViewHolder, position: Int) {
        val current = urls[position]
        holder.urlItemView.text = current.url
    }

    internal fun setUrls(urls: List<URLItem>) {
        this.urls = urls
        notifyDataSetChanged()
    }

    override fun getItemCount() = urls.size
}