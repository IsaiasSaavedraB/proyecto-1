package com.example.agencia

import com.example.agencia.data.dao.ContactoDao
import com.example.agencia.data.dao.OportunidadDao
import com.example.agencia.data.dao.OrganizacionDao
import com.example.agencia.data.dao.SeguimientoDao
import com.example.agencia.data.dao.UsuarioDao
import com.example.agencia.data.repository.ContactoRepository
import com.example.agencia.data.repository.OportunidadRepository
import com.example.agencia.data.repository.OrganizacionRepository
import com.example.agencia.data.repository.SeguimientoRepository
import com.example.agencia.data.repository.UsuarioRepository
import com.example.agencia.model.Contacto
import com.example.agencia.model.EstadosOportunidad
import com.example.agencia.model.Oportunidad
import com.example.agencia.model.Organizacion
import com.example.agencia.model.Prioridades
import com.example.agencia.model.Roles
import com.example.agencia.model.Seguimiento
import com.example.agencia.model.TiposEvento
import com.example.agencia.model.TiposGestion
import com.example.agencia.model.Usuario
import com.example.agencia.model.relations.ContactoConOrganizacion
import com.example.agencia.model.relations.OportunidadConDetalles
import com.example.agencia.session.SessionManager
import com.example.agencia.ui.dashboard.DashboardUiState
import com.example.agencia.ui.login.LoginUiState
import com.example.agencia.ui.navigation.Screen
import com.example.agencia.ui.oportunidades.FiltrosOportunidades
import com.example.agencia.ui.oportunidades.OportunidadDetailUiState
import com.example.agencia.ui.oportunidades.OportunidadFormUiState
import com.example.agencia.ui.oportunidades.OportunidadesDetailUiStateWrapper
import com.example.agencia.ui.oportunidades.OportunidadesListUiState
import com.example.agencia.ui.organizaciones.OrganizacionesUiState
import com.example.agencia.ui.registro.RegistroUiState
import com.example.agencia.ui.seguimientos.SeguimientoUiState
import com.example.agencia.ui.supervisor.ResumenEjecutivo
import com.example.agencia.ui.supervisor.SupervisorPanelUiState
import com.example.agencia.ui.theme.ColorError
import com.example.agencia.ui.theme.PrimaryBlue
import com.example.agencia.ui.theme.PrimaryBlueDark
import com.example.agencia.ui.theme.PriorityAlta
import com.example.agencia.ui.theme.SecondaryTeal
import com.example.agencia.ui.theme.StatusConfirmada
import com.example.agencia.ui.theme.StatusNueva
import com.example.agencia.ui.theme.Typography
import com.example.agencia.util.DateUtils
import com.example.agencia.util.passwordsCoinciden
import com.example.agencia.util.validarApellido
import com.example.agencia.util.validarCorreo
import com.example.agencia.util.validarMonto
import com.example.agencia.util.validarNombre
import com.example.agencia.util.validarPassword
import com.example.agencia.util.validarPublico
import com.example.agencia.util.validarRUT
import com.example.agencia.util.validarTelefono
import com.example.agencia.util.validarTextoNoVacio
import com.example.agencia.util.validarTransicionEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// --- Fake DAOs for unit testing com.example.agencia.data, dao, and repository ---

class FakeUsuarioDao : UsuarioDao {
    private val usuarios = mutableListOf<Usuario>()

    override suspend fun insertar(usuario: Usuario): Long {
        val newId = if (usuario.id == 0) (usuarios.size + 1) else usuario.id
        val item = usuario.copy(id = newId)
        usuarios.add(item)
        return newId.toLong()
    }

    override suspend fun insertarTodos(usuarios: List<Usuario>) {
        this.usuarios.addAll(usuarios)
    }

    override suspend fun actualizar(usuario: Usuario) {
        val index = usuarios.indexOfFirst { it.id == usuario.id }
        if (index != -1) usuarios[index] = usuario
    }

    override fun obtenerUsuarios(): Flow<List<Usuario>> = flowOf(usuarios.toList())

    override fun obtenerUsuariosPorRol(rol: String): Flow<List<Usuario>> =
        flowOf(usuarios.filter { it.rol == rol })

    override suspend fun obtenerPorCorreo(correo: String): Usuario? =
        usuarios.find { it.correo == correo }

