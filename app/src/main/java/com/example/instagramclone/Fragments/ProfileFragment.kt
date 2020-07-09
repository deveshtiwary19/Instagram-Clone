package com.example.instagramclone.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.AccountSettingsActivity
import com.example.instagramclone.Adapter.MyImagesAdapter
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.example.instagramclone.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var profileID: String
    private lateinit var firebaseUser: FirebaseUser

    var postList:List<Post>?=null
    var myImagesAdapter:MyImagesAdapter?=null


    var postListSaved:List<Post>?=null
    var myImagesAdapterSavedImg:MyImagesAdapter?=null
    var mySavedImg:List<String>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser=FirebaseAuth.getInstance().currentUser!!
        val pref= context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)

        if(pref!=null){
            this.profileID= pref.getString("profileID","none").toString()
        }

        if(profileID==firebaseUser.uid)
        {
            view.edit_account_settings_button.text="Edit Profile"
        }
        else if(profileID!=firebaseUser.uid)
        {
            checkFollowandFollowing()
            view.edit_account_settings_button.text="Following"
        }

            //Adding recycler view for uploaded posts
        var recyclerViewUploadedImages:RecyclerView
        recyclerViewUploadedImages=view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadedImages.setHasFixedSize(true)
        val linearLayoutManager:LinearLayoutManager=GridLayoutManager(context,3)
        recyclerViewUploadedImages.layoutManager=linearLayoutManager

        postList=ArrayList()
        myImagesAdapter=context?.let { MyImagesAdapter( it,postList as ArrayList<Post>) }
        recyclerViewUploadedImages.adapter=myImagesAdapter

        //Adding recycler view for saved posts
        var recyclerViewSavedImages:RecyclerView
        recyclerViewSavedImages=view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2:LinearLayoutManager=GridLayoutManager(context,3)
        recyclerViewSavedImages.layoutManager=linearLayoutManager2

        postListSaved=ArrayList()
        myImagesAdapterSavedImg=context?.let { MyImagesAdapter( it,postListSaved as ArrayList<Post>) }
        recyclerViewSavedImages.adapter=myImagesAdapterSavedImg

        //Default
        recyclerViewSavedImages.visibility=View.GONE
        recyclerViewUploadedImages.visibility=View.VISIBLE


        //To view savedimages button function
        val uploadedImagesBtn: ImageButton
        uploadedImagesBtn=view.findViewById(R.id.images_grid_view_btn)
        uploadedImagesBtn.setOnClickListener{
            recyclerViewSavedImages.visibility=View.GONE
           recyclerViewUploadedImages.visibility=View.VISIBLE
        }


        //To view uploadedimages button function
        val savedImagesBtn: ImageButton
        savedImagesBtn=view.findViewById(R.id.images_save_btn)
        savedImagesBtn.setOnClickListener{
            recyclerViewSavedImages.visibility=View.VISIBLE
            recyclerViewUploadedImages.visibility=View.GONE
        }

          view.total_followers.setOnClickListener {
              val intent=Intent(context,ShowUsersActivity::class.java)
              intent.putExtra("id",profileID)
              intent.putExtra("title","followers")
              startActivity(intent)
          }

        view.total_following.setOnClickListener {
            val intent=Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileID)
            intent.putExtra("title","following")
            startActivity(intent)
        }


        view.edit_account_settings_button.setOnClickListener {

            val getBUttonText=view.edit_account_settings_button.text.toString()

            when
            {
                getBUttonText==="Edit Profile"->startActivity(Intent(context,AccountSettingsActivity::class.java))

                getBUttonText=="Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileID).setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }

                    addNotification()
                }

                getBUttonText=="Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileID).removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileID)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }

        }

        getFollowers() //Method to get followers count
        getFollowings()//Method to get the following count
        myPhotos()//Method to get all the posts of user
        userInfo()//Method to retriev user information
        getTotalNumberOfPosts()//Method to get total number of posts
        mySaves()//Method to retrieve all the saved posts

        return view
    }

    private fun mySaves() {

        mySavedImg=ArrayList()
        val savesRef=FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        savesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(pO in snapshot.children)
                    {
                        ( mySavedImg as ArrayList<String>).add(pO.key!!)
                    }

                    readSavedImagesData()//Following is thr function to get the details of the saved posts
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun readSavedImagesData() {

        val PostsRef=FirebaseDatabase.getInstance().reference.child("Posts")
        PostsRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists())
                {
                    (postListSaved as ArrayList<Post>).clear()

                    for(snapshot in datasnapshot.children)
                    {
                        val post=snapshot.getValue(Post::class.java)

                        for(key in mySavedImg!!)
                        {
                            if (post!!.getPostid()==key)
                            {
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun checkFollowandFollowing() {

        val followingRef=firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        if(followingRef!=null)
        {
            followingRef.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileID).exists())
                    {
                        view?.edit_account_settings_button?.text="Following"

                    }
                    else
                    {
                        view?.edit_account_settings_button?.text="Follow"
                    }

                }

                override fun onCancelled(error: DatabaseError) {


                }
            })
        }



    }

    //getting number of followers
    private fun getFollowers()
    {
        val followersgRef= FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileID)
                .child("Followers")


        followersgRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_followers?.text= snapshot.childrenCount.toString()
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

//Getting the number of following
    private fun getFollowings()
    {
        val followingRef= FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileID)
                .child("Following")


        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    view?.total_following?.text= snapshot.childrenCount.toString()
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    private fun myPhotos()
    {
        val postsRef=FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    (postList as ArrayList<Post>).clear()

                    for(snap in snapshot.children)
                    {
                        val post=snap.getValue(Post::class.java)
                        if(post!!.getPublisher().equals(profileID))
                            {
                                (postList as ArrayList<Post>).add(post)
                            }

                        Collections.reverse(postList)
                    }
                    myImagesAdapter!!.notifyDataSetChanged()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }




    private fun userInfo()
    {
        val userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(profileID)

        userRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               /* if(context!=null)
                {
                    return
                }*/

                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)


                    view?.profile_fragment_username?.text= user!!.getUserName()

                    view?.full_name_profie_frag?.text= user!!.getFullName()

                    view?.bio_profile_frag?.text= user!!.getBio()
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    override fun onStop() {
        super.onStop()

        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()

    }

    override fun onDestroy() {
        super.onDestroy()

        val pref=context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileID",firebaseUser.uid)
        pref?.apply()

    }

    private fun getTotalNumberOfPosts()
    {
        val postsRef=FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                if(datasnapshot.exists())
                {
                    var postCounter=0
                    for(snapshot in datasnapshot.children)
                    {
                        val post=snapshot.getValue(Post::class.java)!!
                        if(post.getPublisher()==profileID)
                        {
                            postCounter++
                        }
                    }
                    total_posts.text= " "+postCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun addNotification()
    {
        val notiRef=FirebaseDatabase.getInstance().reference.child("Notifications").child(profileID)

        val notiMap= HashMap<String,Any>()
        notiMap["userid"]=firebaseUser!!.uid
        notiMap["text"]="started following you"
        notiMap["postid"]=""
        notiMap["ispost"]=false

        notiRef.push().setValue(notiMap)

    }



















    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}