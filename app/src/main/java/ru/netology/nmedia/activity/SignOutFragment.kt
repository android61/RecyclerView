package ru.netology.nmedia.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

@AndroidEntryPoint
class SignOutFragment : DialogFragment() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.are_you_sure)
                .setCancelable(true)
                .setPositiveButton(R.string.logout) { _, _ ->
                    appAuth.removeAuth()
                    findNavController().navigateUp()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}