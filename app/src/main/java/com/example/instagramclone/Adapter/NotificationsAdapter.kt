package com.example.instagramclone.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Fragments.Post_Details_Fragments
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.Model.Notifications
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments__avtivity.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationsAdapter(
    private val mContext:Context,
    private val mNotifications:List<Notifications>):RecyclerView.Adapter<NotificationsAdapter.ViewHolder>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return mNotifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val  notifications=mNotifications[position]

        if (notifications.getText().equals("started following you"))
        {
            holder.text.text="started following you"
        }
        else  if (notifications.getText().equals("liked your post"))
        {
            holder.text.text="liked your post"

        }
        else  if (notifications.getText().contains("commented:"))
        {
            holder.text.text=notifications.getText().replace("commented:","commented: ")

        }
        else
        {
            holder.text.text=notifications.getText()
        }









        userInfo(holder.profileImage,holder.userName,notifications.getUserId())

        if(notifications.isIsPost())
        {
            holder.postImage.visibility=View.VISIBLE
            getPostImage(holder.postImage,notifications.getPostid())
        }
        else
        {
            holder.postImage.visibility=View.GONE
        }

        holder.itemView.setOnClickListener{
            if(notifications.isIsPost())
            {
                val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                editor.putString("postID",notifications.getPostid())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Post_Details_Fragments()).commit()

            }
            else
            {
                val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                editor.putString("profileID",notifications.getUserId())
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,ProfileFragment()).commit()

            }
        }


    }





    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var postImage:ImageView
        var profileImage: CircleImageView
        var userName: TextView
        var text: TextView

        init {
            postImage=itemView.findViewById(R.id.notification_post_image)
            profileImage=itemView.findViewById(R.id.notification_profile_image)
            userName=itemView.findViewById(R.id.username_notifications)
            text=itemView.findViewById(R.id.comment_notifications)
        }
    }

    private fun userInfo(imageView: ImageView,userName:TextView,publisherId:String)
    {
        val userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                /* if(context!=null)
                 {
                     return
                 }*/

                if(snapshot.exists())
                {
                    val user=snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageView)


                   userName.text= user!!.getUserName()


                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostImage(imageView: ImageView,postID:String)
    {
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts").child(postID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                if(snapshot.exists())
                {
                    val post=snapshot.getValue<Post>(Post::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile).into(imageView)


                }


            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}