package com.gamerdex.core.domain

import org.junit.Assert.*
import org.junit.Test

class CollectionBrowsingTest {
    private fun game(id:Long,title:String="Juego $id")=Game(id,title,"PC","Estudio",createdAt=id)
    @Test fun combinesAllFiltersAndKeepsSeparateCopies() {
        val match=game(1,"  Alpha ").copy(genres=listOf("RPG"),isFavorite=true,isFinished=true,editionType=EditionType.COLLECTOR)
        val games=listOf(match,match.copy(id=2),match.copy(id=3,isFavorite=false),match.copy(id=4,isFinished=false),
            match.copy(id=5,platform="Switch"),match.copy(id=6,genres=emptyList()),match.copy(id=7,editionType=EditionType.STANDARD))
        assertEquals(listOf(1L,2L),browseCollection(games," ALPHA ","PC","RPG",true,true,true).map{it.id})
        assertEquals(listOf(6L),browseCollection(games,genre="Sin género").map{it.id})
        assertEquals(7,browseCollection(games).size)
    }
    @Test fun sortsUnknownValuesLastAndNeverMixesCurrencies() {
        val games=listOf(game(1,"Zeta"),game(2,"Beta").copy(rating=0f,pricePaidCents=100,priceCurrency="JPY"),
            game(3,"Alpha").copy(rating=90f,pricePaidCents=900,priceCurrency="EUR"),
            game(4,"Delta").copy(rating=90f,pricePaidCents=500,priceCurrency="EUR"))
        assertEquals(listOf(3L,2L,4L,1L),browseCollection(games).map{it.id})
        assertEquals(listOf(4L,3L,2L,1L),browseCollection(games,order=CollectionOrder.NEWEST).map{it.id})
        assertEquals(listOf(3L,4L,2L,1L),browseCollection(games,order=CollectionOrder.RATING).map{it.id})
        assertEquals(listOf(4L,3L,2L,1L),browseCollection(games,order=CollectionOrder.PRICE_ASC).map{it.id})
        assertEquals(listOf(3L,4L,2L,1L),browseCollection(games,order=CollectionOrder.PRICE_DESC).map{it.id})
    }
}
