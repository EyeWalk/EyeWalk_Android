package com.insane.eyewalk.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.insane.eyewalk.adapter.ContactViewAdapter
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.FragmentContactBinding
import com.insane.eyewalk.model.Contact
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.service.UserService
import com.insane.eyewalk.utils.Tools
import kotlinx.coroutines.launch

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
        return binding.root
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

    fun openContactDetail(contact: Contact) {
        Tools.Show.message(requireContext(), "Abrindo contato id ${contact.id}")
    }

}