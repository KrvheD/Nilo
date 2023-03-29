package com.example.nilo.order

import com.example.nilo.entities.Order

interface OnOrderListener {
    fun onTrack(order: Order)
    fun onStartChat(order: Order)
}