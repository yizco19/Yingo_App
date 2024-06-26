package com.zy.proyecto_final.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zy.proyecto_final.R
import com.zy.proyecto_final.pojo.Car
import com.zy.proyecto_final.recyclerviewadapter.CarRecyclerViewAdapter
import com.zy.proyecto_final.retrofit.YingoViewModel
import com.zy.proyecto_final.utils.PriceUtils
import com.zy.proyecto_final.viewmodel.CarViewModel
import com.zy.proyecto_final.viewmodel.OfferViewModel
import com.zy.proyecto_final.viewmodel.OrderViewModel
import com.zy.proyecto_final.viewmodel.ProductViewModel
import com.zy.proyecto_final.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class CarFragment : Fragment() {
    private val viewmodel: CarViewModel by activityViewModels<CarViewModel>()
    private val userviewmodel: UserViewModel by activityViewModels<UserViewModel>()
    private val productviewmodel: ProductViewModel by activityViewModels<ProductViewModel>()
    private val offerViewModel: OfferViewModel by activityViewModels<OfferViewModel>()
    private val orderviewmodel: OrderViewModel by activityViewModels<OrderViewModel>()
    private val yingoviewmodel: YingoViewModel by activityViewModels<YingoViewModel>()
    var plus_click: ((Int, Car) -> Unit)? = null
    var minus_click: ((Int, Car) -> Unit)? = null
    private lateinit var txt_total: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPlaceOrder: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_car, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 1)
        recyclerView.adapter = viewmodel.items.value?.let { itemList ->
            CarRecyclerViewAdapter(
                itemList.toMutableList(),
                productviewmodel,
                offerViewModel,
                requireContext()
            )
        }

        (recyclerView.adapter as? CarRecyclerViewAdapter)?.plus_click = { position: Int, item: Car ->
            run {
                item.product_count = item.product_count + 1
                viewmodel.update(item)
                loadData()
                plus_click?.let { it(position, item) }
            }
        }

        (recyclerView.adapter as? CarRecyclerViewAdapter)?.minus_click = { position: Int, item: Car ->
            run {
                item.product_count = item.product_count - 1
                if (item.product_count == 0) {
                    viewmodel.delete(item)
                } else {
                    viewmodel.update(item)
                }
                loadData()
                minus_click?.let { it(position, item) }
            }
        }

        view.findViewById<LinearLayout>(R.id.clear_cart).setOnClickListener {
            viewmodel.deleteAll()
            loadData()
        }

        txt_total = view.findViewById(R.id.total)
        val btn_total = view.findViewById<TextView>(R.id.btn_total)

        viewmodel.items.value?.let {
            setTotal(it)
        }

        btn_total.setOnClickListener {
            yingoviewmodel.setCar(viewmodel.items.value!!)
            lifecycleScope.launch {
                if (!viewmodel.items.value.isNullOrEmpty() && userviewmodel.userlogged != null) {
                    orderviewmodel.setAll(userviewmodel.userlogged!!, viewmodel.items.value!!)
                    parentFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(
                            R.id.fragmentContainerView,
                            OrderFragment::class.java,
                            null,
                            "OrderFragment"
                        )
                        addToBackStack(null)
                    }
                }
            }
        }

        return view
    }

    private fun setTotal(list: List<Car>) {
        var total = 0.0
        for (item in list) {
            try {
                val product = productviewmodel.getProductbyId(item.product_id!!)
                val offer = product?.offerId?.let { offerViewModel.getOfferById(it) }
                if (product != null && offer != null) {
                    val product_price = PriceUtils.calculateDiscountedPrice(product, offer)
                    total += item.product_count * (product_price ?: 0.0)
                } else if (product != null) {
                    // Si no hay oferta, usar el precio original del producto
                    total += item.product_count * (product.price ?: 0.0)
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Error al calcular el total", Toast.LENGTH_SHORT).show()
            }
        }

        // Mostrar el total calculado
        var formattedTotal = String.format("%.2f", total)
        txt_total.text = "$ ${formattedTotal}"
    }


    private fun loadData() {
        lifecycle.coroutineScope.launch {
            viewmodel.items.value?.let {
                (recyclerView.adapter as? CarRecyclerViewAdapter)?.setValues(it.toMutableList())
                recyclerView.adapter?.notifyDataSetChanged()
                setTotal(it)
            }
        }
    }
}
