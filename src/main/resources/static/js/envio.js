const formulario = document.getElementById("form-envio");

const campoIluminacao = document.getElementById("iluminacao");
const campoTomadas = document.getElementById("tomadas");

const listaApartamentos = document.getElementById("lista-apartamentos");
const erroApartamentos = document.getElementById("erro-apartamentos");
const modoResidencial = document.getElementById("modo-residencial");
const entradaGruposResidenciais = document.getElementById("entrada-grupos-residenciais");
const entradaApartamentosIndividuais = document.getElementById("entrada-apartamentos-individuais");
const listaGruposResidenciais = document.getElementById("lista-grupos-residenciais");
const erroGruposResidenciais = document.getElementById("erro-grupos-residenciais");
const camposServicoLegado = document.getElementById("campos-servico-legado");
const avisoPdfGrupos = document.getElementById("aviso-pdf-grupos");

const botaoAdicionar = document.getElementById("btn-adicionar");
const botaoAdicionarGrupo = document.getElementById("btn-adicionar-grupo");
const botaoEnviar = document.getElementById("btn-enviar");
const botaoRelatorio = document.getElementById("btn-relatorio-pdf");

const listaPendencias = document.getElementById("lista-pendencias");
const resumoOk = document.getElementById("resumo-ok");
const contagemPendencias = document.getElementById("contagem-pendencias");
const iconeChecklist = document.getElementById("icone-checklist");

const mensagem = document.getElementById("mensagem");
const validacoesNormativas = new WeakMap();
let gerandoRelatorio = false;

const FAIXAS_RESIDENCIAIS = [
    { min: 1, max: 40, rotulo: "Até 40 m²" },
    { min: 41, max: 45, rotulo: "41–45 m²" },
    { min: 46, max: 50, rotulo: "46–50 m²" },
    { min: 51, max: 55, rotulo: "51–55 m²" },
    { min: 56, max: 60, rotulo: "56–60 m²" },
    { min: 61, max: 65, rotulo: "61–65 m²" },
    { min: 66, max: 70, rotulo: "66–70 m²" },
    { min: 71, max: 75, rotulo: "71–75 m²" },
    { min: 76, max: 80, rotulo: "76–80 m²" },
    { min: 81, max: 85, rotulo: "81–85 m²" },
    { min: 86, max: 90, rotulo: "86–90 m²" },
    { min: 91, max: 95, rotulo: "91–95 m²" },
    { min: 96, max: 100, rotulo: "96–100 m²" },
    { min: 101, max: 110, rotulo: "101–110 m²" },
    { min: 111, max: 120, rotulo: "111–120 m²" },
    { min: 121, max: 130, rotulo: "121–130 m²" },
    { min: 131, max: 140, rotulo: "131–140 m²" },
    { min: 141, max: 150, rotulo: "141–150 m²" },
    { min: 151, max: 160, rotulo: "151–160 m²" },
    { min: 161, max: 170, rotulo: "161–170 m²" },
    { min: 171, max: 180, rotulo: "171–180 m²" },
    { min: 181, max: 190, rotulo: "181–190 m²" },
    { min: 191, max: 200, rotulo: "191–200 m²" },
    { min: 201, max: 220, rotulo: "201–220 m²" },
    { min: 221, max: 240, rotulo: "221–240 m²" },
    { min: 241, max: 260, rotulo: "241–260 m²" },
    { min: 261, max: 280, rotulo: "261–280 m²" },
    { min: 281, max: 300, rotulo: "281–300 m²" },
    { min: 301, max: 350, rotulo: "301–350 m²" },
    { min: 351, max: 400, rotulo: "351–400 m²" },
    { min: 401, max: 450, rotulo: "401–450 m²" },
    { min: 451, max: 500, rotulo: "451–500 m²" },
    { min: 501, max: 550, rotulo: "501–550 m²" },
    { min: 551, max: 600, rotulo: "551–600 m²" },
    { min: 601, max: 650, rotulo: "601–650 m²" },
    { min: 651, max: 700, rotulo: "651–700 m²" },
    { min: 701, max: 800, rotulo: "701–800 m²" },
    { min: 801, max: 900, rotulo: "801–900 m²" },
    { min: 901, max: 1000, rotulo: "901–1.000 m²" }
];

