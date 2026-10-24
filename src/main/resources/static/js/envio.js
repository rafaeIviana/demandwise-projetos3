// Tela de envio (US-06): bloqueia o botão enquanto houver campo obrigatório faltando
// e mostra qual campo está faltando. A validação "de verdade" continua no servidor
// (POST /api/checklist/enviar), então burlar esta tela não adianta.

const form = document.getElementById("form-envio");
const campoIluminacao = document.getElementById("iluminacao");
const campoTomadas = document.getElementById("tomadas");
const listaApartamentos = document.getElementById("lista-apartamentos");
const erroApartamentos = document.getElementById("erro-apartamentos");
const listaPendencias = document.getElementById("lista-pendencias");
const resumoOk = document.getElementById("resumo-ok");
const contadorPendencias = document.getElementById("contador-pendencias");
const indicadorChecklist = document.getElementById("indicador-checklist");
const botaoEnviar = document.getElementById("btn-enviar");
const botaoRelatorio = document.getElementById("btn-relatorio-pdf");
const botaoAdicionar = document.getElementById("btn-adicionar");
const mensagem = document.getElementById("mensagem");
let gerandoRelatorio = false;

// mesma regra do ChecklistController: vazio ou <= 0 conta como não informado
function valorFaltando(input) {
    const texto = input.value.trim();
    if (texto === "") return true;
    const numero = Number(texto);
    return Number.isNaN(numero) || numero <= 0;
}

function marcarCampo(input, faltando, textoErro) {
    const caixa = input.closest(".campo");
    caixa.classList.toggle("campo-faltando", faltando);
    input.setAttribute("aria-invalid", faltando ? "true" : "false");
    caixa.querySelector(".erro").textContent = faltando ? textoErro : "";
}

function textoDoErro(input) {
    return input.value.trim() === ""
        ? "Campo obrigatório"
        : "Informe um valor maior que zero";
}

function criarCampoApartamento(rotulo, classe) {
    const caixa = document.createElement("div");
    caixa.className = "campo";

    const label = document.createElement("label");
    label.textContent = rotulo + " ";
    const asterisco = document.createElement("span");
    asterisco.className = "asterisco";
    asterisco.textContent = "*";
    label.appendChild(asterisco);

    const input = document.createElement("input");
    input.type = "number";
    input.min = "0";
    input.step = "any";
    input.inputMode = "decimal";
    input.className = classe;
    input.addEventListener("input", validar);

    const erro = document.createElement("span");
    erro.className = "erro";

    caixa.append(label, input, erro);
    return caixa;
}

function adicionarApartamento() {
    const linha = document.createElement("div");
    linha.className = "apartamento";

    const titulo = document.createElement("h3");
    titulo.className = "apartamento-titulo";

    const remover = document.createElement("button");
    remover.type = "button";
    remover.className = "btn-remover";
    remover.textContent = "Remover";
    remover.addEventListener("click", () => {
        linha.remove();
        validar();
    });

    const cabecalho = document.createElement("div");
    cabecalho.className = "apartamento-cabecalho";
    cabecalho.append(titulo, remover);

    linha.append(
        cabecalho,
        criarCampoApartamento("Área útil (m²)", "area-util"),
        criarCampoApartamento("Potência instalada (kW)", "potencia-instalada")
    );

    listaApartamentos.appendChild(linha);
    validar();
}

// confere tudo, pinta os campos faltando, atualiza o resumo e liga/desliga o botão
function validar() {
    const requisitos = [];

    const iluminacaoFaltando = valorFaltando(campoIluminacao);
    marcarCampo(campoIluminacao, iluminacaoFaltando, textoDoErro(campoIluminacao));
    requisitos.push({ texto: "Potência de iluminação", concluido: !iluminacaoFaltando });

    const tomadasFaltando = valorFaltando(campoTomadas);
    marcarCampo(campoTomadas, tomadasFaltando, textoDoErro(campoTomadas));
    requisitos.push({ texto: "Potência de tomadas", concluido: !tomadasFaltando });

    const linhas = listaApartamentos.querySelectorAll(".apartamento");
    linhas.forEach((linha, i) => {
        const numero = i + 1;
        linha.querySelector(".apartamento-titulo").textContent = "Apartamento " + numero;

        const area = linha.querySelector(".area-util");
        const potencia = linha.querySelector(".potencia-instalada");

        const areaFaltando = valorFaltando(area);
        marcarCampo(area, areaFaltando, textoDoErro(area));
        requisitos.push({
            texto: "Apartamento " + numero + " - área útil",
            concluido: !areaFaltando
        });

        const potenciaFaltando = valorFaltando(potencia);
        marcarCampo(potencia, potenciaFaltando, textoDoErro(potencia));
        requisitos.push({
            texto: "Apartamento " + numero + " - potência instalada",
            concluido: !potenciaFaltando
        });
    });

    if (linhas.length === 0) {
        erroApartamentos.textContent = "Adicione pelo menos um apartamento";
        requisitos.push({ texto: "Adicionar pelo menos um apartamento", concluido: false });
    } else {
        erroApartamentos.textContent = "";
    }

    mostrarChecklist(requisitos);
    const pendencias = requisitos.filter(requisito => !requisito.concluido);
    botaoEnviar.disabled = pendencias.length > 0;
    botaoRelatorio.disabled = pendencias.length > 0 || gerandoRelatorio;
    return pendencias.map(requisito => requisito.texto);
}

