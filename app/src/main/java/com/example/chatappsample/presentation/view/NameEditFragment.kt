package com.example.chatappsample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.presentation.view.MainActivity.Companion.CURRENT_USER
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.CharLengthInputFilter
import com.example.chatappsample.util.LetterDigitsInputFilter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameEditFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this@NameEditFragment.requireActivity())[UserViewModel::class.java] }
    private lateinit var fragmentNameEdit: View

    private val nameEditText : TextInputEditText by lazy { this@NameEditFragment.requireActivity().findViewById(R.id.et_name_editor) }
    private val continueIfNameCreatedButton : MaterialButton by lazy { this@NameEditFragment.requireActivity().findViewById(R.id.btn_continue_if_name_created) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentNameEdit = inflater.inflate(R.layout.fragment_create_new_name, container, false)

        return fragmentNameEdit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        continueIfNameCreatedButton.setOnClickListener(btnClickListener)
        nameEditText.filters = arrayOf(LetterDigitsInputFilter(), CharLengthInputFilter(10))
    }

    private val btnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            when (v.id) {
                R.id.btn_continue_if_name_created -> {
                    val name = nameEditText.text.toString()
                    viewModel.signUp(name, emailVerificationListener)
                }
                else -> {

                }
            }
        }
    }

    private val emailVerificationListener = object: OnEmailVerificationListener {
        override fun onSuccess(user: UserDomain) {

            Toast.makeText(this@NameEditFragment.requireActivity(), "가입이 완료됐습니다.", Toast.LENGTH_SHORT).show()
            this@NameEditFragment.requireActivity().finish()

            val mIntent = Intent(this@NameEditFragment.requireActivity(), MainActivity::class.java)
            mIntent.putExtra(CURRENT_USER, user.uid)
            startActivity(mIntent)
        }

        override fun onFailEmailVerification() {

        }

        override fun onFail(exception: Exception) {
            exception.printStackTrace()
        }
    }
}