package com.sebaudracco.aguasdelrey.ui.home.ui.home

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ramotion.circlemenu.CircleMenuView
import com.sebaudracco.aguasdelrey.databinding.FragmentHomeBinding
import com.sebaudracco.aguasdelrey.ui.home.ui.gallery.GalleryFragment
import com.sebaudracco.aguasdelrey.ui.home.ui.slideshow.SlideshowFragment


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        setCircleMenu()

        return root
    }

    private fun setCircleMenu() {

        val menu: CircleMenuView = binding.circleMenu
        menu.setEventListener(object : CircleMenuView.EventListener() {
            override fun onMenuOpenAnimationStart(view: CircleMenuView) {
                Log.d("D", "onMenuOpenAnimationStart")
            }

            override fun onMenuOpenAnimationEnd(view: CircleMenuView) {
                Log.d("D", "onMenuOpenAnimationEnd")
            }

            override fun onMenuCloseAnimationStart(view: CircleMenuView) {
                Log.d("D", "onMenuCloseAnimationStart")
            }

            override fun onMenuCloseAnimationEnd(view: CircleMenuView) {
                Log.d("D", "onMenuCloseAnimationEnd")
            }

            override fun onButtonClickAnimationStart(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonClickAnimationStart| index: $index")
            }

            override fun onButtonClickAnimationEnd(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonClickAnimationEnd| index: $index")

                when (index) {
                    0 -> {
                      //  val intent = Intent().setClass(context!!, GalleryFragment::class.java)
                      //  startActivity(intent)
                        val fm: FragmentManager? = fragmentManager
                        val ft: FragmentTransaction = fm!!.beginTransaction()
                        val llf = GalleryFragment()
                      //  ft.replace(R.id.list_container, llf)
                        ft.commit()
                    }
                    1 -> {
                     //   val intent = Intent().setClass(context!!, SlideshowFragment::class.java)
                     //   startActivity(intent)
                        val fm: FragmentManager? = fragmentManager
                        val ft: FragmentTransaction = fm!!.beginTransaction()
                        val llf = SlideshowFragment()
                        //  ft.replace(R.id.list_container, llf)
                        ft.commit()
                    }

                    2 -> {
                      //  val intent = Intent().setClass(context!!, GalleryFragment::class.java)
                      //  startActivity(intent)
                        val fm: FragmentManager? = fragmentManager
                        val ft: FragmentTransaction = fm!!.beginTransaction()
                        val llf = GalleryFragment()
                        //  ft.replace(R.id.list_container, llf)
                        ft.commit()
                    }
                }
            }

            override fun onButtonLongClick(view: CircleMenuView, index: Int): Boolean {
                Log.d("D", "onButtonLongClick| index: $index")
                return true
            }

            override fun onButtonLongClickAnimationStart(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonLongClickAnimationStart| index: $index")
            }

            override fun onButtonLongClickAnimationEnd(view: CircleMenuView, index: Int) {
                Log.d("D", "onButtonLongClickAnimationEnd| index: $index")
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}