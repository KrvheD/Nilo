package com.example.nilo.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.cart.CartFragment
import com.example.nilo.databinding.ActivityMainBinding
import com.example.nilo.detail.DetailFragment
import com.example.nilo.entities.Product
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity(), OnProductListener, MainAux{

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var  adapter: ProductAdapter

    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: Product? = null

    private val productCartList = mutableListOf<Product>()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if(it.resultCode == RESULT_OK){
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (response == null){
                Toast.makeText(this, "Hasta Pronto", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                response.error?.let {
                    if (it.errorCode == ErrorCodes.NO_NETWORK){
                        Toast.makeText(this, "Sin Red", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Codigo del error: ${it.errorCode}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        configButtons()
    }

    private fun configAuth(){
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null) {
                supportActionBar?.title = auth.currentUser?.displayName
                binding.LLProgress.visibility = View.GONE
                binding.nsvProducts.visibility = View.VISIBLE

            } else {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build())

                resultLauncher.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build())


            }



        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
    }

    private fun configRecyclerView(){
        adapter = ProductAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3,
                GridLayoutManager.HORIZONTAL, false)
            adapter = this@MainActivity.adapter
        }
    }

    private fun configButtons(){
        binding.btnViewCart.setOnClickListener {
            val fragment = CartFragment()
            fragment.show(supportFragmentManager.beginTransaction(), CartFragment::class.java.simpleName)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesion terminada.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.nsvProducts.visibility = View.GONE
                            binding.LLProgress.visibility = View.VISIBLE

                        } else {
                            Toast.makeText(this, "No se pudo cerrar la sesión.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun configFirestoreRealtime(){
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        firestoreListener = productRef.addSnapshotListener { snapshots, error ->
            if (error != null){
                Toast.makeText(this, "Error al consultar datos.", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges ){
                val product = snapshot.document.toObject(Product::class.java)
                product.id = snapshot.document.id
                when(snapshot.type){
                    DocumentChange.Type.ADDED -> adapter.add(product)
                    DocumentChange.Type.MODIFIED -> adapter.update(product)
                    DocumentChange.Type.REMOVED -> adapter.delete(product)
                }
            }
        }
    }

    override fun onClick(product: Product) {
        val index = productCartList.indexOf(product)
        if (index != -1){
            productSelected = productCartList[index]
        }else {
            productSelected = product
        }

        val fragment = DetailFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain, fragment)
            .addToBackStack(null)
            .commit()

        showButton(false)
    }

    override fun getProductsCart(): MutableList<Product> = productCartList


    override fun getProductSelected(): Product? = productSelected

    override fun showButton(isVisible: Boolean) {
        binding.btnViewCart.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun addProductToCart(product: Product) {
        val index = productCartList.indexOf(product)
        if (index != -1) {
            productCartList.set(index, product)
        } else {
            productCartList.add(product)
        }

        updateTotal()
    }

    override fun updateTotal() {
        var total = 0.0
        productCartList.forEach{ product ->
            total += product.totalPrice()
        }

        if(total == 0.0){
            binding.tvTotal.text = getString(R.string.product_empty_cart)
        } else{
            binding.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }

    override fun clearCart(){
        productCartList.clear()
    }
}