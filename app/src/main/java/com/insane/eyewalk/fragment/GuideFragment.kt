package com.insane.eyewalk.fragment

//import jakarta.ws.rs.client.Client
//import jakarta.ws.rs.client.ClientBuilder
//import jakarta.ws.rs.core.MediaType
//import jakarta.ws.rs.core.Response

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import kotlin.math.roundToInt


class GuideFragment : Fragment(),LocationListener {

    private lateinit var binding: FragmentGuideBinding
    private lateinit var db: AppDataBase
    private lateinit var roomService: RoomService
    private lateinit var map: MapView
    private lateinit var mapController: IMapController
    private lateinit var roadManager: RoadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)

        db = AppDataBase.getDataBase(this.requireContext())
        roomService = RoomService(db)
        roadManager = OSRMRoadManager(this.requireContext(), "Mozilla/5.0")

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

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

            val addressStartPoint = binding.etStartPoint.text.toString()
            val addressEndPoint = binding.etDestination.text.toString()
            val geocoder = Geocoder(requireContext(), Locale.getDefault())

            if (!addressStartPoint.isNullOrEmpty() && !addressEndPoint.isNullOrEmpty()) {
                try {
                    val coordinatesStartPoint = geocoder.getFromLocationName(addressStartPoint, 1)
                    val coordinatesEndPoint = geocoder.getFromLocationName(addressEndPoint, 1)
                    if (!coordinatesStartPoint.isNullOrEmpty() && !coordinatesEndPoint.isNullOrEmpty()) {
                        traceRoute(coordinatesStartPoint[0].latitude, coordinatesStartPoint[0].longitude, coordinatesEndPoint[0].latitude, coordinatesEndPoint[0].longitude)
                        // traceRoute(-23.533773, -46.625290, -23.536973, -46.629590)
                    } else {
                        Tools.Show.message(requireContext(), "Endereço não encontrado! Verifique os dados.")
                    }
                } catch (e:IOException) {
                    e.printStackTrace()
                }
            } else {
                Tools.Show.message(requireContext(), "Insira um endereço de partida e destino")
            }
        }
    }

    private fun showMap() {
        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setZoomRounding(true)
        map.setMultiTouchControls(true)
        map.setUseDataConnection(true)
        map.isClickable = true
        mapController = map.controller
        mapController.setZoom(17.0)
        map.overlayManager.tilesOverlay.loadingBackgroundColor = resources.getColor(R.color.primary_background, requireActivity().theme)

        if (roomService.getSetting().switchMap)
            map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)

        val startPoint = GeoPoint(-23.533773, -46.625290)
        mapController.setCenter(startPoint)
        addMarker(startPoint)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun traceRoute(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double) {

        val startPoint = GeoPoint(fromLat, fromLon)
        val endPoint = GeoPoint(toLat, toLon)
        val waypoints = ArrayList<GeoPoint>()

        mapController.setCenter(startPoint)
        addMarker(startPoint)

        waypoints.add(startPoint)
        waypoints.add(endPoint)

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
                println("**************** ManeuverType ${node.mManeuverType}")
                val icon = when (node.mManeuverType) {
                    24 -> resources.getDrawable(R.drawable.ic_destination, requireActivity().theme)
                    4 -> resources.getDrawable(R.drawable.ic_turn_left, requireActivity().theme)
                    7 -> resources.getDrawable(R.drawable.ic_turn_right, requireActivity().theme)
                    else -> resources.getDrawable(R.drawable.ic_continue, requireActivity().theme)
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
            nodeMarker.icon = resources.getDrawable(R.drawable.ic_location_light, requireActivity().theme)
            nodeMarker.title = "Chegada ao destino"
            map.overlays.add(nodeMarker)
        }

        map.invalidate()
    }

//    private fun createRoute() {
//        val client: Client = ClientBuilder.newClient()
//        val response: Response =
//            client.target("https://api.openrouteservice.org/v2/directions/foot-walking?api_key=5b3ce3597851110001cf6248dd0e7d8f590e4a17a574621c89ba855a&start=8.681495,49.41461&end=8.687872,49.420318")
//                .request(MediaType.TEXT_PLAIN_TYPE)
//                .header(
//                    "Accept",
//                    "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8"
//                )
//                .get()
//
//        println("status: " + response.status)
//        println("headers: " + response.headers)
//        println("body:" + response.readEntity(String::class.java))
//
//    }

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