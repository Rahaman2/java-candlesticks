/* chart.js — Lightweight Charts v4 + STOMP + indicators + pattern markers */

// ── Shared state (declared first — referenced by everything below) ────────────

let latestPatterns = [];
let latestBars     = [];

// ── Chart setup ───────────────────────────────────────────────────────────────

const chart = LightweightCharts.createChart(document.getElementById('chart-container'), {
  layout:          { background: { color: '#131722' }, textColor: '#d1d4dc' },
  grid:            { vertLines: { color: '#2a2e39' }, horzLines: { color: '#2a2e39' } },
  crosshair:       { mode: LightweightCharts.CrosshairMode.Normal },
  rightPriceScale: { borderColor: '#2a2e39' },
  timeScale:       { borderColor: '#2a2e39', timeVisible: true, secondsVisible: false },
  width:  document.getElementById('chart-container').clientWidth,
  height: document.getElementById('chart-container').clientHeight,
});

const candleSeries = chart.addCandlestickSeries({
  upColor:       '#26a69a', downColor:       '#ef5350',
  borderUpColor: '#26a69a', borderDownColor: '#ef5350',
  wickUpColor:   '#26a69a', wickDownColor:   '#ef5350',
});

const volSeries = chart.addHistogramSeries({
  priceFormat:  { type: 'volume' },
  priceScaleId: 'vol',
  color:        '#2a2e39',
});
chart.priceScale('vol').applyOptions({ scaleMargins: { top: 0.82, bottom: 0 } });

const sma20Series  = chart.addLineSeries({ color: '#f6bf26', lineWidth: 1, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });
const ema50Series  = chart.addLineSeries({ color: '#26a69a', lineWidth: 1, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });
const ema200Series = chart.addLineSeries({ color: '#ef5350', lineWidth: 1, priceLineVisible: false, lastValueVisible: false, crosshairMarkerVisible: false });

window.addEventListener('resize', () => {
  chart.resize(
    document.getElementById('chart-container').clientWidth,
    document.getElementById('chart-container').clientHeight
  );
});

// ── Pattern markers ───────────────────────────────────────────────────────────

function buildMarkers(patterns, bars) {
  const timeByIndex = new Map(bars.map((b, i) => [i, b.time]));
  const markers = [];
  patterns.forEach(p => {
    const time = timeByIndex.get(p.index);
    if (time == null) return;
    if (p.direction === 'BULLISH') {
      markers.push({ time, position: 'belowBar', color: '#26a69a', shape: 'arrowUp' });
    } else if (p.direction === 'BEARISH') {
      markers.push({ time, position: 'aboveBar', color: '#ef5350', shape: 'arrowDown' });
    } else {
      markers.push({ time, position: 'inBar',    color: '#f0b429', shape: 'circle' });
    }
  });
  markers.sort((a, b) => a.time - b.time);
  return markers;
}

function refreshMarkers() {
  candleSeries.setMarkers(
    indicators.patterns ? buildMarkers(latestPatterns, latestBars) : []
  );
}

// ── Indicator toggles ─────────────────────────────────────────────────────────

const indicators = { vol: true, sma20: true, ema50: true, ema200: false, patterns: true };

function applyVisibility() {
  volSeries.applyOptions({ visible: indicators.vol });
  sma20Series.applyOptions({ visible: indicators.sma20 });
  ema50Series.applyOptions({ visible: indicators.ema50 });
  ema200Series.applyOptions({ visible: indicators.ema200 });
  refreshMarkers();
}

document.querySelectorAll('[data-ind]').forEach(btn => {
  const key = btn.dataset.ind;
  if (indicators[key]) btn.classList.add('active');
  btn.addEventListener('click', () => {
    indicators[key] = !indicators[key];
    btn.classList.toggle('active', indicators[key]);
    applyVisibility();
  });
});

applyVisibility();

// ── Indicator math ────────────────────────────────────────────────────────────

function computeSMA(bars, period) {
  const out = [];
  for (let i = period - 1; i < bars.length; i++) {
    let sum = 0;
    for (let j = 0; j < period; j++) sum += bars[i - j].close;
    out.push({ time: bars[i].time, value: sum / period });
  }
  return out;
}

function computeEMA(bars, period) {
  if (bars.length < period) return [];
  const k   = 2 / (period + 1);
  const out = [];
  let ema = bars.slice(0, period).reduce((s, b) => s + b.close, 0) / period;
  out.push({ time: bars[period - 1].time, value: ema });
  for (let i = period; i < bars.length; i++) {
    ema = bars[i].close * k + ema * (1 - k);
    out.push({ time: bars[i].time, value: ema });
  }
  return out;
}

function updateIndicators(bars) {
  volSeries.setData(bars.map(b => ({
    time:  b.time,
    value: b.volume || 0,
    color: b.close >= b.open ? 'rgba(38,166,154,0.4)' : 'rgba(239,83,80,0.4)',
  })));
  sma20Series.setData(computeSMA(bars, 20));
  ema50Series.setData(computeEMA(bars, 50));
  ema200Series.setData(computeEMA(bars, 200));
}