function usaGruposResidenciais() {
    return modoResidencial.value === "grupos";
}

function valorFaltando(input) {
    const valor = input.value.trim();

    return valor === "" ||
        !Number.isFinite(Number(valor)) ||
        Number(valor) <= 0;
}

function marcarCampo(input, faltando, textoErro) {
    const caixa = input.closest(".campo");
    const erro = caixa.querySelector(".erro");

    caixa.classList.toggle("campo-faltando", faltando);
    input.setAttribute("aria-invalid", String(faltando));
    erro.textContent = faltando ? textoErro : "";
}

function criarCampoApartamento(rotulo, classe) {
    const campo = document.createElement("div");
    campo.className = "campo";

    const label = document.createElement("label");
    label.textContent = rotulo;

    const input = document.createElement("input");
    input.type = "number";
    input.min = "0";
    input.step = "any";
    input.inputMode = "decimal";
    input.className = classe;
    input.setAttribute("aria-invalid", "false");

    const erro = document.createElement("span");
    erro.className = "erro";

    const aviso = document.createElement("span");
    aviso.className = "aviso-tecnico";
    aviso.hidden = true;

    const indicadorAviso = document.createElement("span");
    indicadorAviso.className = "aviso-icone";
    indicadorAviso.setAttribute("aria-hidden", "true");
    indicadorAviso.textContent = "!";

    const textoAviso = document.createElement("span");
    textoAviso.className = "aviso-texto";
    aviso.append(indicadorAviso, textoAviso);

    const id = `${classe}-${crypto.randomUUID()}`;
    input.id = id;
    label.htmlFor = id;

    campo.append(label, input, erro, aviso);

    input.addEventListener("input", () => {
        limparResultado();
        validar();

        if (classe === "potencia-instalada") {
            atualizarAlertaNormativo(campo.closest(".apartamento"));
        }
    });

    return campo;
}

function adicionarApartamento() {
    limparResultado();

    const apartamento = document.createElement("div");
    apartamento.className = "apartamento";

    const cabecalho = document.createElement("div");
    cabecalho.className = "apartamento-cabecalho";

    const titulo = document.createElement("h3");
    titulo.className = "apartamento-titulo";

    const botaoRemover = document.createElement("button");
    botaoRemover.type = "button";
    botaoRemover.className = "btn-remover";
    botaoRemover.textContent = "Remover";

    botaoRemover.addEventListener("click", () => {
        validacoesNormativas.get(apartamento)?.abort();
        validacoesNormativas.delete(apartamento);
        apartamento.remove();
        limparResultado();
        validar();
    });

    cabecalho.append(titulo, botaoRemover);

    const campoArea = criarCampoApartamento(
        "Área útil (m²) *",
        "area-util"
    );

    const campoPotencia = criarCampoApartamento(
        "Potência instalada (kW) *",
        "potencia-instalada"
    );

    apartamento.append(cabecalho, campoArea, campoPotencia);
    listaApartamentos.appendChild(apartamento);

    validar();
}