    override suspend fun obtenerPorId(id: Int): Usuario? =
        usuarios.find { it.id == id }

    override suspend fun contarUsuarios(): Int = usuarios.size
}

class FakeOrganizacionDao : OrganizacionDao {
    private val orgs = mutableListOf<Organizacion>()

    override suspend fun insertar(organizacion: Organizacion): Long {
        val newId = if (organizacion.id == 0) (orgs.size + 1) else organizacion.id
        val item = organizacion.copy(id = newId)
        orgs.add(item)
        return newId.toLong()
    }

    override suspend fun insertarTodas(organizaciones: List<Organizacion>) {
        orgs.addAll(organizaciones)
    }

    override suspend fun actualizar(organizacion: Organizacion) {
        val idx = orgs.indexOfFirst { it.id == organizacion.id }
        if (idx != -1) orgs[idx] = organizacion
    }

    override fun obtenerTodas(): Flow<List<Organizacion>> = flowOf(orgs.toList())

    override suspend fun obtenerPorId(id: Int): Organizacion? = orgs.find { it.id == id }

    override suspend fun contar(): Int = orgs.size
}

class FakeContactoDao : ContactoDao {
    private val contactos = mutableListOf<Contacto>()

    override suspend fun insertar(contacto: Contacto): Long {
        val newId = if (contacto.id == 0) (contactos.size + 1) else contacto.id
        val item = contacto.copy(id = newId)
        contactos.add(item)
        return newId.toLong()
    }

    override suspend fun insertarTodos(contactos: List<Contacto>) {
        this.contactos.addAll(contactos)
    }

    override suspend fun actualizar(contacto: Contacto) {
        val idx = contactos.indexOfFirst { it.id == contacto.id }
        if (idx != -1) contactos[idx] = contacto
    }

    override fun obtenerTodos(): Flow<List<Contacto>> = flowOf(contactos.toList())

    override fun obtenerTodosConOrganizacion(): Flow<List<ContactoConOrganizacion>> = flowOf(emptyList())

    override fun obtenerPorOrganizacion(organizacionId: Int): Flow<List<Contacto>> =
        flowOf(contactos.filter { it.organizacionId == organizacionId })

    override suspend fun obtenerPorId(id: Int): Contacto? = contactos.find { it.id == id }

    override suspend fun contar(): Int = contactos.size
}

class FakeOportunidadDao : OportunidadDao {
    private val ops = mutableListOf<Oportunidad>()

    override suspend fun insertar(oportunidad: Oportunidad): Long {
        val newId = if (oportunidad.id == 0) (ops.size + 1) else oportunidad.id
        val item = oportunidad.copy(id = newId)
        ops.add(item)
        return newId.toLong()
    }

    override suspend fun insertarTodas(oportunidades: List<Oportunidad>) {
        ops.addAll(oportunidades)
    }

    override suspend fun actualizar(oportunidad: Oportunidad) {
        val idx = ops.indexOfFirst { it.id == oportunidad.id }
        if (idx != -1) ops[idx] = oportunidad
    }

    override fun obtenerTodasConDetalles(): Flow<List<OportunidadConDetalles>> = flowOf(emptyList())

    override fun obtenerPorEjecutivoConDetalles(ejecutivoId: Int): Flow<List<OportunidadConDetalles>> = flowOf(emptyList())

    override fun obtenerPorIdConDetalles(id: Int): Flow<OportunidadConDetalles?> = flowOf(null)

    override suspend fun obtenerPorId(id: Int): Oportunidad? = ops.find { it.id == id }

    override suspend fun reasignarEjecutivo(oportunidadId: Int, nuevoEjecutivoId: Int?) {
        val idx = ops.indexOfFirst { it.id == oportunidadId }
        if (idx != -1) ops[idx] = ops[idx].copy(ejecutivoId = nuevoEjecutivoId)
    }

    override suspend fun actualizarEstado(oportunidadId: Int, nuevoEstado: String) {
        val idx = ops.indexOfFirst { it.id == oportunidadId }
        if (idx != -1) ops[idx] = ops[idx].copy(estado = nuevoEstado)
    }

    override suspend fun actualizarFoto(oportunidadId: Int, fotoPath: String?) {
        val idx = ops.indexOfFirst { it.id == oportunidadId }
        if (idx != -1) ops[idx] = ops[idx].copy(fotoPath = fotoPath)
    }

