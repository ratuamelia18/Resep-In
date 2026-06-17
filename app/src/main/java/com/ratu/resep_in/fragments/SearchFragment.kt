package com.ratu.resep_in.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.ratu.resep_in.R

class SearchFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var search: EditText
    private lateinit var adapter: ArrayAdapter<String>

    private val resepList = arrayListOf(
        "Nasi Goreng",
        "Ayam Taliwang",
        "Rendang"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        listView = view.findViewById(R.id.listView)
        search = view.findViewById(R.id.edtSearch)


        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resepList
        )
        listView.adapter = adapter


        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }
}