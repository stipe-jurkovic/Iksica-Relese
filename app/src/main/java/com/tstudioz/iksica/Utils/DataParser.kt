package com.tstudioz.iksica.Utils

import com.tstudioz.iksica.Data.Models.PaperUser
import com.tstudioz.iksica.Data.Models.Transaction
import com.tstudioz.iksica.Data.Models.TransactionDetails
import com.tstudioz.iksica.Data.Models.TransactionItem
import com.tstudioz.iksica.Utils.Exceptions.WrongCredsException
import okhttp3.Response
import org.jsoup.Jsoup
import org.w3c.dom.Element
import timber.log.Timber
import java.io.IOException

/**
 * Created by etino7 on 07/10/2019.
 */
class DataParser {

    private var afterToken: String? = null
    private var loginToken: String? = null
    private var token: String? = null
    private var authToken: String? = null

    @Throws(IOException::class)
    fun parseSAMLToken(response: Response): String? {
        val doc = Jsoup.parse(response.body?.string())
        val el = doc.getElementById("SAMLRequest")
        authToken = el.`val`()
        Timber.d("Parsan token iz coor %s", authToken)
        return authToken
    }

    @Throws(IOException::class)
    fun parseAuthToken(response: Response): String? {

        try {
            // Parse the HTML content of the response using Jsoup
            val document = Jsoup.parse(response.body?.string())

            // Select the first hidden input element with a "value" attribute under a div with class "error"
            var token0 = document.select(".login-form input[type=hidden]")

            var token01 = token0.first()
            var token02 = token01.attr("value")
            token = token02

            // Log the obtained authentication token using Timber
            Timber.d("Auth token $token ")

            // Return the extracted token (which may be null)
            return token
        } catch (e: Exception) {
            // Handle exceptions, log or return a default value
            Timber.e(e, "Error parsing authentication token")
            return null
        }
        return token
    }


    @Throws(IOException::class)
    fun parseLoginToken(response: Response): String? {
        val document = Jsoup.parse(response.body?.string())
        val el = document.select("body > main > div > div > div.content > form > input[type=hidden]:nth-child(2)")
                .first()

        loginToken = el?.attr("value")
        Timber.d("Login token $loginToken ")

        return loginToken ?: throw WrongCredsException()
    }

    @Throws(IOException::class, NullPointerException::class)
    fun parseResponseToken(response: Response): String {
        val document = Jsoup.parse(response.body?.string())

        val el = document.select("body > main > div > div > div.content > form > input[type=hidden]:nth-child(2)")
                .first()

        afterToken = el.attr("value")
        Timber.d("After token $afterToken ")

        return afterToken ?: ""
    }

    @Throws(IOException::class, NullPointerException::class)
    fun parseUserInfo(response: Response): PaperUser {

        val document = Jsoup.parse(response.body?.string())

        val slikaLink: String? = document.select(".slikastud").attr("src")
        Timber.d("slikaLink  $slikaLink")

        val saldo: String? = document.select(".col-lg-3:eq(1) .toEuro").text()
        Timber.d("saldo $saldo")

        val number: String? = document.select("td:contains(Izdana)")?.first()
            ?.parent()?.select("td")?.first()?.text()
        Timber.d("number $number")

        val user: String? = document.select(".card-title")?.first()?.text()
        Timber.d("user $user")

        val uciliste: String? = document.select(".col-7 > p >span.font-weight-bold")?.first()
            ?.nextSibling().toString()
        Timber.d("uciliste $uciliste")

        val razinaPrava: String? = document.select(".col-lg-3:eq(0) .h5")?.text()
        Timber.d("rprava $razinaPrava")

        val pravaOd: String? = document.select("span.font-weight-bold:contains(Prava od datuma:)")?.first()
            ?.nextSibling().toString()
        Timber.d("pod $pravaOd")
        val pravaDo: String? = document.select("span.font-weight-bold:contains(Prava do datuma:)")?.first()
            ?.nextSibling().toString()
        Timber.d("pdo $pravaDo")

        val spent: String? = document.select(".col-lg-3:eq(2) .toEuro").text()
        Timber.d("spent $spent")

        val oib: Long? = document.select("span.font-weight-bold:contains(Oib:)")
            ?.first()?.nextSibling()?.toString()?.trim()?.toLong()
        Timber.d("OIB $oib")

        val jmbag: Long? = document.select("span.font-weight-bold:contains(JMBAG:)")?.first()
            ?.nextSibling()?.toString()?.trim()?.toLong()

        return PaperUser(1,
                         "",
                         "",
                         user ?: "",
                         number ?: "",
                         saldo ?: "",
                         spent ?: "",
                         razinaPrava?.toInt() ?: 0,
                         pravaOd ?: "",
                         pravaDo ?: "",
                         uciliste ?: "",
                         slikaLink ?: "",
                         oib ?: 0,
                         jmbag ?: 0)
    }

