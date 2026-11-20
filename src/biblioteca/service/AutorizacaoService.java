package biblioteca.service;

import biblioteca.exception.OperacaoNaoPermitidaException;
import biblioteca.model.usuario.Cargo;
import biblioteca.model.usuario.Permissao;
import biblioteca.model.usuario.Usuario;

public class AutorizacaoService {

    public boolean possuiPermissao(Usuario usuario, Permissao permissao) {
        if (usuario == null) {
            return false;
        }

        Cargo cargo = usuario.getCargo();

        return switch (cargo) {
            case ADMINISTRADOR -> true;

            case BIBLIOTECARIO -> switch (permissao) {
                case GERENCIAR_CATALOGO,
                	 GERENCIAR_SOLICITACOES,
                     REGISTRAR_EMPRESTIMO,
                     REGISTRAR_DEVOLUCAO,
                     CONSULTAR_CATALOGO,
                     CONSULTAR_TODOS_EMPRESTIMOS,
                     CONSULTAR_USUARIOS,
                     ATUALIZAR_PROPRIOS_DADOS,
                     CONSULTAR_TODAS_SOLICITACOES,
                     GERENCIAR_SOLICITACOES,
                     CONSULTAR_TODAS_MULTAS,
                     GERENCIAR_MULTAS,
                     CONSULTAR_PROPRIOS_EMPRESTIMOS,
                     CONSULTAR_TODAS_SOLICITACOES,
                     ATUALIZAR_PROPRIOS_DADOS,
                     CONSULTAR_PROPRIAS_MULTAS
                     -> true;

                default -> false;
            };

            case LEITOR -> switch (permissao) {
                case CONSULTAR_CATALOGO,
                     CONSULTAR_PROPRIOS_EMPRESTIMOS,
                     CONSULTAR_PROPRIAS_SOLICITACOES,
                     SOLICITAR_EMPRESTIMO,
                     ATUALIZAR_PROPRIOS_DADOS,
                     CONSULTAR_PROPRIAS_MULTAS -> true;

                default -> false;
            };
        };
    }

    public void exigirPermissao(Usuario usuario, Permissao permissao) throws OperacaoNaoPermitidaException {

        if (!possuiPermissao(usuario, permissao)) {
            throw new OperacaoNaoPermitidaException();
        }
    }
}