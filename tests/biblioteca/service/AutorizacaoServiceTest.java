package biblioteca.service;

import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Permissao;
import biblioteca.model.usuario.Usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AutorizacaoServiceTest {

    private AutorizacaoService autorizacaoService;

    private Usuario administrador;
    private Usuario bibliotecario;
    private Usuario leitor;

    @BeforeEach
    void setUp() {
        autorizacaoService = new AutorizacaoService();

        administrador = new Usuario(
                "Admin",
                "admin",
                "123456",
                Cargo.ADMINISTRADOR
        );

        bibliotecario = new Usuario(
                "João",
                "joao",
                "123456",
                Cargo.BIBLIOTECARIO
        );

        leitor = new Usuario(
                "Maria",
                "maria",
                "123456",
                Cargo.LEITOR
        );
    }

    // ADMINISTRADOR

    @Test
    void administradorDeveTerTodasAsPermissoes() {
        for (Permissao permissao : Permissao.values()) {
            assertDoesNotThrow(
                    () -> autorizacaoService.exigirPermissao(
                            administrador,
                            permissao
                    ),
                    "Administrador deveria possuir a permissão: " + permissao
            );
        }
    }

    // BIBLIOTECARIO

    @Test
    void bibliotecarioDeveTerPermissoesDeGerenciamento() {
        Set<Permissao> permissoesEsperadas = EnumSet.of(
                Permissao.GERENCIAR_CATALOGO,
                Permissao.REGISTRAR_EMPRESTIMO,
                Permissao.REGISTRAR_DEVOLUCAO,
                Permissao.CONSULTAR_CATALOGO,
                Permissao.CONSULTAR_TODOS_EMPRESTIMOS,
                Permissao.CONSULTAR_USUARIOS
        );

        for (Permissao permissao : permissoesEsperadas) {
            assertDoesNotThrow(
                    () -> autorizacaoService.exigirPermissao(
                            bibliotecario,
                            permissao
                    ),
                    "Bibliotecário deveria possuir a permissão: " + permissao
            );
        }
    }

    @Test
    void bibliotecarioNaoDeveTerPermissoesRestritas() {
        Set<Permissao> permissoesNegadas = EnumSet.of(
                Permissao.CADASTRAR_USUARIO,
                Permissao.REMOVER_USUARIO,
                Permissao.ATUALIZAR_USUARIOS
        );

        for (Permissao permissao : permissoesNegadas) {
            assertThrows(
                    OperacaoNaoPermitidaException.class,
                    () -> autorizacaoService.exigirPermissao(
                            bibliotecario,
                            permissao
                    ),
                    "Bibliotecário não deveria possuir a permissão: " + permissao
            );
        }
    }

    // LEITOR

    @Test
    void leitorDeveTerPermissoesDeConsulta() {
        Set<Permissao> permissoesEsperadas = EnumSet.of(
                Permissao.CONSULTAR_CATALOGO,
                Permissao.CONSULTAR_PROPRIOS_EMPRESTIMOS
        );

        for (Permissao permissao : permissoesEsperadas) {
            assertDoesNotThrow(
                    () -> autorizacaoService.exigirPermissao(
                            leitor,
                            permissao
                    ),
                    "Leitor deveria possuir a permissão: " + permissao
            );
        }
    }

    @Test
    void leitorNaoDeveTerPermissoesDeGerenciamento() {
        Set<Permissao> permissoesNegadas = EnumSet.of(
                Permissao.CADASTRAR_USUARIO,
                Permissao.REMOVER_USUARIO,
                Permissao.ATUALIZAR_USUARIOS,
                Permissao.GERENCIAR_CATALOGO,
                Permissao.REGISTRAR_EMPRESTIMO,
                Permissao.REGISTRAR_DEVOLUCAO,
                Permissao.CONSULTAR_TODOS_EMPRESTIMOS,
                Permissao.CONSULTAR_USUARIOS
        );

        for (Permissao permissao : permissoesNegadas) {
            assertThrows(
                    OperacaoNaoPermitidaException.class,
                    () -> autorizacaoService.exigirPermissao(
                            leitor,
                            permissao
                    ),
                    "Leitor não deveria possuir a permissão: " + permissao
            );
        }
    }

    @Test
    void bibliotecarioDeveTerPermissoesDeGerenciamentoDeMultas() {
        assertDoesNotThrow(
                () -> autorizacaoService.exigirPermissao(
                        bibliotecario,
                        Permissao.GERENCIAR_MULTAS
                )
        );

        assertDoesNotThrow(
                () -> autorizacaoService.exigirPermissao(
                        bibliotecario,
                        Permissao.CONSULTAR_TODAS_MULTAS
                )
        );
    }

    @Test
    void bibliotecarioDeveConsultarPropriasMultas() {
        assertDoesNotThrow(
                () -> autorizacaoService.exigirPermissao(
                        bibliotecario,
                        Permissao.CONSULTAR_PROPRIAS_MULTAS
                )
        );
    }

    @Test
    void leitorDeveConsultarPropriasMultas() {
        assertDoesNotThrow(
                () -> autorizacaoService.exigirPermissao(
                        leitor,
                        Permissao.CONSULTAR_PROPRIAS_MULTAS
                )
        );
    }

    @Test
    void leitorNaoDeveGerenciarMultas() {
        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> autorizacaoService.exigirPermissao(
                        leitor,
                        Permissao.GERENCIAR_MULTAS
                )
        );
    }

    @Test
    void leitorNaoDeveConsultarTodasAsMultas() {
        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> autorizacaoService.exigirPermissao(
                        leitor,
                        Permissao.CONSULTAR_TODAS_MULTAS
                )
        );
    }

    @Test
    void usuarioNuloNaoDevePossuirPermissoes() {
        assertFalse(
                autorizacaoService.possuiPermissao(
                        null,
                        Permissao.CONSULTAR_PROPRIAS_MULTAS
                )
        );
    }
}