    fun parseUserTransactions(response: Response): ArrayList<Transaction> {
        val document = Jsoup.parse(response.body?.string())
        val transactions = ArrayList<Transaction>()

        val table = document?.select("tbody")?.first()

        val rows = table?.select("tr")

        rows?.let {

            for (row in rows) {
                var restourant = ""
                var date = ""
                var time = ""
                var amount = ""
                var subvention = ""
                var authorization = ""
                var linkOfReceipt = ""

                val items = row.select("td")

                for ((index, value) in items.withIndex()) {

                    when (index) {
                        0 -> restourant = value?.text() ?: ""
                        1 -> date = value?.text() ?: ""
                        2 -> time = value?.text() ?: ""
                        3 -> amount = value?.text() ?: ""
                        4 -> subvention = value?.text() ?: ""
                        5 -> authorization = value?.text() ?: ""
                        6 -> linkOfReceipt = value?.select("a")?.attr("href")
                                ?: ""

                        else -> {
                            Timber.d("None of the criteria met")
                        }
                    }

                }
                transactions.add(
                        Transaction(restourant, date, time, amount, subvention, authorization, linkOfReceipt)
                )
            }
        }

        return transactions
    }

    fun parseTransactionDetails(response: Response): TransactionDetails {
        var transactionDetails = TransactionDetails("", "", ArrayList())
        val document = Jsoup.parse(response.body?.string())

        val mainDiv = document.getElementById("mainDivContent")

        mainDiv?.let { div -> document?.let {

                val tableBody = it.select("tbody")?.first()
                val rows = tableBody?.select("tr")

                rows?.let { rows ->
                    for ((index, row) in rows.withIndex()) {

                        /*if (index == rows.size - 1) {
                            transactionDetails.total = row.select(" th:nth-child(1)")?.first()?.text()?: ""
                            transactionDetails.subventionTotal = row.select(" th:nth-child(2)")?.first()?.text()?: ""
                            continue
                        }*/

                        var itemName = ""
                        var itemQuantity: Int = -1
                        var itemPrice = ""
                        var itemsTotal = ""
                        var itemSubvention = ""

                        val cols = row?.select("td")

                        cols?.let {
                            for ((index, data) in cols.withIndex()) {
                                when (index) {
                                    0 -> itemName = data?.text() ?: ""
                                    1 -> itemQuantity = data?.text()?.toInt()?: -1
                                    2 -> itemPrice = data?.text() ?: ""
                                    3 -> itemsTotal = data?.text() ?: ""
                                    4 -> itemSubvention = data?.text() ?: ""

                                    else -> {
                                        Timber.d("Dunno where to put table data detail")
                                    }
                                }
                            }
                        }
                        if (index == 0 || transactionDetails.items?.last()?.name != itemName)
                            transactionDetails.items?.add(TransactionItem(itemName, itemQuantity, itemPrice, itemsTotal, itemSubvention))
                        else
                            transactionDetails.items?.set((transactionDetails.items?.size?.minus(1) ?: -1), TransactionItem(itemName, itemQuantity + 1, itemPrice, itemsTotal, itemSubvention))
                    }

                }

            }

        }
        return transactionDetails
    }

    fun clearAllTokens() {
        afterToken = null
        loginToken = null
        token = null
        authToken = null
    }
}