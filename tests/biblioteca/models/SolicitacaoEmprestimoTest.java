
package biblioteca.models;

import biblioteca.model.SolicitacaoEmprestimo;
import biblioteca.model.StatusEnums.StatusSolicitacao;
import biblioteca.model.material.Livro;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Usuario;
import biblioteca.repository.MaterialRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoEmprestimoTest {

    private Usuario usuario;
    private Livro livro;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "João",
                "joao",
                "123456",
                Cargo.LEITOR
        );

        livro = new Livro(
                "Clean Code",
                2008,
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                Path.of(MaterialRepositorio.uploadsDirectory, "clean_code.jpg")
        );
    }

    @Test
    void deveCriarSolicitacaoComStatusPendente() {
        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(usuario, livro);

        assertNotNull(solicitacao);
        assertEquals(StatusSolicitacao.PENDENTE, solicitacao.getStatus());
    }

    @Test
    void deveGerarIdentificadorParaNovaSolicitacao() {
        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(usuario, livro);

        assertNotEquals(0, solicitacao.getId());
    }

    @Test
    void deveArmazenarSolicitanteEMaterialCorretamente() {
        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(usuario, livro);

        assertSame(usuario, solicitacao.getSolicitante());
        assertSame(livro, solicitacao.getMaterialSolicitado());
    }

    @Test
    void deveDefinirDataDaSolicitacaoComoDataAtual() {
        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(usuario, livro);

        assertEquals(LocalDate.now(), solicitacao.getDataSolicitacao());
    }

    @Test
    void devePermitirCriacaoComStatusEIdEspecificos() {
        int id = 123;

        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(
                        usuario,
                        livro,
                        StatusSolicitacao.APROVADA,
                        id
                );

        assertEquals(id, solicitacao.getId());
        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertSame(usuario, solicitacao.getSolicitante());
        assertSame(livro, solicitacao.getMaterialSolicitado());
    }

    @Test
    void devePermitirAlteracaoDeStatus() {
        SolicitacaoEmprestimo solicitacao =
                new SolicitacaoEmprestimo(usuario, livro);

        solicitacao.setStatus(StatusSolicitacao.REJEITADA);

        assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
    }
}
