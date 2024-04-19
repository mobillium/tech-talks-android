package com.moter.crystalball.utl

import java.time.LocalDate
import kotlin.random.Random

object PromptHelper {

    val emotionalStates = listOf(
        "Mutlu",
        "Mutsuz",
        "Depresif",
        "Heyecanlı",
        "Sakin",
        "Huzurlu",
        "Öfkeli",
        "Korkmuş",
        "Şaşırmış",
        "Utangaç",
        "Gururlu",
        "Aşk dolu",
        "Nefret dolu",
        "Kıskanç",
        "Hayal kırıklığına uğramış",
        "Umutlu",
        "Umutsuz",
        "Endişeli",
        "Tedirgin",
        "Rahat",
        "Güvende",
        "Güvensiz",
        "Düşünceli",
        "Yaratıcı",
        "Odaklı",
        "Dağınık",
        "Enerjik",
        "Yorgun",
        "Motivasyonlu",
        "Tembel",
        "Kararsız",
        "Cesaretli",
        "Korkak",
        "Yalnız",
        "Eşlik eden",
        "Sevilen",
        "Reddedilen",
        "Arkadaş canlısı",
        "Sosyal",
        "İçe dönük",
        "Dışa dönük",
        "Lider",
        "Takipçi",
        "Ağrılı",
        "Rahat",
        "Hasta",
        "Sağlıklı",
        "Aç",
        "Tok",
        "Uykusuz",
        "Dinlenmiş",
        "Aydınlanmış",
        "Kafası karışık",
        "Spiritüel",
        "Materyalist",
        "Optimist",
        "Pesimist",
        "Minnettar",
        "Kindar",
        "Doğum günü",
        "Yıldönümü",
        "Tatil",
        "Cenaze",
        "Mezuniyet",
        "Düğün",
        "İş görüşmesi",
        "Sınav",
    )


    private fun generateIndices(text: String, list: List<String>): List<Int> {
        val indices = mutableListOf<Int>()
        val random = Random(text.hashCode() + LocalDate.now().hashCode())

        while (indices.size < 5) {
            val index = random.nextInt(list.size)
            if (index !in indices) {
                indices.add(index)
            }
        }

        return indices
    }

    fun generatePrompt(text: String): Pair<String,String> {
        val indices = generateIndices(text = text, list = emotionalStates)
        val emotionalList = mutableListOf<String>()
        indices.forEach {
            emotionalList.add(emotionalStates[it])
        }

        return Pair(emotionalList.joinToString(),getPromptTemplate(emotionalList))
    }

    private fun getPromptTemplate(emotionalStates: List<String>): String {
        return "Fal yorumu yapan bir uygulama için vereceğim duygu durumlarına göre fal yorumu üretmeni istiyorum. " +
                "Bu yorum şu başlıklar altında olmalı; Genel Yorum, Kişisel Yorum, Tavsiye ve Önerilen Eylemler. " +
                "Her duygu durumu için ayrı ayrı yorum istemiyorum. Hepsinin ortak yorumlandığı fal yorumu yeterli olur." +
                "Hadi başlayalım; ${emotionalStates.joinToString()}"
    }
}