package com.ganeshsingha.musicplayer

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ganeshsingha.musicplayer.databinding.ActivityFeedbackBinding
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class FeedbackActivity : AppCompatActivity() {
    lateinit var binding:ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(R.style.coolPinkNav)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.title = "Feedback"

        binding.sendFA.setOnClickListener {
            val feedbackMessage = binding.feedbackMessageFA.text.toString() + "\n" + binding.emailFA.text.toString()
            val subject = binding.topicFA.text.toString()
            val username = "quickplay095@gmail.com"
            val password = "quickplay@2002"
            val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (feedbackMessage.isNotEmpty() && subject.isNotEmpty() &&(cm.activeNetworkInfo?.isConnectedOrConnecting == true)){
                Thread{
                    try {
                        val properties = Properties()
                        properties["mail.smtp.auth"] = "true"
                        properties["mail.smtp.starttls"] = "true"
                        properties["mail.smtp.host"] = "smtp.gmail.com"
                        properties["mail.smtp.port"] = "587"
                        val session = Session.getInstance(properties,object : Authenticator(){
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(username,password)
                            }
                        })
                        val mail = MimeMessage(session)
                        mail.subject = subject
                        mail.setText(feedbackMessage)
                        mail.setFrom(InternetAddress(username))
                        mail.setRecipients(Message.RecipientType.TO,InternetAddress.parse(username))
                        Transport.send(mail)
                        runOnUiThread {
                            Toast.makeText(this@FeedbackActivity,"Thanks for feedback",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }catch (e:Exception){
                        runOnUiThread {
                            Toast.makeText(this@FeedbackActivity,e.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }else{

                Toast.makeText(this@FeedbackActivity,"Went Something Wrong",Toast.LENGTH_SHORT).show()
            }
        }
    }
}