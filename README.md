# INTRODUCCIÓN

![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-blue)
![Android Studio](https://img.shields.io/badge/IDE-Android%20Studio-green)

Este proyecto forma parte de la “Actividad Evaluable del Tema 1” en el módulo de “Programación Multimedia y Dispositivos Móviles”. El objetivo principal ha sido diseñar y construir una aplicación funcional que permita al usuario configurar tres elementos clave: un número de teléfono, una hora de alarma y una ubicación personalizada. A partir de estos datos, la app ofrece accesos directos para realizar una llamada, abrir una URL, programar una alarma y visualizar la ubicación en un mapa.

En este documento se podrá ver cómo he estructurado el proyecto, explicación de uso de código, errores que he cometido mientras picaba el código, funcionalidades que he utilizado, permisos personalizados y una conclusión.

# ESTRUCTURACIÓN DEL PROYECTO

El proyecto está organizado siguiendo la estructura típica de una aplicación Android desarrollada con Android Studio:

- **Paquete principal:** `com.example.actividadevaluabletema2`  
  Contiene las clases Kotlin que gestionan la lógica de la aplicación:
    - `MainActivity.kt` → Actividad principal que actúa como menú de navegación. Desde aquí el usuario puede acceder a las funciones de llamada, alarma, URL, ubicación, configuración, información, chistes, dados y calculadora. La visibilidad de los botones depende de las preferencias guardadas.
    - `ConfActivity.kt` → Actividad encargada de recoger y guardar los datos introducidos por el usuario: número de teléfono, ubicación y opciones de interfaz (mostrar/ocultar botones, seleccionar modo Chistes o Dados). Utiliza `SharedPreferences` para almacenar esta información de forma persistente.
    - `Phone.kt` → Actividad que lanza la llamada telefónica utilizando `Intent.ACTION_CALL`, previa comprobación del permiso `CALL_PHONE`.
    - `Info.kt` → Actividad secundaria que muestra información adicional sobre el proyecto o el autor.
    - `Chistes.kt` → Actividad de entretenimiento que muestra chistes al usuario.
    - `Dados.kt` → Actividad de entretenimiento que simula la tirada de tres dados y muestra retos asociados a la suma obtenida.
    - `Calculadora.kt` → Actividad que implementa una calculadora básica con botones estilizados y sin funcionalidad.

- **Recursos (`res/`)**
    - `res/layout` → Contiene los archivos XML que definen la interfaz gráfica de cada actividad.
    - `res/values` → Incluye recursos como cadenas de texto (`strings.xml`), colores (`colors.xml`) y estilos.

- **Configuración**
    - `AndroidManifest.xml` → Declara las actividades del proyecto y los permisos requeridos (`CALL_PHONE`, `SET_ALARM`).
    - Los modos de visualización (Chistes/Dados) y la visibilidad de botones se gestionan mediante `SharedPreferences`.

- **Persistencia de datos**
    - La aplicación utiliza `SharedPreferences` para guardar los datos configurados por el usuario. Las claves utilizadas están definidas en `strings.xml` y permiten recuperar la información en cualquier momento desde otras actividades.

# DOCUMENTACIÓN DEL CÓDIGO

## Código en común en todas las activities

Lo primero que hago en cada `activity` es crear un `binding` con el que poder referirme a los elementos gráficos e incluso a datos guardados en `SharedPreferences`.

```java
private lateinit var binding: ActivityMainBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ...
```

Obviamente, en cada `activity` hay un método `onCreate()` y específicamente `ConfActivity.kt`, `MainActivity.kt` y `Phone.kt` tienen un método en común que es `onResume()`.

## Código en `ConfActivity.kt`

En el `activity` de configuración lo primero que hago es crear las variables para acceder a `SharedPreferences`, creo una para cada dato que quiero manejar.

```java
// Acceder a SharedPreferences
val sharedFich = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
val phoneKey = getString(R.string.string_phone)
val ubiKey = getString(R.string.string_ubi)
```

Precargo toda la información de los check, teléfono y ubicación pero no de los radios.

```java
//Precargar valores guardados en los EditText
val savedPhone = sharedFich.getString(phoneKey, "")
val savedUbi = sharedFich.getString(ubiKey, "")
binding.editPhone.setText(savedPhone)
binding.editUbi.setText(savedUbi)

//Precargar estado de los CheckBox
val mostrarCalculadora = sharedFich.getBoolean("mostrar_calculadora", true)
val mostrarInformacion = sharedFich.getBoolean("mostrar_informacion", true)
binding.checkCalculadora.isChecked = mostrarCalculadora
binding.checkInformacion.isChecked = mostrarInformacion
```

Luego procedo a añadir un `listener` al botón que hay en `conf`, creo variables para ir guardando los datos a manejar que ingresa el usuario, valido todos los datos introducidos y guardo todo en `SharedPreferences`.
A parte también guardo el modo seleccionado ya sea chiste, dados o ninguno.

```java
/** Botón para confirmar y guardar la configuración */
binding.btnConf.setOnClickListener {
    val numberPhone = binding.editPhone.text.toString()
    val ubicacion = binding.editUbi.text.toString()

    if (numberPhone.isEmpty()) {
        Toast.makeText(this, "Número vacío", Toast.LENGTH_LONG).show()
    } else if (!isValidPhoneNumber(numberPhone)) {
        Toast.makeText(this, "Número no válido", Toast.LENGTH_LONG).show()
    } else if (ubicacion.isEmpty()) {
        Toast.makeText(this, "Ubicación vacía", Toast.LENGTH_LONG).show()
    } else {
        val edit = sharedFich.edit()
        edit.putString(phoneKey, numberPhone)
        edit.putString(ubiKey, ubicacion)

        // Guardar el modo seleccionado
        val selectedId = binding.radioGroupModo.checkedRadioButtonId
        when (selectedId) {
            R.id.radio_dados -> edit.putString("modo", "dados")
            R.id.radio_chistes -> edit.putString("modo", "chistes")
                    else -> edit.putString("modo", "ninguno")
        }

        edit.apply()
        startMainActivity(numberPhone)
    }
}
```

También añadimos los listener a los check para que se guarden los cambios.

```java
// Guardar cambios de los CheckBox al instante
binding.checkCalculadora.setOnCheckedChangeListener { _, isChecked ->
    sharedFich.edit().putBoolean("mostrar_calculadora", isChecked).apply()
}

binding.checkInformacion.setOnCheckedChangeListener { _, isChecked ->
    sharedFich.edit().putBoolean("mostrar_informacion", isChecked).apply()
}
```

Este es un método para validar el número de teléfono que ingresa el usuario. Realmente lo podía haber hecho para el resto de comprobaciones de los otros campos.

```java
/** Valida el formato del número de teléfono */
private fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.length in 9..15 && phoneNumber.all { it.isDigit() || it == '+' } // Permitir dígitos y el símbolo '+'
}
```

Tenemos esta función para resumir el estado del activity y ocultar la barra de estado y navegación.

```java
override fun onResume() {
    super.onResume()

    // Ocultar barra de estado y navegación con WindowInsetsController
    window.insetsController?.let { controller ->
        controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
```

## Código en `Phone.kt`

Lo primero es comprobar si el número esta vacío y acto seguido llama al teléfono con un Intent implícito. Si el teléfono estuviera vacío lanzaría un Toast avisándole al usuario.

```java
// Botón para realizar la llamada
binding.btnPhone.setOnClickListener {
    if (!phoneSOS.isNullOrEmpty()) { // Verificar que el número de teléfono no sea nulo o vacío
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneSOS")
        }
        startActivity(intent)
    } else { // Mostrar un mensaje si el número de teléfono no está disponible
        Toast.makeText(this, "Número de teléfono no disponible", Toast.LENGTH_LONG).show()
    }
}
```

También hay un botón para ir al MainActivity.

```java
// Botón para ir al MainActivity
binding.btnGotoMain.setOnClickListener {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
}
```

## Código en `MainActivity.kt`

Lo primero que hago es obtener el teléfono y la hora de la alarma.

```java
// Obtener SharedPreferences y claves
val prefs = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)
val phoneKey = getString(R.string.string_phone)
```

Ahora obtengo los booleanos para saber si mostrar botones o no.

```java
// Establecer visibilidad de botones según las preferencias
val mostrarCalculadora = prefs.getBoolean("mostrar_calculadora", true)
val mostrarInformacion = prefs.getBoolean("mostrar_informacion", true)
binding.btnCalculadora.visibility = if (mostrarCalculadora) android.view.View.VISIBLE else android.view.View.GONE
binding.btnInfo.visibility = if (mostrarInformacion) android.view.View.VISIBLE else android.view.View.GONE
```

Ahora añado el listener al botón de llamada, que lo primero que hace es verificar si se han aceptado los permisos para hacer la propia llamada y si no, los pide.

```java
/** Botón para ir al activity phone */
binding.btnLlamada.setOnClickListener {
    val updatedPhone = prefs.getString(phoneKey, "") ?: ""

    if (isPermissionCall()) { // Verificar si el permiso de llamada está concedido
        val intent = Intent(this, Phone::class.java)
        intent.putExtra(phoneKey, updatedPhone) // Pasar el número de teléfono actualizado
        startActivity(intent)
    } else { // Solicitar permiso de llamada si no está concedido
        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 100)
    }
}
```

Los permisos también se pueden pedir de otra forma, con el código de la imagen de abajo los permisos se piden al iniciar la aplicación, pero me gustaba más la otra forma ya que así solo se pedirían los permisos al llamar.

```java
// Solicitar permiso de llamada en tiempo de ejecución si no está concedido, prefiero utilizar el que nos dio en clase que es "isPermisionCall()" para no liarla mucho.

if (android.os.Build.VERSION.SDK_INT >= android.os.Buid.VERSION_CODES.M) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 100)
        }
        }
```

También añado el listener del botón de la busqueda en internet, en este simplemente lanzo un Intent implícito que busca un link.

```java
/** Botón para abrir URL */
binding.btnUrl.setOnClickListener {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/titin04/"))
    startActivity(intent)
}
```

Añadimos que se pueda poner una alarma en 2 minutos

```java
/** Botón para configurar alarma con hora actual + 2 minutos */
binding.btnAlarm.setOnClickListener {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 2) // Sumar 2 minutos
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
        putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minutes)
        putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "Alarma en 2 minutos")
        putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
    }
    startActivity(intent)
}
```

Añado el botón para buscar la ubicación en google maps, antes pasada por el Activity de configuración. Lo hago con un Intent implícito y con unas comprobaciones antes.

```java
/** Botón para mostrar ubicación elegida en el mapa */
binding.btnUbicacion.setOnClickListener {
    // Obtener la ubicación guardada en SharedPreferences
    val ubiKey = getString(R.string.string_ubi)
    val ubicacion = prefs.getString(ubiKey, null)

    if (!ubicacion.isNullOrEmpty()) { // Verificar si la ubicación está configurada
        val intent = Intent(Intent.ACTION_VIEW).apply { // Crear intención para ver ubicación
            data = Uri.parse("geo:0,0?q=${Uri.encode(ubicacion)}") // Formatear la ubicación para la URI
        }
        startActivity(intent)
    } else { // Mostrar mensaje si la ubicación no está configurada
        Toast.makeText(this, "Ubicación no configurada", Toast.LENGTH_LONG).show()
    }
}
```

Añado el listener de chistes, solo se puede acceder si el modo está configurado en Chistes, si no muestra un Toast.

```java
/** Botón para ir a la actividad de chistes */
binding.btnChistes.setOnClickListener {
    val modo = prefs.getString("modo", "ninguno")
    if (modo == "chistes") {
        startActivity(Intent(this, Chistes::class.java))
    } else {
        Toast.makeText(this, "Activa el modo Chistes en configuración", Toast.LENGTH_SHORT).show()
    }
}
```

Añado el listener de dados, solo se puede acceder si el modo está configurado en Dados, si no muestra un Toast.

```java
/** Botón para ir a la actividad de dados */
    binding.btnDados.setOnClickListener {
    val modo = prefs.getString("modo", "ninguno")
    if (modo == "dados") {
        startActivity(Intent(this, Dados::class.java))
    } else {
        Toast.makeText(this, "Activa el modo Dados en configuración", Toast.LENGTH_SHORT).show()
    }
}
```

Añado el listener de calculadora.

```java
/** Botón para ir a la actividad de calculadora */
binding.btnCalculadora.setOnClickListener {
    val intent = Intent(this, Calculadora::class.java)
    startActivity(intent)
}
```

Tengo también el botón para ir a la configuración, me acabo de dar cuenta de que el nombre no es muy correcto debería ser algo como "btnConf", con un Intent explícito te redirige al Activity de configuración.

```java
/** Botón para ir a la configuración */
binding.btnLlamadaConf.setOnClickListener {
    val intent = Intent(this, ConfActivity::class.java)
    startActivity(intent)
}
```

Por último en botones tengo el de info que simplemente con un Intent explícito te redirige a un Activity de información.

```java
/** Botón para ir al activity info */
binding.btnInfo.setOnClickListener {
    val intent = Intent(this, Info::class.java)
    startActivity(intent)
}
```

En el método onResume() he puesto para cada vez que se reanude el activity se compruebe si se debe mostrar calculadora e info.

```java
override fun onResume() {
    super.onResume()

    val prefs = getSharedPreferences(getString(R.string.shared_prefs_file), Context.MODE_PRIVATE)

    val mostrarCalculadora = prefs.getBoolean("mostrar_calculadora", true)
    val mostrarInformacion = prefs.getBoolean("mostrar_informacion", true)

    binding.btnCalculadora.visibility = if (mostrarCalculadora) android.view.View.VISIBLE else android.view.View.GONE
    binding.btnInfo.visibility = if (mostrarInformacion) android.view.View.VISIBLE else android.view.View.GONE
}
```

Aquí tengo un manejador de permisos que analiza la solicitud de estos, simplemente muestra un Toast si el usuario acepta y otro si deniega, código extraido de [stackoverflow](https://stackoverflow.com/questions/65610114/do-i-have-to-call-super-onrequestpermissionsresultrequestcode-permissions-gra).

```java
/** Maneja el resultado de la solicitud de permisos */
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Siempre llama al método de la superclase

    if (requestCode == 100) { // Verificar si el código de solicitud coincide
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permiso concedido
            Toast.makeText(this, "Permiso de llamada concedido", Toast.LENGTH_SHORT).show()
        } else { // Permiso denegado
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_LONG).show()
        }
    }
}
```

Por último en el Main. tengo una función que se asegura si el permiso existe, pero, si la version API es menor a 23 lo omite porque en versiones menores a esta no hacen falta permisos.

```java
/** Verifica si el permiso de llamada está concedido */
private fun isPermissionCall(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
        true // Permiso concedido automáticamente en versiones anteriores a Marshmallow
    } else { // Verificar el permiso en tiempo de ejecución
        checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }
}
```

## Código en `Chistes.kt`

Lo único que he cambiado en chistes ha sido que ahora los chistes los coge de un array.

```java
private fun initEvent() {
binding.btnExample.setOnClickListener {
    val currentTime = System.currentTimeMillis()
    if (currentTime - touchLastTime < TOUCH_MAX_TIME) {
        // Elegimos un chiste aleatorio del array
        val chistes = resources.getStringArray(R.array.chistes_array)
        val chisteAleatorio = chistes.random()
        executorDoubleTouch(chisteAleatorio)
        Log.i(MYTAG, "Escuchamos el chiste: $chisteAleatorio")
    } else {
        Log.i(MYTAG, "Hemos pulsado 1 vez.")
        speakMeDescription("Botón para escuchar un chiste")
    }
    touchLastTime = currentTime
}
```

## Código en `Dados.kt`

Aquí he creado un array para los retos.

```java
private val mensajes = arrayOf(
        "3 → ¡Haz una voltereta!",
        "4 → Cuenta un chiste malo",
        "5 → Baila 10 segundos",
        "6 → Envía un emoji a un amigo",
        "7 → Haz 5 flexiones",
        "8 → Canta una canción corta",
        "9 → Imita a un animal",
        "10 → Haz una pose divertida",
        "11 → Di tu comida favorita",
        "12 → Haz 3 sentadillas",
        "13 → Aplaude fuerte 5 veces",
        "14 → Di un trabalenguas",
        "15 → Haz una cara graciosa",
        "16 → Da una vuelta sobre ti mismo",
        "17 → Di tu número de la suerte",
        "18 → ¡Premio! Puedes descansar 🎉"
)
```

Y el viewResult() que muestra el mensaje.

```java
private fun viewResult() {
    val index = sum - 3 // porque el array empieza en 0 y la suma mínima es 3
    val mensaje = mensajes[index]
    binding.txtResultado.text = mensaje
}
```

## Código en `Info.kt`

En este Activity lo único que tengo es un botón para volver al Main.

```java
/** Botón para ir al MainActivity */
binding.btnGotoMain.setOnClickListener {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
}
```

## Código en `AndroidManifest.xml`

En el Manifest es necesario aplicar los permisos, según muchos foros el permiso del reloj no es necesario, pero si no se pone la aplicación crashea.

```xml
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="com.android.alarm.permission.SET_ALARM" />
```

## Código en `strings.xml` y `colors.xml`

Aquí guardo las cadenas utilizadas de forma gráfica en el proyecto.

```xml
<!-- Cadenas de texto de la aplicación -->
<string name="app_name">ActividadEvaluableTema1</string>
<string name="text_conf_title">Configuración de la aplicación</string>
<string name="text_conf_phone">Configurar número SOS</string>
<string name="text_conf_phone_selector">Número de teléfono:</string>
<string name="text_conf_btn_accept">Aceptar</string>
<string name="text_SOS">Llamar SOS</string>
<string name="text_conf_alarm">Configurar alarma</string>
<string name="text_conf_alarm_hour">Hora de la alarma:</string>
<string name="text_conf_alarm_minutes">Minutos de la alarma:</string>
<string name="text_conf_ubi_title">Configurar ubicación</string>
<string name="text_conf_ubi">Introducir ubicación:</string>
<string name="text_info_title">Uso de la aplicación:</string>
<string name="text_goto_main">Volver al inicio</string>
<string name="text_info_content">
Esta aplicación permite realizar una llamada de emergencia a un número predefinido,
configurar una alarma y buscar una ubicación. Utiliza los botones en la pantalla
principal para acceder a cada función. Asegúrate de configurar el número de teléfono SOS,
la hora de la alarma y la ubicación antes de usar estas funciones.</string>
```

Y aquí las utilizadas en las variables y en SharedPreferences.

```xml
<!-- Fichero de preferencias compartidas -->
<string name="name_preferen_shared_fich">shared_fich</string>

    <!-- Claves de las preferencias compartidas -->
<string name="name_shared_phone">phone_number</string>
<string name="msg_empty_phone">Debes introducir un teléfono válido</string>
<string name="msg_not_valid_phone">Teléfono no válido</string>
<string name="msg_new_phone">Debes poner un nuevo teléfono</string>
<string name="shared_prefs_file">sharedPrefs</string>
<string name="string_phone">phone</string>
<string name="hour_alarm">horaAlarma</string>
<string name="minutes_alarm">minutosAlarma</string>
<string name="string_ubi">ubi</string>
```

Y aquí el array utilizado en chistes.

```xml
<!-- Chistes -->
<string-array name="chistes_array">
    <item>¿Cuál es el animal más antiguo? La cebra, porque está en blanco y negro.</item>
    <item>¿Qué le dice una impresora a otra? ¿Esa hoja es tuya o es una copia?</item>
    <item>¿Por qué las focas miran siempre hacia arriba? ¡Porque ahí están los focos!</item>
    <item>¿Qué hace una abeja en el gimnasio? ¡Zum-ba!</item>
    <item>¿Cuál es el café más peligroso del mundo? El ex-preso.</item>
    <item>¿Qué le dice un semáforo a otro? No me mires, me estoy cambiando.</item>
    <item>¿Por qué el libro de matemáticas estaba triste? Porque tenía demasiados problemas.</item>
    <item>¿Qué hace una vaca cuando sale el sol? Sombra.</item>
    <item>¿Cuál es el colmo de un electricista? Que no le sigan la corriente.</item>
    <item>¿Qué le dice un techo a otro? Techo de menos.</item>
</string-array>

<string name="describe">Chicos de PMDM del Virgen del Carmen, soy Santi. Este ejemplo, te muestra un botón donde escuchar un chiste</string>
```

Estos son los colores que he utilizado en el proyecto.

```xml
<color name="black">#FF000000</color>
<color name="white">#FFFFFFFF</color>
<color name="color_conf_text_fondo">#300A4B</color>
<color name="color_conf_text">#434243</color>
<color name="color_default_button">#664EA7</color>

<!-- Colores calculadora -->
<color name="color_boton_operador_no_presionado">#BF9E16</color>
<color name="color_boton_top_no_presionado">#9D9797</color>
<color name="color_boton_numero_no_presionado">#383434</color>
```

## Código en `build.gradle.kts (:app)`

En este archivo de configuración he cambiado la versión mínima del SDK que puede utilizar un usuario y he añadido el viewBinding.

```java
defaultConfig {
    applicationId = "com.example.actividadevaluabletema1"
    minSdk = 22
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}

viewBinding{
    enable = true
}
```

## Autor
**Antonio José Marín Rueda** / aka **titin**  
Proyecto evaluable para el módulo de **Programación multimedia y dispositivos móviles**.  
Enlaces a los vídeos: [API21](https://youtu.be/RKl_J27db7k) | [Dispositivo físico](https://youtu.be/x_wPdcSpqaE)  
GitHub: [@titin04](https://github.com/titin04/)
