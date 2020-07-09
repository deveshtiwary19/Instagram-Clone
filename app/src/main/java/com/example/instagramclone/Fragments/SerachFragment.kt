package com.example.instagramclone.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.UserAdapter
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_serach.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SerachFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SerachFragment : Fragment() {

    private var recyclerView:RecyclerView?=null
    private var userAdapter: UserAdapter?=null
    private var muser:MutableList<User>?=null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val view= inflater.inflate(R.layout.fragment_serach, container, false)

        recyclerView=view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager=LinearLayoutManager(context)


        muser=ArrayList()
        userAdapter= context?.let { UserAdapter(it, muser as ArrayList<User>,true) }
        recyclerView?.adapter=userAdapter


        view.search_edit_text.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


            }



            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(view.search_edit_text.text.toString()=="")
                {

                }
                else
                {
                    recyclerView?.visibility =View.VISIBLE
                    retrieveUsers() //Method to serach users among Database
                    searchUsers(s.toString().toLowerCase())


                }
            }

            override fun afterTextChanged(s: Editable?) {


            }
        })





        return view
    }

    private fun searchUsers(input: String) {
        val query=FirebaseDatabase.getInstance().getReference()
            .child("Users").orderByChild("fullname")
            .startAt(input).endAt(input+ "\uf8ff")


       query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {

                muser?.clear()

                for (snapshot in datasnapshot.children)
                {
                    val  user=snapshot.getValue(User::class.java)
                    if(user!= null)
                    {
                        muser?.add(user)
                    }
                }

                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }

    private fun retrieveUsers() {

        val userRef=FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(view?.search_edit_text?.text.toString()=="")
                {
                    muser?.clear()
                    for (snapshot in datasnapshot.children)
                    {
                        val  user=snapshot.getValue(User::class.java)
                        if(user!= null)
                        {
                            muser?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SerachFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SerachFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}