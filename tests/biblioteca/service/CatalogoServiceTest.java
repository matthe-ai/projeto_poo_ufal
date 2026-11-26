package biblioteca.service;

import biblioteca.auth.Sessao;
import biblioteca.exception.*;
import biblioteca.model.material.Livro;
import biblioteca.model.material.MaterialBibliografico;
import biblioteca.model.material.Periodico;
import biblioteca.model.material.TipoPeriodico;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.MaterialRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoServiceTest {

    private static final Path CAPA_VALIDA =
            Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg");

    private static final Path CAPA_INEXISTENTE =
            Path.of(MaterialRepositorio.uploadsDirectory, "arquivo_inexistente.jpg");

    private CatalogoService service;
    private MaterialRepositorio repositorio;
    private AutorizacaoService autorizacaoService;
    private Sessao sessao;
    private Livro livroTeste;

    @BeforeEach
    void setUp() throws Exception {
        repositorio = new MaterialRepositorio();
        autorizacaoService = new AutorizacaoService();
        sessao = new Sessao();
        service = new CatalogoService(repositorio, autorizacaoService, sessao);

        Usuario bibliotecario = new Usuario(
                "Bibliotecário", "bibliotecario", "123456", Cargo.BIBLIOTECARIO
        );

        sessao.iniciarSessao(bibliotecario);

        livroTeste = service.cadastrarLivro(
                "Clean Code",
                2008,
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                CAPA_VALIDA
        );
    }

    private Periodico cadastrarPeriodicoValido(TipoPeriodico tipo) throws Exception {
        return service.cadastrarPeriodico(
                "National Geographic",
                "National Geographic Society",
                150,
                "15/05/2024",
                tipo,
                CAPA_VALIDA
        );
    }

    // CADASTRO DE LIVROS

    @Test
    void deveCadastrarLivroComDadosValidos() throws Exception {
        Livro livro = service.cadastrarLivro(
                "Refactoring",
                1999,
                "Martin Fowler",
                "9780201485677",
                "Addison-Wesley",
                CAPA_VALIDA
        );

        MaterialBibliografico encontrado = service.buscarMaterial(livro.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Livro.class, encontrado);
        assertEquals(livro.getId(), encontrado.getId());
        assertEquals("Refactoring", encontrado.getTitulo());
    }

    @Test
    void deveImpedirCadastroDeLivroComTituloInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        " ",
                        2008,
                        "Robert C. Martin",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComAutorInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        2008,
                        " ",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComIsbnInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        2008,
                        "Robert C. Martin",
                        "",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComEditoraInvalida() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        2008,
                        "Robert C. Martin",
                        "9780132350884",
                        " ",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComAnoInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        0,
                        "Robert C. Martin",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComAnoNegativo() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        -1,
                        "Robert C. Martin",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComAnoFuturo() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        LocalDate.now().getYear() + 1,
                        "Robert C. Martin",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDeLivroComCapaInexistente() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarLivro(
                        "Clean Code",
                        2008,
                        "Robert C. Martin",
                        "9780132350884",
                        "Prentice Hall",
                        CAPA_INEXISTENTE
                ));
    }

    // CADASTRO DE PERIÓDICOS

    @Test
    void deveCadastrarPeriodicoDoTipoJornal() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.JORNAL);

        MaterialBibliografico encontrado = service.buscarMaterial(periodico.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Periodico.class, encontrado);
        assertEquals(periodico.getId(), encontrado.getId());
        assertEquals(TipoPeriodico.JORNAL,
                ((Periodico) encontrado).getTipoPeriodico());
    }

    @Test
    void deveCadastrarPeriodicoDoTipoRevista() throws Exception {
        Periodico periodico = service.cadastrarPeriodico(
                "Revista Ciência Hoje",
                "Instituto Ciência Hoje",
                320,
                "20/06/2024",
                TipoPeriodico.REVISTA,
                CAPA_VALIDA
        );

        MaterialBibliografico encontrado = service.buscarMaterial(periodico.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Periodico.class, encontrado);
        assertEquals(periodico.getId(), encontrado.getId());
        assertEquals(TipoPeriodico.REVISTA,
                ((Periodico) encontrado).getTipoPeriodico());
    }

    @Test
    void deveImpedirCadastroDePeriodicoComTituloInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "",
                        "Editora",
                        10,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComEditoraInvalida() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        " ",
                        10,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComAnoInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComAnoFuturo() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComNumeroDeEdicaoInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        0,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComNumeroDeEdicaoNegativo() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        -1,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComDataDePublicacaoVazia() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        " ",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComFormatoDeDataInvalido() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        "2024-05-15",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComDataInexistente() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        "31/02/24",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComDataFutura() {
        String dataFutura = LocalDate.now()
                .plusDays(1)
                .format(DateTimeFormatter.ofPattern("dd/MM/yy"));

        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        dataFutura,
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoComCapaInexistente() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.cadastrarPeriodico(
                        "Revista Ciência Hoje",
                        "Editora",
                        10,
                        "15/05/24",
                        TipoPeriodico.REVISTA,
                        CAPA_INEXISTENTE
                ));
    }

    // BUSCA E LISTAGEM

    @Test
    void deveBuscarMaterialCadastradoPorId() throws Exception {
        MaterialBibliografico encontrado = service.buscarMaterial(livroTeste.getId());

        assertNotNull(encontrado);
        assertEquals(livroTeste.getId(), encontrado.getId());
    }

    @Test
    void deveImpedirBuscaDeMaterialInexistente() {
        assertThrows(RegistroNaoEncontradoException.class,
                () -> service.buscarMaterial(Integer.MIN_VALUE));
    }

    @Test
    void deveListarTodosOsMateriaisCadastrados() throws Exception {
        List<MaterialBibliografico> materiais = service.listarMateriais();

        assertEquals(1, materiais.size());
        assertTrue(materiais.contains(livroTeste));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverMateriaisCadastrados() throws Exception {
        repositorio.remover(livroTeste.getId());

        List<MaterialBibliografico> materiais = service.listarMateriais();

        assertNotNull(materiais);
        assertTrue(materiais.isEmpty());
    }

    @Test
    void deveListarMateriaisQueCorrespondemAoTitulo() throws Exception {
        List<MaterialBibliografico> materiais =
                service.listarMateriaisPorTitulo("Clean Code");

        assertEquals(1, materiais.size());
        assertTrue(materiais.contains(livroTeste));
    }

    @Test
    void deveRetornarListaVaziaQuandoNenhumTituloCorresponder() throws Exception {
        List<MaterialBibliografico> materiais =
                service.listarMateriaisPorTitulo("Java");

        assertNotNull(materiais);
        assertTrue(materiais.isEmpty());
    }

    // ATUALIZAÇÃO DE LIVROS

    @Test
    void deveAtualizarLivroCadastrado() throws Exception {
        service.atualizarLivro(
                "Clean Code - Nova edição",
                2008,
                "Prentice Hall",
                CAPA_VALIDA,
                "Robert C. Martin",
                "9780132350884",
                livroTeste.getId()

        );

        MaterialBibliografico encontrado = service.buscarMaterial(livroTeste.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Livro.class, encontrado);
        assertEquals(livroTeste.getId(), encontrado.getId());
        assertEquals("Clean Code - Nova edição", encontrado.getTitulo());
    }

    @Test
    void deveImpedirAtualizacaoDeLivroComDadosInvalidos() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> service.atualizarLivro(
                        "",
                        2008,
                        "Prentice Hall",
                        CAPA_VALIDA,
                        "Robert C. Martin",
                        "9780132350884",
                        livroTeste.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDeLivroComCapaInexistente() {
        assertThrows(CredenciaisInvalidasException.class,
                () ->  service.atualizarLivro(
                        "Clean Code - Nova edição",
                        2008,
                        "Prentice Hall",
                        CAPA_INEXISTENTE,
                        "Robert C. Martin",
                        "9780132350884",
                        livroTeste.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDeLivroComTipoDeMaterialIncorreto() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.JORNAL);

        assertThrows(TipoMaterialInvalidoException.class,
                () ->  service.atualizarLivro(
                        "Clean Code - Nova edição",
                        2008,
                        "Prentice Hall",
                        CAPA_VALIDA,
                        "Robert C. Martin",
                        "9780132350884",
                        periodico.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDeLivroInexistente() {
        assertThrows(RegistroNaoEncontradoException.class,
                () ->  service.atualizarLivro(
                        "Clean Code - Nova edição",
                        2008,
                        "Prentice Hall",
                        CAPA_VALIDA,
                        "Robert C. Martin",
                        "9780132350884",
                        Integer.MIN_VALUE
                ));
    }

    // ATUALIZAÇÃO DE PERIÓDICOS

    @Test
    void deveAtualizarPeriodicoDoTipoJornalSemAlterarSeuTipo() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.JORNAL);

        service.atualizarPeriodico(
                "National Geographic - Nova edição",
                "National Geographic Society",
                CAPA_VALIDA,
                151,
                "15/06/2025",
                periodico.getId()
        );

        MaterialBibliografico encontrado = service.buscarMaterial(periodico.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Periodico.class, encontrado);
        assertEquals(periodico.getId(), encontrado.getId());
        assertEquals("National Geographic - Nova edição", encontrado.getTitulo());
        assertEquals(TipoPeriodico.JORNAL,
                ((Periodico) encontrado).getTipoPeriodico());
    }

    @Test
    void deveAtualizarPeriodicoDoTipoRevistaSemAlterarSeuTipo() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);

        service.atualizarPeriodico(
                "Revista Ciência Hoje - Nova edição",
                "National Geographic Society",
                CAPA_VALIDA,
                151,
                "15/06/2025",
                periodico.getId()
        );

        MaterialBibliografico encontrado = service.buscarMaterial(periodico.getId());

        assertNotNull(encontrado);
        assertInstanceOf(Periodico.class, encontrado);
        assertEquals("Revista Ciência Hoje - Nova edição", encontrado.getTitulo());
        assertEquals(TipoPeriodico.REVISTA,
                ((Periodico) encontrado).getTipoPeriodico());
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoComTipoDeMaterialIncorreto() {
        assertThrows(TipoMaterialInvalidoException.class,
                () ->  service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        151,
                        "15/06/2025",
                        livroTeste.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoInexistente() {
        assertThrows(RegistroNaoEncontradoException.class,
                () ->  service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        151,
                        "15/06/2025",
                        Integer.MIN_VALUE
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoComDadosInvalidos() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);

        assertThrows(CredenciaisInvalidasException.class,
                () ->  service.atualizarPeriodico(
                        " ",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        151,
                        "15/06/2025",
                        periodico.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoComNumeroDeEdicaoInvalido() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);

        assertThrows(CredenciaisInvalidasException.class,
                () ->  service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        0,
                        "15/06/2025",
                        periodico.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoComDataInvalida() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);

        assertThrows(CredenciaisInvalidasException.class,
                () ->  service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        151,
                        "31/02/2024",
                        periodico.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoComCapaInexistente() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);

        assertThrows(CredenciaisInvalidasException.class,
                () ->  service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_INEXISTENTE,
                        151,
                        "15/06/2025",
                        periodico.getId()
                ));
    }

    // REMOÇÃO

    @Test
    void deveRemoverMaterialDisponivelCadastrado() throws Exception {
        service.removerMaterial(livroTeste.getId());

        assertTrue(service.listarMateriais().isEmpty());
    }

    @Test
    void deveImpedirRemocaoDeMaterialInexistente() {
        assertThrows(RegistroNaoEncontradoException.class,
                () -> service.removerMaterial(Integer.MIN_VALUE));
    }

    @Test
    void deveImpedirRemocaoDeMaterialIndisponivel() {
        livroTeste.marcarComoIndisponivel();

        assertThrows(MaterialIndisponivelException.class,
                () -> service.removerMaterial(livroTeste.getId()));

        assertDoesNotThrow(() -> repositorio.buscarPorId(livroTeste.getId()));
    }

    // AUTENTICAÇÃO

    @Test
    void deveImpedirBuscaDeMaterialSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(UsuarioNaoAutenticadoException.class,
                () -> service.buscarMaterial(livroTeste.getId()));
    }

    @Test
    void deveImpedirListagemDeMateriaisSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(UsuarioNaoAutenticadoException.class,
                () -> service.listarMateriais());
    }

    @Test
    void deveImpedirCadastroDeLivroSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(UsuarioNaoAutenticadoException.class,
                () -> service.cadastrarLivro(
                        "Livro",
                        2024,
                        "Autor",
                        "9780132350884",
                        "Editora",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(UsuarioNaoAutenticadoException.class,
                () -> service.cadastrarPeriodico(
                        "Revista",
                        "Editora",
                        10,
                        "15/05/2024",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirRemocaoDeMaterialSemAutenticacao() {
        sessao.encerrarSessao();

        assertThrows(UsuarioNaoAutenticadoException.class,
                () -> service.removerMaterial(livroTeste.getId()));
    }

    // AUTORIZAÇÃO

    @Test
    void deveImpedirCadastroDeLivroPorUsuarioSemPermissao() {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.cadastrarLivro(
                        "Livro",
                        2024,
                        "Autor",
                        "9780132350884",
                        "Editora",
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirCadastroDePeriodicoPorUsuarioSemPermissao() {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.cadastrarPeriodico(
                        "Revista",
                        "Editora",
                        10,
                        "15/05/2024",
                        TipoPeriodico.REVISTA,
                        CAPA_VALIDA
                ));
    }

    @Test
    void deveImpedirAtualizacaoDeLivroPorUsuarioSemPermissao() {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        assertThrows(OperacaoNaoPermitidaException.class,
                () ->  service.atualizarLivro(
                        "Clean Code - Nova edição",
                        2008,
                        "Prentice Hall",
                        CAPA_VALIDA,
                        "Robert C. Martin",
                        "9780132350884",
                        livroTeste.getId()
                ));
    }

    @Test
    void deveImpedirAtualizacaoDePeriodicoPorUsuarioSemPermissao() throws Exception {
        Periodico periodico = cadastrarPeriodicoValido(TipoPeriodico.REVISTA);
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.atualizarPeriodico(
                        "National Geographic - Nova edição",
                        "National Geographic Society",
                        CAPA_VALIDA,
                        151,
                        "15/06/2025",
                        periodico.getId()
                ));
    }

    @Test
    void deveImpedirRemocaoDeMaterialPorUsuarioSemPermissao() {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.removerMaterial(livroTeste.getId()));
    }

    @Test
    void devePermitirConsultaDeCatalogoPorUsuarioLeitor() throws Exception {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        MaterialBibliografico encontrado = service.buscarMaterial(livroTeste.getId());

        assertNotNull(encontrado);
        assertEquals(livroTeste.getId(), encontrado.getId());
    }

    @Test
    void devePermitirListagemDeCatalogoPorUsuarioLeitor() throws Exception {
        Usuario leitor = new Usuario("Leitor", "leitor", "123456", Cargo.LEITOR);
        sessao.iniciarSessao(leitor);

        List<MaterialBibliografico> materiais = service.listarMateriais();

        assertEquals(1, materiais.size());
        assertTrue(materiais.contains(livroTeste));
    }
}