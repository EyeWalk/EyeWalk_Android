package com.insane.eyewalk.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.*
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.insane.eyewalk.R
import com.insane.eyewalk.config.Constants
import com.insane.eyewalk.database.room.AppDataBase
import com.insane.eyewalk.databinding.FragmentGuideBinding
import com.insane.eyewalk.service.RoomService
import com.insane.eyewalk.utils.Tools
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class GuideFragment : Fragment(),LocationListener {

    private lateinit var binding: FragmentGuideBinding
    private lateinit var db: AppDataBase
    private lateinit var roomService: RoomService
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var roadManager: RoadManager
    private lateinit var locationGeoPoint: GeoPoint
    private var zoomLevel: Double = 18.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)

        db = AppDataBase.getDataBase(this.requireContext())
        roomService = RoomService(db)
        roadManager = OSRMRoadManager(this.requireContext(), "Mozilla/5.0")
        locationGeoPoint = getLocation()
        map = binding.mapView

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        if (allPermissionGranted()) {
            println("Location permission granted!")
            showMap()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.MAP_REQUIRED_PERMISSIONS,
                Constants.MAP_REQUEST_CODE_PERMISSION
            )
        }

        setUpClickListeners()

        return binding.root
    }

    private fun setUpClickListeners() {

        //FLOAT BUTTON CLICK
        binding.fabStartNavigation.setOnClickListener {
            val addressEndPoint = binding.etDestination.text.toString()
            val geocoder = Geocoder(requireContext(), Locale.getDefault())

            if (addressEndPoint.isNotEmpty()) {
                try {
                    val coordinatesStartPoint = getLocation()
                    val coordinatesEndPoint = geocoder.getFromLocationName(addressEndPoint, 1)
                    if (!coordinatesEndPoint.isNullOrEmpty()) {
                        traceRoute(coordinatesStartPoint, GeoPoint(coordinatesEndPoint[0].latitude, coordinatesEndPoint[0].longitude))
                    } else {
                        Tools.Show.message(requireContext(), "Endereço não encontrado! Verifique os dados.")
                    }
                } catch (e:IOException) {
                    Tools.Show.message(requireContext(),"Algo deu errado, por favor tente novamente!")
                    e.printStackTrace()
                }
            } else {
                Tools.Show.message(requireContext(), "Insira um local ou endereço de destino.")
            }
        }
    }

    private fun showMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setZoomRounding(true)
        map.setMultiTouchControls(true)
        map.setUseDataConnection(true)
        map.isClickable = true
        mapController = map.controller
        mapController.setZoom(zoomLevel)
        map.overlayManager.tilesOverlay.loadingBackgroundColor = resources.getColor(R.color.primary_background, requireActivity().theme)
        if (roomService.getSetting().switchMap)
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        mapController.setCenter(locationGeoPoint)
        addMarker(locationGeoPoint)
        mapController.animateTo(locationGeoPoint)
    }

    private fun getLocation(): GeoPoint {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, false)
        var geoPoint = GeoPoint(-23.5000,-46.5000) // DEFAULT IN CASE NO SERVICE WAS FOUND

        // CHECK PERMISSIONS
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // PERMISSION NOT GRANTED
            Tools.Show.message(requireContext(), "É necessário a permissão de localização.")
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.MAP_REQUIRED_PERMISSIONS,
                Constants.MAP_REQUEST_CODE_PERMISSION
            )
        } else {
            // PERMISSION GRANTED
            if (provider != null) {
                val latitude = locationManager.getLastKnownLocation(provider)?.latitude
                val longitude = locationManager.getLastKnownLocation(provider)?.longitude
                if (latitude != null && longitude != null)
                    geoPoint = GeoPoint(latitude, longitude)
                else {
                   Tools.Show.message(requireContext(), "Não foi possível encontrar a sua localizacão.")
                }
            } else {
                Tools.Show.message(requireContext(), "Provedor do serviço de GPS não encontrado.")
            }
        }
        // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, requireActivity())
        return geoPoint
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun traceRoute(startPoint: GeoPoint, endPoint: GeoPoint) {

        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(startPoint)
        waypoints.add(endPoint)
        locationGeoPoint = getLocation()
        showMap()

        // SET UP WAY BY FOOT
        (roadManager as OSRMRoadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT)
        val road = roadManager.getRoad(waypoints)
        val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road,resources.getColor(R.color.danger_background,requireActivity().theme),20F)
        map.overlays.add(roadOverlay)

        // SET UP BUBBLES FOR EACH NODE
        val nodeIcon = resources.getDrawable(R.drawable.marker_node,requireActivity().theme)
        for (i in road.mNodes.indices) {
            try {
                val node = road.mNodes[i]
                val nodeMarker = Marker(map)
                nodeMarker.position = node.mLocation
                nodeMarker.icon = nodeIcon
                nodeMarker.title = "Passo $i"
                // FILL UP BUBBLES
                nodeMarker.snippet = node.mInstructions;
//            nodeMarker.subDescription = road.getLengthDurationText(requireContext(),node.mLength.roundToInt())
                val icon = when (node.mManeuverType) {
                    24 -> resources.getDrawable(R.drawable.ic_destination, requireActivity().theme)
                    4 -> resources.getDrawable(R.drawable.ic_turn_left, requireActivity().theme)
                    7 -> resources.getDrawable(R.drawable.ic_turn_right, requireActivity().theme)
                    else -> resources.getDrawable(R.drawable.ic_continue, requireActivity().theme)
                    // TODO Needs to implement the other maneuver type || println("${node.mManeuverType}")
                }
                nodeMarker.image = icon
                map.overlays.add(nodeMarker)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // SET UP DESTINATION BUBBLE
        road.mNodes.last().let { node ->
            val nodeMarker = Marker(map)
            nodeMarker.position = node.mLocation
            nodeMarker.icon = resources.getDrawable(R.drawable.ic_flag_light, requireActivity().theme)
            nodeMarker.title = "Chegada ao destino"
            map.overlays.add(nodeMarker)
        }

        // addMarker(startPoint)
        map.invalidate()
        map.zoomToBoundingBox(roadOverlay.bounds, true)
    }

    private fun zoomToBounds(box: BoundingBox?) {
        if (map.height > 0) {
            map.zoomToBoundingBox(box, true)
        } else {
            val vto = map.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    map.zoomToBoundingBox(box, true)
                    val vto2 = map.viewTreeObserver
                    vto2.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    fun computeArea(points: ArrayList<GeoPoint?>): BoundingBox {
        var nord = 0.0
        var sud = 0.0
        var ovest = 0.0
        var est = 0.0
        for (i in 0 until points.size) {
            if (points[i] == null) continue
            val lat = points[i]!!.latitude
            val lon = points[i]!!.longitude
            if (i == 0 || lat > nord) nord = lat
            if (i == 0 || lat < sud) sud = lat
            if (i == 0 || lon < ovest) ovest = lon
            if (i == 0 || lon > est) est = lon
        }
        return BoundingBox(nord, est, sud, ovest)
    }

    private fun addMarker(center: GeoPoint) {
        val marker = Marker(map)
        marker.position = center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = getMarkerIcon()
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

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getMarkerIcon(): Drawable {
        return if (roomService.getSetting().switchMap) {
            // DARKER MAP MUST USE LIGHTER ICON
            resources.getDrawable(R.drawable.ic_marker_light, requireActivity().theme)
        } else {
            // LIGHTER MAP MUST USE DARKER ICON
            resources.getDrawable(R.drawable.ic_marker_dark, requireActivity().theme)
        }
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

//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray) {
//        if (requestCode == Constants.MAP_REQUEST_CODE_PERMISSION) {
//            if (allPermissionGranted())
//                onResume()
//        } else {
//            Tools.Show.message(requireContext(),"É necessário permitir o acesso a localização.")
//        }
//    }

    override fun onLocationChanged(location: Location) {
        val center = GeoPoint(location.latitude, location.longitude)
        mapController.animateTo(center)
    }

}