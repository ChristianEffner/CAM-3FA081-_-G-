document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";

  // Hilfsfunktion zur Erzeugung einer UUID (RFC4122 Version 4)
  function generateUUID() {
    let d = new Date().getTime();
    let d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      let r = Math.random() * 16;
      if (d > 0) {
        r = (d + r) % 16 | 0;
        d = Math.floor(d / 16);
      } else {
        r = (d2 + r) % 16 | 0;
        d2 = Math.floor(d2 / 16);
      }
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
  }

  // Merkt sich das aktuell zu bearbeitende Reading-Objekt
  let currentReading = null;

  /**
   * Lädt alle Readings vom Server.
   * Optionaler Filter: userId (aus localStorage), kindOfMeter (aus Parameter)
   */
  async function loadReadings(kindOfMeter = "") {
    console.log("Lade Readings, Filter =", kindOfMeter);
    try {
      const userId = localStorage.getItem("userId");
      let url = `${apiBaseUrl}/readings`;
      const queryParams = [];
      if (userId) {
        queryParams.push(`userId=${userId}`);
      }
      if (kindOfMeter) {
        queryParams.push(`kindOfMeter=${encodeURIComponent(kindOfMeter)}`);
      }
      if (queryParams.length > 0) {
        url += "?" + queryParams.join("&");
      }
      console.log("Request-URL =", url);
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Fehler beim Laden der Ablesungen (Status: ${response.status})`);
      }
      const readings = await response.json();
      console.log("Readings empfangen:", readings);
      renderReadings(readings);
    } catch (error) {
      console.error("Error loading readings:", error);
      alert("Fehler beim Laden der Ablesungen: " + error.message);
    }
  }

  /**
   * Fügt die geladenen Readings in die Tabelle ein.
   */
  function renderReadings(readings) {
    const tableBody = document.getElementById("readingTableBody");
    tableBody.innerHTML = "";
    readings.forEach((reading) => {
      const row = document.createElement("tr");
      row.innerHTML =
        `<td>${reading.id}</td>
         <td>
           ${reading.customer?.firstName || "?"}
           ${reading.customer?.lastName || "?"}
         </td>
         <td>${reading.dateOfReading}</td>
         <td>${reading.kindOfMeter}</td>
         <td>${reading.meterCount}</td>
         <td>
           <button class="btn btn-warning btn-edit" data-id="${reading.id}">Bearbeiten</button>
           <button class="btn btn-danger btn-delete" data-id="${reading.id}">Löschen</button>
         </td>`;
      tableBody.appendChild(row);
    });
    attachRowEventListeners();
  }

  function attachRowEventListeners() {
    document.querySelectorAll(".btn-edit").forEach((btn) =>
      btn.addEventListener("click", handleEdit)
    );
    document.querySelectorAll(".btn-delete").forEach((btn) =>
      btn.addEventListener("click", handleDelete)
    );
  }

  /**
   * Klick auf "Bearbeiten": Einzelnes Reading holen und im Modal anzeigen.
   */
  async function handleEdit(event) {
    const id = event.target.dataset.id;
    console.log("Bearbeiten geklickt, ID =", id);
    try {
      const response = await fetch(`${apiBaseUrl}/readings/${id}`);
      if (!response.ok) {
        throw new Error(`Fehler beim Laden (ID=${id}), Status=${response.status}`);
      }
      const reading = await response.json();
      console.log("Reading zum Bearbeiten:", reading);
      currentReading = reading;
      document.getElementById("editReadingId").value = reading.id || "";
      document.getElementById("editKindOfMeter").value = reading.kindOfMeter || "";
      document.getElementById("editMeterCount").value = reading.meterCount || 0;
      document.getElementById("editDateOfReading").value = reading.dateOfReading || "";
      document.getElementById("editComment").value = reading.comment || "";
      const editModal = new bootstrap.Modal(document.getElementById("editReadingModal"));
      editModal.show();
    } catch (error) {
      console.error("Error editing reading:", error);
      alert("Fehler beim Bearbeiten: " + error.message);
    }
  }

  /**
   * Klick auf "Speichern" im Bearbeiten-Modal => PUT /readings
   */
  document.getElementById("updateReadingBtn").addEventListener("click", async () => {
    console.log("Update-Button geklickt!");
    if (!currentReading) {
      alert("Keine Ablesung zum Bearbeiten geladen!");
      return;
    }
    const id = document.getElementById("editReadingId").value.trim();
    const kindOfMeter = document.getElementById("editKindOfMeter").value.trim();
    const meterCount = parseFloat(document.getElementById("editMeterCount").value);
    const dateOfReading = document.getElementById("editDateOfReading").value.trim();
    const comment = document.getElementById("editComment").value.trim();
    if (!id || !kindOfMeter || isNaN(meterCount) || !dateOfReading) {
      alert("Bitte alle Felder korrekt ausfüllen.");
      return;
    }
    currentReading.id = id;
    currentReading.kindOfMeter = kindOfMeter;
    currentReading.meterCount = meterCount;
    currentReading.dateOfReading = dateOfReading;
    currentReading.comment = comment;
    console.log("PUT /readings =>", currentReading);
    try {
      const response = await fetch(`${apiBaseUrl}/readings`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(currentReading)
      });
      if (!response.ok) {
        throw new Error("Fehler beim Aktualisieren (Status: " + response.status + ")");
      }
      alert("Ablesung erfolgreich aktualisiert!");
      bootstrap.Modal.getInstance(document.getElementById("editReadingModal")).hide();
      currentReading = null;
      loadReadings();
    } catch (error) {
      console.error("Error updating reading:", error);
      alert("Fehler beim Aktualisieren: " + error.message);
    }
  });

  /**
   * Klick auf "Löschen" => DELETE /readings/{id}
   */
  async function handleDelete(event) {
    const id = event.target.dataset.id;
    console.log("Löschen geklickt, ID =", id);
    if (!confirm("Soll diese Ablesung wirklich gelöscht werden?")) {
      return;
    }
    try {
      const response = await fetch(`${apiBaseUrl}/readings/${id}`, {
        method: "DELETE"
      });
      if (!response.ok) {
        throw new Error("Fehler beim Löschen (Status: " + response.status + ")");
      }
      alert("Ablesung erfolgreich gelöscht!");
      loadReadings();
    } catch (error) {
      console.error("Error deleting reading:", error);
      alert("Fehler beim Löschen: " + error.message);
    }
  }

  /**
   * Klick auf "Speichern" bei neuer Ablesung => POST /readings
   * Wichtig: Der Kunde muss existieren – falls nicht, wird er automatisch für den aktiven User angelegt.
   */
  document.getElementById("saveReadingBtn").addEventListener("click", async () => {
    console.log("Neue Ablesung speichern geklickt!");

    let readingId = document.getElementById("readingId").value.trim();
    if (!readingId) {
      readingId = undefined; // Server generiert eine UUID, falls nicht gesetzt
    }
    const comment = document.getElementById("comment").value.trim();

    // Kunden-Daten
    let customerId = document.getElementById("customerId").value.trim();
    if (!customerId) {
      // Falls keine Kunden-ID eingegeben wurde, generiere eine neue UUID
      customerId = generateUUID();
    }
    const customerFirstName = document.getElementById("customerFirstName").value.trim();
    const customerLastName = document.getElementById("customerLastName").value.trim();
    const customerBirthDate = document.getElementById("customerBirthDate").value.trim();
    const customerGender = document.getElementById("customerGender").value;

    // Validierung der Pflichtfelder für den Kunden
    if (!customerFirstName || !customerLastName || !customerBirthDate) {
      alert("Bitte füllen Sie alle Kundenfelder aus (Vorname, Nachname, Geburtsdatum).");
      return;
    }

    const dateOfReading = document.getElementById("dateOfReading").value.trim();
    const kindOfMeter = document.getElementById("kindOfMeter").value.trim();
    const meterCount = parseFloat(document.getElementById("meterCount").value);
    const meterId = document.getElementById("meterId").value.trim();
    const substituteInput = document.getElementById("substitute").value.trim();
    // Konvertiere substitute in Boolean: "1" oder "true" (unabhängig von Groß-/Kleinschreibung) wird zu true, sonst false.
    const substitute = (substituteInput === "1" || substituteInput.toLowerCase() === "true");

    // Validierung der Pflichtfelder für die Reading
    if (!kindOfMeter || isNaN(meterCount) || !dateOfReading || !customerId) {
      alert("Bitte die Pflichtfelder (Kunden-ID, Zählertyp, Zählerstand, Datum) korrekt ausfüllen.");
      return;
    }

    const newReading = {
      id: readingId,
      comment: comment,
      customer: {
        id: customerId,
        firstName: customerFirstName,
        lastName: customerLastName,
        birthDate: customerBirthDate,
        gender: customerGender
      },
      dateOfReading: dateOfReading,
      kindOfMeter: kindOfMeter,
      meterCount: meterCount,
      meterId: meterId || "METER001",
      substitute: substitute
    };

    console.log("POST /readings =>", newReading);

    try {
      const response = await fetch(`${apiBaseUrl}/readings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newReading)
      });
      if (!response.ok) {
        throw new Error("Fehler beim Speichern (Status: " + response.status + ")");
      }
      alert("Ablesung erfolgreich gespeichert!");
      bootstrap.Modal.getInstance(document.getElementById("addReadingModal")).hide();
      loadReadings();
    } catch (error) {
      console.error("Error saving reading:", error);
      alert("Fehler beim Speichern: " + error.message);
    }
  });

  // Klick auf den Filter-Button: => loadReadings(filterValue)
  document.getElementById("filterButton").addEventListener("click", () => {
    const filterValue = document.getElementById("filterKindOfMeter").value.trim();
    loadReadings(filterValue);
  });

  // Beim Laden der Seite: alle Readings ohne Filter
  loadReadings();
});
