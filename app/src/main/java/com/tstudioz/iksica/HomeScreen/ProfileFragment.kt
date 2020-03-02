package com.tstudioz.iksica.HomeScreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.tstudioz.iksica.Adapter.AdapterInfo
import com.tstudioz.iksica.Data.Models.PaperUser
import com.tstudioz.iksica.Data.Models.UserInfoItem
import com.tstudioz.iksica.R
import com.tstudioz.iksica.SignInScreen.SignInActivity
import kotlinx.android.synthetic.main.profile_layout.*

/**
 * Created by etino7 on 18-Oct-17.
 */

class ProfileFragment : Fragment() {

    var viewModel: MainViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedBundleInstance: Bundle?): View? {
        val view = inflater.inflate(R.layout.profile_layout, parent, false)

        viewModel = ViewModelProvider(activity!!)[MainViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AdapterInfo(ArrayList())

        recyclerProfile.layoutManager = GridLayoutManager(view.context, 2, GridLayoutManager.VERTICAL, false)
        recyclerProfile.adapter = adapter


        viewModel?.getUserData()?.observe(viewLifecycleOwner, Observer {
            it?.let {
                Glide.with(view.context)
                        .load(it.avatarLink)
                        .into(avatar)
                name_surname.text = it.name
                user_desc.text = getString(R.string.student_desc).plus(" ").plus(it.university)

                adapter.updateData(convertUserDataToList(it))
            }
        })

        button_logout.setOnClickListener{
            viewModel?.logOutUser()
            startActivity(Intent(activity, SignInActivity::class.java))

        }
    }

    private fun convertUserDataToList(user: PaperUser): ArrayList<UserInfoItem> {

        val razinaPrava = UserInfoItem("Razina prava", user.rightsLevel.toString())
        val pravaOd = UserInfoItem("Prava od", user.rightsFrom)
        val pravaDo = UserInfoItem("Prava do", user.rightsTo)

        return arrayListOf(razinaPrava, pravaOd, pravaDo)
    }

    override fun onDestroy() {
        Glide.get(context!!).clearMemory()
        super.onDestroy()
    }


}
