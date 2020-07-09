package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*
import kotlin.collections.HashMap

class SignupActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this,SigninActivity::class.java))
            finish()
        }

        //The account creationn functioning
        signup_btn.setOnClickListener {
            createAccount()
        }


    }

    private fun createAccount() {
        val fullName= fullname_signup.text.toString()
        val userName= username_signup.text.toString()
        val email= email_signup.text.toString()
        val password= password_signup.text.toString()

        when {

            TextUtils.isEmpty(fullName) -> Toast.makeText(this,"Enter your name",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this,"Please create a username",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Enter your Email",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Please Create a Password",Toast.LENGTH_SHORT).show()


           else ->
           {
               val progressDialog= ProgressDialog(this@SignupActivity)
               progressDialog.setTitle("Sign Up")
               progressDialog.setMessage("Please Wait While Creating Account")
               progressDialog.setCanceledOnTouchOutside(false)
               progressDialog.show()


               val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
               mAuth.createUserWithEmailAndPassword(email,password)
                   .addOnCompleteListener{task ->
                       if(task.isSuccessful){
                           saveUserInfo(fullName,userName,email,progressDialog)
                       }
                       else
                       {
                           val msg=task.exception!!.toString()
                           Toast.makeText(this,"Error: $msg",Toast.LENGTH_SHORT).show()
                           mAuth.signOut()
                           progressDialog.dismiss()
                       }
                   }



           }
        }



    }

    private fun saveUserInfo(fullName: String, userName: String, email: String,progressDialog: ProgressDialog) {



            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            val usersRef: DatabaseReference =
                FirebaseDatabase.getInstance().reference.child("Users")

            val usermap = HashMap<String, Any>()
            usermap["uid"] = currentUserId
            usermap["fullname"] = fullName.toLowerCase()
            usermap["username"] = userName.toLowerCase()
            usermap["email"] = email
            usermap["bio"] = "Hey!! I am using CodeFreaks Instagram Clone,you Must Try!!"
            usermap["image"] = "gs://instagram-clone-e001b.appspot.com/Default Images/profile.png"

            usersRef.child(currentUserId).setValue(usermap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Account Created Succesfully!!", Toast.LENGTH_SHORT)
                            .show()


                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(currentUserId)
                                .child("Following").child(currentUserId)
                                .setValue(true)



                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = task.exception!!.toString()
                        Toast.makeText(this, "Error: $msg", Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    }
                }





    }


}