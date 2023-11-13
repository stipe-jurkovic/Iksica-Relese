package com.tstudioz.iksica.SignInScreen

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.tstudioz.iksica.HomeScreen.HomeActivity
import com.tstudioz.iksica.R
import kotlinx.android.synthetic.main.sign_in_layout.*
import org.aviran.cookiebar2.CookieBar
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class SignInActivity : AppCompatActivity() {

    private var animationDrawable: AnimationDrawable? = null
    private val viewmodel: SignInViewModel by viewModel()

    override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)
        setContentView(R.layout.sign_in_layout)
        supportActionBar?.hide()

        checkIfIsUserLogged()
        setUpAimation()
    }

    override fun onResume() {
        super.onResume()
        listenForErrors()

        animationDrawable?.start()

        sign_in_button.setOnClickListener {
            viewmodel.authenticateCredentials(parseInputFields())
            sign_in_button.visibility = View.INVISIBLE
            progressBarLoading.visibility = View.VISIBLE
        }
    }

    private fun parseInputFields(): Pair<String, String> {
        return Pair(sign_in_username.text.toString(), sign_in_password.text.toString())
    }

    private fun checkIfIsUserLogged() {
        viewmodel.isUserLoggedAlready().observe(this, Observer {
            it?.let { isUserLogged ->
                if (isUserLogged) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        animationDrawable?.stop()
    }

    private fun listenForErrors() {
        viewmodel.getErrors().observe(this, Observer {errorMessage ->
            sign_in_button.visibility = View.VISIBLE
            progressBarLoading.visibility = View.INVISIBLE

            errorMessage?.let { message ->
                CookieBar.build(this)
                        .setMessage(message)
                        .setCookiePosition(CookieBar.TOP)
                        .setBackgroundColor(R.color.darker_grey)
                        .setDuration(4000)
                        .show()
            }
        })
    }

    override fun onBackPressed() {
        Timber.d("Closing aplication")
    }

    private fun setUpAimation() {
        animationDrawable = sign_in_relative?.background as AnimationDrawable
        animationDrawable?.setEnterFadeDuration(1500)
        animationDrawable?.setExitFadeDuration(1500)
    }

}