function adicionarGrupoResidencial() {
    limparResultado();

    const grupo = document.createElement("div");
    grupo.className = "apartamento grupo-residencial";

    const cabecalho = document.createElement("div");
    cabecalho.className = "apartamento-cabecalho";

    const titulo = document.createElement("h3");
    titulo.className = "apartamento-titulo grupo-residencial-titulo";

    const remover = document.createElement("button");
    remover.type = "button";
    remover.className = "btn-remover";
    remover.textContent = "Remover";
    remover.addEventListener("click", () => {
        grupo.remove();
        limparResultado();
        validar();
    });
    cabecalho.append(titulo, remover);

    const campoFaixa = document.createElement("div");
    campoFaixa.className = "campo";
    const labelFaixa = document.createElement("label");
    labelFaixa.textContent = "Faixa de área útil";
    const selectFaixa = document.createElement("select");
    selectFaixa.className = "faixa-area-residencial";
    selectFaixa.setAttribute("aria-invalid", "false");
    selectFaixa.add(new Option("Selecione a faixa", ""));
    FAIXAS_RESIDENCIAIS.forEach(faixa => {
        const option = new Option(faixa.rotulo, `${faixa.min}|${faixa.max}`);
        option.dataset.rotulo = faixa.rotulo;
        selectFaixa.add(option);
    });
    const erroFaixa = document.createElement("span");
    erroFaixa.className = "erro";
    labelFaixa.appendChild(selectFaixa);
    campoFaixa.append(labelFaixa, erroFaixa);

    const campoQuantidade = document.createElement("div");
    campoQuantidade.className = "campo";
    const labelQuantidade = document.createElement("label");
    labelQuantidade.textContent = "Quantidade de unidades";
    const inputQuantidade = document.createElement("input");
    inputQuantidade.type = "number";
    inputQuantidade.min = "1";
    inputQuantidade.step = "1";
    inputQuantidade.inputMode = "numeric";
    inputQuantidade.className = "quantidade-residencial";
    inputQuantidade.setAttribute("aria-invalid", "false");
    const erroQuantidade = document.createElement("span");
    erroQuantidade.className = "erro";
    labelQuantidade.appendChild(inputQuantidade);
    campoQuantidade.append(labelQuantidade, erroQuantidade);

    const aoAlterar = () => {
        limparResultado();
        validar();
    };
    selectFaixa.addEventListener("change", aoAlterar);
    inputQuantidade.addEventListener("input", aoAlterar);

    grupo.append(cabecalho, campoFaixa, campoQuantidade);
    listaGruposResidenciais.appendChild(grupo);
    validar();
}

function atualizarModoResidencial() {
    const porGrupos = usaGruposResidenciais();
    entradaGruposResidenciais.hidden = !porGrupos;
    entradaApartamentosIndividuais.hidden = porGrupos;
    camposServicoLegado.hidden = porGrupos;
    avisoPdfGrupos.hidden = !porGrupos;
    limparResultado();
    validar();
}

function renderizarChecklist(requisitos) {
    listaPendencias.replaceChildren();

    requisitos.forEach(({ texto, concluido }) => {
        const item = document.createElement("li");
        item.className = `item-checklist ${concluido ? "concluido" : "pendente"}`;

        const indicador = document.createElement("span");
        indicador.className = "checklist-indicador";
        indicador.setAttribute("aria-hidden", "true");
        indicador.textContent = concluido ? "✓" : "!";

        const nome = document.createElement("span");
        nome.className = "checklist-requisito";
        nome.textContent = texto;

        const estado = document.createElement("span");
        estado.className = "checklist-estado";
        estado.textContent = concluido ? "Concluído" : "Pendente";

        item.append(indicador, nome, estado);
        listaPendencias.appendChild(item);
    });

    const totalPendencias = requisitos.filter(({ concluido }) => !concluido).length;
    const tudoConcluido = totalPendencias === 0;

    contagemPendencias.textContent = totalPendencias;
    resumoOk.hidden = !tudoConcluido;
    iconeChecklist.classList.toggle("concluido", tudoConcluido);
    iconeChecklist.classList.toggle("pendente", !tudoConcluido);
    iconeChecklist.textContent = tudoConcluido ? "✓" : "!";
    iconeChecklist.setAttribute(
        "aria-label",
        tudoConcluido ? "Checklist concluído" : "Checklist com pendências"
    );

    return totalPendencias;
}

async function atualizarAlertaNormativo(apartamento) {
    if (!apartamento) {
        return;
    }

    validacoesNormativas.get(apartamento)?.abort();

    const campoArea = apartamento.querySelector(".area-util");
    const campoPotencia = apartamento.querySelector(".potencia-instalada");
    const aviso = campoPotencia.closest(".campo").querySelector(".aviso-tecnico");
    const textoAviso = aviso.querySelector(".aviso-texto");
    const potencia = campoPotencia.value.trim();

    if (
        potencia === "" ||
        !Number.isFinite(Number(potencia)) ||
        Number(potencia) <= 0
    ) {
        aviso.hidden = true;
        textoAviso.textContent = "";
        validacoesNormativas.delete(apartamento);
        return;
    }

    const controller = new AbortController();
    validacoesNormativas.set(apartamento, controller);

    try {
        const resposta = await fetch("/api/validacao/apartamento", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                areaUtil: Number(campoArea.value) || 0,
                potenciaInstalada: Number(potencia)
            }),
            signal: controller.signal
        });

        if (!resposta.ok) {
            throw new Error("Não foi possível validar a potência informada.");
        }

        const resultado = await resposta.json();

        if (
            validacoesNormativas.get(apartamento) !== controller ||
            !apartamento.isConnected
        ) {
            return;
        }

        const alertas = Array.isArray(resultado.alertas)
            ? resultado.alertas.filter(Boolean)
            : [];

        textoAviso.textContent = alertas.join(" ");
        aviso.hidden = alertas.length === 0;
    } catch (erro) {
        if (erro.name !== "AbortError" && validacoesNormativas.get(apartamento) === controller) {
            aviso.hidden = true;
            textoAviso.textContent = "";
        }
    } finally {
        if (validacoesNormativas.get(apartamento) === controller) {
            validacoesNormativas.delete(apartamento);
        }
    }
}

