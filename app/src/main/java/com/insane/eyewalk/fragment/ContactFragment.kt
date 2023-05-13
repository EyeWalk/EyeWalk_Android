package com.insane.eyewalk.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.insane.eyewalk.adapter.ContactViewAdapter
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.FragmentContactBinding
import com.insane.eyewalk.model.Contact
import com.insane.eyewalk.model.Phone
import com.insane.eyewalk.model.input.ContactInput
import com.insane.eyewalk.model.input.EmailInput
import com.insane.eyewalk.model.input.PhoneInput
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.UserService
import com.insane.eyewalk.utils.Tools
import kotlinx.coroutines.launch
import java.util.*

class ContactFragment : Fragment() {

    private lateinit var binding: FragmentContactBinding
    private lateinit var db: AppDataBase
    private lateinit var roomService: RoomService
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactBinding.inflate(inflater, container, false)
        db = AppDataBase.getDataBase(this.requireContext())
        roomService = RoomService(db)
        recyclerView = binding.recyclerViewContacts
        setUpViews()
        setUpClickListeners()
        return binding.root
    }

    private fun setUpClickListeners() {
        binding.llAddContact.setOnClickListener {
            addContact()
        }
        binding.btnCancelContact.setOnClickListener {
            closeContact()
        }
        binding.btnSaveContact.setOnClickListener {
            saveContact()
        }
        binding.scModalContact.setOnClickListener {
            requireActivity().hideKeyboard(binding.root)
        }
        binding.rlBackgroundModalContact.setOnClickListener {
            requireActivity().hideKeyboard(binding.root)
        }
    }

    private fun setUpViews() {
        lifecycleScope.launch {
            val contacts = UserService.getContacts(roomService.getToken().toToken())
            if (contacts.isSuccessful) {
                contacts.body()?.let {
                    refreshContactsList(it)
                }
            } else {
                errorLoadingContacts()
            }
        }
    }

    private fun errorLoadingContacts() {
        Tools.Show.message(requireContext(), "Não foi possível carregar a lista de contatos")
    }

    private fun refreshContactsList(contacts: List<Contact>) {
        recyclerView.adapter = ContactViewAdapter(contacts, this)
    }

    private fun addContact() {
        binding.clModalAddContact.visibility = View.VISIBLE
    }

    private fun saveContact() {
        loader(true)
        val contact = ContactInput(
            name = binding.etInputName.text.toString(),
            phones = listOf(PhoneInput(binding.etInputPhone.text.toString())),
            emails = listOf(EmailInput(binding.etInputEmail.text.toString())),
            emergency = binding.switchInputEmergency.isChecked
        )
        try {
            lifecycleScope.launch {
                UserService.saveContact(RoomService(db).getToken().toToken(), contact)
                closeContact()
                setUpViews()
            }
        } catch (e: Exception) {
            System.err.println(e.message)
            closeContact()
        }
    }

    private fun closeContact() {
        loader(false)
        requireActivity().hideKeyboard(binding.root)
        binding.clModalAddContact.visibility = View.GONE
    }

    fun openContactDetail(contact: Contact) {
        Tools.Show.message(requireContext(), "Abrindo contato id ${contact.id}")
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loader(display: Boolean) {
        if (display) binding.rlContactLoader.visibility = View.VISIBLE
        else binding.rlContactLoader.visibility = View.GONE
    }
}