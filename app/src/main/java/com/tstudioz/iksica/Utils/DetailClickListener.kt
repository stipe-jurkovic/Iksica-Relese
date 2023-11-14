package com.tstudioz.iksica.Utils

import com.tstudioz.iksica.Data.Models.Transaction

/**
 * Created by etino7 on 1/9/2020.
 */
interface DetailClickListener {
    fun onClicked(transaction: Transaction)
}