package ind.shop.task_app
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapter(
    private var items: List<Tasks_detail>,
    private val context: Context,
    private val onCompleteClick: (Tasks_detail) -> Unit
) : RecyclerView.Adapter<ind.shop.task_app.MyAdapter.MyViewHolder>() {

    var currentFilter: String = "pending" // "pending" or "completed"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_in_task_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pos = holder.adapterPosition
        if (pos == RecyclerView.NO_POSITION) return

        val task = items[pos]
        holder.title.text = task.title
        holder.desc.text = task.text_description

        if (currentFilter == "pending") {
            holder.completedDate.visibility = View.GONE
            holder.animation.progress = 0f
            holder.animation.pauseAnimation()
            holder.title.alpha = 1.0f
            holder.desc.alpha = 1.0f
        } else {
            val dateStr = task.completedAt?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "Completed on: " + sdf.format(Date(it))
            } ?: "Completed"
            holder.completedDate.text = dateStr
            holder.completedDate.visibility = View.VISIBLE
            holder.animation.progress = 1f
            holder.animation.pauseAnimation()
            holder.title.alpha = 0.9f
            holder.desc.alpha = 0.9f
        }

        holder.card.setOnClickListener {
            val p = holder.adapterPosition
            if (p == RecyclerView.NO_POSITION) return@setOnClickListener
            val t = items[p]
            val intent = Intent(context, Adding_Editing_Task::class.java).apply {
                putExtra(Adding_Editing_Task.ACTION_TYPE_KEY, "view")
                putExtra(Adding_Editing_Task.TASK_ID_KEY, t.id)
                putExtra(Adding_Editing_Task.TASK_TITLE_KEY, t.title)
                putExtra(Adding_Editing_Task.TASK_DESC_KEY, t.text_description)
            }
            context.startActivity(intent)
        }

        holder.animation.setOnClickListener {
            val p = holder.adapterPosition
            if (p == RecyclerView.NO_POSITION) return@setOnClickListener
            onCompleteClick(items[p])
        }
    }

    override fun getItemCount(): Int = items.size

    fun refreshData(newList: List<Tasks_detail>) {
        items = newList
        notifyDataSetChanged()
    }

    fun getTaskAt(position: Int): Tasks_detail = items[position]

    fun indexOf(task: Tasks_detail): Int = items.indexOfFirst { it.id == task.id }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tasktitle)
        val desc: TextView = itemView.findViewById(R.id.taskdescription)
        val completedDate: TextView = itemView.findViewById(R.id.completedDate)
        val card: CardView = itemView.findViewById(R.id.listitemview)
        val animation: LottieAnimationView = itemView.findViewById(R.id.completeAnimation)
    }

}