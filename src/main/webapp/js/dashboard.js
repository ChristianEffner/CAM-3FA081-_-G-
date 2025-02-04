document.addEventListener("DOMContentLoaded", async function () {
    // Überprüfung, ob der Nutzer eingeloggt ist
    const isLoggedIn = localStorage.getItem("isLoggedIn");
    if (isLoggedIn !== "true") {
        window.location.href = "../index.html";
    }

    // Base URL für die API
    const apiBaseUrl = "http://localhost:8080";

    // Elemente für Statistiken
    const customerCountElement = document.getElementById("customerCount");
    const readingCountElement = document.getElementById("readingCount");

    try {
        // Kundenanzahl laden
        const customersResponse = await fetch(`${apiBaseUrl}/customers`);
        const customers = await customersResponse.json();
        const customerCount = customers.length;
        customerCountElement.textContent = customerCount;

        // Ablesungsanzahl laden
        const readingsResponse = await fetch(`${apiBaseUrl}/readings`);
        const readings = await readingsResponse.json();
        const readingCount = readings.length;
        readingCountElement.textContent = readingCount;

        // Diagramme initialisieren
        initGenderChart(customers);
        initReadingChart(readings);
    } catch (error) {
        console.error("Fehler beim Laden der Statistiken:", error);
    }

    // Kuchendiagramm für Geschlechterverteilung
    function initGenderChart(customers) {
        const genderCounts = customers.reduce(
            (acc, customer) => {
                acc[customer.gender] = (acc[customer.gender] || 0) + 1;
                return acc;
            },
            { M: 0, W: 0, D: 0, U: 0 }
        );

        const ctx = document.getElementById("genderChart").getContext("2d");
        new Chart(ctx, {
            type: "pie",
            data: {
                labels: ["Männlich", "Weiblich", "Divers", "Unbekannt"],
                datasets: [
                    {
                        label: "Geschlechterverteilung",
                        data: [
                            genderCounts.M,
                            genderCounts.W,
                            genderCounts.D,
                            genderCounts.U,
                        ],
                        backgroundColor: ["#007bff", "#dc3545", "#ffc107", "#6c757d"],
                    },
                ],
            },
            options: {
                plugins: {
                    legend: {
                        position: "bottom",
                    },
                },
            },
        });
    }

    // Linien- und Flächendiagramm für Ablesungen pro Monat
    function initReadingChart(readings) {
        const monthlyCounts = readings.reduce((acc, reading) => {
            const month = new Date(reading.dateOfReading).getMonth();
            acc[month] = (acc[month] || 0) + 1;
            return acc;
        }, {});

        const months = [
            "Januar",
            "Februar",
            "März",
            "April",
            "Mai",
            "Juni",
            "Juli",
            "August",
            "September",
            "Oktober",
            "November",
            "Dezember",
        ];

        const data = months.map((_, i) => monthlyCounts[i] || 0);

        const ctx = document.getElementById("readingChart").getContext("2d");
        new Chart(ctx, {
            type: "line",
            data: {
                labels: months,
                datasets: [
                    {
                        label: "Ablesungen",
                        data: data,
                        backgroundColor: "rgba(0, 123, 255, 0.2)",
                        borderColor: "#007bff",
                        fill: true,
                        tension: 0.4,
                    },
                ],
            },
            options: {
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: "Monate",
                        },
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: "Anzahl der Ablesungen",
                        },
                    },
                },
                plugins: {
                    legend: {
                        position: "top",
                    },
                },
            },
        });
    }
});
