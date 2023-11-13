package com.tstudioz.iksica.Utils

import android.app.Dialog
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tstudioz.iksica.Adapter.TransactionDetailsAdapter
import com.tstudioz.iksica.HomeScreen.MainViewModel
import com.tstudioz.iksica.R
import org.koin.android.viewmodel.ext.android.sharedViewModel


/**
 * Created by etino7 on 1/11/2020.
 */
class BottomSheetTransactionDetails : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by sharedViewModel()

    companion object {
        fun newInstance(): BottomSheetTransactionDetails {
            return BottomSheetTransactionDetails()
        }
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView: View = View.inflate(context, R.layout.transaction_details_layout, null)
        dialog.setContentView(contentView)

        val location = contentView.findViewById(R.id.details_location) as TextView
        val time = contentView.findViewById(R.id.details_time) as TextView
        val total = contentView.findViewById(R.id.total) as TextView
        val totalPaid = contentView.findViewById(R.id.totalPaid) as TextView
        val recycler = contentView.findViewById(R.id.details_recycler) as RecyclerView
        val progress = contentView.findViewById(R.id.progressBar) as ProgressBar

        progress.visibility = View.VISIBLE

        recycler.layoutManager = LinearLayoutManager(contentView.context)
        val adapter = TransactionDetailsAdapter(ArrayList())
        recycler.adapter = adapter

        viewModel.getCurrentTransaction().observe(this, Observer { transaction ->
            transaction?.let {
                location.text = it.restourant
                time.text = "${it.date}, ${it.time}"
                total.text = "${"%.2f".format(it.subvention.toFloat())} eur"
                totalPaid.text = "${"%.2f".format(it.amount.toFloat()-it.subvention.toFloat())} eur"
            }
        })

        viewModel.getCurrentTransactionItems().observe(this, Observer { transactionDetails ->
            transactionDetails?.let {
                progress.visibility = View.INVISIBLE
                //total.text = "${it.subventionTotal} eur"
                adapter.updateItems(transactionDetails.items)
            }
        })
    }
}