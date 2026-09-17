package com.sleeplessdog.banquerito.domain.model

import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class Country(
    val code: String,
    val nameRes: StringResource,
    val flag: DrawableResource,
    val currency: Currency,
    val asResidence: CountryOfResidence? = null,
    val asCitizenship: Citizenship? = null,
    val asTaxResidency: TaxResidency? = null,
)

object Countries {

    val SPAIN = Country(
        code = "ES", nameRes = Res.string.country_es, flag = Res.drawable.flag_es,
        currency = Currency.EUR,
        asResidence = CountryOfResidence.SPAIN, asTaxResidency = TaxResidency.SPAIN,
    )
    val SERBIA = Country(
        code = "RS", nameRes = Res.string.country_rs, flag = Res.drawable.flag_rs,
        currency = Currency.RSD,
        asResidence = CountryOfResidence.SERBIA, asTaxResidency = TaxResidency.SERBIA,
    )
    val ARMENIA = Country(
        code = "AM", nameRes = Res.string.country_am, flag = Res.drawable.flag_am,
        currency = Currency.AMD,
        asResidence = CountryOfResidence.ARMENIA, asTaxResidency = TaxResidency.ARMENIA,
    )
    val RUSSIA = Country(
        code = "RU",
        nameRes = Res.string.country_ru,
        flag = Res.drawable.flag_ru,
        currency = Currency.RUB,
        asCitizenship = Citizenship.RUSSIA,
        asTaxResidency = TaxResidency.RUSSIA,
    )
    val UKRAINE = Country(
        code = "UA", nameRes = Res.string.country_ua, flag = Res.drawable.flag_ua,
        currency = Currency.UAH, asCitizenship = Citizenship.UKRAINE,
    )
    val BELARUS = Country(
        code = "BY", nameRes = Res.string.country_by, flag = Res.drawable.flag_by,
        currency = Currency.BYN, asCitizenship = Citizenship.BELARUS,
    )