    override suspend fun contar(): Int = ops.size
}

class FakeSeguimientoDao : SeguimientoDao {
    private val segs = mutableListOf<Seguimiento>()

    override suspend fun insertar(seguimiento: Seguimiento): Long {
        val newId = if (seguimiento.id == 0) (segs.size + 1) else seguimiento.id
        val item = seguimiento.copy(id = newId)
        segs.add(item)
        return newId.toLong()
    }

    override suspend fun insertarTodos(seguimientos: List<Seguimiento>) {
        segs.addAll(seguimientos)
    }

    override fun obtenerPorOportunidad(oportunidadId: Int): Flow<List<Seguimiento>> =
        flowOf(segs.filter { it.oportunidadId == oportunidadId })

    override fun obtenerTodos(): Flow<List<Seguimiento>> = flowOf(segs.toList())

    override suspend fun contar(): Int = segs.size
}

/**
 * Pruebas unitarias para los paquetes solicitados de la aplicación CRM Agencia.
 */
class ExampleUnitTest {

    // 1. com.example.agencia.data
    @Test
    fun test_data_package_typealiases() {
        // Verifica que los typealiases de UsuarioDao y UsuarioRepository en com.example.agencia.data resuelvan correctamente.
        val dao: com.example.agencia.data.UsuarioDao = FakeUsuarioDao()
        assertNotNull(dao)
    }

    // 2. com.example.agencia.data.dao
    @Test
    fun test_data_dao_package() = runTest {
        val usuarioDao: UsuarioDao = FakeUsuarioDao()
        val u = Usuario(id = 1, nombre = "Juan", apellido = "Pérez", correo = "juan@test.com", password = "pass", rol = Roles.EJECUTIVO)
        usuarioDao.insertar(u)
        assertEquals(1, usuarioDao.contarUsuarios())

        val busqueda = usuarioDao.obtenerPorCorreo("juan@test.com")
        assertNotNull(busqueda)
        assertEquals("Juan Pérez", busqueda?.nombreCompleto)

        val orgDao: OrganizacionDao = FakeOrganizacionDao()
        orgDao.insertar(Organizacion(id = 1, nombre = "TechCorp", rut = "12.345.678-5", industria = "TI", direccion = "Santiago", telefono = "+56911112222"))
        assertEquals(1, orgDao.contar())

        val contactoDao: ContactoDao = FakeContactoDao()
        contactoDao.insertar(Contacto(id = 1, organizacionId = 1, nombre = "Maria", cargo = "Jefa", correo = "m@tech.cl", telefono = "+56933334444"))
        assertEquals(1, contactoDao.contar())

        val opDao: OportunidadDao = FakeOportunidadDao()
        opDao.insertar(Oportunidad(id = 1, titulo = "Op 1", organizacionId = 1, contactoId = 1, montoEstimado = 100.0, fechaEvento = "2026-05-01", ubicacion = "Stgo", tipoEvento = TiposEvento.FERIA, publicoEsperado = 50, fechaCreacion = "2026-01-01"))
        assertEquals(1, opDao.contar())

        val segDao: SeguimientoDao = FakeSeguimientoDao()
        segDao.insertar(Seguimiento(id = 1, oportunidadId = 1, usuarioId = 1, fechaHora = "2026-05-02 10:00", tipoGestion = TiposGestion.LLAMADA, descripcion = "Llamada inicial"))
        assertEquals(1, segDao.contar())
    }

