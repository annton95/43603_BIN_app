package com.annton.mrbincardinfo

data class CardInfo(
    val bin: String,
    val scheme: String,
    val type: String,
    val brand: String,
    val country: CardCountryInfo,
    val bank: CardBankInfo
)

data class CardCountryInfo(
    val name: String,
    val emoji: String,
    val latitude: String,
    val longitude: String
)

data class CardBankInfo(
    val name: String,
    val url: String,
    val phone: String,
    val city: String
)