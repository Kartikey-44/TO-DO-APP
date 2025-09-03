package ind.shop.task_app

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import ind.shop.task_app.databinding.ActivityTaskListDisplayBinding
import ind.shop.task_app.databinding.DelteAndMarkCompleteConfirmationBinding
import ind.shop.task_app.databinding.DeletedEditedSavedBinding
import ind.shop.task_app.databinding.InstructionsBinding

class Task_List_Display : AppCompatActivity() {
    private lateinit var binding: ActivityTaskListDisplayBinding
    private lateinit var adapter: MyAdapter
    private lateinit var db: ToDoAppDatabaseHelper

    // "pending" or "completed"
    private var currentFilter: String = "pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskListDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets(binding.main)
        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val color = if (isNightMode) Color.WHITE else Color.BLACK

        binding.backanimation.addValueCallback(
            KeyPath("**"), // applies to all layers
            LottieProperty.COLOR_FILTER
        ) { PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }

        db = ToDoAppDatabaseHelper(this)

        adapter = MyAdapter(
            items = emptyList(),
            context = this,
            onCompleteClick = { task -> onCompleteAction(task) }
        )
        adapter.currentFilter = currentFilter

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        attachSwipeHelper(binding.recyclerView)

        // Simple toggle: tap the orange message text to switch filter
        binding.messageattaskpage.text = "Showing: Pending (tap to switch)"
        binding.messageattaskpage.setOnClickListener { toggleFilter() }

        binding.backanimation.setOnClickListener { finish() }


        loadDataForCurrentFilter()

        binding.instructions.setOnClickListener {
            val dialog= Dialog(this)
            val dialogBinding= InstructionsBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
            dialogBinding.understood.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.addtask.setOnClickListener {
            val intent=Intent(this,Adding_Editing_Task::class.java)
            intent.putExtra(Adding_Editing_Task.ACTION_TYPE_KEY,"add")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataForCurrentFilter()
        emptyornot()
    }

    private fun toggleFilter() {
        currentFilter = if (currentFilter == "pending") "completed" else "pending"
        adapter.currentFilter = currentFilter
        binding.messageattaskpage.text = if (currentFilter == "pending")
            "Showing: Pending (tap to switch)" else "Showing: Completed (tap to switch)"
        loadDataForCurrentFilter()
    }

    private fun loadDataForCurrentFilter() {
        val data = if (currentFilter == "pending") db.getAllPendingTasks() else db.getAllCompletedTasks()
        adapter.refreshData(data)
    }

    private fun applyWindowInsets(view: androidx.constraintlayout.widget.ConstraintLayout) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



    private fun attachSwipeHelper(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                val currentItem = adapter.getTaskAt(position)

                when (direction) {
                    // Right swipe = EDIT (confirm)
                    ItemTouchHelper.RIGHT -> showEditDialog(position, currentItem)

                    // Left swipe = DELETE (confirm)
                    ItemTouchHelper.LEFT -> showDeleteDialog(position, currentItem.id)
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    // Lottie tap handler = Complete (in Pending) / Incomplete (in Completed)
    private fun onCompleteAction(task: Tasks_detail) {
        val pos = adapter.indexOf(task).takeIf { it >= 0 } ?: return
        if (currentFilter == "pending") {
            showMarkCompleteDialog(pos, task)
        } else {
            showMarkIncompleteDialog(pos, task)
        }
    }

    private fun showEditDialog(position: Int, task: Tasks_detail) {
        val dialogBinding = DelteAndMarkCompleteConfirmationBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))

        dialogBinding.messageConfirm.text = "Do you want to edit this task?"
        dialogBinding.animationConfirm.setAnimation("Coolemoji.json")
        dialogBinding.animationConfirm.playAnimation()
        dialogBinding.deleteComplete.text = "CONFIRM"
        dialogBinding.cancel.text = "CANCEL"

        dialogBinding.deleteComplete.setOnClickListener {

            dialog.dismiss()
            if(task.isCompleted==1){
                showSuccessDialog("Task Cannot Be Edited After Compeletion","ERRORAnimation.json")
                adapter.notifyItemChanged(position)
            }
            else {
                startActivity(
                    Intent(this, Adding_Editing_Task::class.java).apply {
                        putExtra(Adding_Editing_Task.ACTION_TYPE_KEY, "edit")
                        putExtra(Adding_Editing_Task.TASK_ID_KEY, task.id)
                    }
                )
            }
        }
        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
            adapter.notifyItemChanged(position)
        }

        dialog.show()
    }

