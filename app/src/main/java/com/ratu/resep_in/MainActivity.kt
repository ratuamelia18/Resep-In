package com.ratu.resep_in

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ratu.resep_in.fragments.AccountFragment
import com.ratu.resep_in.fragments.AddRecipeFragment
import com.ratu.resep_in.fragments.FavoriteFragment
import com.ratu.resep_in.fragments.HomeFragment
import com.ratu.resep_in.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        replaceFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener {

            when(it.itemId){

                R.id.nav_home ->
                    replaceFragment(HomeFragment())

                R.id.nav_search ->
                    replaceFragment(SearchFragment())

                R.id.nav_add ->
                    replaceFragment(AddRecipeFragment())

                R.id.nav_favorite ->
                    replaceFragment(FavoriteFragment())

                R.id.nav_account ->
                    replaceFragment(AccountFragment())


            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment){

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}