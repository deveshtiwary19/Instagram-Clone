package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*

class SigninActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)




        signup_link_btn.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
            finish()
        }
        //The log in functioning
        login_btn.setOnClickListener {
            loginUser()
        }




    }

    private fun loginUser() {
        val email= email_login.text.toString()
        val password= password_login.text.toString()


        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Email can not be empty", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Password can not be empty", Toast.LENGTH_SHORT).show()

            else ->
            {
                val progressDialog= ProgressDialog(this@SigninActivity)
                progressDialog.setTitle("Logging In")
                progressDialog.setMessage("Please Wait While Logging You In")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            progressDialog.dismiss()

                            val intent=Intent(this@SigninActivity,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()

                            Toast.makeText(this,"Logged in sucessfully",Toast.LENGTH_SHORT).show()

                        }
                        else
                        {
                            val msg=task.exception!!.toString()
                            Toast.makeText(this,"Error: $msg",Toast.LENGTH_SHORT).show()
                            FirebaseAuth.getInstance().signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }




    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent=Intent(this@SigninActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

    }



}