function validar() {
    const requisitos = [];
    if (usaGruposResidenciais()) {
        const grupos = [...listaGruposResidenciais.querySelectorAll(".grupo-residencial")];
        erroGruposResidenciais.textContent = grupos.length === 0
            ? "Adicione pelo menos um grupo residencial."
            : "";

        grupos.forEach((grupo, indice) => {
            grupo.querySelector(".grupo-residencial-titulo").textContent = `Grupo residencial ${indice + 1}`;
            const faixa = grupo.querySelector(".faixa-area-residencial");
            const quantidade = grupo.querySelector(".quantidade-residencial");
            const faixaFaltando = faixa.value === "";
            const quantidadeValor = Number(quantidade.value);
            const quantidadeInvalida = quantidade.value.trim() === "" ||
                !Number.isInteger(quantidadeValor) || quantidadeValor <= 0;

            marcarCampo(faixa, faixaFaltando, "Selecione uma faixa do Quadro 35");
            marcarCampo(quantidade, quantidadeInvalida, "Informe uma quantidade inteira maior que zero");
            requisitos.push({ texto: `Grupo ${indice + 1} - faixa de área`, concluido: !faixaFaltando });
            requisitos.push({ texto: `Grupo ${indice + 1} - quantidade`, concluido: !quantidadeInvalida });
        });

        if (grupos.length === 0) {
            requisitos.push({ texto: "Adicionar pelo menos um grupo residencial", concluido: false });
        }
    } else {
        const iluminacaoFaltando = valorFaltando(campoIluminacao);
        const tomadasFaltando = valorFaltando(campoTomadas);
        marcarCampo(campoIluminacao, iluminacaoFaltando, "Campo obrigatório");
        marcarCampo(campoTomadas, tomadasFaltando, "Campo obrigatório");
        requisitos.push({ texto: "Potência de iluminação", concluido: !iluminacaoFaltando });
        requisitos.push({ texto: "Potência de tomadas", concluido: !tomadasFaltando });

        const apartamentos = [...listaApartamentos.querySelectorAll(".apartamento")];
        erroApartamentos.textContent = apartamentos.length === 0
            ? "Adicione pelo menos um apartamento."
            : "";

        apartamentos.forEach((apartamento, indice) => {
            apartamento.querySelector(".apartamento-titulo").textContent = `Apartamento ${indice + 1}`;
            const campoArea = apartamento.querySelector(".area-util");
            const campoPotencia = apartamento.querySelector(".potencia-instalada");
            const areaFaltando = valorFaltando(campoArea);
            const potenciaFaltando = valorFaltando(campoPotencia);
            const areaForaDoIntervalo = !areaFaltando && Number(campoArea.value) > 1000;
            const avisoArea = campoArea.closest(".campo").querySelector(".aviso-tecnico");
            avisoArea.hidden = !areaForaDoIntervalo;
            avisoArea.querySelector(".aviso-texto").textContent = areaForaDoIntervalo
                ? "Área fora do intervalo atendido pelo Quadro 35 (até 1.000 m²). Confira o método de cálculo aplicável."
                : "";
            marcarCampo(campoArea, areaFaltando, "Campo obrigatório");
            marcarCampo(campoPotencia, potenciaFaltando, "Campo obrigatório");
            requisitos.push({ texto: `Apartamento ${indice + 1} - área útil`, concluido: !areaFaltando });
            requisitos.push({ texto: `Apartamento ${indice + 1} - potência instalada`, concluido: !potenciaFaltando });
        });

        if (apartamentos.length === 0) {
            requisitos.push({ texto: "Adicionar pelo menos um apartamento", concluido: false });
        }
    }

    const totalPendencias = renderizarChecklist(requisitos);

    botaoEnviar.disabled = totalPendencias > 0;
    botaoAdicionar.disabled = usaGruposResidenciais();
    botaoAdicionarGrupo.disabled = !usaGruposResidenciais();
    botaoRelatorio.disabled = totalPendencias > 0 || gerandoRelatorio || usaGruposResidenciais();

    return totalPendencias === 0;
}

