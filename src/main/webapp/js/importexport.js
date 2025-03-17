document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";

  /**
   * Exportiert Kunden in das gewählte Format.
   */
 function exportCustomers(format) {
     console.log(`Exportiere Kunden als ${format}...`);
     window.location.href = `${apiBaseUrl}/export/customers?format=${format}`;
 }


  /**
   * Importiert Kunden aus der hochgeladenen Datei.
   */
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


          if (!response.ok) {
              throw new Error("Import fehlgeschlagen, HTTP " + response.status);
          }
          alert("Import erfolgreich!");
      } catch (err) {
          console.error("Fehler beim Import:", err);
          alert("Fehler: " + err.message);
      }
  }


  // Event-Listener für die Buttons
  document.getElementById("exportJsonBtn").addEventListener("click", () => exportCustomers("json"));
  document.getElementById("exportXmlBtn").addEventListener("click", () => exportCustomers("xml"));
  document.getElementById("exportCsvBtn").addEventListener("click", () => exportCustomers("csv"));
  document.getElementById("importBtn").addEventListener("click", importCustomers);
});