    // 3. com.example.agencia.data.repository
    @Test
    fun test_data_repository_package() = runTest {
        val usuarioDao = FakeUsuarioDao()
        val usuarioRepo = UsuarioRepository(usuarioDao)
        val u = Usuario(id = 0, nombre = "Ana", apellido = "Silva", correo = "ana@test.com", password = "123", rol = Roles.SUPERVISOR)
        val id = usuarioRepo.guardar(u)
        assertTrue(id > 0)
        val encontrado = usuarioRepo.buscarPorCorreo("ana@test.com")
        assertNotNull(encontrado)
        assertTrue(encontrado?.esSupervisor == true)

        val orgDao = FakeOrganizacionDao()
        val orgRepo = OrganizacionRepository(orgDao)
        val orgId = orgRepo.guardar(Organizacion(nombre = "Empresa X", rut = "12.345.678-5", industria = "Minería", direccion = "Norte", telefono = "+56911112222"))
        assertTrue(orgId > 0)
        assertNotNull(orgRepo.buscarPorId(orgId.toInt()))

        val contactoDao = FakeContactoDao()
        val contactoRepo = ContactoRepository(contactoDao)
        val cId = contactoRepo.guardar(Contacto(organizacionId = orgId.toInt(), nombre = "Pedro", cargo = "Analista", correo = "p@x.cl", telefono = "+56922223333"))
        assertTrue(cId > 0)
        assertNotNull(contactoRepo.buscarPorId(cId.toInt()))

        val opDao = FakeOportunidadDao()
        val opRepo = OportunidadRepository(opDao)
        val opId = opRepo.guardar(Oportunidad(titulo = "Op Repo", organizacionId = orgId.toInt(), contactoId = cId.toInt(), montoEstimado = 5000.0, fechaEvento = "2026-06-01", ubicacion = "Stgo", tipoEvento = TiposEvento.CONFERENCIA, publicoEsperado = 10, fechaCreacion = "2026-01-01"))
        assertTrue(opId > 0)
        assertNotNull(opRepo.buscarPorId(opId.toInt()))
        opRepo.actualizarEstado(opId.toInt(), EstadosOportunidad.CONFIRMADA)
        assertEquals(EstadosOportunidad.CONFIRMADA, opRepo.buscarPorId(opId.toInt())?.estado)

        val segDao = FakeSeguimientoDao()
        val segRepo = SeguimientoRepository(segDao)
        val sId = segRepo.guardar(Seguimiento(oportunidadId = opId.toInt(), usuarioId = id.toInt(), fechaHora = "2026-06-02 12:00", tipoGestion = TiposGestion.CORREO, descripcion = "Mail de seguimiento"))
        assertTrue(sId > 0)
        assertEquals(1, segRepo.obtenerPorOportunidad(opId.toInt()).first().size)
    }

    // 4. com.example.agencia.model
    @Test
    fun test_model_package() {
        val ejecutivo = Usuario(id = 1, nombre = "Pedro", apellido = "Rojas", correo = "p@test.com", password = "123", rol = Roles.EJECUTIVO)
        assertEquals("Pedro Rojas", ejecutivo.nombreCompleto)
        assertFalse(ejecutivo.esSupervisor)

        val supervisor = Usuario(id = 2, nombre = "Laura", apellido = "Mendoza", correo = "l@test.com", password = "123", rol = Roles.SUPERVISOR)
        assertTrue(supervisor.esSupervisor)

        assertEquals(6, EstadosOportunidad.TODOS.size)
        assertTrue(EstadosOportunidad.TODOS.contains(EstadosOportunidad.NUEVA))

        assertEquals(3, Prioridades.TODAS.size)
        assertTrue(Prioridades.TODAS.contains(Prioridades.ALTA))

        assertEquals(5, TiposEvento.TODOS.size)
        assertTrue(TiposEvento.TODOS.contains(TiposEvento.CONFERENCIA))

        assertEquals(4, TiposGestion.TODOS.size)
        assertTrue(TiposGestion.TODOS.contains(TiposGestion.LLAMADA))
    }

    // 5. com.example.agencia.model.relations
    @Test
    fun test_model_relations_package() {
        val org = Organizacion(id = 10, nombre = "TechCorp", rut = "12.345.678-5", industria = "TI", direccion = "Av 1", telefono = "+56911112222")
        val contacto = Contacto(id = 5, organizacionId = 10, nombre = "Carlos", cargo = "Gerente", correo = "c@tech.cl", telefono = "+56922223333")

        val contactoConOrg = ContactoConOrganizacion(contacto = contacto, organizacion = org)
        assertEquals("TechCorp", contactoConOrg.organizacion.nombre)
        assertEquals("Carlos", contactoConOrg.contacto.nombre)

        val op = Oportunidad(
            id = 1, titulo = "Evento Tech", organizacionId = 10, contactoId = 5,
            montoEstimado = 500000.0, fechaEvento = "2026-05-10", ubicacion = "Santiago",
            tipoEvento = TiposEvento.CONFERENCIA, publicoEsperado = 100, fechaCreacion = "2026-01-01"
        )
        val opConDetalles = OportunidadConDetalles(
            oportunidad = op,
            organizacion = org,
            contacto = contacto,
            ejecutivo = null
        )
        assertEquals("Evento Tech", opConDetalles.oportunidad.titulo)
        assertNull(opConDetalles.ejecutivo)
    }

