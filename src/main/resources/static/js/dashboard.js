// US-10 — busca as métricas na API e monta o painel
document.addEventListener("DOMContentLoaded", carregarDashboard);

const formatador = new Intl.NumberFormat("pt-BR", { minimumFractionDigits: 1, maximumFractionDigits: 1 });

function porcentagem(valor) {
    return formatador.format(valor) + "%";
}

async function carregarDashboard() {
    const mensagem = document.getElementById("mensagem");

    try {
        const resposta = await fetch("/api/dashboard/metricas");
        if (!resposta.ok) {
            throw new Error("HTTP " + resposta.status);
        }
        const dados = await resposta.json();

        preencherCards(dados);
        desenharGrafico(dados);
        preencherTabela(dados.meses);
    } catch (erro) {
        mensagem.textContent = "Não foi possível carregar as métricas. Tente novamente mais tarde.";
        mensagem.classList.add("mensagem-erro");
        console.error(erro);
    }
}

function preencherCards(dados) {
    const antes = dados.meses.filter(m => !m.aposAdocao);
    const depois = dados.meses.filter(m => m.aposAdocao);

    document.getElementById("taxa-antes").textContent = porcentagem(dados.taxaMediaAntes);
    document.getElementById("taxa-depois").textContent = porcentagem(dados.taxaMediaDepois);
    document.getElementById("reducao").textContent = porcentagem(dados.reducaoPercentual);

    if (antes.length) {
        document.getElementById("detalhe-antes").textContent =
            antes[0].mes + " a " + antes[antes.length - 1].mes;
    }
    if (depois.length) {
        document.getElementById("detalhe-depois").textContent =
            depois[0].mes + " a " + depois[depois.length - 1].mes + " (adoção em " + dados.mesAdocao + ")";
    }
}

function desenharGrafico(dados) {
    const grafico = document.getElementById("grafico");
    const maiorTaxa = Math.max(...dados.meses.map(m => m.taxaReprovacao), 1);
    // folga de 15% acima da maior barra para caber o valor escrito em cima
    const escala = maiorTaxa * 1.15;

    grafico.innerHTML = "";

    dados.meses.forEach(m => {
        const coluna = document.createElement("div");
        coluna.className = "dash-coluna";
        if (m.mes === dados.mesAdocao) {
            coluna.classList.add("dash-marco");
        }

        // trilho: área onde a barra cresce de baixo para cima
        const trilho = document.createElement("div");
        trilho.className = "dash-trilho";

        const valor = document.createElement("span");
        valor.className = "dash-valor-barra";
        valor.textContent = porcentagem(m.taxaReprovacao);

        const barra = document.createElement("div");
        barra.className = "dash-barra " + (m.aposAdocao ? "dash-barra-depois" : "dash-barra-antes");
        barra.style.height = (m.taxaReprovacao / escala * 100) + "%";
        barra.title = m.mes + ": " + porcentagem(m.taxaReprovacao)
            + " (" + m.projetosReprovados + " de " + m.projetosAnalisados + " projetos)";

        const mes = document.createElement("span");
        mes.className = "dash-mes";
        mes.textContent = m.mes;

        trilho.append(valor, barra);
        coluna.append(trilho, mes);
        grafico.appendChild(coluna);
    });
}

function preencherTabela(meses) {
    const corpo = document.getElementById("tabela-meses");
    corpo.innerHTML = "";

    meses.forEach(m => {
        const linha = document.createElement("tr");
        const periodo = m.aposAdocao
            ? '<span class="dash-tag dash-tag-depois">Depois</span>'
            : '<span class="dash-tag dash-tag-antes">Antes</span>';

        linha.innerHTML =
            "<td>" + m.mes + "</td>" +
            "<td>" + periodo + "</td>" +
            '<td class="num">' + m.projetosAnalisados + "</td>" +
            '<td class="num">' + m.projetosReprovados + "</td>" +
            '<td class="num">' + porcentagem(m.taxaReprovacao) + "</td>";
        corpo.appendChild(linha);
    });
}
