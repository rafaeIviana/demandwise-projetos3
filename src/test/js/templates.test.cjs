const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { runInNewContext } = require('node:vm');
const { resolve } = require('node:path');

const source = readFileSync(resolve(__dirname,
    '../../main/resources/static/js/envio.js'), 'utf8');

function setup() {
    const listeners = {};
    const selector = { value: '', addEventListener: (_, fn) => listeners.change = fn };
    const message = {};
    const lighting = { value: '' };
    const outlets = { value: '' };
    const apartments = [];
    const pending = [];
    let resultVisible = true;
    const list = {
        querySelectorAll: () => [...apartments],
        get lastElementChild() { return apartments.at(-1); }
    };
    const context = {
        document: { getElementById: id => id === 'seletor-template'
            ? selector : { addEventListener() {} } },
        mensagem: message, campoIluminacao: lighting, campoTomadas: outlets,
        listaApartamentos: list, validacoesNormativas: new WeakMap(),
        limparResultado: () => resultVisible = false,
        validar() {}, atualizarAlertaNormativo() {},
        adicionarApartamento() {
            const fields = { '.area-util': { value: '' }, '.potencia-instalada': { value: '' } };
            const apartment = {
                querySelector: name => fields[name],
                remove: () => apartments.splice(apartments.indexOf(apartment), 1)
            };
            apartments.push(apartment);
        },
        fetch: url => url === '/api/templates' ? Promise.resolve({ ok: false })
            : new Promise((resolve, reject) => pending.push({ resolve, reject })),
        lerResposta: async response => response.body,
        obterMensagemErro: (_, fallback) => fallback
    };
    runInNewContext(source.slice(source.indexOf('// ===== US-05:')), context);
    return {
        lighting, outlets, apartments, message, pending,
        resultVisible: () => resultVisible,
        select(id) { selector.value = id; return listeners.change(); },
        respond(index, value) {
            pending[index].resolve({ ok: true, body: {
                nome: value, edificacao: { potenciaIluminacao: value, potenciaTomadas: value,
                    apartamentos: [{ areaUtil: 50, potenciaInstalada: 1000 }] }
            } });
        }
    };
}

test('a resposta atrasada de A não substitui B', async () => {
    const ui = setup();
    const a = ui.select('A');
    const b = ui.select('B');
    ui.respond(1, 'B'); await b;
    ui.respond(0, 'A'); await a;
    assert.equal(ui.lighting.value, 'B');
    assert.match(ui.message.textContent, /Template "B"/);
});

test('começar do zero limpa campos, apartamentos e resultado e descarta resposta pendente', async () => {
    const ui = setup();
    const loaded = ui.select('A'); ui.respond(0, 'A'); await loaded;
    const pending = ui.select('B');
    await ui.select('');
    assert.equal(ui.lighting.value, '');
    assert.equal(ui.outlets.value, '');
    assert.equal(ui.apartments.length, 1);
    assert.equal(ui.apartments[0].querySelector('.area-util').value, '');
    assert.equal(ui.apartments[0].querySelector('.potencia-instalada').value, '');
    assert.equal(ui.resultVisible(), false);
    assert.equal(ui.message.textContent, '');
    ui.respond(1, 'B'); await pending;
    assert.equal(ui.lighting.value, '');
    assert.equal(ui.message.textContent, '');
});

test('erro de uma seleção antiga não sobrescreve a mensagem atual', async () => {
    const ui = setup();
    const a = ui.select('A');
    const b = ui.select('B'); ui.respond(1, 'B'); await b;
    ui.pending[0].reject(new Error('Falha de A')); await a;
    assert.match(ui.message.textContent, /Template "B"/);
});