    private fun showMarkCompleteDialog(position: Int, task: Tasks_detail) {
        val dialogBinding = DelteAndMarkCompleteConfirmationBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))

        dialogBinding.messageConfirm.text = "Mark this task as completed?"
        dialogBinding.animationConfirm.setAnimation("Coolemoji.json")
        dialogBinding.animationConfirm.playAnimation()
        dialogBinding.deleteComplete.text = "CONFIRM"
        dialogBinding.cancel.text = "CANCEL"

        dialogBinding.deleteComplete.setOnClickListener {
            dialog.dismiss()
            db.markTaskCompleted(task.id)
            loadDataForCurrentFilter()
            showSuccessDialog("Task Completed", "celebrationandbirthdayemoji.json")
        }
        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
            adapter.notifyItemChanged(position)
        }

        dialog.show()
    }

    private fun showMarkIncompleteDialog(position: Int, task: Tasks_detail) {
        val dialogBinding = DelteAndMarkCompleteConfirmationBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))

        dialogBinding.messageConfirm.text = "Mark this task as incomplete?"
        dialogBinding.animationConfirm.setAnimation("Failed.json")
        dialogBinding.animationConfirm.playAnimation()
        dialogBinding.deleteComplete.text="CONFIRM"
        dialogBinding.cancel.text = "CANCEL"

        dialogBinding.deleteComplete.setOnClickListener {
            dialog.dismiss()
            db.markTaskIncomplete(task.id)
            loadDataForCurrentFilter()
            showSuccessDialog("Task Moved to Pending", "Coolemoji.json")
        }
        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
            adapter.notifyItemChanged(position)
        }

        dialog.show()



    }

    private fun showDeleteDialog(position: Int, taskId: Int) {
        val confirmBinding = DelteAndMarkCompleteConfirmationBinding.inflate(LayoutInflater.from(this))
        val confirmDialog = Dialog(this)
        confirmDialog.setContentView(confirmBinding.root)
        confirmDialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))

        confirmBinding.messageConfirm.text = "Do you really want to delete this task?"
        confirmBinding.animationConfirm.setAnimation("loading.json")
        confirmBinding.animationConfirm.playAnimation()
        confirmBinding.deleteComplete.text = "DELETE"
        confirmBinding.cancel.text = "CANCEL"
        confirmDialog.show()

        confirmBinding.deleteComplete.setOnClickListener {
            confirmDialog.dismiss()
            db.deleteTask(taskId)
            loadDataForCurrentFilter()
            showSuccessDialog("Task Deleted", "DeleteBin.json")
        }
        confirmBinding.cancel.setOnClickListener {
            confirmDialog.dismiss()
            adapter.notifyItemChanged(position)
        }
    }

    private fun showSuccessDialog(message: String, animation: String) {
        val dBinding = DeletedEditedSavedBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dBinding.animation.setAnimation(animation)
        dBinding.animation.playAnimation()
        dBinding.message.text = message
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
        dialog.show()
    }

    private fun emptyornot(){
        val hasitem=(binding.recyclerView.adapter?.itemCount?:0)>0
        binding.recyclerView.isVisible=hasitem
        binding.emptylottie.isVisible=!hasitem
        if(hasitem){
            binding.emptylottie.cancelAnimation()
        }
        else{
            binding.emptylottie.playAnimation()
        }
    }




}