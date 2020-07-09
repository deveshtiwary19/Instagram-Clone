package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.UserAdapter
import com.example.instagramclone.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_serach.view.*

class ShowUsersActivity : AppCompatActivity() {

    var  id:String=""
    var title:String=""

    var userAdapter:UserAdapter?=null
    var userList:List<User>?=null
    var idList:List<String>?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent=intent
        id=intent.getStringExtra("id")
        title=intent.getStringExtra("title")


        val toolbar:Toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title=title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        var recyclerView:RecyclerView
        recyclerView=findViewById(R.id.recycler_view_)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager=LinearLayoutManager(this)

        userList=ArrayList()
        userAdapter= UserAdapter(this,userList as ArrayList<User>,false)

        recyclerView.adapter=userAdapter


        idList=ArrayList()


        when(title)
        {
            "likes"-> getLikes()//Method to get list of users likes the post
            "following"-> getFollowings()//Method to get the list of people one is following
            "followers"-> getFollowers()//Method to get the list of foollowers of one
            "views"-> getViews()//Method to get the list of views for your story
        }


    }

    private fun getViews() {

        val ref= FirebaseDatabase.getInstance().reference
            .child("Story").child(id!!)
            .child(intent.getStringExtra("storyid"))
            .child("views")


        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(datasnapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(datasnapshot.key!!)
                }

                showUsers()


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })





    }

    private fun getFollowers() {

        val followersgRef= FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Followers")


        followersgRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(datasnapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(datasnapshot.key!!)
                }

                showUsers()


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun getFollowings() {

        val followingRef= FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Following")


        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for(datasnapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(datasnapshot.key!!)
                }

                showUsers()


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })




    }

    private fun getLikes() {

        val likesRef= FirebaseDatabase.getInstance().reference
            .child("Likes").child(id!!)


        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                    (idList as ArrayList<String>).clear()

                    for(datasnapshot in snapshot.children)
                    {
                        (idList as ArrayList<String>).add(datasnapshot.key!!)
                    }

                    showUsers()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun showUsers() {
        val userRef=FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in datasnapshot.children)
                {
                    val  user=snapshot.getValue(User::class.java)

                    for (id in idList!!)
                    {
                        if(user!!.getUID()==id)
                        {
                            (userList as ArrayList<User>).add(user!!)
                        }
                    }

                }

                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}