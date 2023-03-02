package com.annton.mrbincardinfo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.edit
import com.annton.mrbincardinfo.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var localResults: Boolean = false
    private var items = mutableListOf<String?>(null)
    private var url = ""
    private var phone = ""
    private var countryCoordinates = ""
    private var requestNumber = 0
    private lateinit var localCardInfo: CardInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        requestNumber = sharedPreferences.getInt("requestNumber", 0)


        updateListView()

        setUpOnClickForTextViews()

        binding.cardViewCardResult.visibility = View.GONE

        binding.progressBarCheck.visibility = View.GONE

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            binding.editTextBin.setText(item)
//            getSavedData(item)
            localResults = true
            getBinInfo(item.toString())
        }


        binding.buttonClearList.setOnClickListener {
            sharedPreferences.edit {
                clear()
                apply()
            }
            items.clear()
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
            binding.listView.adapter = adapter
            requestNumber = 0

        }

        binding.buttonGetInfo.setOnClickListener {
            val bin = binding.editTextBin.text.toString().trim()
            localResults = false
            getBinInfo(bin)


        }
    }

    private fun getBinInfo(bin: String) {

        val client = BinListApiClient()

        if (localResults) {
            localCardInfo = getSavedCardInfo(bin)
        }

        if (bin.length <= 5) {
            Toast.makeText(this, "Please enter at least 6 symbols", Toast.LENGTH_SHORT).show()

        } else {

            hideResults()


            client.getCardInfo(bin, object : Callback<CardInfo> {
                override fun onResponse(call: Call<CardInfo>, response: Response<CardInfo>) {

                    if (response.isSuccessful) {

                        requestNumber++
                        saveCardNumber(bin, response.body())
                        if (!localResults) {
                            updateListView()
                        }


                        var cardInfo: CardInfo? = null
                        if (localResults) {
                            cardInfo = localCardInfo
                        } else {
                            cardInfo = response.body()
                        }

//                        if(localResults){ getSavedCardInfo(bin)}
//                        else{ cardInfo = response.body()}


                        if (cardInfo != null) {
                            showResults()



                            if (cardInfo.bank.name != null) {

                                setUpUrls(cardInfo)

                                setBankTextViews(cardInfo)


                            } else {
                                binding.textViewBankName.text = "no bank info"
                                binding.textViewBankPhone.text = ""
                                binding.textViewBankUrl.text = ""
                            }

                            setCardInfoTextViews(cardInfo)


                        } else {
                            hideResultsAndProgress()

                            Toast.makeText(
                                this@MainActivity, "Card info not found", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        hideResultsAndProgress()

                        Toast.makeText(

                            this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_LONG
                        ).show()
                    }


                }

                override fun onFailure(call: Call<CardInfo>, t: Throwable) {
                    if (localResults) {
                        setBankTextViews(localCardInfo)
                        setCardInfoTextViews(localCardInfo)
                        binding.progressBarCheck.visibility = View.GONE

                    } else {
                        binding.cardViewCardResult.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG)
                            .show()
                        hideResultsAndProgress()
                    }

                }
            })

        }


    }

    private fun setCardInfoTextViews(cardInfo: CardInfo) {
        binding.textViewCardType.text = "Type: ${cardInfo.type}"
        binding.textViewCardBrand.text = "Brand: ${cardInfo.brand}"
        binding.textViewCountry.text = "Country:${cardInfo.country.emoji}${cardInfo.country.name}"
        binding.textViewPaymentSystem.text = "Scheme: ${cardInfo.scheme}"


    }

    private fun setBankTextViews(cardInfo: CardInfo) {
        binding.textViewBankName.text = "Bank: ${cardInfo.bank.name} City: ${cardInfo.bank.city}"
        binding.textViewBankPhone.text = "tel.:\uD83D\uDCDE ${cardInfo.bank.phone} "
        binding.textViewBankUrl.text = "url.:\uD83C\uDF0E ${cardInfo.bank.url} "


    }

    private fun hideResultsAndProgress() {
        hideResults()
        binding.progressBarCheck.visibility = View.GONE
    }

    private fun hideResults() {
        if (localResults) binding.cardViewCardResult.visibility = View.VISIBLE
        else binding.cardViewCardResult.visibility = View.GONE
        binding.progressBarCheck.visibility = View.VISIBLE


    }

    private fun showResults() {
        binding.progressBarCheck.visibility = View.GONE
        binding.cardViewCardResult.visibility = View.VISIBLE

    }

    private fun setUpUrls(cardInfo: CardInfo) {
        url = "https://${cardInfo.bank.url}"
        phone = "tel:${cardInfo.bank.phone}"
        countryCoordinates = "geo:${cardInfo.country.latitude},${cardInfo.country.longitude}"
    }

    private fun setUpOnClickForTextViews() {
        binding.textViewBankUrl.setOnClickListener {
            Toast.makeText(this@MainActivity, "url clicked", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

        }

        binding.textViewBankPhone.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("$phone")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        binding.textViewCountry.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(countryCoordinates)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }

        }


    }


    private fun updateListView() {
        if (items != null) {
            items.clear()


            for (i in 1..requestNumber) {
                var item = sharedPreferences.getString("binRequest#$i", null)

                items.add(item)
            }


            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

            binding.listView.adapter = adapter
        } else {
        }


    }


    private fun saveCardNumber(bin: String, body: CardInfo?) {

        sharedPreferences.edit {
            putInt("requestNumber", requestNumber)
            putString("binRequest#$requestNumber", bin)

            putString("card#$bin bankName", body?.bank?.name)
            putString("card#$bin bankPhone", body?.bank?.phone)
            putString("card#$bin bankUrl", body?.bank?.url)
            putString("card#$bin bankCity", body?.bank?.city)

            putString("card#$bin cardType", body?.type)
            putString("card#$bin Brand", body?.brand)

            putString("card#$bin country", body?.country?.name)
            putString("card#$bin country emoji", body?.country?.emoji)
            putString("card#$bin country latitude", body?.country?.latitude)
            putString("card#$bin country longitude", body?.country?.longitude)
            putString("card#$bin scheme", body?.scheme)


        }
    }

    private fun getSavedCardInfo(bin: String?): CardInfo {

        val countrySaved = CardCountryInfo(
            sharedPreferences.getString("card#$bin country", "-").toString(),
            sharedPreferences.getString("card#$bin country emoji", "").toString(),
            sharedPreferences.getString("card#$bin country latitude", "0").toString(),
            sharedPreferences.getString("card#$bin country longitude", "0").toString()
        )

        val bankSaved = CardBankInfo(
            sharedPreferences.getString("card#$bin bankName", "no bank info").toString(),
            sharedPreferences.getString("card#$bin bankUrl", "").toString(),
            sharedPreferences.getString("card#$bin bankPhone", "").toString(),
            sharedPreferences.getString("card#$bin bankCity", "").toString()
        )


        return CardInfo(
            bin.toString(),
            sharedPreferences.getString("card#$bin scheme", "").toString(),
            sharedPreferences.getString("card#$bin cardType", "").toString(),
            sharedPreferences.getString("card#$bin Brand", "").toString(),
            countrySaved,
            bankSaved
        )
    }


}


