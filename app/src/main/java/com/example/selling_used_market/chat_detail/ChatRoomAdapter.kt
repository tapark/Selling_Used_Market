package com.example.selling_used_market.chat_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selling_used_market.databinding.ItemChatroomBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatRoomAdapter(): ListAdapter<ChatModel, ChatRoomAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemChatroomBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(chatModel: ChatModel) {
            val auth = Firebase.auth
            val set = ConstraintSet()
            val idTextView = binding.idTextView.id
            val messageTextView = binding.messageTextView.id
            val layout = binding.messageLayout
            set.clone(layout)
            if (chatModel.senderId == auth.currentUser?.uid) {
                set.setHorizontalBias(idTextView, 1F)
                set.setHorizontalBias(messageTextView, 1F)
            } else {
                set.setHorizontalBias(idTextView, 0F)
                set.setHorizontalBias(messageTextView, 0F)
            }
            set.applyTo(layout)
            binding.idTextView.text = chatModel.senderId
            binding.messageTextView.text = chatModel.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ItemChatroomBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ChatModel>() {
            override fun areItemsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatModel, newItem: ChatModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}