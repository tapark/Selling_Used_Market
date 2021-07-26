package com.example.selling_used_market.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selling_used_market.DBKey.Companion.DB_ARTICLES
import com.example.selling_used_market.R
import com.example.selling_used_market.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var articleDB: DatabaseReference

    private val articleList = mutableListOf<ArticleModel>()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Fragment_home", "onViewCreated 가 실행되었습니다.")

        //val fragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)

        // initialize
        binding = fragmentHomeBinding
        articleAdapter = ArticleAdapter()
        auth = Firebase.auth
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleList.clear()

        binding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.articleRecyclerView.adapter = articleAdapter

        binding.addFloatingButton.setOnClickListener {

            if (auth.currentUser == null) {
                val intent = Intent(requireActivity(), AddArticleActivity::class.java)
                //requireActivity() or requireContext() or activity or getActivity() 셋다 가능한가?
                startActivity(intent)
            } else {
                //Toast.makeText(requireActivity(), "회원가입이 필요한 기능입니다.", Toast.LENGTH_SHORT).show()
                Snackbar.make(view, "로그인 후 상품을 등록할 수 있습니다.", Snackbar.LENGTH_SHORT).show()
            }


        }

        articleDB.addChildEventListener(listener)
    }

    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        articleDB.removeEventListener(listener)
    }
}