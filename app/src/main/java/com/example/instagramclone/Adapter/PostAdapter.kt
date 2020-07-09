package com.example.instagramclone.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Comments_Avtivity
import com.example.instagramclone.Fragments.Post_Details_Fragments
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.MainActivity
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
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments__avtivity.*
import java.util.*

class PostAdapter(private val mContext:Context,
                  private val mPost:List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser?=null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        firebaseUser=FirebaseAuth.getInstance().currentUser

        val post=mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if(post.getDescription().equals(""))
        {
            holder.description.visibility=View.GONE
        }
        else
        {
            holder.description.visibility=View.VISIBLE

            holder.description.setText(post.getDescription())
        }

        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.getPublisher())

        isLiked(post.getPostid(),holder.likeButton) //Method to check online user has liked or not
        numberOfLikes(holder.likes,post.getPostid())//Method to get the number of likes
        numberOfComments(holder.comments,post.getPostid())//Method tto get number of comments
        checkSavedStatus(post.getPostid(),holder.saveButton)//Method to check the save/unsave status of post

        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag=="Like")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.getPublisher(),post.getPostid())
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()




            }
        }

        holder.commentButton.setOnClickListener {
            val intentCom=Intent(mContext,Comments_Avtivity::class.java)
            intentCom.putExtra("postID",post.getPostid())
            intentCom.putExtra("publisherID",post.getPublisher())
            mContext.startActivity(intentCom)
        }
        holder.comments.setOnClickListener {
            val intentCom=Intent(mContext,Comments_Avtivity::class.java)
            intentCom.putExtra("postID",post.getPostid())
            intentCom.putExtra("publisherID",post.getPublisher())
            mContext.startActivity(intentCom)
        }

        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag=="Save")
            {
               FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).setValue(true)
                Toast.makeText(mContext,"Post Saved",Toast.LENGTH_SHORT).show()

            }
            else
            {
               FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).removeValue()
                Toast.makeText(mContext,"Post Unaved",Toast.LENGTH_SHORT).show()
            }

        }

        holder.likes.setOnClickListener {
            val intent=Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostid())
            intent.putExtra("title","likes")
                mContext.startActivity(intent)
        }


        holder.postImage.setOnClickListener {
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postID",post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Post_Details_Fragments()).commit()

        }

        holder.publisher.setOnClickListener {
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileID",post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                ProfileFragment()
            ).commit()

        }
        holder.profileImage.setOnClickListener{
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileID",post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,ProfileFragment()).commit()


        }
        holder.postImage.setOnClickListener {
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postID",post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Post_Details_Fragments()).commit()

        }








    }

    private fun numberOfLikes(likes: TextView, postid: String) {

        val likesRef= FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)

        likesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                   likes.text=snapshot.childrenCount.toString()+" likes"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }

    private fun numberOfComments(comments: TextView, postid: String) {

        val likesRef= FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postid)

        likesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                    comments.text= "View all "+snapshot.childrenCount.toString()+" comments"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }

    private fun isLiked(postid: String, likeButton: ImageView) {

        val firebaseUser=FirebaseAuth.getInstance().currentUser
        val likesRef= FirebaseDatabase.getInstance().reference
                                     .child("Likes")
                                                .child(postid)

        likesRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag="Liked"
                }
                else
                {

                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag="Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    inner class ViewHolder(@NonNull itemView: View) :RecyclerView.ViewHolder(itemView)
    {
        //Image Views
        var profileImage:CircleImageView
        var postImage:ImageView
        var likeButton:ImageView
        var commentButton:ImageView
        var saveButton:ImageView
        //TextViews
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView


        init {
            profileImage=itemView.findViewById(R.id.user_profile_image_post)
            postImage=itemView.findViewById(R.id.post_image_home)
            likeButton=itemView.findViewById(R.id.post_image_like_btn)
            commentButton=itemView.findViewById(R.id.post_image_comment_btn)
            saveButton=itemView.findViewById(R.id.post_save_comment_btn)
            userName=itemView.findViewById(R.id.user_name_post)
            likes=itemView.findViewById(R.id.likes)
            publisher=itemView.findViewById(R.id.publisher)
            description=itemView.findViewById(R.id.description)
            comments=itemView.findViewById(R.id.comments)
        }

    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {

        val usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        usersRef.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
           if(snapshot.exists())
           {
               val user=snapshot.getValue<User>(User::class.java)

               Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)


              userName.text=user!!.getUserName()

               publisher.text=user!!.getFullName()





           }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

private fun checkSavedStatus(postid: String,imageView: ImageView)
{
    val savesRef= FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

    savesRef.addValueEventListener(object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {

            if(snapshot.child(postid).exists()){
                imageView.setImageResource(R.drawable.save_large_icon)
                imageView.tag="Saved"
            }
            else
            {
                imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                imageView.tag="Save"
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })

}

    private fun addNotification(userId:String,postId: String)
    {
        val notiRef=FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        val notiMap= HashMap<String,Any>()
        notiMap["userid"]=firebaseUser!!.uid
        notiMap["text"]="liked your post"
        notiMap["postid"]=postId
        notiMap["ispost"]=true

        notiRef.push().setValue(notiMap)

    }



}