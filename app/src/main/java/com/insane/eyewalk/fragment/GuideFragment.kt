package com.insane.eyewalk.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.insane.eyewalk.R
import com.insane.eyewalk.config.Constants
import com.insane.eyewalk.databinding.FragmentGuideBinding
import com.insane.eyewalk.utils.Tools
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.TilesOverlay


class GuideFragment : Fragment(),LocationListener {

    private lateinit var binding: FragmentGuideBinding
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationManager: LocationManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        if (allPermissionGranted()) {
            println("Camera permissions granted!")
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.MAP_REQUIRED_PERMISSIONS,
                Constants.MAP_REQUEST_CODE_PERMISSION
            )
        }

        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setZoomRounding(true)
        map.setMultiTouchControls(true)
        map.setUseDataConnection(true)
        map.isClickable = true
        mapController = map.controller
        mapController.setZoom(17.0)
        map.overlayManager.tilesOverlay.loadingBackgroundColor = resources.getColor(R.color.primary_background, requireActivity().theme)
        map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)

        val startPoint = GeoPoint(-23.533773, -46.625290)
        mapController.setCenter(startPoint)
        addMarker(startPoint)

        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun addMarker(center: GeoPoint) {
        val marker = Marker(map)
        marker.position = center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = resources.getDrawable(R.drawable.ic_marker, requireActivity().theme)
        marker.isDraggable = true

        marker.title = "Sua Localização"
        marker.snippet = "${center.latitude},${center.longitude}"
        marker.subDescription = "São Paulo - SP"

        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP)
        map.overlays.clear()
        map.overlays.add(MapOverlay(requireActivity()))
        map.overlays.add(marker)
        map.invalidate()
    }

    internal class MapOverlay(ctx: Context?) : Overlay(ctx) {
        override fun draw(c: Canvas?, osmv: MapView?, shadow: Boolean) {}
        override fun onSingleTapConfirmed(me: MotionEvent, mv: MapView): Boolean {
            val p: Projection = mv.projection
            val gp = p.fromPixels(me.x.toInt(), me.y.toInt()) as GeoPoint
//            addMarker(gp)
            return true
        }
    }

    private fun allPermissionGranted() =
        Constants.MAP_REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == Constants.MAP_REQUEST_CODE_PERMISSION) {
            if (allPermissionGranted())
                onResume()
        } else {
            Tools.Show.message(requireContext(),"É necessário permitir o acesso a localização.")
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onLocationChanged(location: Location) {
        val center = GeoPoint(location.latitude, location.longitude)
        mapController.animateTo(center)
    }

}