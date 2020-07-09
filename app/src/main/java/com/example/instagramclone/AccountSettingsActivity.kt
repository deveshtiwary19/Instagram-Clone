package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagramclone.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser

    private var checker=""

    private var myUrl=""

    private var imageUri: Uri?=null

    private var storageProfileRef: StorageReference?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!

        storageProfileRef=FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()


            val intent= Intent(this@AccountSettingsActivity,SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()


            Toast.makeText(this,"Logged out", Toast.LENGTH_SHORT).show()
        }
            userInfo()

        close_profile_btn.setOnClickListener{
            finish()
        }

            change_image_text_btn.setOnClickListener {

                checker="clicked"

                CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(this@AccountSettingsActivity)
            }


        save_info_profile_button.setOnClickListener {
            if(checker=="clicked")
            {
                updloadImageandInfo()
            }
            else
            {
                updateUserInfoOnly()
            }

        }



    }

    private fun updloadImageandInfo() {



        when
        {

            imageUri==null ->{
                Toast.makeText(this,"Please select Image", Toast.LENGTH_SHORT).show()
            }


            full_name_profie_frag_settings.text.toString()=="" -> {
                Toast.makeText(this,"Field Fullname can not be empty", Toast.LENGTH_SHORT).show()

            }
            username_profile_frag.text.toString()=="" -> {
                Toast.makeText(this,"Field Username can not be empty", Toast.LENGTH_SHORT).show()

            }
            bio_profile_frag2.text.toString()=="" -> {
                Toast.makeText(this,"Field Bio can not be empty", Toast.LENGTH_SHORT).show()

            }

            else->{

                val progressDialog=ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait while updating the credentials")
                progressDialog.show()


                val fileRef=storageProfileRef!!.child(firebaseUser!!.uid+".jpg")

                val uploadTask:StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful){

                        val downloadUrl=task.result
                        myUrl=downloadUrl.toString()


                        val ref=FirebaseDatabase.getInstance().reference.child("Users")

                        val usermap = HashMap<String, Any>()

                        usermap["fullname"] = full_name_profie_frag_settings.text.toString().toLowerCase()
                        usermap["username"] = username_profile_frag.text.toString().toLowerCase()

                        usermap["bio"] = bio_profile_frag2.text.toString().toLowerCase()
                        usermap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(usermap)

                        Toast.makeText(this, "Account Updated Succesfully!!", Toast.LENGTH_SHORT)
                            .show()
                        val intent=Intent(this@AccountSettingsActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()


                        progressDialog.dismiss()

                    }
                    else
                    {
                        progressDialog.dismiss()
                    }


                })



            }



        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==Activity.RESULT_OK && data!=null)
        {

            val result= CropImage.getActivityResult(data)
            imageUri=result.uri

            profile_image_view_profile_frag.setImageURI(imageUri)





        }
        else
        {
            Toast.makeText(this,"Not able to get image!! Try again",Toast.LENGTH_SHORT).show()
        }

    }










    private fun updateUserInfoOnly() {

        when {
            full_name_profie_frag_settings.text.toString()=="" -> {
                Toast.makeText(this,"Field Fullname can not be empty", Toast.LENGTH_SHORT).show()

            }
            username_profile_frag.text.toString()=="" -> {
                Toast.makeText(this,"Field Username can not be empty", Toast.LENGTH_SHORT).show()

            }
            bio_profile_frag2.text.toString()=="" -> {
                Toast.makeText(this,"Field Bio can not be empty", Toast.LENGTH_SHORT).show()

            }
            else -> {

                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val usermap = HashMap<String, Any>()

                usermap["fullname"] = full_name_profie_frag_settings.text.toString().toLowerCase()
                usermap["username"] = username_profile_frag.text.toString().toLowerCase()

                usermap["bio"] = bio_profile_frag2.text.toString().toLowerCase()

                userRef.child(firebaseUser.uid).updateChildren(usermap).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        Toast.makeText(this, "Account Updated Succesfully!!", Toast.LENGTH_SHORT)
                            .show()
                        val intent=Intent(this@AccountSettingsActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        val msg=task.exception

                        Toast.makeText(this, "Error $msg", Toast.LENGTH_SHORT).show()
                    }

                }



            }
        }

    }

    private fun userInfo()
    {
        val userRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_profile_frag)


                   username_profile_frag.setText( user!!.getUserName())

                    full_name_profie_frag_settings.setText(user!!.getFullName())

                    bio_profile_frag2.setText(user!!.getBio())



                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}