function montarEdificacao() {
    if (usaGruposResidenciais()) {
        const gruposResidenciais = [
            ...listaGruposResidenciais.querySelectorAll(".grupo-residencial")
        ].map((grupo, indice) => {
            const [areaMinimaM2, areaMaximaM2] = grupo
                .querySelector(".faixa-area-residencial").value.split("|").map(Number);
            const faixa = grupo.querySelector(".faixa-area-residencial");
            return {
                identificacao: `Grupo ${indice + 1}`,
                tipologia: faixa.selectedOptions[0].dataset.rotulo,
                areaMinimaM2,
                areaMaximaM2,
                quantidade: Number(grupo.querySelector(".quantidade-residencial").value)
            };
        });
        return { gruposResidenciais };
    }

    const apartamentos = [
        ...listaApartamentos.querySelectorAll(".apartamento")
    ].map(apartamento => ({
        areaUtil: Number(apartamento.querySelector(".area-util").value),
        potenciaInstalada: Number(
            apartamento.querySelector(".potencia-instalada").value
        )
    }));

    return {
        apartamentos,
        potenciaIluminacao: Number(campoIluminacao.value),
        potenciaTomadas: Number(campoTomadas.value)
    };
}

async function lerResposta(resposta) {
    const texto = await resposta.text();

    if (!texto) {
        return {};
    }

    try {
        return JSON.parse(texto);
    } catch {
        return { mensagem: texto };
    }
}

function obterMensagemErro(resultado, mensagemPadrao) {
    if (resultado.mensagem) {
        return resultado.mensagem;
    }

    if (
        Array.isArray(resultado.pendencias) &&
        resultado.pendencias.length > 0
    ) {
        return resultado.pendencias.join("; ");
    }

    if (
        Array.isArray(resultado.checklist?.pendencias) &&
        resultado.checklist.pendencias.length > 0
    ) {
        return resultado.checklist.pendencias.join("; ");
    }

    return mensagemPadrao;
}

function limparResultado() {
    const cartao = document.getElementById("resultado-demanda");

    if (cartao) {
        cartao.hidden = true;
        cartao.replaceChildren();
    }
}