// ── STOMP / SockJS ────────────────────────────────────────────────────────────

let stompClient = null;
let candleSub   = null;
let patternSub  = null;
let loadRetry   = null;

const statusEl = document.getElementById('status');

function connect() {
  stompClient = new StompJs.Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 5000,
  });

  stompClient.onConnect = () => {
    statusEl.textContent = 'Connected';
    statusEl.className   = 'connected';
    subscribeToKey(activeKey());
  };

  stompClient.onDisconnect = () => {
    statusEl.textContent = 'Reconnecting…';
    statusEl.className   = '';
  };

  stompClient.onStompError = frame => console.error('STOMP error', frame);
  stompClient.activate();
}

function activeKey() {
  const sym      = document.getElementById('sym-input').value.trim().toUpperCase();
  const interval = document.getElementById('interval-select').value;
  return sym + '_' + interval;
}

function subscribeToKey(key) {
  if (candleSub)  { candleSub.unsubscribe();  candleSub  = null; }
  if (patternSub) { patternSub.unsubscribe(); patternSub = null; }
  if (loadRetry)  { clearTimeout(loadRetry);  loadRetry  = null; }

  latestPatterns = [];
  latestBars     = [];
  candleSeries.setData([]);
  candleSeries.setMarkers([]);

  const [sym, interval] = key.split('_');

  candleSub = stompClient.subscribe('/topic/candles/' + key, msg => {
    const c   = JSON.parse(msg.body);
    const bar = { time: Math.floor(c.ts / 1000), open: c.o, high: c.h, low: c.l, close: c.c, volume: c.v };

    if (latestBars.length && latestBars[latestBars.length - 1].time === bar.time) {
      latestBars[latestBars.length - 1] = bar;
    } else {
      latestBars.push(bar);
    }

    candleSeries.update(bar);
    volSeries.update({
      time:  bar.time,
      value: bar.volume,
      color: bar.close >= bar.open ? 'rgba(38,166,154,0.4)' : 'rgba(239,83,80,0.4)',
    });
  });

  patternSub = stompClient.subscribe('/topic/patterns/' + key, msg => {
    latestPatterns = JSON.parse(msg.body);
    renderPatterns(latestPatterns);
    refreshMarkers();
  });

  loadHistory(sym, interval);
}

function loadHistory(sym, interval, attempt) {
  attempt = attempt || 1;
  fetch(`/api/v1/candles/${sym}/${interval}?limit=300`)
    .then(r => r.json())
    .then(candles => {
      if (candles.length > 0) {
        latestBars = candles.map(c => ({
          time: Math.floor(c.ts / 1000), open: c.o, high: c.h, low: c.l, close: c.c, volume: c.v,
        }));
        candleSeries.setData(latestBars);
        updateIndicators(latestBars);
        chart.timeScale().scrollToRealTime();

        // Fetch patterns immediately — already scanned on server seed, no need to wait for close
        fetch(`/api/v1/patterns/${sym}/${interval}`)
          .then(r => r.json())
          .then(patterns => {
            latestPatterns = patterns;
            renderPatterns(latestPatterns);
            refreshMarkers();
          });
      } else if (attempt < 10) {
        loadRetry = setTimeout(() => loadHistory(sym, interval, attempt + 1), 2000);
      }
    })
    .catch(() => {
      if (attempt < 10) {
        loadRetry = setTimeout(() => loadHistory(sym, interval, attempt + 1), 2000);
      }
    });
}

// ── Pattern table ─────────────────────────────────────────────────────────────

function renderPatterns(patterns) {
  const tbody = document.getElementById('pattern-tbody');
  tbody.innerHTML = '';
  patterns.forEach(p => {
    const cls  = p.direction === 'BULLISH' ? 'bull' : p.direction === 'BEARISH' ? 'bear' : 'neut';
    const icon = p.direction === 'BULLISH' ? '▲'    : p.direction === 'BEARISH' ? '▼'    : '◆';
    const tr   = document.createElement('tr');
    tr.innerHTML = `
      <td>${p.index}</td>
      <td class="${cls}">${icon} ${p.name}</td>
      <td>${p.type}</td>
      <td class="${cls}">${p.direction}</td>
      <td>${p.strength}</td>
      <td class="${cls}">${Math.round(p.confidence * 100)}%</td>`;
    tbody.appendChild(tr);
  });
}

// ── Subscribe button ──────────────────────────────────────────────────────────

document.getElementById('subscribe-btn').addEventListener('click', () => {
  const sym      = document.getElementById('sym-input').value.trim().toUpperCase();
  const interval = document.getElementById('interval-select').value;
  if (!sym) return;

  fetch('/api/v1/subscribe', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ symbol: sym, interval }),
  }).then(() => {
    if (stompClient && stompClient.connected) subscribeToKey(sym + '_' + interval);
  });
});

// ── Boot ──────────────────────────────────────────────────────────────────────

connect();
