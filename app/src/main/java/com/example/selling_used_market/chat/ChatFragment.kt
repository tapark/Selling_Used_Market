package com.example.selling_used_market.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selling_used_market.DBKey.Companion.DB_CHAT
import com.example.selling_used_market.DBKey.Companion.DB_USERS
import com.example.selling_used_market.R
import com.example.selling_used_market.chat_detail.ChatroomActivity
import com.example.selling_used_market.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatFragment: Fragment(R.layout.fragment_chat) {

    private lateinit var binding: FragmentChatBinding
    private lateinit var auth: FirebaseAuth
    private val chatRoomList = mutableListOf<ChatListItem>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatDB: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatBinding.bind(view)
        auth = Firebase.auth

        chatRoomList.clear()

        chatAdapter = ChatAdapter {
            //채팅방으로 이동
            context?.let { context ->
                val intent = Intent(context, ChatroomActivity::class.java)
                intent.putExtra("chatKey", it.key)
                intent.putExtra("title", it.itemTitle)
                startActivity(intent)
            }
        }

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatRecyclerView.adapter = chatAdapter

        if (auth.currentUser == null) {
            return
        }
        chatDB = Firebase.database.reference.child(DB_USERS).child(auth.currentUser!!.uid ).child(DB_CHAT)

        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return
                    chatRoomList.add(model)
                }
                chatAdapter.submitList(chatRoomList)
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        chatAdapter.notifyDataSetChanged()
    }
}