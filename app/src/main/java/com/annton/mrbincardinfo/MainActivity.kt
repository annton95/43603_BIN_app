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
    private var items = mutableListOf<String?>(null)
    private var url=""
    private var phone=""
    private var countryCoordinates =""




    private var requestNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        requestNumber = sharedPreferences.getInt("requestNumber", 0)


        updateListView()

        setUpOnClickForTextViews()


        binding.progressBarCheck.visibility = View.GONE

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            binding.editTextBin.setText(item)
            getSavedData(item)
            getBinInfo()
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


            getBinInfo()



        }
    }

    private fun getBinInfo() {

        val client = BinListApiClient()


        val bin = binding.editTextBin.text.toString().trim()


        if (bin.length < 6) {
            Toast.makeText(this, "Please enter at least 6 symbols", Toast.LENGTH_SHORT).show()
//            return@setOnClickListener
        }

        binding.progressBarCheck.visibility = View.VISIBLE




        client.getCardInfo(bin, object : Callback<CardInfo> {
            override fun onResponse(call: Call<CardInfo>, response: Response<CardInfo>) {
                if (response.isSuccessful) {

                    requestNumber++
                    saveCardNumber(bin, response.body())

                    updateListView()


                    val cardInfo = response.body()

                    if (cardInfo != null) {



                        if (cardInfo.bank.name != null) {
                            setUpUrls(cardInfo)

                            binding.textViewBankName.text =
                                "Bank: ${cardInfo.bank.name} City: ${cardInfo.bank.city}"
                            binding.textViewBankPhone.text = "tel.:\uD83D\uDCDE ${cardInfo.bank.phone} "
                            binding.textViewBankUrl.text = "url.:\uD83C\uDF0E ${cardInfo.bank.url} "

                        } else {
                            binding.textViewBankName.text = "no bank info"
                            binding.textViewBankPhone.text = ""
                            binding.textViewBankUrl.text = ""
                        }


                        binding.textViewCardType.text = "Type: ${cardInfo.type}"
                        binding.textViewCardBrand.text = "Brand: ${cardInfo.brand}"
                        binding.textViewCountry.text =
                            "Country:${cardInfo.country.emoji}${cardInfo.country.name}"
                        binding.textViewPaymentSystem.text = "Scheme: ${cardInfo.scheme}"


                    } else {
                        Toast.makeText(
                            this@MainActivity, "Card info not found", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT
                    ).show()
                }

                binding.progressBarCheck.visibility = View.GONE
            }

            override fun onFailure(call: Call<CardInfo>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                binding.progressBarCheck.visibility = View.GONE
            }
        })



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
            if (intent.resolveActivity(packageManager) != null) { startActivity(intent) }
        }

        binding.textViewCountry.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(countryCoordinates)
            }
            if (intent.resolveActivity(packageManager) != null) { startActivity(intent) }

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

            putString("card#$bin cardType", body?.type)
            putString("card#$bin Brand", body?.brand)
            putString("card#$bin country", body?.country?.name)
            putString("card#$bin country emoji", body?.country?.emoji)
            putString("card#$bin scheme", body?.scheme)

        }
    }


    private fun getSavedData(item: String?) {
        binding.textViewBankName.text = sharedPreferences.getString("card#$item bankName", null)
        binding.textViewBankPhone.text = sharedPreferences.getString("card#$item bankPhone", null)
        binding.textViewBankUrl.text = sharedPreferences.getString("card#$item bankUrl", null)


        binding.textViewCardType.text = sharedPreferences.getString("card#$item cardType", null)
        binding.textViewCardBrand.text = sharedPreferences.getString("card#$item Brand", null)
        binding.textViewCountry.text = sharedPreferences.getString("card#$item country", null)
        binding.textViewPaymentSystem.text = sharedPreferences.getString("card#$item scheme", null)

    }
}