    val GERMANY     = Country("DE", Res.string.country_de, Res.drawable.flag_de, Currency.EUR)
    val FRANCE      = Country("FR", Res.string.country_fr, Res.drawable.flag_fr, Currency.EUR)
    val ITALY       = Country("IT", Res.string.country_it, Res.drawable.flag_it, Currency.EUR)
    val UK          = Country("GB", Res.string.country_gb, Res.drawable.flag_uk, Currency.GBP)
    val POLAND      = Country("PL", Res.string.country_pl, Res.drawable.flag_pl, Currency.PLN) // ← исправлено
    val CZECHIA     = Country("CZ", Res.string.country_cz, Res.drawable.flag_cz, Currency.CZK) // ← исправлено
    val SLOVAKIA    = Country("SK", Res.string.country_sk, Res.drawable.flag_sk, Currency.EUR)
    val HUNGARY     = Country("HU", Res.string.country_hu, Res.drawable.flag_hu, Currency.HUF) // ← исправлено
    val ROMANIA     = Country("RO", Res.string.country_ro, Res.drawable.flag_ro, Currency.RON) // ← исправлено
    val BULGARIA    = Country("BG", Res.string.country_bg, Res.drawable.flag_bg, Currency.BGN)
    val CROATIA     = Country("HR", Res.string.country_hr, Res.drawable.flag_hr, Currency.EUR) // с 2023
    val SLOVENIA    = Country("SI", Res.string.country_si, Res.drawable.flag_si, Currency.EUR)
    val BOSNIA      = Country("BA", Res.string.country_ba, Res.drawable.flag_ba, Currency.BAM) // ← исправлено
    val MONTENEGRO  = Country("ME", Res.string.country_me, Res.drawable.flag_me, Currency.EUR) // использует EUR
    val N_MACEDONIA = Country("MK", Res.string.country_mk, Res.drawable.flag_mk, Currency.MKD) // ← исправлено
    val ALBANIA     = Country("AL", Res.string.country_al, Res.drawable.flag_al, Currency.ALL) // ← исправлено
    val GREECE      = Country("GR", Res.string.country_gr, Res.drawable.flag_gr, Currency.EUR)
    val TURKEY      = Country("TR", Res.string.country_tr, Res.drawable.flag_tr, Currency.TRY) // ← исправлено
    val PORTUGAL    = Country("PT", Res.string.country_pt, Res.drawable.flag_pt, Currency.EUR)
    val NETHERLANDS = Country("NL", Res.string.country_nl, Res.drawable.flag_nl, Currency.EUR)
    val BELGIUM     = Country("BE", Res.string.country_be, Res.drawable.flag_be, Currency.EUR)
    val LUXEMBOURG  = Country("LU", Res.string.country_lu, Res.drawable.flag_lu, Currency.EUR)
    val SWITZERLAND = Country("CH", Res.string.country_ch, Res.drawable.flag_ch, Currency.CHF) // ← исправлено
    val AUSTRIA     = Country("AT", Res.string.country_at, Res.drawable.flag_at, Currency.EUR)
    val DENMARK     = Country("DK", Res.string.country_dk, Res.drawable.flag_dk, Currency.DKK) // ← исправлено
    val SWEDEN      = Country("SE", Res.string.country_se, Res.drawable.flag_se, Currency.SEK) // ← исправлено
    val NORWAY      = Country("NO", Res.string.country_no, Res.drawable.flag_no, Currency.NOK) // ← исправлено
    val FINLAND     = Country("FI", Res.string.country_fi, Res.drawable.flag_fi, Currency.EUR)
    val ICELAND     = Country("IS", Res.string.country_is, Res.drawable.flag_is, Currency.ISK) // ← исправлено
    val IRELAND     = Country("IE", Res.string.country_ie, Res.drawable.flag_ie, Currency.EUR)
    val LITHUANIA   = Country("LT", Res.string.country_lt, Res.drawable.flag_lt, Currency.EUR)
    val LATVIA      = Country("LV", Res.string.country_lv, Res.drawable.flag_lv, Currency.EUR)
    val ESTONIA     = Country("EE", Res.string.country_ee, Res.drawable.flag_ee, Currency.EUR)
    val MOLDOVA     = Country("MD", Res.string.country_md, Res.drawable.flag_md, Currency.MDL)
    val GEORGIA     = Country("GE", Res.string.country_ge, Res.drawable.flag_ge, Currency.GEL)
    val AZERBAIJAN  = Country("AZ", Res.string.country_az, Res.drawable.flag_az, Currency.AZN)
    val CYPRUS      = Country("CY", Res.string.country_cy, Res.drawable.flag_cy, Currency.EUR)
    val MALTA       = Country("MT", Res.string.country_mt, Res.drawable.flag_mt, Currency.EUR)

    // ── Центральная Азия ──────────────────────────────────────────────────────────
    val KAZAKHSTAN   = Country("KZ", Res.string.country_kz, Res.drawable.flag_kz, Currency.KZT) // ← исправлено
    val UZBEKISTAN   = Country("UZ", Res.string.country_uz, Res.drawable.flag_uz, Currency.UZS) // ← исправлено
    val TURKMENISTAN = Country("TM", Res.string.country_tm, Res.drawable.flag_tm, Currency.TMT) // ← исправлено
    val KYRGYZSTAN   = Country("KG", Res.string.country_kg, Res.drawable.flag_kg, Currency.KGS) // ← исправлено
    val TAJIKISTAN   = Country("TJ", Res.string.country_tj, Res.drawable.flag_tj, Currency.TJS) // ← исправлено