function mostrarChecklist(requisitos) {
    listaPendencias.replaceChildren();
    requisitos.forEach(requisito => {
        const item = document.createElement("li");
        const estado = requisito.concluido ? "concluido" : "pendente";
        item.className = "item-checklist item-checklist--" + estado;

        const texto = document.createElement("span");
        texto.className = "item-checklist-requisito";
        texto.textContent = requisito.texto;

        const indicador = document.createElement("span");
        indicador.className = "item-checklist-estado";

        const icone = document.createElement("span");
        icone.className = "item-checklist-icone";
        icone.setAttribute("aria-hidden", "true");
        icone.textContent = requisito.concluido ? "✓" : "!";

        const rotuloEstado = document.createElement("span");
        rotuloEstado.textContent = requisito.concluido ? "Concluído" : "Pendente";
        indicador.append(icone, rotuloEstado);
        item.append(texto, indicador);
        listaPendencias.appendChild(item);
    });

    const pendencias = requisitos.filter(requisito => !requisito.concluido).length;
    const completo = pendencias === 0;
    contadorPendencias.textContent = pendencias === 1
        ? "1 pendência"
        : pendencias + " pendências";
    resumoOk.hidden = !completo;

    indicadorChecklist.className = "indicador-checklist indicador-checklist--"
        + (completo ? "concluido" : "pendente");
    indicadorChecklist.querySelector(".indicador-checklist-icone").textContent = completo ? "✓" : "!";
    indicadorChecklist.querySelector(".indicador-checklist-texto").textContent = completo
        ? "Concluído"
        : "Em andamento";
}

function montarEdificacao() {
    const apartamentos = [...listaApartamentos.querySelectorAll(".apartamento")].map(linha => ({
        areaUtil: Number(linha.querySelector(".area-util").value),
        potenciaInstalada: Number(linha.querySelector(".potencia-instalada").value)
    }));

    return {
        apartamentos: apartamentos,
        potenciaIluminacao: Number(campoIluminacao.value),
        potenciaTomadas: Number(campoTomadas.value)
    };
}

form.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mensagem.className = "mensagem";
    mensagem.textContent = "";

    // mesmo com o botão desabilitado, confere de novo antes de enviar
    if (validar().length > 0) return;

    botaoEnviar.disabled = true;
    try {
        const resposta = await fetch("/api/checklist/enviar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(montarEdificacao())
        });

        if (resposta.ok) {
            mensagem.classList.add("mensagem-ok");
            mensagem.textContent = "Projeto enviado com sucesso.";
        } else {
            // o servidor recusou: mostra as pendências que ele encontrou
            const checklist = await resposta.json().catch(() => null);
            mensagem.classList.add("mensagem-erro");
            const pendenciasServidor = checklist?.pendencias?.join("; ");
            mensagem.textContent = pendenciasServidor
                ? "Envio recusado pelo servidor. Pendências: " + pendenciasServidor
                : "Envio recusado pelo servidor. Corrija as pendências indicadas.";
        }
    } catch (erro) {
        mensagem.classList.add("mensagem-erro");
        mensagem.textContent = "Não foi possível conectar ao servidor. Tente novamente.";
    } finally {
        // só reabilita se a tela ainda estiver completa
        botaoEnviar.disabled = validar().length > 0;
    }
});

botaoRelatorio.addEventListener("click", async () => {
    mensagem.className = "mensagem";
    mensagem.textContent = "";

    if (validar().length > 0) {
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
            const erro = await resposta.json().catch(() => null);
            const texto = erro?.pendencias?.length
                ? erro.pendencias.join("; ")
                : erro?.mensagem || "O servidor recusou a geração do relatório.";
            throw new Error(texto);
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
        botaoRelatorio.disabled = validar().length > 0;
    }
});

campoIluminacao.addEventListener("input", validar);
campoTomadas.addEventListener("input", validar);
botaoAdicionar.addEventListener("click", adicionarApartamento);

// começa com um apartamento em branco
adicionarApartamento();
