document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";

  // Filter‐State initial aus URL holen
  const params = new URLSearchParams(window.location.search);
  const readingId = params.get("readingId");
  const filter = {
    kind: params.get("kind") || "",
    start: params.get("start") || "",
    end:   params.get("end")   || ""
  };

  let customerId = null;
  let chart = null;

  function formatDate(value) {
    // Array ([2025,5,1])
    if (Array.isArray(value) && value.length === 3) {
      const [y, m, d] = value;
      const mm = String(m).padStart(2,"0");
      const dd = String(d).padStart(2,"0");
      return `${dd}.${mm}.${y}`;
    }
    // echtes JS-Date
    if (value instanceof Date) {
      const iso = value.toISOString().slice(0,10);
      return formatDate(iso);
    }
    // ISO-String "yyyy-mm-dd"
    if (typeof value === "string") {
      const m = value.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
      if (m) {
        const [, y, mm, dd] = m;
        return `${dd.padStart(2,"0")}.${mm.padStart(2,"0")}.${y}`;
      }
      return value;
    }
    // Fallback
    return String(value);
  }

  // 1) Hole Reading, um customerId zu ermitteln
  async function fetchCustomerId() {
    const res = await fetch(`${apiBaseUrl}/readings/${readingId}`);
    if (!res.ok) throw new Error("Lesen der Ablesung fehlgeschlagen");
    const reading = await res.json();
    customerId = reading.customer.id;
  }

  // 2) Lade alle Readings für diesen Kunden + Filter
  async function fetchReadings() {
    let url = `${apiBaseUrl}/readings?customer=${customerId}`;
    if (filter.start) url += `&start=${filter.start}`;
    if (filter.end)   url += `&end=${filter.end}`;
    if (filter.kind)  url += `&kindOfMeter=${filter.kind}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error("Lesen der Ablesungen fehlgeschlagen");
    return await res.json();
  }

async function buildChart(readings) {
    // Debug…
    console.log("→ buildChart got readings:", readings);

    const toDate = raw => {
      if (Array.isArray(raw) && raw.length === 3) {
        const [y,m,d] = raw;
        const mm = String(m).padStart(2,"0"),
              dd = String(d).padStart(2,"0");
        return new Date(`${y}-${mm}-${dd}`);
      }
      return new Date(raw);
    };

    // Gruppieren & datasets erzeugen…
    const grouped = {};
    readings.forEach(r => {
      (grouped[r.kindOfMeter] ||= []).push(r);
    });

    const datasets = Object.entries(grouped).map(([kind, arr]) => {
      arr.sort((a,b) => toDate(a.dateOfReading) - toDate(b.dateOfReading));
      const data = arr.map(r => ({ x: toDate(r.dateOfReading), y: r.meterCount }));
      console.log(`→ Dataset ${kind}:`, data);
      return {
        label: kind,
        data,
        fill: false,
        tension: 0.3,
        pointRadius: 5
      };
    });

    // Domain ermitteln
    const allTs = datasets.flatMap(ds => ds.data.map(p => p.x.getTime()));
    const min = new Date(Math.min(...allTs));
    const max = new Date(Math.max(...allTs));
    console.log("→ x-Domain:", min, max);

    // Chart rendern
    const ctx = document.getElementById("lineChart").getContext("2d");
    if (chart) chart.destroy();
chart = new Chart(ctx, {
  type: "line",
  data: { datasets },
  options: {
    parsing: false,
    responsive: true,

    // → so wird der nächste Punkt schon vor Überschneiden getriggert
    interaction: {
      mode: "nearest",
      intersect: false
    },

    plugins: {
      title: {
        display: true,
        text: "Ablesewerte über Zeit",
        padding: { bottom: 15 }
      },
      legend: {
        display: true,
        position: "bottom",
        align: "center",
        labels: { boxWidth:20, boxHeight:12, padding:16 }
      },
      tooltip: {
        // wir bauen das Datum selbst im Tooltip‐Titel zusammen
        callbacks: {
          title: (items) => {
            const raw = items[0].parsed.x;         // JS-Timestamp oder Date
            const d   = new Date(raw);
            const dd  = String(d.getDate()).padStart(2,"0");
            const mm  = String(d.getMonth()+1).padStart(2,"0");
            const yyyy= d.getFullYear();
            return `${dd}.${mm}.${yyyy}`;         // z.B. "02.05.2025"
          },
          label: (ctx) => `${ctx.dataset.label}: ${ctx.parsed.y}`
        }
      }
    },

    scales: {
      x: {
        type: "time",
        offset: true,
        time: {
          unit: "day",
          // so sehen auch die Achsenlabels DD.MM
          displayFormats: { day: "dd.MM" }
        },
        ticks: {
          callback: (v) => {
            const d  = new Date(v);
            const dd = String(d.getDate()).padStart(2,"0");
            const mm = String(d.getMonth()+1).padStart(2,"0");
            return `${dd}.${mm}`;
          },
          maxRotation: 0,
          autoSkip: true,
          padding: 10
        },
        min,
        max
      },
      y: {
        beginAtZero: false
      }
    }
  }
});
}

  // Tabelle rendern
  function renderTable(readings) {
    const tbody = document.getElementById("readingDataTableBody");
    tbody.innerHTML = "";
    readings.forEach((r) => {
      tbody.insertAdjacentHTML(
        "beforeend",
        `<tr>
           <td>${r.id}</td>
           <td>${r.customer?.firstName || "?"} ${r.customer?.lastName || ""}</td>
           <td>${formatDate(r.dateOfReading)}</td>
           <td>${r.kindOfMeter}</td>
           <td>${r.meterCount}</td>
           <td>${r.comment || ""}</td>
         </tr>`
      );
    });
    // MutationObserver in HTML kümmert sich um Kürzen/Formatieren
  }

  // Haupt‐Flow
  async function loadAll() {
    try {
      await fetchCustomerId();
      const all = await fetchReadings();
      buildChart(all);
      renderTable(all);
    } catch (e) {
      console.error(e);
      alert(e.message);
    }
  }

  // Filter‐Modal → neu laden
  const advForm = document.getElementById("advFilterForm");
  const advModal = new bootstrap.Modal(
    document.getElementById("advFilterModal")
  );

  advForm.addEventListener("submit", (e) => {
    e.preventDefault();
    filter.kind = document.getElementById("fKind").value;
    filter.start = document.getElementById("fStart").value;
    filter.end = document.getElementById("fEnd").value;
    advModal.hide();
    loadAll();
  });

  document.getElementById("resetFilter").onclick = () => {
    advForm.reset();
    filter.kind = filter.start = filter.end = "";
    loadAll();
  };

  // Kickoff
  loadAll();
});