    // ── Америка ───────────────────────────────────────────────────────────────────
    val USA       = Country("US", Res.string.country_us, Res.drawable.flag_us, Currency.USD)
    val CANADA    = Country("CA", Res.string.country_ca, Res.drawable.flag_ca, Currency.CAD) // ← исправлено
    val MEXICO    = Country("MX", Res.string.country_mx, Res.drawable.flag_mx, Currency.MXN) // ← исправлено
    val BRAZIL    = Country("BR", Res.string.country_br, Res.drawable.flag_br, Currency.BRL)
    val ARGENTINA = Country("AR", Res.string.country_ar, Res.drawable.flag_ar, Currency.ARS) // ← исправлено
    val CHILE     = Country("CL", Res.string.country_cl, Res.drawable.flag_cl, Currency.CLP) // ← исправлено
    val COLOMBIA  = Country("CO", Res.string.country_co, Res.drawable.flag_co, Currency.COP) // ← исправлено
    val PERU      = Country("PE", Res.string.country_pe, Res.drawable.flag_pe, Currency.PEN) // ← исправлено
    val VENEZUELA = Country("VE", Res.string.country_ve, Res.drawable.flag_ve, Currency.VES) // ← исправлено
    val ECUADOR   = Country("EC", Res.string.country_ec, Res.drawable.flag_ec, Currency.USD)

    // ── Азия ──────────────────────────────────────────────────────────────────────
    val CHINA       = Country("CN", Res.string.country_cn, Res.drawable.flag_cn, Currency.CNY)
    val JAPAN       = Country("JP", Res.string.country_jp, Res.drawable.flag_jp, Currency.JPY) // ← исправлено
    val SOUTH_KOREA = Country("KR", Res.string.country_kr, Res.drawable.flag_kr, Currency.KRW)
    val INDIA       = Country("IN", Res.string.country_in, Res.drawable.flag_in, Currency.INR) // ← исправлено
    val THAILAND    = Country("TH", Res.string.country_th, Res.drawable.flag_th, Currency.THB)
    val VIETNAM     = Country("VN", Res.string.country_vn, Res.drawable.flag_vn, Currency.VND) // ← исправлено
    val INDONESIA   = Country("ID", Res.string.country_id, Res.drawable.flag_id, Currency.IDR) // ← исправлено
    val PHILIPPINES = Country("PH", Res.string.country_ph, Res.drawable.flag_ph, Currency.PHP) // ← исправлено
    val MALAYSIA    = Country("MY", Res.string.country_my, Res.drawable.flag_my, Currency.MYR) // ← исправлено
    val SINGAPORE   = Country("SG", Res.string.country_sg, Res.drawable.flag_sg, Currency.SGD) // ← исправлено
    val MONGOLIA    = Country("MN", Res.string.country_mn, Res.drawable.flag_mn, Currency.MNT) // ← исправлено
    val NEPAL       = Country("NP", Res.string.country_np, Res.drawable.flag_np, Currency.NPR) // ← исправлено
    val SRI_LANKA   = Country("LK", Res.string.country_lk, Res.drawable.flag_lk, Currency.LKR) // ← исправлено

    // ── Океания ───────────────────────────────────────────────────────────────────
    val AUSTRALIA   = Country("AU", Res.string.country_au, Res.drawable.flag_au, Currency.AUD)
    val NEW_ZEALAND = Country("NZ", Res.string.country_nz, Res.drawable.flag_nz, Currency.NZD)

    // ── Прочее ────────────────────────────────────────────────────────────────

    val OTHER = Country(
        code = "XX", nameRes = Res.string.country_other, flag = Res.drawable.flag_un, // флаг ООН как заглушка
        currency = Currency.EUR,
        asResidence = CountryOfResidence.OTHER,
        asCitizenship = Citizenship.OTHER,
        asTaxResidency = TaxResidency.OTHER,
    )

    // ── Все страны — отсортированы по алфавиту, OTHER в конце ────────────────

