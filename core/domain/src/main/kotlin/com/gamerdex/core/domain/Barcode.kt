package com.gamerdex.core.domain

data class BarcodeMatch(val title: String, val platform: String)
interface BarcodeRepository { suspend fun lookup(code: String): BarcodeMatch? }
fun validBarcode(code: String): Boolean {
    if (code.length !in setOf(8,12,13,14) || code.any { it !in '0'..'9' }) return false
    val sum=code.dropLast(1).reversed().mapIndexed { index,c -> c.digitToInt() * if(index%2==0) 3 else 1 }.sum()
    return (10-sum%10)%10 == code.last().digitToInt()
}
