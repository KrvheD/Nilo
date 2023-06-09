package com.example.nilo.product

import com.example.nilo.entities.Product

interface MainAux {

    fun getProductsCart(): MutableList<Product>

    fun updateTotal()

    fun clearCart()

    fun getProductSelected(): Product?

    fun showButton(isVisible: Boolean)

    fun addProductToCart(product: Product)



}