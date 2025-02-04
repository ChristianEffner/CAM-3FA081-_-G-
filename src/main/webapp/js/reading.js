document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";

  // Merkt sich das aktuell zu bearbeitende Reading-Objekt
  let currentReading = null;

  /**
   * Lädt alle Readings vom Server.
   * - Wenn "kindOfMeter" leer, dann GET /readings (alle)
   * - Sonst GET /readings?kindOfMeter=XXX
   */
  async function loadReadings(kindOfMeter = "") {
    console.log("Lade Readings, Filter =", kindOfMeter);
    try {
      let url;
      if (kindOfMeter) {
        url = `${apiBaseUrl}/readings?kindOfMeter=${encodeURIComponent(kindOfMeter)}`;
      } else {
        url = `${apiBaseUrl}/readings`;
      }

      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(
          `Fehler beim Laden der Ablesungen (Status: ${response.status})`
        );
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
      row.innerHTML = `
        <td>${reading.id}</td>
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
        </td>
      `;
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
   * => GET /readings/{id}
   */
  async function handleEdit(event) {
    const id = event.target.dataset.id;
    console.log("Bearbeiten geklickt, ID =", id);

    try {
      const response = await fetch(`${apiBaseUrl}/readings/${id}`);
      if (!response.ok) {
        throw new Error(
          `Fehler beim Laden (ID=${id}), Status=${response.status}`
        );
      }
      const reading = await response.json();
      console.log("Reading zum Bearbeiten:", reading);

      currentReading = reading;

      // Formularfelder im Modal befüllen
      document.getElementById("editReadingId").value = reading.id || "";
      document.getElementById("editKindOfMeter").value = reading.kindOfMeter || "";
      document.getElementById("editMeterCount").value = reading.meterCount || 0;
      document.getElementById("editDateOfReading").value = reading.dateOfReading || "";
      document.getElementById("editComment").value = reading.comment || "";

      // Falls du meterId/substitute auch bearbeiten möchtest,
      // könntest du hier weitere Felder ergänzen.

      // Modal anzeigen
      const editModal = new bootstrap.Modal(document.getElementById("editReadingModal"));
      editModal.show();

    } catch (error) {
      console.error("Error editing reading:", error);
      alert("Fehler beim Bearbeiten: " + error.message);
    }
  }

  /**
   * Klick auf "Speichern" im Bearbeiten-Modal
   * => PUT /readings (mit komplettem JSON)
   */
  document.getElementById("updateReadingBtn").addEventListener("click", async () => {
    console.log("Update-Button geklickt!");
    if (!currentReading) {
      alert("Keine Ablesung zum Bearbeiten geladen!");
      return;
    }

    // Geänderte Felder
    const id = document.getElementById("editReadingId").value.trim();
    const kindOfMeter = document.getElementById("editKindOfMeter").value.trim();
    const meterCount = parseFloat(document.getElementById("editMeterCount").value);
    const dateOfReading = document.getElementById("editDateOfReading").value.trim();
    const comment = document.getElementById("editComment").value.trim();

    if (!id || !kindOfMeter || isNaN(meterCount) || !dateOfReading) {
      alert("Bitte alle Felder korrekt ausfüllen.");
      return;
    }

    // Wir nehmen das alte Objekt (currentReading) und aktualisieren nur die geänderten Felder.
    currentReading.id = id;
    currentReading.kindOfMeter = kindOfMeter;
    currentReading.meterCount = meterCount;
    currentReading.dateOfReading = dateOfReading;
    currentReading.comment = comment;

    // Falls dein Objekt immer "meterId", "substitute", "customer" usw. braucht,
    // sind diese Felder noch in currentReading drin (vom GET), wir überschreiben sie nicht.

    console.log("PUT /readings =>", currentReading);
    try {
      const response = await fetch(`${apiBaseUrl}/readings`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(currentReading),
      });
      if (!response.ok) {
        throw new Error(
          "Fehler beim Aktualisieren (Status: " + response.status + ")"
        );
      }
      alert("Ablesung erfolgreich aktualisiert!");

      // Modal schließen
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
        throw new Error(
          "Fehler beim Löschen (Status: " + response.status + ")"
        );
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
   * Laut deinem Beispiel braucht dein Server diesen kompletten Body:
   * {
   *   "id": "...",
   *   "comment": "...",
   *   "customer": { "id": "...", "firstName": "...", ... },
   *   "dateOfReading": "...",
   *   "kindOfMeter": "...",
   *   "meterCount": 0,
   *   "meterId": "...",
   *   "substitute": 0
   * }
   */
  document.getElementById("saveReadingBtn").addEventListener("click", async () => {
    console.log("Neue Ablesung speichern geklickt!");

    const readingId = document.getElementById("readingId").value.trim(); // optional
    const comment = document.getElementById("comment").value.trim();

    // Kunden-Felder
    const customerId = document.getElementById("customerId").value.trim();
    const customerFirstName = document.getElementById("customerFirstName").value.trim();
    const customerLastName = document.getElementById("customerLastName").value.trim();
    const customerBirthDate = document.getElementById("customerBirthDate").value.trim();
    const customerGender = document.getElementById("customerGender").value;

    const dateOfReading = document.getElementById("dateOfReading").value.trim();
    const kindOfMeter = document.getElementById("kindOfMeter").value.trim();
    const meterCount = parseFloat(document.getElementById("meterCount").value);
    const meterId = document.getElementById("meterId").value.trim();
    const substitute = parseFloat(document.getElementById("substitute").value);

    // Minimale Plausibilitäten
    if (!kindOfMeter || isNaN(meterCount) || !dateOfReading || !customerId) {
      alert("Bitte die Pflichtfelder (Kunden-ID, Zählertyp, Zählerstand, Datum) korrekt ausfüllen.");
      return;
    }

    // Kompletter Body nach deinem Beispiel
    const newReading = {
      // Nur setzen, wenn du nicht willst, dass der Server die ID generiert
      // Wenn dein Server bei POST eine ID erwartet, fülle readingId aus
      // oder lass es weg, wenn der Server das selber macht.
      id: readingId || undefined,

      comment: comment || "",
      customer: {
        id: customerId,
        firstName: customerFirstName,
        lastName: customerLastName,
        birthDate: customerBirthDate,
        gender: customerGender
      },
      dateOfReading,
      kindOfMeter,
      meterCount,
      meterId: meterId || "METER001",  // Standard, falls leer
      substitute: isNaN(substitute) ? 0 : substitute
    };

    console.log("POST /readings =>", newReading);

    try {
      const response = await fetch(`${apiBaseUrl}/readings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newReading),
      });
      if (!response.ok) {
        throw new Error(
          "Fehler beim Speichern (Status: " + response.status + ")"
        );
      }
      alert("Ablesung erfolgreich gespeichert!");

      bootstrap.Modal
        .getInstance(document.getElementById("addReadingModal"))
        .hide();

      loadReadings();

    } catch (error) {
      console.error("Error saving reading:", error);
      alert("Fehler beim Speichern: " + error.message);
    }
  });

  /**
   * Klick auf den Filter-Button:
   * => loadReadings(filterValue)
   */
  document.getElementById("filterButton").addEventListener("click", () => {
    const filterValue = document.getElementById("filterKindOfMeter").value.trim();
    loadReadings(filterValue);
  });

  // Beim Laden der Seite: alle Readings ohne Filter
  loadReadings();
});
