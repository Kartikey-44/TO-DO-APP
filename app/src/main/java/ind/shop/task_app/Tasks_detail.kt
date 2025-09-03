package ind.shop.task_app

data class Tasks_detail(
    val id: Int,
    val title: String,
    val text_description: String,
    val isCompleted: Int = 0,
    val completedAt: Long? = null
)