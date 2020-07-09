package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.CommentsAdapter
import com.example.instagramclone.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments__avtivity.*
import org.w3c.dom.Comment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Comments_Avtivity : AppCompatActivity() {

    private var postID=""
    private var publisherID=""
    private var firebaseUser:FirebaseUser?=null
    private var commentsAdapter:CommentsAdapter?=null
    private var commentList:MutableList<com.example.instagramclone.Model.Comment>?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments__avtivity)

        val intent=intent
        postID=intent.getStringExtra("postID")
        publisherID=intent.getStringExtra("publisherID")


        firebaseUser=FirebaseAuth.getInstance().currentUser

        //Following are the code to insert all values from database in recycler view and keep theblatest one at the top using reverse layout
        var recyclerView:RecyclerView
        recyclerView=findViewById(R.id.recycler_view_comments)
        val linearLayoutManager=LinearLayoutManager(this)
        linearLayoutManager.reverseLayout=true
       recyclerView.layoutManager=linearLayoutManager

        commentList=ArrayList()
        commentsAdapter= CommentsAdapter(this,commentList)

        recyclerView.adapter=commentsAdapter
        //Over here


        getPostImage()//Method to get the selected post
        readComments()//Method to read all the existing posts
        userInfo()//Method to fetch the current user online info

        post_comment.setOnClickListener {
            if(add_comment!!.text.toString()=="")
            {
                Toast.makeText(this,"Comment can not be empty",Toast.LENGTH_SHORT).show()
            }
            else
            {
                addComment()
            }
        }


    }

    private fun addComment() {
        val commentsRef= FirebaseDatabase.getInstance().reference.child("Comments").child(postID!!)


        val commentsMap=HashMap<String,Any>()

        commentsMap["comment"]=add_comment!!.text.toString()
        commentsMap["publisher"]=firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        addNotification()//Method to send a notification in notification activity

        add_comment!!.text.clear()

    }

    private fun userInfo()
    {
        val userRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comment)


                }


            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostImage()
    {
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts").child(postID!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if(snapshot.exists())
                {
                    val image=snapshot.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comments)


                }


            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readComments()
    {
        val commentsRef=FirebaseDatabase.getInstance().reference.child("Comments").child(postID)
        commentsRef.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    commentList!!.clear()

                    for(datasnapshot in snapshot.children)
                    {
                        val comments=datasnapshot.getValue(com.example.instagramclone.Model.Comment::class.java)
                        commentList!!.add(comments!!)
                    }

                    commentsAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification()
    {
        val notiRef=FirebaseDatabase.getInstance().reference.child("Notifications").child(publisherID!!)

        val notiMap= HashMap<String,Any>()
        notiMap["userid"]=firebaseUser!!.uid
        notiMap["text"]="commented:"+ add_comment!!.text.toString()
        notiMap["postid"]=postID
        notiMap["ispost"]=true

        notiRef.push().setValue(notiMap)

    }


}