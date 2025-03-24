document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";
  let currentCustomer = null;
  let allCustomers = []; // <== Hier speichern wir später alle Kunden

  async function loadCustomers() {
    console.log("Lade Kunden ...");
    try {
      const userId = localStorage.getItem("userId");
      if (!userId) {
        throw new Error("Keine userId im LocalStorage gefunden!");
      }
      let url = `${apiBaseUrl}/customers?userId=${userId}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(
          "Fehler beim Laden der Kunden (Status: " + response.status + ")"
        );
      }
      const customers = await response.json();
      console.log("Kunden empfangen:", customers);

      // Speichern für handleDetails()
      allCustomers = customers;

      renderCustomers(customers);
    } catch (error) {
      console.error("Error loading customers:", error);
      alert("Fehler beim Laden der Kunden: " + error.message);
    }
  }

  function renderCustomers(customers) {
    const tableBody = document.getElementById("customerTableBody");
    tableBody.innerHTML = "";

    customers.forEach((customer) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${customer.id}</td>
        <td>${customer.firstName}</td>
        <td>${customer.lastName}</td>
        <td>${customer.birthDate}</td>
        <td>${customer.gender}</td>
        <td>
          <button class="btn btn-warning btn-edit" data-id="${customer.id}">
            Bearbeiten
          </button>
          <button class="btn btn-danger btn-delete" data-id="${customer.id}">
            Löschen
          </button>
          <button class="btn btn-info btn-details" data-id="${customer.id}">
            Daten
          </button>
        </td>
      `;
      tableBody.appendChild(row);
    });

    attachTableEventListeners();
  }

  function attachTableEventListeners() {
    document
      .querySelectorAll(".btn-edit")
      .forEach((btn) => btn.addEventListener("click", handleEdit));
    document
      .querySelectorAll(".btn-delete")
      .forEach((btn) => btn.addEventListener("click", handleDelete));
    document
      .querySelectorAll(".btn-details")
      .forEach((btn) => btn.addEventListener("click", handleDetails));
  }

  /**
   * == Kunde bearbeiten ==
   */
  async function handleEdit(event) {
    const id = event.target.dataset.id;
    console.log("Bearbeiten geklickt, ID =", id);

    try {
      const response = await fetch(`${apiBaseUrl}/customers/${id}`);
      if (!response.ok) {
        throw new Error(
          `Fehler beim Laden (ID=${id}), Status=${response.status}`
        );
      }
      const customer = await response.json();
      console.log("Kunde zum Bearbeiten:", customer);

      currentCustomer = customer;

      // Felder im Modal
      document.getElementById("editCustomerId").value = customer.id || "";
      document.getElementById("editCustomerFirstName").value =
        customer.firstName || "";
      document.getElementById("editCustomerLastName").value =
        customer.lastName || "";
      document.getElementById("editCustomerBirthDate").value =
        customer.birthDate || "";
      document.getElementById("editCustomerGender").value =
        customer.gender || "M";

      const editModal = new bootstrap.Modal(
        document.getElementById("editCustomerModal")
      );
      editModal.show();
    } catch (error) {
      console.error("Error editing customer:", error);
      alert("Fehler beim Bearbeiten: " + error.message);
    }
  }

  /**
   * == Kunde löschen ==
   */
  async function handleDelete(event) {
    const id = event.target.dataset.id;
    console.log("Löschen geklickt, ID =", id);

    if (!confirm("Soll dieser Kunde wirklich gelöscht werden?")) return;

    try {
      const response = await fetch(`${apiBaseUrl}/customers/${id}`, {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error(
          "Fehler beim Löschen (Status: " + response.status + ")"
        );
      }
      alert("Kunde erfolgreich gelöscht!");
      loadCustomers();
    } catch (error) {
      console.error("Error deleting customer:", error);
      alert("Fehler beim Löschen: " + error.message);
    }
  }

  /**
   * == Button "Daten" ==
   * => Weiterleitung zu reading.html?customerSearch=...
   */
  function handleDetails(event) {
    const id = event.target.dataset.id;
    console.log("Details geklickt, ID =", id);

    // Kunden in "allCustomers" suchen
    const thisCust = allCustomers.find(c => c.id === id);
    if (!thisCust) {
      console.warn("Kunde nicht gefunden:", id);
      return;
    }
    // Bilde Suchbegriff, zB "Vorname Nachname"
    const searchString = `${thisCust.firstName} ${thisCust.lastName}`.trim();

    // Weiterleiten + QueryParam
    window.location.href = `reading.html?customerSearch=${encodeURIComponent(searchString)}`;
  }

  /**
   * == Klick auf "Speichern" im Bearbeiten-Modal => PUT /customers ==
   */
  const updateBtn = document.getElementById("updateCustomerBtn");
  updateBtn.addEventListener("click", async () => {
    console.log("Update-Button geklickt!");
    if (!currentCustomer) {
      alert("Kein Kunde zum Bearbeiten geladen!");
      return;
    }

    const id = document.getElementById("editCustomerId").value.trim();
    const firstName = document
      .getElementById("editCustomerFirstName")
      .value.trim();
    const lastName = document
      .getElementById("editCustomerLastName")
      .value.trim();
    const birthDate = document
      .getElementById("editCustomerBirthDate")
      .value.trim();
    const gender = document.getElementById("editCustomerGender").value;
    const userId = localStorage.getItem("userId");

    if (!id || !firstName || !lastName || !birthDate) {
      alert("Bitte alle Pflichtfelder ausfüllen.");
      return;
    }

    currentCustomer.id = id;
    currentCustomer.firstName = firstName;
    currentCustomer.lastName = lastName;
    currentCustomer.birthDate = birthDate;
    currentCustomer.gender = gender;
    currentCustomer.userId = userId ? parseInt(userId) : null;

    console.log("PUT /customers =>", currentCustomer);
    try {
      const response = await fetch(`${apiBaseUrl}/customers`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(currentCustomer),
      });
      if (!response.ok) {
        throw new Error(
          "Fehler beim Aktualisieren (Status: " + response.status + ")"
        );
      }
      alert("Kunde erfolgreich aktualisiert!");
      bootstrap.Modal.getInstance(
        document.getElementById("editCustomerModal")
      ).hide();

      currentCustomer = null;
      loadCustomers();
    } catch (error) {
      console.error("Error updating customer:", error);
      alert("Fehler beim Aktualisieren: " + error.message);
    }
  });

  /**
   * == Klick auf "Speichern" im "Neuer Kunde"-Modal => POST /customers ==
   */
  const saveBtn = document.getElementById("saveCustomerBtn");
  saveBtn.addEventListener("click", async () => {
    console.log("Neuen Kunde Button geklickt!");
    const userIdField = document.getElementById("userId").value.trim();
    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const birthDate = document.getElementById("birthDate").value.trim();
    const gender = document.getElementById("gender").value;

    if (!firstName || !lastName || !birthDate) {
      alert("Bitte alle Felder ausfüllen.");
      return;
    }

    const requestBody = {
      customer: {
        firstName,
        lastName,
        birthDate,
        gender,
        userId: userIdField ? parseInt(userIdField) : null,
      },
    };

    console.log("POST /customers =>", requestBody);
    try {
      const response = await fetch(`${apiBaseUrl}/customers`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });
      if (!response.ok) {
        throw new Error(
          "Fehler beim Speichern (Status: " + response.status + ")"
        );
      }
      alert("Kunde erfolgreich gespeichert!");
      bootstrap.Modal.getInstance(
        document.getElementById("addCustomerModal")
      ).hide();
      loadCustomers();
    } catch (error) {
      console.error("Error saving customer:", error);
      alert("Fehler beim Speichern des Kunden: " + error.message);
    }
  });

  /**
   * Client-seitige Suche
   */
  const searchInput = document.getElementById("searchCustomer");
  if (searchInput) {
    searchInput.addEventListener("input", (event) => {
      const searchValue = event.target.value.toLowerCase();
      document.querySelectorAll("#customerTableBody tr").forEach((row) => {
        const rowText = row.textContent.toLowerCase();
        row.style.display = rowText.includes(searchValue) ? "" : "none";
      });
    });
  }

  // Beim Laden der Seite
  loadCustomers();
});
