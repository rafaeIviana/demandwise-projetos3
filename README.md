# Automação do Cálculo de Demanda Elétrica

## Descrição do projeto

O projeto tem como objetivo desenvolver um protótipo capaz de **automatizar o cálculo de demanda elétrica de edificações com múltiplas unidades consumidoras**, seguindo as regras estabelecidas pela norma **DIS-NOR-053 da Neoenergia Pernambuco**.

A proposta surgiu a partir da análise do material disponibilizado pela Neoenergia, buscando entender como o cálculo é realizado atualmente e quais etapas podem ser automatizadas.

A solução pretende tornar o processo **mais simples, rápido e padronizado**, reduzindo erros e diferenças nos resultados causadas pela realização manual dos cálculos.

---

## Visão atual do projeto

A partir do material disponibilizado pela Neoenergia, a equipe identificou que o cálculo de demanda envolve diferentes informações, fórmulas, tabelas e fatores definidos pela norma DIS-NOR-053.

Neste momento, o projeto está focado em **entender e organizar essas regras**, identificando:

* Quais dados precisam ser informados pelo usuário;
* Quais fórmulas devem ser utilizadas;
* Quais tabelas e fatores de demanda são necessários;
* Como as diferentes unidades consumidoras influenciam no cálculo;
* Quais etapas do processo podem ser automatizadas;
* Como apresentar o resultado de forma clara para o usuário.

A partir desse entendimento, será desenvolvido um protótipo que permita realizar o cálculo de forma automática, seguindo os critérios definidos pela norma.

> **Esta visão poderá ser atualizada ao longo do projeto conforme novas informações forem identificadas nas análises e nas próximas entregas.**

---

## Tecnologias utilizadas

As tecnologias utilizadas no projeto serão definidas e atualizadas conforme o desenvolvimento do protótipo.

| Tecnologia | Utilização                                  |
| ---------- | ------------------------------------------- |
| Java | Linguagem principal utilizada no desenvolvimento do backend e das regras de negócio |
| SpringBoot  | Framework utilizado para desenvolver o backend e disponibilizar a API da aplicação |
| Maven  | Gerenciamento de dependências e construção do projeto Java |
| Thymeleaf | Construção da interface web integrada ao SpringBoot |
| PostgreSQL  | Banco de dados utilizado para armazenamento das informações | 
| Git/GitHub | Controle de versão e organização do projeto |

---

# Entregas

## Entrega 01 — Visão inicial e análise do problema

Nesta entrega foi realizada a análise inicial do material disponibilizado pela Neoenergia, buscando compreender o problema e organizar as primeiras informações sobre o cálculo de demanda.

### Artefatos

- **Histórias de usuário**
  - US-01 — Cálculo automático de demanda
  - US-02 — Alertas explicando por que um valor está fora do padrão
  - US-03 — Checklist automático das Pendências MUC
  - US-04 — Padronização entre analistas
  - US-05 — Templates reaproveitáveis de prédio-tipo
  - US-06 — Validação impede envio com dado obrigatório faltante
  - US-07 — Geração automática do relatório final de cálculo
  - US-08 — Projeto chega pré-validado para o analista
  - US-09 — Expansão futura para outras distribuidoras
  - US-10 — Dashboard de métricas de redução de reprovação
  - US-11 — Seleção de tipo de instalação aplicando regras automaticamente
  - US-12 — Segurança e conformidade de dados
- **Documentação das histórias:** [Acessar PDF](readme_img/demandwise-backlog.pdf)
* **Protótipo do Figma:** [Acessar protótipo](https://sway-ashen-54282395.figma.site)
- **Vídeo de apresentação do protótipo:** [Assistir no YouTube](https://youtu.be/8bLrNAD9KFs)


---

## Entrega 02

Nesta entrega foram implementadas as primeiras funcionalidades do protótipo, relacionadas às seguintes histórias de usuário:

### US-01 — Cálculo automático de demanda

Implementação do cálculo automático de demanda elétrica da edificação, seguindo as regras e fatores definidos na norma DIS-NOR-053.

### US-02 — Alertas explicando por que um valor está fora do padrão

Implementação da funcionalidade de identificação e explicação de valores que estejam fora dos padrões definidos para o cálculo.

### Screencasts

* **Screencast — Demonstração de uso do sistema:** [Assistir no YouTube](https://youtu.be/yrcTqzvEuuk)

* **Screencast — Explicação do código e das histórias implementadas:** [Assistir no YouTube](https://youtu.be/o75v9s-ksvc)


## Entrega 03 

### Artefatos

* **Documentação:** [Link]
* **Screenshots:** [Link]
* **Outros materiais:** [Link]

---

# Membros da equipe

| Nome                 | E-mail institucional                            | Cargo             |
| -------------------- | ----------------------------------------------- | ----------------- |
| Arthur Guimarães | [avsg@cesar.school](mailto:avsg@cesar.school) | Organizer |
| Breno Gabriel | [bgas@cesar.school](mailto:bgas@cesar.school) | Product Owner |
| Christopher Mark | [cmjm@cesar.school](mailto:cmjm@cesar.school) | Organizer |
| Gilberto Dias | [gdsn@cesar.school](mailto:gdsn@cesar.school) | Developer |
| Heitor Antonio  | [haotd@cesar.school](mailto:haotd@cesar.school) | Developer | 
| João Vitor Lopes | [jvla@cesar.school](mailto:jvla@cesar.school) | Developer |
| Luiz Guilherme Silvestre | [lgsgs@cesar.school](mailto:lgsgs@cesar.school) | Developer |
| Pedro Lima | [phpl@cesar.school](mailto:phpl@cesar.school) | Developer |
| Rafael Viana | [rlvs@cesar.school](mailto:rlvs@cesar.school) | Tech Lead | 

---

# Materiais de referência

### Neoenergia

* **DIS-NOR-053 — Norma utilizada como base para o projeto:** [Link]

### Materiais produzidos pela equipe

* **Documentação:** [Link]
* **Protótipo:** [Link]
* **Documentação dos testes:** [Link]
* **Apresentações:** [Link]
* **Screenshots:** [Link]

---

# Status do projeto

**Em desenvolvimento.**

O projeto encontra-se atualmente na etapa de análise e organização das regras de cálculo apresentadas na norma DIS-NOR-053. As próximas etapas serão voltadas para o desenvolvimento, testes e validação do protótipo.
