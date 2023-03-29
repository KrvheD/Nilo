package com.example.nilo.product

import com.example.nilo.entities.Product

interface OnProductListener {
    fun onClick(product: Product)
}