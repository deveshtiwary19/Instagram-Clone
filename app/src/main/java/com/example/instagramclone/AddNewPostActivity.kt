package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_new_post.*

class AddNewPostActivity : AppCompatActivity() {
    private var myUrl=""

    private var imageUri: Uri?=null

    private var storagePostRef: StorageReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_post)

        storagePostRef= FirebaseStorage.getInstance().reference.child("Posts Pictures")

        close_add_post_btn.setOnClickListener {
            finish()
        }


        save_new_post_button.setOnClickListener {
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(4,3)
            .start(this@AddNewPostActivity)


    }

    private fun uploadImage() {
        when
        {
            imageUri==null ->{
                Toast.makeText(this,"Please select Image", Toast.LENGTH_SHORT).show()
            }


            description_post.text.toString()=="" -> {
                Toast.makeText(this,"Field can not be Empty", Toast.LENGTH_SHORT).show()

            }

            else->
            {
                val progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Uploading new post")
                progressDialog.setMessage("Please wait while your post is uploading")
                progressDialog.show()


                val fileRef=storagePostRef!!.child(System.currentTimeMillis().toString()+".jpg")

                val uploadTask: StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                })
                    .addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful){

                        val downloadUrl=task.result
                        myUrl=downloadUrl.toString()


                        val ref= FirebaseDatabase.getInstance().reference.child("Posts")

                        val usermap = HashMap<String, Any>()
                        val postId=ref.push().key

                        usermap["postid"] = postId!!
                        usermap["description"] = description_post.text.toString()

                        usermap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        usermap["postimage"] = myUrl

                        ref.child(postId).updateChildren(usermap)

                        Toast.makeText(this, "Post Uploading Succesful!!", Toast.LENGTH_SHORT)
                            .show()

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
        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK && data!=null)
        {

            val result= CropImage.getActivityResult(data)
            imageUri=result.uri

           image_post.setImageURI(imageUri)





        }
        else
        {
            Toast.makeText(this,"Image not selected",Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}