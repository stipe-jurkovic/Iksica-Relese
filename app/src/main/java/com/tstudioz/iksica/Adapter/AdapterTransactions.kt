package com.tstudioz.iksica.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tstudioz.iksica.Data.Models.Transaction
import com.tstudioz.iksica.R
import com.tstudioz.iksica.Utils.DetailClickListener

/**
 * Created by etino7 on 11-Oct-17.
 */
class AdapterTransactions(
    private var transactions: List<Transaction>?,
    private val detailClickListener: DetailClickListener
) : RecyclerView.Adapter<AdapterTransactions.DetailViewHolder>() {
    inner class DetailViewHolder(
        view: View,
        private val detailClickListener: DetailClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val time: TextView
        val date: TextView
        val place: TextView
        val amount: TextView

        init {
            time = itemView.findViewById<View>(R.id.transaction_time) as TextView
            place = itemView.findViewById<View>(R.id.transaction_place) as TextView
            amount = itemView.findViewById<View>(R.id.transaction_amount) as TextView
            date = view.findViewById<View>(R.id.transaction_date) as TextView
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            detailClickListener.onClicked(transactions!![adapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.transaction_item_layout, parent, false)
        return DetailViewHolder(view, detailClickListener)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val (restourant, date, time, _, subvention) = transactions!![position]
        holder.time.text = time
        holder.date.text = date
        holder.amount.text = "-${"%.2f".format(subvention.toFloat())}  eur"
        holder.place.text = restourant
    }

    override fun getItemCount(): Int {
        return if (transactions == null) 0 else transactions!!.size
    }

    fun updateItems(transactions: List<Transaction>?) {
        this.transactions = transactions
        notifyDataSetChanged()
    }
}