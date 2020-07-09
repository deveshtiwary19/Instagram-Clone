package com.example.instagramclone.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.MainActivity
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_item_layout.view.*
import java.util.HashMap


class UserAdapter(private var mContext : Context,
                  private var mUser :List<User>,
                  private var isFragment: Boolean=false) :RecyclerView.Adapter<UserAdapter.ViewHolder>()
{
    private var firebaseUser:FirebaseUser?= FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {



        val view=LayoutInflater.from(mContext).inflate(R.layout.user_item_layout,parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size

    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user=mUser[position]




        holder.userNameTextView.text=user.getUserName()
        holder.userFullNameTextView.text=user.getFullName()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)

        checkFollowingStatus(user.getUID(),holder.followButton)

        //Opening the profile from search activity
        holder.itemView.setOnClickListener(View.OnClickListener {
           if (isFragment)
           {
               val pref=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
               pref.putString("profileID",user.getUID())
               pref.apply()

               (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,ProfileFragment()).commit()
           }
            else
           {
               val intent=Intent(mContext,MainActivity::class.java)
               intent.putExtra("publisherId",user.getUID())
               mContext.startActivity(intent)
           }
        })





        holder.followButton.setOnClickListener {
            if(holder.followButton.text.toString()=="Follow") {
                //Code to add user to app users following list
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Code to add the app user in the followers list of profile of person.
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {



                                            }

                                        }
                                }
                            }
                        }
                }
                addNotification(user.getUID())
            }

                    else
                    {

                        //Code to remove user from app users following list
                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(user.getUID())
                                .removeValue().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        //Code to remove the app user from the followers list of profile of person.
                                        firebaseUser?.uid.let { it1 ->
                                            FirebaseDatabase.getInstance().reference
                                                .child("Follow").child(user.getUID())
                                                .child("Followers").child(it1.toString())
                                                .removeValue().addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {



                                                    }

                                                }
                                        }
                                    }
                                }
                        }


                    }


                }
            }




    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var userNameTextView: TextView=itemView.findViewById(R.id.user_name_search)
        var userFullNameTextView: TextView=itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView=itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button=itemView.findViewById(R.id.follow_btn_search)

    }

    private fun checkFollowingStatus(uid: String, followButton: Button) {

        val followingRef=firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object :  ValueEventListener{

            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.child(uid).exists())
                {
                    followButton.text="Following"
                }
                else
                {
                    followButton.text="Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })




    }

    private fun addNotification(userId:String)
    {
        val notiRef=FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        val notiMap= HashMap<String,Any>()
        notiMap["userid"]=firebaseUser!!.uid
        notiMap["text"]="started following you"
        notiMap["postid"]=""
        notiMap["ispost"]=false

        notiRef.push().setValue(notiMap)

    }



}
