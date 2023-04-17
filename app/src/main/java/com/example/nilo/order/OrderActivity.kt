package com.example.nilo.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.databinding.ActivityOrderBinding
import com.example.nilo.entities.Order
import com.example.nilo.track.TrackFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {

    private lateinit var  binding: ActivityOrderBinding

    private lateinit var adapter: OrderAdapter

    private lateinit var orderSelected: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFirestore()
    }



    private fun setupRecyclerView() {
        adapter = OrderAdapter(mutableListOf(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
            adapter = this@OrderActivity.adapter
        }

    }

    private fun setupFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection(Constants.COLL_REQUESTS)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    val order = document.toObject(Order::class.java)
                    order.id = document.id
                    adapter.add(order)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar los datos.", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onTrack(order: Order){
        orderSelected = order

        val fragment = TrackFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStartChat(order: Order){

    }

    override fun getOrderSelected(): Order = orderSelected
}