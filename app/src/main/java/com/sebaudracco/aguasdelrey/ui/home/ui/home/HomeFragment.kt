package com.sebaudracco.aguasdelrey.ui.home.ui.home

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.sebaudracco.aguasdelrey.helpers.NetworkUtils
import com.sebaudracco.aguasdelrey.ui.home.ui.gallery.GalleryFragment
import com.sebaudracco.aguasdelrey.ui.home.ui.slideshow.SlideshowFragment
import com.sebaudracco.aguasdelrey.ui.map.MapsActivity
import com.sebaudracco.aguasdelrey.ui.route.RouteActivity
import com.sebaudracco.aguasdelrey.ui.sync.SyncActivity
import com.sebaudracco.aguasdelrey.ui.route.SelectRutaActivity


class HomeFragment : Fragment() {

    companion object {
        const val ROUTE_SYNC = 11
    }

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var dialog: AlertDialog
    private val binding get() = _binding!!
    private var countLastSync = 0

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
        showLoadingDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            binding.tvLastSync.visibility = View.VISIBLE
            binding.tvLastSyncData.visibility = View.VISIBLE
            binding.tvLastSyncState.visibility = View.VISIBLE
        }, 7000)
        return root

    }

    private fun showLoadingDialog() {
        if (!::dialog.isInitialized) {
            val builder = AlertDialog.Builder(context)
            val view = LayoutInflater.from(context)
                .inflate(com.sebaudracco.aguasdelrey.R.layout.dialog_receiving_data, null)
            builder.setView(view)
            builder.setCancelable(false)
            dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
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
                    0 -> {// REPARTO
                        if (countLastSync > 0) {
                            val intent = Intent().setClass(context!!, SelectRutaActivity::class.java)
                            startActivity(intent)
                        } else {
                            val dialog = AlertDialog.Builder(context)
                                .setTitle("Error al iniciar la ruta de repartos.")
                                .setMessage("¡Primero debes sincronizar!")
                                .setNegativeButton("Entendido")
                                { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }

                                .create()
                            dialog.show()
                        }


                    }
                    1 -> {// Sync
                        if (NetworkUtils.isNetworkConnected(context!!)) {
                            val intent = Intent().setClass(context!!, SyncActivity::class.java)
                            startActivityForResult(intent, ROUTE_SYNC)
                        } else {
                            val dialog = AlertDialog.Builder(context)
                                .setTitle("Parece que no estás conectado a Internet!")
                                .setMessage("Es necesario que tengas conectividad estable para poder sincronizar la ruta de repartos.")
                                .setNegativeButton("Entendido")
                                { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }

                                .create()
                            dialog.show()
                        }
                    }

                    2 -> {//GPS
                        val intent = Intent().setClass(context!!, MapsActivity::class.java)
                        startActivity(intent)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROUTE_SYNC) {

            //  if (resultCode == Activity.RESULT_OK) {
            countLastSync = 1
            //  }
        }
    }

}