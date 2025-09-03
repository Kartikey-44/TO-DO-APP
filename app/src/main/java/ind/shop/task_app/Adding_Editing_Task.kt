package ind.shop.task_app

import android.app.Dialog
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import ind.shop.task_app.databinding.ActivityAddingEditingTaskBinding
import ind.shop.task_app.databinding.DeletedEditedSavedBinding

class Adding_Editing_Task : AppCompatActivity() {

    //initializing the companion objects to take the data fron other screens
    companion object {

        // remember we can use companion ovjects of this class in different class to send data to it

        //this key is used in main acitivity and the task list display to send data about what to do
        //add edit or view
        const val ACTION_TYPE_KEY = "action_type"     // "add", "view", "edit"

        // this used in task list display and in database to send the task id
        const val TASK_ID_KEY = "taskID"

        // this used in task list display and in database to send the task title
        const val TASK_TITLE_KEY = "taskTitle"

        // this used in task list display and in database to send the task description
        const val TASK_DESC_KEY = "taskDesc"
    }



    private lateinit var db: ToDoAppDatabaseHelper
    private lateinit var binding: ActivityAddingEditingTaskBinding
    private var taskID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddingEditingTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val color = if (isNightMode) Color.WHITE else Color.BLACK

        binding.backanimation.addValueCallback(
            KeyPath("**"), // applies to all layers
            LottieProperty.COLOR_FILTER
        ) { PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }



        db = ToDoAppDatabaseHelper(this)



        when ((intent.getStringExtra(ACTION_TYPE_KEY) ?: "add").lowercase()) {
            "add" -> setupAddTask()
            "view" -> setupViewTask()
            "edit" -> setupEditTask()
        }

        binding.backanimation.setOnClickListener { finish() }
    }

    private fun setupAddTask() {


        binding.saveEdit.text = "SAVE"
        binding.saveEdit.setOnClickListener {
            val title = binding.tasktextview.text.toString().trim()
            val desc = binding.taskdesctextview.text.toString().trim()

            val dialog = Dialog(this)
            val dialogBinding = DeletedEditedSavedBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialog.window?.apply {
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setGravity(Gravity.CENTER)
                setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
            }
            dialog.setCanceledOnTouchOutside(true)

            if (title.isEmpty() || desc.isEmpty()) {
                dialogBinding.animation.setAnimation("loading.json")
                dialogBinding.animation.playAnimation()
                dialogBinding.message.text = "Any Field Cannot Be Empty"
                Handler(Looper.getMainLooper()).postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }, 3000)
            } else {
                db.insertTask(Tasks_detail(0, title, desc))
                dialogBinding.animation.setAnimation("Success.json")
                dialogBinding.animation.playAnimation()
                dialogBinding.message.text = "Your Task Is Added"
                binding.tasktextview.text.clear()
                binding.taskdesctextview.text.clear()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }, 2000)
            }
            dialog.show()
        }
    }

    private fun setupViewTask() {

        binding.addedittaskheading.text = "VIEW YOUR TASK HERE"
        val title = intent.getStringExtra(TASK_TITLE_KEY) ?: ""
        val desc = intent.getStringExtra(TASK_DESC_KEY) ?: ""
        binding.tasktextview.setText(title)
        binding.taskdesctextview.setText(desc)
        binding.tasktextview.isEnabled = false
        binding.taskdesctextview.isEnabled = false
        binding.saveEdit.text = "VIEWED"
        binding.saveEdit.setOnClickListener { finish() }
    }

    private fun setupEditTask() {
        binding.addedittaskheading.text = "EDIT YOUR TASK HERE"

        taskID = intent.getIntExtra(TASK_ID_KEY, -1)
        if (taskID == -1) {
            finish()
            return
        }
        val task = db.getTaskById(taskID)
        binding.tasktextview.setText(task.title)
        binding.taskdesctextview.setText(task.text_description)
        binding.saveEdit.text = "CONFIRM"
        binding.saveEdit.setOnClickListener {
            val newTitle = binding.tasktextview.text.toString().trim()
            val newDesc = binding.taskdesctextview.text.toString().trim()
            if (newTitle.isEmpty() || newDesc.isEmpty()) {
                val dialog = Dialog(this)
                val dialogBinding = DeletedEditedSavedBinding.inflate(layoutInflater)
                dialog.setContentView(dialogBinding.root)
                dialogBinding.animation.setAnimation("Success.json")
                dialogBinding.animation.playAnimation()
                dialogBinding.message.text = "Any Field Cannot Be Empty"
                Handler(Looper.getMainLooper()).postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }, 2000)
                dialog.show()
            } else {
                db.updateTask(Tasks_detail(taskID, newTitle, newDesc))
                val dialog = Dialog(this)
                val dialogBinding = DeletedEditedSavedBinding.inflate(layoutInflater)
                dialog.setContentView(dialogBinding.root)
                dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
                dialogBinding.animation.setAnimation("Coolemoji.json")
                dialogBinding.animation.playAnimation()
                dialogBinding.message.text = "Your Task Is Edited"
                Handler(Looper.getMainLooper()).postDelayed({
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }, 2000)
                dialog.setOnDismissListener { finish() }
                dialog.show()
            }
        }
    }
}