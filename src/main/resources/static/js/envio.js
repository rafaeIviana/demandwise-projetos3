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
const botaoEnviar = document.getElementById("btn-enviar");
const botaoAdicionar = document.getElementById("btn-adicionar");
const mensagem = document.getElementById("mensagem");

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
    const pendencias = [];

    marcarCampo(campoIluminacao, valorFaltando(campoIluminacao), textoDoErro(campoIluminacao));
    if (valorFaltando(campoIluminacao)) pendencias.push("Potência de iluminação");

    marcarCampo(campoTomadas, valorFaltando(campoTomadas), textoDoErro(campoTomadas));
    if (valorFaltando(campoTomadas)) pendencias.push("Potência de tomadas");

    const linhas = listaApartamentos.querySelectorAll(".apartamento");
    linhas.forEach((linha, i) => {
        const numero = i + 1;
        linha.querySelector(".apartamento-titulo").textContent = "Apartamento " + numero;

        const area = linha.querySelector(".area-util");
        const potencia = linha.querySelector(".potencia-instalada");

        marcarCampo(area, valorFaltando(area), textoDoErro(area));
        if (valorFaltando(area)) pendencias.push("Apartamento " + numero + " - área útil");

        marcarCampo(potencia, valorFaltando(potencia), textoDoErro(potencia));
        if (valorFaltando(potencia)) pendencias.push("Apartamento " + numero + " - potência instalada");
    });

    if (linhas.length === 0) {
        erroApartamentos.textContent = "Adicione pelo menos um apartamento";
        pendencias.push("Apartamentos da edificação");
    } else {
        erroApartamentos.textContent = "";
    }

    mostrarPendencias(pendencias);
    botaoEnviar.disabled = pendencias.length > 0;
    return pendencias;
}

function mostrarPendencias(pendencias) {
    listaPendencias.replaceChildren();
    pendencias.forEach(texto => {
        const item = document.createElement("li");
        item.textContent = texto;
        listaPendencias.appendChild(item);
    });
    resumoOk.hidden = pendencias.length > 0;
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
            mensagem.textContent = "Envio recusado pelo servidor. Corrija as pendências abaixo.";
            if (checklist && checklist.pendencias) mostrarPendencias(checklist.pendencias);
        }
    } catch (erro) {
        mensagem.classList.add("mensagem-erro");
        mensagem.textContent = "Não foi possível conectar ao servidor. Tente novamente.";
    } finally {
        // só reabilita se a tela ainda estiver completa
        botaoEnviar.disabled = validar().length > 0;
    }
});

campoIluminacao.addEventListener("input", validar);
campoTomadas.addEventListener("input", validar);
botaoAdicionar.addEventListener("click", adicionarApartamento);

// começa com um apartamento em branco
adicionarApartamento();
