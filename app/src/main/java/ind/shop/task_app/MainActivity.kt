package ind.shop.task_app

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.net.Uri
import android.os.Build
import android.renderscript.RenderScript
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.shop.task_app.databinding.ActivityMainBinding
import androidx.core.graphics.toColorInt
import com.google.android.material.snackbar.Snackbar
import ind.shop.task_app.databinding.DialogMagicBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    val channelname="channelname"
    val channelid="channelid"
    private var cachedQuote: String? = null
    //defining the binding for the main activity.xml
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //initializing the binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        //setting the content view with binding
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val retrofitService = Retrofit.Builder()
            .baseUrl("https://zenquotes.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        binding.magictext.setOnClickListener {
            val dialog = Dialog(this)

            if (cachedQuote != null) {
                // Show cached quote without API call
                showQuoteDialog(dialog, cachedQuote!!)
            } else {
                // Fetch from API only once
                retrofitService.getquotedata()
                    .enqueue(object : Callback<List<quote_data_classItem>> {
                        override fun onResponse(
                            call: Call<List<quote_data_classItem>>,
                            response: Response<List<quote_data_classItem>>
                        ) {
                            val quoteList = response.body()
                            if (!quoteList.isNullOrEmpty()) {
                                cachedQuote = quoteList[0].q // Save in cache
                                showQuoteDialog(dialog, cachedQuote!!)
                            }
                        }

                        override fun onFailure(
                            call: Call<List<quote_data_classItem>>,
                            t: Throwable
                        ) {
                            Snackbar.make(binding.root,"No Internet Connection", Snackbar.LENGTH_LONG).show()
                        }
                    })
            }
        }


        val imagePrefs = getSharedPreferences("imagepref", MODE_PRIVATE)
        val savedImageUri = imagePrefs.getString("image", null)

// Restore saved image if available
        savedImageUri?.let {
            try {
                val uri = Uri.parse(it)
                contentResolver.openInputStream(uri)?.close() // Test access
                binding.profileImage.setImageURI(uri)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load saved image: ${e.message}")
                imagePrefs.edit().remove("image").apply() // Remove invalid entry
            }
        }


        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {

                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    // Set the picked image
                    binding.profileImage.setImageURI(it)

                    // Save the image URI string
                    imagePrefs.edit {
                        putString("image", it.toString())
                    }
                }
            }

        binding.profileImage.setOnClickListener {
            pickImage.launch("image/*")
        }


        // Username typewriter animation

        val prefs = getSharedPreferences("name", MODE_PRIVATE)
        val username = prefs.getString("username", null).toString()

        //defining the handler
        val handler = android.os.Handler(Looper.getMainLooper())

        //setting the starting index to 0
        var index = 0

        //for motion of it setting the forward to true
        var forward = true

        //delay between each character
        val delay: Long = 200

        //setting the pause after the whole name is written
        val pause: Long = 1000

        //defining the runnable object responsible for creating animation
        val runnable = object : Runnable {

            //overriding function run with out code
            override fun run() {

                //check condition for forward if it is true
                if (forward) {

                    //index will increase and letter will be written
                    index++

                    //if index is greater and equal to the length of username then forward will be false and
                    //writing will stop
                    if (index >= username.length + 1) {
                        forward = false

                        //the pause will be applied and the function will return to start
                        handler.postDelayed(this, pause)
                        return
                    }

                    //if forward is false then this condition will run after the name is written and index will
                    //start decreasing
                } else {
                    index--

                    //at the moment when the index recaches 0 the writing will start again
                    if (index <= 0) forward = true
                }

                //it will define that at username textview in ui the name should be written and it should be
                //substring so that new words added to it from start 0 to max index
                binding.username.text = username.substring(0, index)

                // this will apply delay between each character
                handler.postDelayed(this, delay)
            }
        }

        //it will start the function
        handler.post(runnable)

        // Gradient shader
        val shader = LinearGradient(

            //defining the start of gradient from top left anf bottom left to end point of text size of username
            0f, 0f, 0f, binding.username.textSize,

            //defining the array which will have colors to used in gradient
            intArrayOf("#FF6F00".toColorInt(), "#ff6921".toColorInt()),

            //position null be define that no gap between colours of the gradient
            null, Shader.TileMode.CLAMP
        )

        //setting the gradient to username
        binding.username.paint.shader = shader

        //setting the gradient to tap to see  magic text
        binding.magictext.paint.shader = shader

        //setting the gradient to add task button
        binding.addtasktext.paint.shader = shader

        //setting the gradient to view task button
        binding.viewtasktext.paint.shader = shader

        // getting the blink animation with a variable defined in xml in anim folder
        val blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink)


        //setting the animation to the add task button
        binding.addtasktext.startAnimation(blinkAnimation)

        //setting the animation to the view task button
        binding.viewtasktext.startAnimation(blinkAnimation)

        //remember these buttons are actually material cardview not button but have clickable property


        /* setting that on tapping add task the adding page opens with the action of add which is send
        through the intent*/
        binding.addtask.setOnClickListener {
            val intent = Intent(this, Adding_Editing_Task::class.java)

            //sending the intent
            intent.putExtra(Adding_Editing_Task.ACTION_TYPE_KEY, "add")
            startActivity(intent)
        }

        // setting that on tapping view task the task list display page will open
        binding.viewtask.setOnClickListener {
            startActivity(Intent(this, Task_List_Display::class.java))
        }


        //defining a array of images for the krishna.xml

        //getting the random image from array


        // Magic dialog

        //setting that  on tapping the tap to see magic krishna xml will open
        binding.magic.setOnClickListener {

            //the krishna xml will have a random image from the array

            //setting the dialog
            val dialog = Dialog(this)

            //inflating the krishna xml

            //setting the dialog background as our defined background
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))

            //giving the shader to the message in krishna xml
        }
    }
        private fun showQuoteDialog(dialog: Dialog, quote: String) {
            val dialogBinding = DialogMagicBinding.inflate(layoutInflater)
            dialog.setContentView(dialogBinding.root)
            dialogBinding.message.text = quote
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
            dialog.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
        }


    fun notificati(){

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            val channel= NotificationChannel(channelid,channelname, NotificationManager.IMPORTANCE_HIGH).apply {
                description="This is my notification"
            }
            val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

        }
    }
    }



