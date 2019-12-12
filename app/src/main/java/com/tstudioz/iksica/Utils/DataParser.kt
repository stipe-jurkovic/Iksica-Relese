package com.tstudioz.iksica.Utils

import com.tstudioz.iksica.Data.Models.PaperUser
import com.tstudioz.iksica.Data.Models.Transaction
import okhttp3.Response
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException

/**
 * Created by etino7 on 07/10/2019.
 */
class DataParser {
    private var afterToken: String? = null
    private var loginToken: String? = null

    @Throws(IOException::class)
    fun parseSAMLToken(response: Response): String {
        val authToken: String
        val doc = Jsoup.parse(response.body()?.string())
        val el = doc.getElementById("SAMLRequest")
        authToken = el.`val`()
     //   Timber.d("Parsan token iz coor %s", authToken)
        return authToken
    }

    @Throws(IOException::class)
    fun parseAuthToken(response: Response): String {
        val document = Jsoup.parse(response.body()?.string())
        val token: String = document
                .select("#aai_centerbox > div.aai_form_container > div.aai_login_form > form > input[type=\"hidden\"]:nth-child(7)")
                .first().attr("value")
     //   Timber.d("Auth token ${token} ")
        return token
    }

    @Throws(IOException::class)
    fun parseLoginToken(response: Response): String? {
        val document = Jsoup.parse(response.body()?.string())
        val el = document.select("body > form > input[type=\"hidden\"]:nth-child(2)").first()
        loginToken = el.attr("value")
    //    Timber.d("Login token ${loginToken} ")
        return loginToken
    }

    @Throws(IOException::class, NullPointerException::class)
    fun parseResponseToken(response: Response): String {
        val document = Jsoup.parse(response.body()?.string())

        val el = document.select("body > form > input[type=\"hidden\"]:nth-child(2)").first()
        afterToken = el.attr("value")

    //    Timber.d("After token ${afterToken} ")

        return afterToken as String
    }

    @Throws(IOException::class, NullPointerException::class)
    fun parseUserInfo(response: Response): PaperUser {

        val document = Jsoup.parse(response.body()?.string())

        val slikaLink: String? = document.select("#mainDivContent > div > section.text-center > div > div.avatar.mx-auto.white > img").attr("src")
     //   Timber.d("slikaLink  $slikaLink")
        val saldo: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(4) > div.col.border-left.border-right > p.font-weight-bold.h3")?.first()?.text()
      //  Timber.d("saldo $saldo")
        val number: String? = document.select("#mainDivContent > div > section:nth-child(3) > div > div.px-4 > div > div > table > tbody > tr.text-success > td.text-right")?.first()?.text()
     //   Timber.d("number $number")
        val user: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > h4")?.first()?.text()
     //   Timber.d("user $user")
        val uciliste: String? = document.select("#mainDivContent > div > section:nth-child(3) > div > div.px-4 > div > div > table > tbody > tr.text-success > td:nth-child(2)")?.first()?.text()
     //   Timber.d("uciliste $uciliste")
        val razinaPrava: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(4) > div:nth-child(1) > p.font-weight-bold.text-success.h3")?.first()?.text()
     //   Timber.d("rprava $razinaPrava")
        val pravaOd: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(8) > div > span:nth-child(1)")?.first()?.nextSibling().toString()
      //  Timber.d("pod $pravaOd")
        val pravaDo: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(8) > div > span:nth-child(3)")?.first()?.nextSibling().toString()
      //  Timber.d("pdo $pravaDo")
        val spent: String? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(4) > div:nth-child(3) > p.font-weight-bold.h3")?.first()?.text()
       // Timber.d("spent $spent")
        val oib: Long? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(6) > div > span:nth-child(7)")?.first()?.nextSibling()?.toString()?.trim()?.toLong()
      //  Timber.d("OIB $oib")
        val jmbag: Long? = document.select("#mainDivContent > div > section.text-center > div > div.card-body > div:nth-child(6) > div > span:nth-child(9)")?.first()?.nextSibling()?.toString()?.trim()?.toLong()


        return PaperUser(1, "", "", user ?: "", number ?: "", saldo ?: "", spent
                ?: "", razinaPrava?.toInt() ?: 0, pravaOd
                ?: "", pravaDo ?: "", uciliste ?: "", slikaLink ?: "", oib ?: 0, jmbag ?: 0)
    }

    fun parseUserTransactions(response: Response): ArrayList<Transaction> {
        val document = Jsoup.parse(response.body()?.string())
        val transactions = ArrayList<Transaction>()

        val table = document.select("#mainDivContent > div > div:nth-child(3) > div > table > tbody").first()
        val rows = table.select("tr")

        for (row in rows) {
            var restourant: String = ""
            var date: String = ""
            var time: String = ""
            var amount: String = ""
            var subvention: String = ""
            var authorization: String = ""
            var linkOfReceipt: String = ""

            val items = row.select("td")

            for ((index, value) in items.withIndex()) {

                when (index) {
                    0 -> restourant = value?.text() ?: ""
                    1 -> date = value?.text() ?: ""
                    2 -> time = value?.text() ?: ""
                    3 -> amount = value?.text() ?: ""
                    4 -> subvention = value?.text() ?: ""
                    5 -> authorization = value?.text() ?: ""
                    6 -> linkOfReceipt = value?.select("a")?.attr("href") ?: ""

                    else -> {
                        // Timber.d("None of the criteria met")
                    }
                }

            }
            transactions.add(Transaction(restourant, date, time, amount, subvention, authorization, linkOfReceipt))
        }

        return transactions
    }
}