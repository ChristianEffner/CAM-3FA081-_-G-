document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";

  // 1) Kunden laden
  async function loadCustomers() {
    try {
      const response = await fetch(`${apiBaseUrl}/customers`);
      if (!response.ok) {
        throw new Error("Error loading customers. HTTP " + response.status);
      }
      const customers = await response.json();
      populateCustomerTable(customers);
    } catch (error) {
      showToast("Error loading customers: " + error.message, "error");
    }
  }

  // 2) Tabelle befüllen
  function populateCustomerTable(customers) {
    const tableBody = document.querySelector("#customerTable tbody");
    if (!tableBody) return;

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

    attachEventListeners();
  }

  // 3) Buttons (Edit, Delete, Details)
  function attachEventListeners() {
    document.querySelectorAll(".btn-edit").forEach((btn) => {
      btn.addEventListener("click", handleEdit);
    });
    document.querySelectorAll(".btn-delete").forEach((btn) => {
      btn.addEventListener("click", handleDelete);
    });
    document.querySelectorAll(".btn-details").forEach((btn) => {
      btn.addEventListener("click", handleDetails);
    });
  }

  // 4) handleEdit
  async function handleEdit(event) {
    const id = event.target.dataset.id;
    try {
      const response = await fetch(`${apiBaseUrl}/customers/${id}`);
      if (!response.ok) {
        throw new Error("Error retrieving customer. HTTP " + response.status);
      }
      const customer = await response.json();

      // Felder füllen
      document.getElementById("editFirstName").value = customer.firstName;
      document.getElementById("editLastName").value = customer.lastName;
      document.getElementById("editBirthDate").value = customer.birthDate;
      document.getElementById("editGender").value = customer.gender;

      // ID speichern
      document.getElementById("editCustomerForm").dataset.customerId = id;

      // Modal öffnen
      const modal = new bootstrap.Modal(document.getElementById("editCustomerModal"));
      modal.show();
    } catch (error) {
      showToast("Error loading customer: " + error.message, "error");
    }
  }

  // 5) Speichern (Edit)
  const saveEditBtn = document.getElementById("saveEditCustomerBtn");
  if (saveEditBtn) {
    saveEditBtn.addEventListener("click", async () => {
      const form = document.getElementById("editCustomerForm");
      const id = form.dataset.customerId;
      const firstName = document.getElementById("editFirstName").value.trim();
      const lastName = document.getElementById("editLastName").value.trim();
      const birthDate = document.getElementById("editBirthDate").value;
      const gender = document.getElementById("editGender").value;

      if (!firstName || !lastName || !birthDate || !gender) {
        showToast("Bitte alle Felder ausfüllen!", "warning");
        return;
      }

      try {
        const response = await fetch(`${apiBaseUrl}/customers`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ id, firstName, lastName, birthDate, gender }),
        });
        if (!response.ok) {
          throw new Error("Error updating customer. HTTP " + response.status);
        }

        showToast("Kunde erfolgreich aktualisiert!", "success");
        bootstrap.Modal.getInstance(document.getElementById("editCustomerModal")).hide();
        loadCustomers();
      } catch (error) {
        showToast("Fehler beim Aktualisieren: " + error.message, "error");
      }
    });
  }

  // 6) handleDelete
  async function handleDelete(event) {
    const id = event.target.dataset.id;
    if (!confirm("Kunden wirklich löschen?")) return;

    try {
      const response = await fetch(`${apiBaseUrl}/customers/${id}`, { method: "DELETE" });
      if (!response.ok) {
        throw new Error("Error deleting customer. HTTP " + response.status);
      }

      showToast("Kunde erfolgreich gelöscht!", "success");
      loadCustomers();
    } catch (error) {
      showToast("Fehler beim Löschen: " + error.message, "error");
    }
  }

  // 7) handleDetails
  function handleDetails(event) {
    const id = event.target.dataset.id;
    // Z.B. reading.html?readingId=...
    // Oder customers/ID => detail page. Hier zeige ich, wie du Reading anzeigst.
    window.location.href = `reading.html?readingId=${id}`;
  }

  // 8) Neuer Kunde
  const saveCustomerBtn = document.getElementById("saveCustomerBtn");
  if (saveCustomerBtn) {
    saveCustomerBtn.addEventListener("click", async () => {
      console.log("Neuer Kunde Button geklickt!"); // Debug-Log

      const firstName = document.getElementById("firstName").value.trim();
      const lastName = document.getElementById("lastName").value.trim();
      const birthDate = document.getElementById("birthDate").value;
      const gender = document.getElementById("gender").value;

      if (!firstName || !lastName || !birthDate || !gender) {
        showToast("Bitte alle Felder ausfüllen!", "warning");
        return;
      }

      try {
        const response = await fetch(`${apiBaseUrl}/customers`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ firstName, lastName, birthDate, gender }),
        });
        if (!response.ok) {
          throw new Error("Error adding customer. HTTP " + response.status);
        }

        showToast("Kunde erfolgreich hinzugefügt!", "success");
        bootstrap.Modal.getInstance(document.getElementById("addCustomerModal")).hide();
        loadCustomers();
      } catch (error) {
        showToast("Fehler beim Hinzufügen: " + error.message, "error");
      }
    });
  }

  // 9) Suche
  const searchInput = document.getElementById("searchCustomer");
  if (searchInput) {
    searchInput.addEventListener("input", (event) => {
      const searchValue = event.target.value.toLowerCase();
      document.querySelectorAll("#customerTable tbody tr").forEach((row) => {
        const rowText = row.textContent.toLowerCase();
        row.style.display = rowText.includes(searchValue) ? "" : "none";
      });
    });
  }

  // 10) Toast-Helfer
  function showToast(message, type) {
    const toastContainer = document.getElementById("toast-container") || createToastContainer();

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerText = message;

    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.remove();
    }, 3000);
  }

  function createToastContainer() {
    const container = document.createElement("div");
    container.id = "toast-container";
    container.style.position = "fixed";
    container.style.bottom = "20px";
    container.style.right = "20px";
    container.style.zIndex = "9999";
    document.body.appendChild(container);
    return container;
  }

  // Initiales Laden
  loadCustomers();
});