function formatarNumero(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatarFator(valor) {
    return Number(valor).toLocaleString("pt-BR", {
        minimumFractionDigits: 4,
        maximumFractionDigits: 4
    });
}

function formatarArea(valor) {
    return Number(valor).toLocaleString("pt-BR", { maximumFractionDigits: 2 });
}

function adicionarTexto(tag, classe, texto, pai) {
    const elemento = document.createElement(tag);

    elemento.className = classe;
    elemento.textContent = texto;

    pai.appendChild(elemento);

    return elemento;
}



const TITULOS_CRITERIOS = {
    QUADRO_35: "Demanda por área útil (Quadro 35)",
    FATOR_COINCIDENCIA: "Fator de coincidência",
    FATOR_SEGURANCA: "Fator de segurança",
    DEMANDA_ILUMINACAO_TOMADAS_PADRAO: "Demanda de iluminação e tomadas"
};
const PARAMETROS_CONHECIDOS = {
    areautil: { rotulo: "Área útil", unidade: "m²" },
    numeroapartamentos: { rotulo: "Número de apartamentos" },
    demandacomcoincidencia: { rotulo: "Demanda com coincidência", unidade: "kVA" },
    potenciailuminacao: { rotulo: "Potência de iluminação", unidade: "kW" },
    potenciatomadas: { rotulo: "Potência de tomadas", unidade: "kW" },
    numeromotores: { rotulo: "Número de motores" }
};
function humanizar(texto) {
    const separado = String(texto)
        .replace(/_/g, " ")
        .replace(/([a-z])([A-Z])/g, "$1 $2")
        .toLowerCase();

    return separado.charAt(0).toUpperCase() + separado.slice(1);
}

function ehNumero(valor) {
    if (typeof valor === "number") {
        return Number.isFinite(valor);
    }

    return typeof valor === "string" &&
        valor.trim() !== "" &&
        Number.isFinite(Number(valor));
}

function formatarValorParametro(bruto) {
    if (/^-?\d+(\.\d+)?$/.test(bruto)) {
        return Number(bruto).toLocaleString("pt-BR", {
            maximumFractionDigits: 2
        });
    }

    return bruto;
}
function interpretarParametros(texto) {
    if (!texto) {
        return [];
    }

    return String(texto)
        .split(",")
        .map(parte => parte.trim())
        .filter(Boolean)
        .map(parte => {
            const posicao = parte.indexOf("=");

            if (posicao === -1) {
                return { rotulo: "Parâmetro", valor: parte };
            }

            let chave = parte.slice(0, posicao).trim();
            const valorBruto = parte.slice(posicao + 1).trim();
            let prefixo = "";
            const apto = chave.match(/^apartamento\[(\d+)\]\.(.+)$/);

            if (apto) {
                prefixo = `Apartamento ${Number(apto[1]) + 1} – `;
                chave = apto[2];
            }

            const conhecido = PARAMETROS_CONHECIDOS[chave.toLowerCase()];
            const rotulo = prefixo + (conhecido ? conhecido.rotulo : humanizar(chave));

            let valor = formatarValorParametro(valorBruto);

            if (conhecido?.unidade && valor !== "") {
                valor += ` ${conhecido.unidade}`;
            }

            return { rotulo, valor };
        });
}

function criarItemMemoria(criterio) {
    const item = document.createElement("li");
    item.className = "memoria-item";
    if (criterio === null || typeof criterio !== "object") {
        adicionarTexto("strong", "memoria-titulo", String(criterio ?? "—"), item);
        return item;
    }

    adicionarTexto(
        "strong",
        "memoria-titulo",
        criterio.criterio
            ? (TITULOS_CRITERIOS[criterio.criterio] ?? humanizar(criterio.criterio))
            : "Critério aplicado",
        item
    );

    if (criterio.descricao) {
        adicionarTexto(
            "p",
            "memoria-descricao",
            String(criterio.descricao).replace(" -> ", " → "),
            item
        );
    }

    const parametros = interpretarParametros(criterio.parametro);

    if (parametros.length > 0) {
        const lista = document.createElement("dl");
        lista.className = "memoria-detalhes";

        parametros.forEach(({ rotulo, valor }) => {
            const linha = document.createElement("div");
            linha.className = "memoria-parametro";

            adicionarTexto("dt", "", rotulo, linha);
            adicionarTexto("dd", "", valor, linha);

            lista.appendChild(linha);
        });

        item.appendChild(lista);
    }

    if (criterio.valorAplicado != null) {
        const aplicado = document.createElement("div");
        aplicado.className = "memoria-aplicado";

        const ehFator = String(criterio.criterio ?? "").startsWith("FATOR_");

        adicionarTexto(
            "span",
            "",
            ehFator ? "Fator aplicado" : "Valor aplicado",
            aplicado
        );

        adicionarTexto(
            "strong",
            "",
            ehNumero(criterio.valorAplicado)
                ? formatarNumero(criterio.valorAplicado)
                : String(criterio.valorAplicado),
            aplicado
        );

        item.appendChild(aplicado);
    }

    return item;
}

function exibirResultadoResidencial(cartao, dados, gruposResidenciais) {
    const residencial = dados.detalhamento?.residencial;
    if (!residencial) return;

    const secao = document.createElement("section");
    secao.className = "memoria-residencial";
    adicionarTexto("h3", "", "Memória de cálculo — Área residencial", secao);

    const etapaNormativa = dados.detalhamento.memoriaCalculo?.find(etapa => etapa.norma);
    if (etapaNormativa) {
        adicionarTexto("p", "memoria-residencial-fonte",
            `Base normativa: ${etapaNormativa.norma} Rev. ${etapaNormativa.revisao}`, secao);
    }

    const valores = document.createElement("dl");
    valores.className = "memoria-residencial-valores";
    const linhas = [
        ["Demanda residencial (Dr)", `${formatarNumero(residencial.demandaResidencialBaseDrKva)} kVA`],
        ["Fator de coincidência (Fc)", formatarFator(residencial.fatorCoincidenciaFc)],
        ["Demanda após coincidência (Dtr)", `${formatarNumero(residencial.demandaCoincidenteDtrKva)} kVA`],
        ["Fator de segurança (Fr)", formatarNumero(residencial.fatorSegurancaFr)],
        ["Demanda residencial final (Drf)", `${formatarNumero(residencial.demandaResidencialFinalDrfKva)} kVA`]
    ];
    linhas.forEach(([rotulo, valor]) => {
        const linha = document.createElement("div");
        adicionarTexto("dt", "", rotulo, linha);
        adicionarTexto("dd", "", valor, linha);
        valores.appendChild(linha);
    });
    secao.appendChild(valores);

    if (Array.isArray(gruposResidenciais) && gruposResidenciais.length > 0) {
        adicionarTexto("h4", "", "Grupos considerados", secao);
        const lista = document.createElement("ul");
        const criteriosDi = (dados.memoriaCalculo ?? [])
            .filter(criterio => criterio.criterio === "QUADRO_35");
        gruposResidenciais.forEach((grupo, indice) => {
            const di = criteriosDi[indice]?.valorAplicado;
            const linha = document.createElement("li");
            const area = grupo.areaMinimaM2 === grupo.areaMaximaM2
                ? `${formatarArea(grupo.areaMinimaM2)} m²`
                : `${formatarArea(grupo.areaMinimaM2)}–${formatarArea(grupo.areaMaximaM2)} m²`;
            const valorDi = ehNumero(di) ? `Di = ${formatarNumero(di)} kVA` : "Di indisponível";
            linha.textContent = `${grupo.quantidade} unidades — ${area} — ${valorDi}`;
            lista.appendChild(linha);
        });
        secao.appendChild(lista);
    }
    cartao.appendChild(secao);
}

function exibirResultado(dados, gruposResidenciais = []) {
    let cartao = document.getElementById("resultado-demanda");

    if (!cartao) {
        cartao = document.createElement("section");
        cartao.id = "resultado-demanda";
        cartao.className = "resultado-demanda";
        cartao.setAttribute("aria-live", "polite");
        const coluna = formulario.querySelector(".coluna-formulario");

        if (!coluna) {
            throw new Error(
                "Não foi possível encontrar a área de exibição do resultado."
            );
        }

        coluna.insertBefore(cartao, coluna.firstChild);
    }

    cartao.replaceChildren();
    cartao.hidden = false;
    const cabecalho = document.createElement("div");
    cabecalho.className = "resultado-cabecalho";

    adicionarTexto("span", "resultado-icone", "⚡", cabecalho);

    const tituloGrupo = document.createElement("div");
    adicionarTexto("h2", "", "Demanda calculada", tituloGrupo);
    adicionarTexto("p", "", "Resultado para os dados informados.", tituloGrupo);

    cabecalho.appendChild(tituloGrupo);
    cartao.appendChild(cabecalho);
    const caixaValor = document.createElement("div");
    caixaValor.className = "resultado-valor";

    const numero = document.createElement("div");
    numero.className = "resultado-numero";
    numero.appendChild(
        document.createTextNode(formatarNumero(dados.demandaTotal))
    );
    adicionarTexto("span", "resultado-unidade", "kVA", numero);

    caixaValor.appendChild(numero);

    adicionarTexto(
        "div",
        "resultado-status",
        "✓ Cálculo concluído com sucesso",
        caixaValor
    );

    cartao.appendChild(caixaValor);
    exibirResultadoResidencial(cartao, dados, gruposResidenciais);
    if (
        Array.isArray(dados.memoriaCalculo) &&
        dados.memoriaCalculo.length > 0
    ) {
        const total = dados.memoriaCalculo.length;

        const memoria = document.createElement("details");
        memoria.className = "memoria-calculo";

        const resumo = document.createElement("summary");
        resumo.className = "memoria-resumo";

        adicionarTexto("span", "", "Memória de cálculo", resumo);
        adicionarTexto(
            "span",
            "memoria-contagem",
            `${total} ${total === 1 ? "critério" : "critérios"}`,
            resumo
        );

        memoria.appendChild(resumo);

        const lista = document.createElement("ul");
        lista.className = "memoria-lista";

        dados.memoriaCalculo.forEach(criterio => {
            lista.appendChild(criarItemMemoria(criterio));
        });

        memoria.appendChild(lista);
        cartao.appendChild(memoria);
    }

    cartao.scrollIntoView({ behavior: "smooth", block: "start" });
}

formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    if (!validar()) {
        mensagem.textContent =
            "Preencha os campos obrigatórios antes de enviar.";
        mensagem.className = "mensagem mensagem-erro";
        return;
    }

    const edificacao = montarEdificacao();

    limparResultado();

    botaoEnviar.disabled = true;
    botaoAdicionar.disabled = true;

    mensagem.textContent = "Verificando checklist...";
    mensagem.className = "mensagem";

    try {
        const respostaChecklist = await fetch("/api/checklist/enviar", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(edificacao)
        });

        const resultadoChecklist = await lerResposta(respostaChecklist);

        if (!respostaChecklist.ok) {
            throw new Error(
                obterMensagemErro(
                    resultadoChecklist,
                    "O checklist não foi aprovado."
                )
            );
        }
        const resultadoCalculo = resultadoChecklist.resultadoDemanda;

        if (
            !resultadoCalculo ||
            typeof resultadoCalculo.demandaTotal !== "number" ||
            !Number.isFinite(resultadoCalculo.demandaTotal)
        ) {
            throw new Error(
                "O cálculo foi respondido, mas o formato da demanda retornada é inválido."
            );
        }
        exibirResultado(resultadoCalculo, edificacao.gruposResidenciais ?? []);

        mensagem.textContent =
            "Projeto enviado e cálculo concluído com sucesso.";
        mensagem.className = "mensagem mensagem-ok";

    } catch (erro) {
        mensagem.textContent =
            erro.message || "Ocorreu um erro ao enviar o projeto.";
        mensagem.className = "mensagem mensagem-erro";

    } finally {
        validar();
        botaoAdicionar.disabled = false;
    }
});
campoIluminacao.addEventListener("input", () => {
    limparResultado();
    validar();
});

