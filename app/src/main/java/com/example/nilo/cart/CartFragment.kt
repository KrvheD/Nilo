package com.example.nilo.cart

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilo.R
import com.example.nilo.databinding.FragmentCartBinding
import com.example.nilo.entities.Product
import com.example.nilo.order.OrderActivity
import com.example.nilo.product.MainAux
import com.example.nilo.product.ProductAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartFragment : BottomSheetDialogFragment(), OnCartListener {

    private var binding: FragmentCartBinding? = null

    private lateinit var  bottomSheetBehavior: BottomSheetBehavior<*>

    private lateinit var adapter: ProductCartAdapter

    private var totalPrice = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = FragmentCartBinding.inflate(LayoutInflater.from(activity))
        binding?.let {
            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)


            bottomSheetBehavior = BottomSheetBehavior.from(it.root.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            setupRecyclerView()
            setupButtons()

            getProducts()

            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun setupRecyclerView() {
        binding?.let {
            adapter = ProductCartAdapter(mutableListOf(), this)

            it.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@CartFragment.adapter
            }

           /* (1..5).forEach{
                val product = Product(it.toString(), "Producto $it", "This product is $it",
                    "", it, 2.0*it)
                adapter.add(product)
            } */
        }
    }

    private fun setupButtons(){
        binding?.let {
            it.ibCancel.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            it.efab.setOnClickListener {
                requestOrder()
            }

        }
    }

        private fun getProducts(){
            (activity as? MainAux)?.getProductsCart()?.forEach{
                adapter.add(it)
            }
        }

    private fun requestOrder(){
        dismiss()
        (activity as? MainAux)?.clearCart()
        startActivity(Intent(context, OrderActivity::class.java))
    }

    override fun onDestroyView() {
        (activity as? MainAux)?.updateTotal()
        super.onDestroyView()
        binding = null
    }

    override fun setQuantity(product: Product) {
        adapter.update(product)
    }

    override fun showTotal(total: Double) {
        totalPrice = total
        binding?.let {
            it.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }
}