    // 6. com.example.agencia.session
    @Test
    fun test_session_package() {
        SessionManager.cerrarSesion()
        assertFalse(SessionManager.estaAutenticado)
        assertNull(SessionManager.usuarioActualId)
        assertFalse(SessionManager.esSupervisor)

        val user = Usuario(id = 99, nombre = "Mario", apellido = "Bros", correo = "mario@nintendo.com", password = "secretPassword", rol = Roles.SUPERVISOR)
        SessionManager.iniciarSesion(user)

        assertTrue(SessionManager.estaAutenticado)
        assertEquals(99, SessionManager.usuarioActualId)
        assertTrue(SessionManager.esSupervisor)

        SessionManager.cerrarSesion()
        assertFalse(SessionManager.estaAutenticado)
    }

    // 7. com.example.agencia.ui.dashboard
    @Test
    fun test_ui_dashboard_package() {
        val state = DashboardUiState()
        assertTrue(state.cargando)
        assertEquals(0, state.totalOportunidades)
        assertEquals(0.0, state.totalPipelineMonto, 0.001)
        assertNull(state.usuarioActual)
    }

    // 8. com.example.agencia.ui.login
    @Test
    fun test_ui_login_package() {
        val state = LoginUiState(correo = "test@domain.com", password = "password123")
        assertEquals("test@domain.com", state.correo)
        assertEquals("password123", state.password)
        assertFalse(state.errorCorreo)
        assertFalse(state.loginExitoso)
    }

    // 9. com.example.agencia.ui.navigation
    @Test
    fun test_ui_navigation_package() {
        assertEquals("login", Screen.LOGIN)
        assertEquals("registro", Screen.REGISTRO)
        assertEquals("dashboard", Screen.DASHBOARD)
        assertEquals("oportunidades_list", Screen.OPORTUNIDADES_LIST)
        assertEquals("organizaciones", Screen.ORGANIZACIONES)
        assertEquals("supervisor_panel", Screen.SUPERVISOR_PANEL)

        assertEquals("oportunidad_detail/42", Screen.crearOportunidadDetailRoute(42))
        assertEquals("oportunidad_form?id=7", Screen.crearOportunidadFormRoute(7))
        assertEquals("seguimiento/15", Screen.crearSeguimientoRoute(15))
    }

    // 10. com.example.agencia.ui.oportunidades
    @Test
    fun test_ui_oportunidades_package() {
        val detailState = OportunidadDetailUiState()
        assertTrue(detailState.cargando)
        assertFalse(detailState.esSupervisor)

        val wrapper = OportunidadesDetailUiStateWrapper(MutableStateFlow(detailState))
        assertNotNull(wrapper.state)

        val formState = OportunidadFormUiState(titulo = "Feria 2026")
        assertEquals("Feria 2026", formState.titulo)
        assertFalse(formState.esEdicion)

        val listState = OportunidadesListUiState()
        assertTrue(listState.cargando)
        assertEquals("TODAS", listState.estadoFiltro)

        val filtros = FiltrosOportunidades(busqueda = "Tech", estado = EstadosOportunidad.NUEVA)
        assertEquals("Tech", filtros.busqueda)
        assertEquals(EstadosOportunidad.NUEVA, filtros.estado)
    }

    // 11. com.example.agencia.ui.organizaciones
    @Test
    fun test_ui_organizaciones_package() {
        val state = OrganizacionesUiState()
        assertTrue(state.cargando)
        assertFalse(state.mostrarDialogoOrg)
        assertFalse(state.mostrarDialogoContacto)
        assertNull(state.mensajeExito)
    }

    // 12. com.example.agencia.ui.registro
    @Test
    fun test_ui_registro_package() {
        val state = RegistroUiState()
        assertFalse(state.aceptaTerminos)
        assertFalse(state.registroExitoso)
        assertFalse(state.cargando)
    }

    // 13. com.example.agencia.ui.seguimientos
    @Test
    fun test_ui_seguimientos_package() {
        val state = SeguimientoUiState()
        assertTrue(state.cargando)
        assertEquals(TiposGestion.LLAMADA, state.tipoGestion)
        assertFalse(state.actualizarEstado)
    }

