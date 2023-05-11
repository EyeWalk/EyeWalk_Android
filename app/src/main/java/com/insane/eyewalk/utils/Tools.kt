package com.insane.eyewalk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.insane.eyewalk.R
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.Normalizer
import java.util.regex.Pattern
import kotlin.math.roundToInt

object Tools {


    private var doubleClick = 0

    object Connection {
        fun internetStatus(context: Context): Boolean {

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
            // TODO: Verify if there is any other type of Connectivity Service other than WIFI, MOBILE and ETHERNET
            // TODO: VERIFY DEPRECATION
        }

    }

    object Text {
        fun removeAccents(string: String): String {
            var st = Normalizer.normalize(string, Normalizer.Form.NFD)
            st = st.replace("[^\\p{ASCII}]".toRegex(), "")
            return Normalizer.normalize(st, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }

        fun isEmail(email: String?): Boolean {
            val emailRegex =
                "^[a-zA-Z\\d_+&*-]+(?:\\.[a-zA-Z\\d_+&*-]+)*@(?:[a-zA-Z\\d-]+\\.)+[a-zA-Z]{2,7}$"
            val pat = Pattern.compile(emailRegex)
            return if (email == null) false else pat.matcher(email).matches()
        }

        fun setPlural(value: Int, singular: String): String {
            return if (value == 0 || value > 1) {
                // PLURAL
                "$value ${singular}s"
            } else {
                // SINGULAR
                "$value $singular"
            }
        }
    }

    object Currency {
        fun toReal(value: Double): String {
            val decimal = DecimalFormat("#,###.00")
            return "R$ ${decimal.format(value)}"
        }
    }

    object Encryption {
        fun sha256(base: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(base.toByteArray(StandardCharsets.UTF_8))
                val hexString = StringBuilder()
                for (b in hash) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                hexString.toString()
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }

    object Window {

        /**
         * Gets the window size either width or height
         * @param activity context
         * @param widthORheight parameter type String needs to be width or height
         * @return a Integer number
         */
        fun size(activity: Activity, widthORheight: String): Int {
            val dm = DisplayMetrics()
            activity.window.windowManager.defaultDisplay.getMetrics(dm)
            var result = 0
            if (widthORheight == "width") {
                result = dm.widthPixels
            } else if (widthORheight == "height") {
                result = dm.heightPixels
            }
            return result
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun spToPx(sp: Int): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp.toFloat(),
                Resources.getSystem().displayMetrics
            )
        }

        fun pxToDp(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        fun convertDpToPixel(dp: Float): Int {
            val metrics = Resources.getSystem().displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return px.roundToInt()
        }
    }

    object Device {

        fun callPhone(context: Context, number: String) {
            var n = number
            n = n.replace("/^[\\d ()+-]+\$/".toRegex(), "")
            if (n.isNotEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // USED ONLY ON STATIC
                callIntent.data = Uri.parse("tel:$n")
                context.startActivity(callIntent)
            } else {
                Toast.makeText(context, "Número de telefone inválido", Toast.LENGTH_SHORT).show()
            }
        }

        fun openWhatsApp(context: Context, number: String, message: String) {
            var n = number
            n = n.replace("\\D".toRegex(), "")
            if (n.isNotEmpty()) {
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$n&text=$message")
                val whatsapp = Intent(Intent.ACTION_VIEW, uri)
                whatsapp.flags = Intent.FLAG_ACTIVITY_NEW_TASK // USED ONLY ON STATIC
                whatsapp.setPackage("com.whatsapp")
                try {
                    context.startActivity(whatsapp)
                } catch (e: Exception) {
                    // WHATSAPP NOT FOUND TRY BROWSER
                    try {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://api.whatsapp.com/send?phone=$n&text=$message")
                        )
                        browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // USED ONLY ON STATIC
                        context.startActivity(browserIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Show.message(context, "Não foi possível abrir o whatsapp!")
                    }
                }
            } else {
                Toast.makeText(context, "Número de telefone inválido", Toast.LENGTH_SHORT).show()
            }
        }

        fun openInstagram(context: Context, profile: String) {
            val uri = Uri.parse("https://instagram.com/_u/$profile")
            val likeIng = Intent(Intent.ACTION_VIEW, uri)
            likeIng.setPackage("com.instagram.android")
            try {
                context.startActivity(likeIng)
            } catch (e: ActivityNotFoundException) {
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://instagram.com/$profile")
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Show.message(context, "O aplicativo instagram não foi encontrado!")
                }
            }
        }

        fun openEmail(context: Context, addresses: Array<String?>?, subject: String?) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Show.message(context, "Aplicativo de email não encontrado!")
            }
        }


        fun vibrate(context: Context) {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        fun currentVersion(): String {
            val release =
                Build.VERSION.RELEASE.replace("(\\d+[.]\\d+)(.*)".toRegex(), "$1").toDouble()
            var codeName = "Unsupported" //below Jelly bean OR above Oreo
            if (release >= 4.1 && release < 4.4) codeName =
                "Jelly Bean" else if (release < 5) codeName =
                "Kit Kat" else if (release < 6) codeName =
                "Lollipop" else if (release < 7) codeName =
                "Marshmallow" else if (release < 8) codeName =
                "Nougat" else if (release < 9) codeName = "Oreo" else if (release < 10) codeName =
                "Android Q" else if (release >= 10) codeName = "Android 10"
            return codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT
        }

        val isDoubleClick: Boolean
            get() {
                doubleClick++
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleClick = 0
                }, 500)
                return if (doubleClick >= 2) {
                    doubleClick = 0
                    true
                } else {
                    false
                }
            }
    }

    object Web {
        fun openBrowser(context: Context, url: String) {
            // OPEN BROWSER
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Show.message(context, "Nenhum navegador foi encontrado para abrir o site!")
            }
        }
    }

    object Show {
        fun noConnection(context: Context) {
            Toast.makeText(context, context.getString(R.string.noConnection), Toast.LENGTH_SHORT).show()
        }

        fun message(context: Context?, message: String?) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    object Component {
        private const val BASE_ID_BUTTON_CATEGORY = 1000
        private const val RECOMMENDATION_WIDTH = 100

        fun getLoader(context: Context): CircularProgressDrawable {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 15f
            circularProgressDrawable.centerRadius = 60f
            circularProgressDrawable.alpha = 50
            circularProgressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(R.color.primary_text, BlendModeCompat.SRC_ATOP)
            circularProgressDrawable.start()
            return  circularProgressDrawable
        }

        fun getDotsLayout(context: Context): LinearLayout {
            val dotsLayout = LinearLayout(context)
            val paramsLinearDot = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            paramsLinearDot.setMargins(0,Tools.Window.convertDpToPixel(0F),0,Tools.Window.convertDpToPixel(0F))
            dotsLayout.layoutParams = paramsLinearDot
            dotsLayout.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            dotsLayout.orientation = LinearLayout.HORIZONTAL
            return dotsLayout
        }

    }

}