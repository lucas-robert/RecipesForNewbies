package com.example.recipesfornewbies.defaultrecipelist

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipesfornewbies.R
import com.example.recipesfornewbies.UtilsClass.RecyclerItemClickListener
import com.example.recipesfornewbies.UtilsClass.SwipeToAddToWishList
import com.example.recipesfornewbies.UtilsClass.SwipeToDeleteCallback
import com.example.recipesfornewbies.databinding.FragmentDefaultRecipeListBinding
import com.example.recipesfornewbies.recipes.Recipe
import com.example.recipesfornewbies.wishlistDatabase.WishlistDatabase





class DefaultRecipeListFragment: Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding : FragmentDefaultRecipeListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_default_recipe_list, container, false)


        val application = requireNotNull(this.activity).application
        val dataSource = WishlistDatabase.getInstance(application).wishlistDatabaseDAO

        val viewModelFactory = DefaultRecipeViewModelFactory(dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        val viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DefaultRecipeListViewModel::class.java)

        binding.viewModel = viewModel

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        //Creating the RecyclerView
        val manager = LinearLayoutManager(activity)

        binding.recyclerRecipeList.layoutManager = manager

        /**HANDLING SWIPE BEHAVIOR**/



        binding.recyclerRecipeList.adapter = RandomRecipeListAdapter()

        val pullToRefresh = binding.pullToRefresh

        pullToRefresh.setOnRefreshListener {
            viewModel.updateRecipeList()
            binding.recyclerRecipeList.adapter!!.notifyDataSetChanged()
        }

        binding.recyclerRecipeList.addOnItemTouchListener(RecyclerItemClickListener(this.context!!,
            binding.recyclerRecipeList, object : RecyclerItemClickListener.OnItemClickListener {

            override fun onItemClick(view: View, position: Int) {
                viewModel.randomRecipeList.value?.let {
                    val recipe : Recipe = it[position]
                    findNavController().navigate(
                    DefaultRecipeListFragmentDirections.actionDefaultRecipeListFragmentToDetailedRecipeFragment(recipe))
                }
            }
            override fun onItemLongClick(view: View?, position: Int) {
                viewModel.randomRecipeList.value?.let {
                    val recipe : Recipe = it[position]
                    findNavController().navigate(
                        DefaultRecipeListFragmentDirections.actionDefaultRecipeListFragmentToDetailedRecipeFragment(recipe))
                }
            }
        }))

        val swipeDelete = object : SwipeToDeleteCallback(application) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.remove(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelperDelete = ItemTouchHelper(swipeDelete)
        itemTouchHelperDelete.attachToRecyclerView(binding.recyclerRecipeList)


        val swipeWishlist = object: SwipeToAddToWishList(application){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.onSwipeWishList(viewHolder.adapterPosition)
                viewModel.remove(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelperWishList = ItemTouchHelper(swipeWishlist)
        itemTouchHelperWishList.attachToRecyclerView(binding.recyclerRecipeList)

        viewModel.showToast.observe(this, Observer {
            if (it == true){
                Toast.makeText(this.context,
                    "Recipe has been added to your wishlist!",
                    Toast.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        })

        viewModel.randomRecipeList.observe(this, Observer {
                binding.recyclerRecipeList.adapter!!.notifyDataSetChanged()
        })


        setHasOptionsMenu(true)
        return binding.root


        TODO("Add a filter to the default screen, so the user can choose the diet, recipe type... he want to display at launch")
        TODO("Add 'WishList' and 'Not interested' buttons to CardView, or implement a sliding behavior ")

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return NavigationUI.onNavDestinationSelected(item!!,
            this.findNavController()) || super.onOptionsItemSelected(item)
    }


}