    // 14. com.example.agencia.ui.supervisor
    @Test
    fun test_ui_supervisor_package() {
        val ej = Usuario(id = 1, nombre = "Diego", apellido = "Torres", correo = "d@test.cl", password = "pass", rol = Roles.EJECUTIVO)
        val resumen = ResumenEjecutivo(ejecutivo = ej, cantidadOportunidades = 5, montoTotalPipeline = 10000000.0)

        assertEquals(ej, resumen.ejecutivo)
        assertEquals(5, resumen.cantidadOportunidades)
        assertEquals(10000000.0, resumen.montoTotalPipeline, 0.001)

        val state = SupervisorPanelUiState()
        assertTrue(state.cargando)
        assertFalse(state.esSupervisor)
    }

    // 15. com.example.agencia.ui.theme
    @Test
    fun test_ui_theme_package() {
        assertNotNull(PrimaryBlue)
        assertNotNull(PrimaryBlueDark)
        assertNotNull(SecondaryTeal)
        assertNotNull(StatusNueva)
        assertNotNull(StatusConfirmada)
        assertNotNull(PriorityAlta)
        assertNotNull(ColorError)

        assertNotNull(Typography.bodyLarge)
    }

    // 16. com.example.agencia.ui.usuarios
    @Test
    fun test_ui_usuarios_package() = runTest {
        val usuarioDao = FakeUsuarioDao()
        val u1 = Usuario(id = 1, nombre = "User1", apellido = "A", correo = "u1@test.cl", password = "123")
        val u2 = Usuario(id = 2, nombre = "User2", apellido = "B", correo = "u2@test.cl", password = "123")
        usuarioDao.insertarTodos(listOf(u1, u2))

        val repo = UsuarioRepository(usuarioDao)
        val lista = repo.obtenerUsuarios().first()
        assertEquals(2, lista.size)
    }

    // 17. com.example.agencia.util
    @Test
    fun test_util_package_validaciones() {
        assertTrue(validarNombre("Juan"))
        assertFalse(validarNombre(" A"))

        assertTrue(validarApellido("Pérez"))
        assertFalse(validarApellido("P"))

        assertTrue(validarCorreo("usuario@empresa.com"))
        assertFalse(validarCorreo("correo_invalido.com"))

        assertTrue(validarTelefono("+56912345678"))
        assertFalse(validarTelefono("abc"))

        assertTrue(validarRUT("12.345.678-5"))
        assertFalse(validarRUT("123"))

        assertTrue(validarMonto("150000"))
        assertFalse(validarMonto("-500"))
        assertFalse(validarMonto("monto"))

        assertTrue(validarPublico("200"))
        assertFalse(validarPublico("-10"))

        assertTrue(validarTextoNoVacio("Texto válido"))
        assertFalse(validarTextoNoVacio("   "))

        assertTrue(validarPassword("12345678"))
        assertFalse(validarPassword("1234"))

        assertTrue(passwordsCoinciden("secret123", "secret123"))
        assertFalse(passwordsCoinciden("secret123", "different"))

        // validarTransicionEstado business rules
        assertNull(validarTransicionEstado(EstadosOportunidad.NUEVA, EstadosOportunidad.NUEVA, esSupervisor = false))

        val errorEjecutivo = validarTransicionEstado(EstadosOportunidad.NUEVA, EstadosOportunidad.CONFIRMADA, esSupervisor = false)
        assertNotNull(errorEjecutivo)

        val permisoSupervisor = validarTransicionEstado(EstadosOportunidad.NUEVA, EstadosOportunidad.CONFIRMADA, esSupervisor = true)
        assertNull(permisoSupervisor)
    }

    @Test
    fun test_util_package_dateutils() {
        val formattedMonto = DateUtils.formatearMonto(150000.0)
        assertNotNull(formattedMonto)

        val fechaActual = DateUtils.obtenerFechaActual()
        assertTrue(fechaActual.matches(Regex("""^\d{4}-\d{2}-\d{2}$""")))

        val fechaHoraActual = DateUtils.obtenerFechaHoraActual()
        assertTrue(fechaHoraActual.matches(Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$""")))

        val proxima = DateUtils.esFechaProxima("2099-12-31")
        assertNotNull(proxima)

        assertTrue(DateUtils.esGestionAtrasada("2000-01-01 10:00"))
    }
}
