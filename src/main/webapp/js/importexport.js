document.addEventListener("DOMContentLoaded", () => {
    const apiBaseUrl = "http://localhost:8080";

    // === Kunden Export ===
    function exportCustomers(format) {
        console.log(`Exportiere Kunden als ${format}...`);
        window.location.href = `${apiBaseUrl}/export/customers?format=${format}`;
    }

    // === Kunden Import ===
    async function importCustomers() {
        const fileInput = document.getElementById("fileInput");
        if (!fileInput.files.length) {
            alert("Bitte eine Datei auswählen.");
            return;
        }

        const format = document.getElementById("importFormat").value;
        const file = fileInput.files[0];

        const formData = new FormData();
        formData.append("file", file);

        try {
            console.log(`Importiere Kunden aus ${file.name} (${format})...`);
            const response = await fetch(`${apiBaseUrl}/import/customers?format=${format}`, {
                method: "POST",
                body: formData
            });

            const result = await response.text();
            if (!response.ok) throw new Error(result);

            alert("✅ Kundenimport erfolgreich!");
        } catch (err) {
            console.error("Fehler beim Kundenimport:", err);
            alert("❌ Fehler: " + err.message);
        }
    }

    // === Ablesedaten Export ===
    function exportReadings(format) {
        console.log(`Exportiere Ablesedaten als ${format}...`);
        window.location.href = `${apiBaseUrl}/export/readings?format=${format}&userId=1`;
    }

    // === Ablesedaten Import ===
    async function importReadings() {
        const fileInput = document.getElementById("filereadingInput");
        if (!fileInput.files.length) {
            alert("Bitte eine Datei auswählen.");
            return;
        }

        const format = document.getElementById("importreadingFormat").value;
        const file = fileInput.files[0];

        const formData = new FormData();
        formData.append("file", file);

        try {
            console.log(`Importiere Ablesedaten aus ${file.name} (${format})...`);
            const response = await fetch(`${apiBaseUrl}/import/readings?format=${format}`, {
                method: "POST",
                body: formData
            });

            const result = await response.text();
            if (!response.ok) throw new Error(result);

            alert("✅ Ablesedatenimport erfolgreich!");
        } catch (err) {
            console.error("Fehler beim Ablesedatenimport:", err);
            alert("❌ Fehler: " + err.message);
        }
    }

    // === Event-Listener Kunden ===
    document.getElementById("exportJsonBtn").addEventListener("click", () => exportCustomers("json"));
    document.getElementById("exportXmlBtn").addEventListener("click", () => exportCustomers("xml"));
    document.getElementById("exportCsvBtn").addEventListener("click", () => exportCustomers("csv"));
    document.getElementById("importBtn").addEventListener("click", importCustomers);

    // === Event-Listener Ablesedaten ===
    document.getElementById("exportcustJsonBtn").addEventListener("click", () => exportReadings("json"));
    document.getElementById("exportcustXmlBtn").addEventListener("click", () => exportReadings("xml"));
    document.getElementById("exportcustCsvBtn").addEventListener("click", () => exportReadings("csv"));
    document.getElementById("importreadingBtn").addEventListener("click", importReadings);
});