campoTomadas.addEventListener("input", () => {
    limparResultado();
    validar();
});

botaoRelatorio.addEventListener("click", async () => {
    mensagem.className = "mensagem";
    mensagem.textContent = "";

    if (usaGruposResidenciais()) {
        mensagem.classList.add("mensagem-erro");
        mensagem.textContent = "A geração de PDF para grupos residenciais ainda não está disponível.";
        return;
    }

    if (!validar()) {
        mensagem.classList.add("mensagem-erro");
        mensagem.textContent = "Preencha os campos obrigatórios antes de gerar o relatório.";
        return;
    }

    const edificacao = montarEdificacao();
    gerandoRelatorio = true;
    botaoRelatorio.disabled = true;
    mensagem.textContent = "Gerando relatório PDF...";

    try {
        const resposta = await fetch("/api/relatorio/pdf", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(edificacao)
        });

        if (!resposta.ok) {
            const erro = await lerResposta(resposta);
            throw new Error(obterMensagemErro(
                erro,
                "O servidor recusou a geração do relatório."
            ));
        }

        const tipoConteudo = resposta.headers.get("Content-Type") || "";
        if (!tipoConteudo.toLowerCase().includes("application/pdf")) {
            throw new Error("O servidor não retornou um arquivo PDF válido.");
        }

        const arquivo = await resposta.blob();
        const url = URL.createObjectURL(arquivo);
        const link = document.createElement("a");
        link.href = url;
        link.download = "relatorio-calculo-demanda.pdf";
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(url), 1000);

        mensagem.classList.add("mensagem-ok");
        mensagem.textContent = "Relatório PDF gerado e download iniciado.";
    } catch (erro) {
        mensagem.classList.add("mensagem-erro");
        mensagem.textContent = erro.message || "Não foi possível gerar o relatório PDF.";
    } finally {
        gerandoRelatorio = false;
        validar();
    }
});
botaoAdicionar.addEventListener("click", adicionarApartamento);
botaoAdicionarGrupo.addEventListener("click", adicionarGrupoResidencial);
modoResidencial.addEventListener("change", atualizarModoResidencial);
adicionarGrupoResidencial();
