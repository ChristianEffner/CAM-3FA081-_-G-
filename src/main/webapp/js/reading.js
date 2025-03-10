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

  // Globale Variablen
  let currentReading = null;
  let allReadings = [];      // Für automatische meterId-Berechnung
  let allCustomers = [];     // Für den Dropdown "bestehende Kunden"

  // --- Funktion: handleEdit ---
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

  // --- Funktion: handleDelete ---
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

  // --- Funktion: attachRowEventListeners ---
  function attachRowEventListeners() {
    document.querySelectorAll(".btn-edit").forEach((btn) =>
      btn.addEventListener("click", handleEdit)
    );
    document.querySelectorAll(".btn-delete").forEach((btn) =>
      btn.addEventListener("click", handleDelete)
    );
  }

  // --- Funktion: loadReadings ---
  async function loadReadings(filterKindOfMeter = "") {
    console.log("Lade Readings, Filter =", filterKindOfMeter);
    try {
      const userId = localStorage.getItem("userId");
      if (!userId) {
        throw new Error("Kein gültiger Benutzer angemeldet.");
      }
      let url = `${apiBaseUrl}/readings?userId=${userId}`;
      if (filterKindOfMeter) {
        url += `&kindOfMeter=${encodeURIComponent(filterKindOfMeter)}`;
      }
      console.log("Request-URL =", url);
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Fehler beim Laden der Ablesungen (Status: ${response.status})`);
      }
      const readings = await response.json();
      allReadings = readings; // Für meterId-Berechnung
      console.log("Readings empfangen:", readings);
      renderReadings(readings);
    } catch (error) {
      console.error("Error loading readings:", error);
      alert("Fehler beim Laden der Ablesungen: " + error.message);
    }
  }

  // --- Funktion: renderReadings ---
  function renderReadings(readings) {
    const tableBody = document.getElementById("readingTableBody");
    tableBody.innerHTML = "";
    readings.forEach((reading) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${reading.id}</td>
        <td>${reading.customer?.firstName || "?"} ${reading.customer?.lastName || "?"}</td>
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

  // --- Funktion: loadCustomersForReadingModal ---
  async function loadCustomersForReadingModal() {
    const userId = localStorage.getItem("userId");
    if (!userId) return;
    try {
      const response = await fetch(`${apiBaseUrl}/customers?userId=${userId}`);
      if (!response.ok) {
        throw new Error("Error loading customers. HTTP " + response.status);
      }
      const customers = await response.json();
      allCustomers = customers;
      populateCustomerDropdown(customers);
    } catch (error) {
      console.error("Error loading customers for modal:", error);
    }
  }

  function populateCustomerDropdown(customers) {
    const selectElem = document.getElementById("customerSelect");
    if (!selectElem) return;
    selectElem.innerHTML = `<option value="">-- Neuer Kunde --</option>`;
    customers.forEach((customer) => {
      const option = document.createElement("option");
      option.value = customer.id;
      option.textContent = `${customer.firstName} ${customer.lastName}`;
      selectElem.appendChild(option);
    });
  }

  // --- Beim Öffnen des "Neue Ablesung"-Modals ---
  const addReadingModalElem = document.getElementById("addReadingModal");
  addReadingModalElem.addEventListener("show.bs.modal", () => {
    // Felder zurücksetzen
    document.getElementById("comment").value = "";
    document.getElementById("dateOfReading").value = "";
    // Dropdown für Zählertyp zurücksetzen (erste Option wählen)
    document.getElementById("kindOfMeter").selectedIndex = 0;
    document.getElementById("meterCount").value = "";
    document.getElementById("meterId").value = "";
    document.getElementById("substitute").value = "0";
    // Lade bestehende Kunden für den aktuellen Benutzer in das Dropdown
    loadCustomersForReadingModal();
    // Optional: Hier kannst Du den Bereich "newCustomerFields" ein- oder ausblenden.
  });

  // --- Wenn der Zählertyp geändert wird, generiere automatisch eine meterId ---
  const kindOfMeterSelect = document.getElementById("kindOfMeter");
  kindOfMeterSelect.addEventListener("change", () => {
    const selectedType = kindOfMeterSelect.value;
    if (!selectedType) return;
    // Zähle, wie viele Readings mit diesem Zählertyp bereits existieren (für den aktuellen User)
    const count = allReadings.filter(r => r.kindOfMeter === selectedType).length;
    const newNumber = String(count + 1).padStart(3, "0");
    document.getElementById("meterId").value = selectedType + newNumber;
  });

  // --- Beim Speichern einer neuen Ablesung ---
  document.getElementById("saveReadingBtn").addEventListener("click", async () => {
    console.log("Neue Ablesung speichern geklickt!");

    const comment = document.getElementById("comment").value.trim();
    const dateOfReading = document.getElementById("dateOfReading").value.trim();
    const kindOfMeter = document.getElementById("kindOfMeter").value;
    const meterCount = parseFloat(document.getElementById("meterCount").value);
    const meterId = document.getElementById("meterId").value.trim();
    const substituteInput = document.getElementById("substitute").value.trim();
    const substitute = (substituteInput === "1" || substituteInput.toLowerCase() === "true");

    // Ermitteln, ob ein bestehender Kunde ausgewählt wurde:
    const customerSelect = document.getElementById("customerSelect");
    let customer;
    if (customerSelect && customerSelect.value) {
      customer = allCustomers.find(c => c.id === customerSelect.value);
    } else {
      let customerId = document.getElementById("customerId").value.trim();
      if (!customerId) {
        customerId = generateUUID();
      }
      const customerFirstName = document.getElementById("customerFirstName").value.trim();
      const customerLastName = document.getElementById("customerLastName").value.trim();
      const customerBirthDate = document.getElementById("customerBirthDate").value.trim();
      const customerGender = document.getElementById("customerGender").value;
      if (!customerFirstName || !customerLastName || !customerBirthDate) {
        alert("Bitte füllen Sie alle Kundenfelder aus (Vorname, Nachname, Geburtsdatum).");
        return;
      }
      customer = {
        id: customerId,
        firstName: customerFirstName,
        lastName: customerLastName,
        birthDate: customerBirthDate,
        gender: customerGender,
        userId: localStorage.getItem("userId")
      };
    }

    if (!kindOfMeter || isNaN(meterCount) || !dateOfReading) {
      alert("Bitte füllen Sie alle Pflichtfelder für die Ablesung aus.");
      return;
    }

    const newReading = {
      comment: comment,
      customer: customer,
      dateOfReading: dateOfReading,
      kindOfMeter: kindOfMeter,
      meterCount: meterCount,
      meterId: meterId || (kindOfMeter + "001"),
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

  // Filter-Button: Läd die Readings anhand des ausgewählten Filters neu
  document.getElementById("filterButton").addEventListener("click", () => {
    const filterValue = document.getElementById("filterKindOfMeter").value.trim();
    loadReadings(filterValue);
  });

  // Beim Laden der Seite: initiale Readings laden
  loadReadings();
});
