package com.example.joymap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.joymap.databinding.ActivityMainBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.Animation.Type
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapObjectCollection: MapObjectCollection // Коллекция различных объектов на карте
    private lateinit var placemarkMapObject: PlacemarkMapObject // Геопозиционированный объект (метка со значком) на карте
    private val startLocation = Point(59.9402, 30.315) // Координаты Эрмитажа
    private var zoomValue: Float = 16.5f // Величина зума
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session
    private val mapObjectTapListener = MapObjectTapListener { mapObject, point ->
        Toast.makeText(applicationContext, "Эрмитаж — музей изобразительных искусств", Toast.LENGTH_SHORT).show()
        true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState) // Проверяем: был ли уже ранее установлен API-ключ в приложении. Если нет - устанавливаем его.
        MapKitFactory.initialize(this) // Инициализация библиотеки для загрузки необходимых нативных библиотек.
        binding = ActivityMainBinding.inflate(layoutInflater) // Раздуваем макет только после того, как установили API-ключ
        setContentView(binding.root) // Размещаем пользовательский интерфейс в экране активности

        moveToStartLocation() // Перемещаем камеру в определенную область на карте
        setMarkerInStartLocation() // Устанавливаем маркер на карте
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
            MapKitFactory.setApiKey("3acec1e4-7330-400c-bbad-0e73b429e1ee") // API-ключ должен быть задан единожды перед инициализацией MapKitFactory
    }

    // Если Activity уничтожается (например, при нехватке памяти или при повороте экрана) - сохраняем информацию, что API-ключ уже был получен ранее
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    private fun setMarkerInStartLocation() {
        val marker = createBitmapFromVector(R.drawable.ic_pin_black_svg)
        mapObjectCollection = binding.mapview.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        placemarkMapObject =
            mapObjectCollection.addPlacemark(startLocation, ImageProvider.fromBitmap(marker)) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.5f // Устанавливаем прозрачность метке
        placemarkMapObject.setText("Обязательно к посещению!") // Устанавливаем текст сверху метки
        placemarkMapObject.addTapListener(mapObjectTapListener) //Добавляем слушатель клика на метку
    }
    private fun moveToStartLocation() {
        binding.mapview.map.move(
            CameraPosition(startLocation, zoomValue, 0.0f, 0.0f), // Позиция камеры
            Animation(Type.SMOOTH, 2f), // Красивая анимация при переходе на стартовую точку
            null
        )
    }
    // Отображаем карты перед моментом, когда активити с картой станет видимой пользователю:
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    // Останавливаем обработку карты, когда активити с картой становится невидимым для пользователя:
    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    companion object {
        const val MAPKIT_API_KEY = "3acec1e4-7330-400c-bbad-0e73b429e1ee\n"
    }
}