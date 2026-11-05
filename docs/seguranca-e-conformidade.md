# Segurança e conformidade de dados — DemandWise

## Estado do protótipo acadêmico

O protótipo implementa uma tela de login e autenticação básica com usuário configurável e senha armazenada como derivação PBKDF2-HMAC-SHA256 (salt e hash em `application.properties`/variáveis de ambiente). A sessão HTTP protege páginas, APIs de cálculo, validação, envio, relatórios, dashboard e dados de projetos. O logout invalida a sessão. Credenciais padrão de demonstração: `demo` / `DemandWise2026!`.

Esta implementação serve apenas para demonstração local. Não há gestão individual de contas, recuperação/troca de senha, MFA, controle de papéis, proteção contra abuso, política de expiração refinada, auditoria, gestão segura de chaves ou garantia de TLS. A senha padrão é pública por estar no código; deve ser trocada e nunca usada em produção. Sessões dependem da configuração padrão do servidor e os dados continuam mantidos conforme o armazenamento em memória existente.

## Abordagem planejada para produção

- **Criptografia:** TLS moderno em todo o tráfego; criptografia de volumes e backups em repouso; chaves geridas por serviço dedicado, com rotação e acesso restrito. Senhas seriam armazenadas com algoritmo adaptativo apropriado (Argon2id ou BCrypt) e parâmetros revistos periodicamente.
- **Acesso por conta/usuário:** identidade individual, menor privilégio, papéis por função/projeto, revisão e revogação de acessos, trilha de auditoria e segregação de ambientes.
- **Autenticação:** provedor corporativo de identidade (SSO/OIDC), MFA, política de sessão, proteção contra tentativas automatizadas e recuperação segura.
- **Proteção dos projetos:** autorização por projeto em cada operação, validação de entrada, backups protegidos e testados, retenção e descarte definidos, monitoramento de acesso e resposta a incidentes.
- **Privacidade e conformidade:** mapear dados pessoais e finalidade, minimizar coleta, definir base legal, retenção e direitos dos titulares; avaliar requisitos da LGPD e políticas/contratos da Neoenergia; registrar operadores, subprocessadores e fluxos de dados; realizar avaliação de impacto quando aplicável.
- **Operação:** gestão de segredos fora do repositório, atualizações de dependências, análise de vulnerabilidades, logging sem dados sensíveis e procedimentos de resposta a incidentes.

Essas medidas são recomendações para uma futura versão e **não estão implementadas nem certificadas** neste protótipo. A adoção formal deve depender de revisão de segurança, privacidade e conformidade da organização.
