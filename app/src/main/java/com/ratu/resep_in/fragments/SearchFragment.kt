package com.ratu.resep_in.fragments

import android.os.Bundle
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

        val view =
            inflater.inflate(R.layout.fragment_search, container, false)

        listView = view.findViewById(R.id.listView)
        search = view.findViewById(R.id.edtSearch)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resepList
        )

        listView.adapter = adapter

        return view
    }
}
