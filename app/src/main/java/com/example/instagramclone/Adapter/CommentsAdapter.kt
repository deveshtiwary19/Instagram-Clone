package com.example.instagramclone.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Comment
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val mContext:Context,
                        private val mComment:MutableList<Comment>?)
                            :RecyclerView.Adapter<CommentsAdapter.ViewHolder>()



{
    private var firebaseUser:FirebaseUser?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view= LayoutInflater.from(mContext).inflate(R.layout.comments_litems_ayout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {

        firebaseUser=FirebaseAuth.getInstance().currentUser
        val comment=mComment!![position]

        holder.commentTV.text=comment.getComment()
        getUserInfo(holder.imageProfile,holder.userNameTV,comment.getPublisher())

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    inner class ViewHolder(@NonNull itemView:View):RecyclerView.ViewHolder(itemView)
    {
        var imageProfile:CircleImageView
        var userNameTV:TextView
        var commentTV:TextView

        init {
            imageProfile=itemView.findViewById(R.id.user_profile_image_comments)
            userNameTV=itemView.findViewById(R.id.user_name_comments)
            commentTV=itemView.findViewById(R.id.comment_comments)
        }
    }

    private fun getUserInfo(imageProfile: CircleImageView, userNameTV: TextView, publisher: String) {

        val userRef=FirebaseDatabase.getInstance().reference.child("Users").child(publisher)

        userRef.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user=snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageProfile)
                    userNameTV.text=user!!.getUserName()
                }



            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }


}