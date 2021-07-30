package com.example.selling_used_market.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.selling_used_market.R
import com.example.selling_used_market.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MyPageFragment: Fragment(R.layout.fragment_mypage) {

    private lateinit var binding: FragmentMypageBinding
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMypageBinding.bind(view)
        auth = Firebase.auth

        binding.signUpButton.setOnClickListener {
            binding?.let { binding ->
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        } else {
                            if (password.length < 8 || 16 < password.length) {
                                Toast.makeText(context, "비밀번호는 8자 이상 16자 이하여야 합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "이미 존재하는 이메일 계정입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        binding.signInOutButton.setOnClickListener {
            binding?.let {

                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                if (auth.currentUser == null) { /*로그인이 안되어있는 상태 -> 로그인*/
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                successSignIn()
                            } else {
                                Toast.makeText(context, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else { /*로그인이 되어있는 상태 -> 로그아웃*/
                    auth.signOut()
                    binding.emailEditText.text.clear()
                    binding.passwordEditText.text.clear()
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true
                    binding.signInOutButton.isEnabled = false
                    binding.signUpButton. isEnabled = false
                    binding.signInOutButton.text = "로그인"
                }
            }
        }

        binding.emailEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty()
                        && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }

        binding.passwordEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty()
                        && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            binding?.let { binding ->
                binding.emailEditText.text.clear()
                binding.passwordEditText.text.clear()
                binding.emailEditText.isEnabled = true
                binding.passwordEditText.isEnabled = true
                binding.signInOutButton.isEnabled = false
                binding.signUpButton. isEnabled = false
                binding.signInOutButton.text = "로그인"
            }
        } else {
            binding?.let { binding ->
                binding.emailEditText.setText(auth.currentUser!!.email)
                binding.passwordEditText.setText("로그인 되어있는 상태입니다.")
                binding.emailEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false
                binding.signInOutButton.isEnabled = true
                binding.signUpButton. isEnabled = false
                binding.signInOutButton.text = "로그아웃"
            }
        }
    }

    private fun successSignIn() {
        if (auth.currentUser == null) {
            Toast.makeText(context, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isEnabled = false
        binding.signUpButton.isEnabled = false
        binding.signInOutButton.text = "로그아웃"
    }
}