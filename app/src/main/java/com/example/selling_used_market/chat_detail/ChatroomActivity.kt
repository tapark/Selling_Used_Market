package com.example.selling_used_market.chat_detail

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selling_used_market.databinding.ActivityRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatroomActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRoomBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    private lateinit var chatDB: DatabaseReference

    private val chatList = mutableListOf<ChatModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Log.d("teddy_chatRoom", "액티비티 생성?")
        auth = Firebase.auth
        chatRoomAdapter = ChatRoomAdapter()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd

        binding.chatRecyclerView.layoutManager = layoutManager
        binding.chatRecyclerView.adapter = chatRoomAdapter

        keyboardResizeRecyclerView()


        val chatKey = intent.getLongExtra("chatKey", -1)
        val title = intent.getStringExtra("title")
        binding.titleTextView.text = title

        chatDB = Firebase.database.reference.child("message").child("$chatKey")

        chatDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatModel = snapshot.getValue(ChatModel::class.java)
                chatModel ?: return
                chatList.add(chatModel)
                chatRoomAdapter.submitList(chatList)
                chatRoomAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.adapter!!.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })

        binding.sendButton.setOnClickListener {
            val chatModel = ChatModel(
                senderId = auth.currentUser?.uid ?: "",
                message = binding.messageEditText.text.toString()
            )
            chatDB.push().setValue(chatModel)
            binding.messageEditText.text.clear()
        }

        binding.exitButton.setOnClickListener {
            finish()
        }

    }

    private fun keyboardResizeRecyclerView() {

        binding.chatRoomLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val screenHeight = binding.chatRoomLayout.rootView.height
            val changedScreenHeight = binding.chatRoomLayout.height
            val diff = screenHeight - changedScreenHeight
                if (diff > dpToPx(this, 200f)) {
                    binding.chatRecyclerView.smoothScrollToPosition(binding.chatRecyclerView.adapter!!.itemCount - 1)
                }
            }
        }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    private fun View.setHeight(value: Int) {
        val lp = layoutParams
        lp?.let {
            lp.height = value
            layoutParams = lp
        }
    }

}