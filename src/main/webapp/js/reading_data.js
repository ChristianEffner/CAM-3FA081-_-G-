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

  // Baue Chart
  function buildChart(readings) {
    // Gruppiere nach Zählertyp
    const grouped = {};
    readings.forEach((r) => {
      grouped[r.kindOfMeter] = grouped[r.kindOfMeter] || [];
      grouped[r.kindOfMeter].push(r);
    });

    const datasets = Object.entries(grouped).map(([kind, arr]) => {
      arr.sort((a, b) =>
        new Date(a.dateOfReading) - new Date(b.dateOfReading)
      );
      return {
        label: kind,
        data: arr.map((r) => ({
          x: r.dateOfReading,
          y: r.meterCount
        })),
        fill: false,
        tension: 0.3,
        pointRadius: 5
      };
    });

    const ctx = document.getElementById("lineChart").getContext("2d");
    if (chart) chart.destroy();
    chart = new Chart(ctx, {
      type: "line",
      data: { datasets },
      options: {
        parsing: false,
        plugins: {
          title: {
            display: true,
            text: "Ablesewerte über Zeit"
          },
          legend: { position: "bottom" }
        },
        scales: {
          x: {
            type: "time",
            time: { unit: "day" }
          },
          y: {
            beginAtZero: true
          }
        },
        responsive: true
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
