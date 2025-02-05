document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";
  const userId = localStorage.getItem("userId");

  // Falls keine User-ID gefunden wird, leite zum Login um
  if (!userId) {
    window.location.href = "index.html";
    return;
  }

  // Kunden für den aktuell angemeldeten User laden
  fetch(`${apiBaseUrl}/customers?userId=${userId}`)
    .then((response) => response.json())
    .then((customers) => {
      // Zeige die Anzahl der Kunden an
      document.getElementById("customerCount").textContent = customers.length;
    })
    .catch((error) => console.error("Error fetching customers:", error));

  // Readings für den aktuell angemeldeten User laden
  fetch(`${apiBaseUrl}/readings?userId=${userId}`)
    .then((response) => response.json())
    .then((readings) => {
      // Zeige die Anzahl der Readings an
      document.getElementById("readingCount").textContent = readings.length;
      buildGenderChart(readings);
      buildReadingChart(readings);
    })
    .catch((error) => console.error("Error fetching readings:", error));

  // Erstellt einen Pie-Chart zur Geschlechterverteilung
  function buildGenderChart(readings) {
    const genderCounts = {};
    readings.forEach((r) => {
      if (r.customer && r.customer.gender) {
        const gender = r.customer.gender;
        genderCounts[gender] = (genderCounts[gender] || 0) + 1;
      }
    });
    const labels = Object.keys(genderCounts);
    const data = Object.values(genderCounts);
    const ctx = document.getElementById("genderChart").getContext("2d");
    new Chart(ctx, {
      type: "pie",
      data: {
        labels: labels,
        datasets: [{
          label: "Geschlechterverteilung",
          data: data,
          backgroundColor: ["#36A2EB", "#FF6384", "#FFCE56"]
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: "bottom" },
          title: { display: true, text: "Verteilung der Geschlechter in den Ablesungen" }
        }
      }
    });
  }

  // Erstellt einen Bar-Chart zur Anzahl der Readings pro Zählertyp
  function buildReadingChart(readings) {
    const meterTypeCounts = {};
    readings.forEach((r) => {
      if (r.kindOfMeter) {
        const meterType = r.kindOfMeter;
        meterTypeCounts[meterType] = (meterTypeCounts[meterType] || 0) + 1;
      }
    });
    const labels = Object.keys(meterTypeCounts);
    const data = Object.values(meterTypeCounts);
    const ctx = document.getElementById("readingChart").getContext("2d");
    new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [{
          label: "Anzahl Ablesungen",
          data: data,
          backgroundColor: "rgba(75, 192, 192, 0.5)",
          borderColor: "rgba(75, 192, 192, 1)",
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { display: false },
          title: { display: true, text: "Ablesungen nach Zählertyp" }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: { precision: 0 }
          }
        }
      }
    });
  }
});