    val all: List<Country> = listOf(
        ALBANIA, ARGENTINA, ARMENIA, AUSTRALIA, AUSTRIA, AZERBAIJAN,
        BELARUS, BELGIUM, BOSNIA, BRAZIL,
        BULGARIA, CANADA, CHILE, CHINA, COLOMBIA, CROATIA, CYPRUS, CZECHIA,
        DENMARK, ECUADOR, ESTONIA, FINLAND, FRANCE, GEORGIA, GERMANY,
        GREECE, HUNGARY, ICELAND, INDIA, INDONESIA, IRELAND, ITALY,
        JAPAN, KAZAKHSTAN, KYRGYZSTAN, LATVIA, LITHUANIA, LUXEMBOURG,
        MALAYSIA, MALTA, MEXICO, MOLDOVA, MONGOLIA, MONTENEGRO,
        N_MACEDONIA, NEPAL, NETHERLANDS, NEW_ZEALAND, NORWAY,
        PERU, PHILIPPINES, POLAND, PORTUGAL, ROMANIA, RUSSIA,
        SERBIA, SINGAPORE, SLOVAKIA, SLOVENIA, SOUTH_KOREA, SPAIN,
        SRI_LANKA, SWEDEN, SWITZERLAND, TAJIKISTAN, THAILAND, TURKEY,
        TURKMENISTAN, UK, UKRAINE, USA, UZBEKISTAN, VENEZUELA, VIETNAM,
        OTHER,
    )

    val currencyRepresentative: Map<Currency, Country> = mapOf(
        Currency.EUR to OTHER,
        Currency.USD to USA,
        Currency.GBP to UK,
        Currency.RUB to RUSSIA,
        Currency.RSD to SERBIA,
        Currency.AMD to ARMENIA,
        Currency.UAH to UKRAINE,
        Currency.BYN to BELARUS,
        Currency.AUD to AUSTRALIA,
        Currency.AZN to AZERBAIJAN,
        Currency.BGN to BULGARIA,
        Currency.BRL to BRAZIL,
        Currency.THB to THAILAND,
        Currency.KRW to SOUTH_KOREA,
        Currency.PLN to POLAND,
        Currency.CZK to CZECHIA,
        Currency.HUF to HUNGARY,
        Currency.RON to ROMANIA,
        Currency.TRY to TURKEY,
        Currency.CHF to SWITZERLAND,
        Currency.DKK to DENMARK,
        Currency.SEK to SWEDEN,
        Currency.NOK to NORWAY,
        Currency.ISK to ICELAND,
        Currency.MDL to MOLDOVA,
        Currency.GEL to GEORGIA,
        Currency.MKD to N_MACEDONIA,
        Currency.ALL to ALBANIA,
        Currency.BAM to BOSNIA,
        Currency.KZT to KAZAKHSTAN,
        Currency.UZS to UZBEKISTAN,
        Currency.TMT to TURKMENISTAN,
        Currency.KGS to KYRGYZSTAN,
        Currency.TJS to TAJIKISTAN,
        Currency.CAD to CANADA,
        Currency.MXN to MEXICO,
        Currency.ARS to ARGENTINA,
        Currency.CLP to CHILE,
        Currency.COP to COLOMBIA,
        Currency.PEN to PERU,
        Currency.VES to VENEZUELA,
        Currency.CNY to CHINA,
        Currency.JPY to JAPAN,
        Currency.INR to INDIA,
        Currency.VND to VIETNAM,
        Currency.IDR to INDONESIA,
        Currency.PHP to PHILIPPINES,
        Currency.MYR to MALAYSIA,
        Currency.SGD to SINGAPORE,
        Currency.MNT to MONGOLIA,
        Currency.NPR to NEPAL,
        Currency.LKR to SRI_LANKA,
        Currency.NZD to NEW_ZEALAND,
    )

    // ── Фильтры ───────────────────────────────────────────────────────────────

    val forResidence: List<Country>  = all
    val forCitizenship: List<Country> = all
    val forTaxResidency: List<Country> = all


    fun byResidence(value: CountryOfResidence): Country =
        all.first { it.asResidence == value }

    fun byCitizenship(value: Citizenship): Country =
        all.first { it.asCitizenship == value }

    fun byTaxResidency(value: TaxResidency): Country =
        all.first { it.asTaxResidency == value }

    val byCurrency: List<Country> = Currency.entries
        .mapNotNull { currencyRepresentative[it] }
}