package ind.shop.task_app

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.shop.task_app.databinding.ActivitySplashBinding
import ind.shop.task_app.databinding.DeletedEditedSavedBinding
import ind.shop.task_app.databinding.UsernameDialogBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val ui = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }



        val logo=binding.applogocontainer
        val logoanim= AnimationUtils.loadAnimation(this,R.anim.fade_in)
        logo.startAnimation(logoanim)


        val appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val firstOpen = appPrefs.getBoolean("firstopen", true)

        if(firstOpen){
            val dialog= Dialog(this)
            val dialogbinding= UsernameDialogBinding.inflate(layoutInflater)
            dialog.setContentView(dialogbinding.root)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
            dialog.window?.setLayout(900,1000)
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    dialog.show()
                },2500
            )
            dialogbinding.confirmButton.setOnClickListener {
                val username=dialogbinding.pinEdditext.text.toString()
                if(username.isEmpty()){
                    val dialog1= Dialog(this)
                    val dialogbinding2= DeletedEditedSavedBinding.inflate(layoutInflater)
                    dialog1.setContentView(dialogbinding2.root)
                    dialogbinding2.animation.setAnimation("Failed.json")
                    dialogbinding2.animation.playAnimation()
                    dialogbinding2.message.text="Username Cannot Be Empty"
                    dialog1.show()
                    dialog1.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog1.dismiss()
                    },3000
                    )

                }
                else{
                    val prefs1=getSharedPreferences("name",MODE_PRIVATE)
                    val username=prefs1.edit().putString("username",username).apply()
                    val appprefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    appprefs.edit().putBoolean("firstopen", false).apply()
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            dialog.dismiss()
                        },2000
                    )
                    dialog.setOnDismissListener {
                        val intent=Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }


                }
            }
        }
        else
        {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    val intent=Intent(this, MainActivity::class.java)
                    startActivity(intent)
                },2000
            )
        }
    }





}