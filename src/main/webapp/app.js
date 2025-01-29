document.addEventListener("DOMContentLoaded", () => {
    const apiBaseUrl = "http://localhost:8080/customers"; // Basis-URL für die API

    // Funktion, um Kunden zu laden
    async function loadCustomers() {
        try {
            const response = await fetch(apiBaseUrl);
            if (!response.ok) {
                throw new Error("Fehler beim Laden der Kunden.");
            }
            const customers = await response.json();
            const tableBody = document.querySelector("#customerTable tbody");

            tableBody.innerHTML = ""; // Leert die Tabelle vor dem Hinzufügen neuer Daten

            customers.forEach((customer) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${customer.id}</td>
                    <td>${customer.firstName}</td>
                    <td>${customer.lastName}</td>
                    <td>${customer.birthDate}</td>
                    <td>${customer.gender}</td>
                    <td>
                        <button class="btn btn-warning btn-edit" data-id="${customer.id}">Bearbeiten</button>
                        <button class="btn btn-danger btn-delete" data-id="${customer.id}">Löschen</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });

            attachEventListeners(); // Neue Event-Listener an die Buttons binden
        } catch (error) {
            showToast("Fehler beim Laden der Kunden: " + error.message, "error");
        }
    }

    // Event-Listener für die Buttons
    function attachEventListeners() {
        document.querySelectorAll(".btn-edit").forEach((button) => {
            button.addEventListener("click", handleEdit);
        });

        document.querySelectorAll(".btn-delete").forEach((button) => {
            button.addEventListener("click", handleDelete);
        });
    }

    // Bearbeiten eines Kunden
    async function handleEdit(event) {
        const id = event.target.dataset.id;
        const newFirstName = prompt("Neuer Vorname:");
        const newLastName = prompt("Neuer Nachname:");

        if (!newFirstName || !newLastName) {
            showToast("Vorname und Nachname dürfen nicht leer sein.", "warning");
            return;
        }

        try {
            const response = await fetch(`${apiBaseUrl}/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id, firstName: newFirstName, lastName: newLastName }),
            });

            if (!response.ok) {
                throw new Error("Fehler beim Aktualisieren des Kunden.");
            }

            showToast("Kunde erfolgreich aktualisiert!", "success");
            loadCustomers(); // Tabelle neu laden
        } catch (error) {
            showToast("Fehler bei der PUT-Anfrage: " + error.message, "error");
        }
    }

    // Löschen eines Kunden
    async function handleDelete(event) {
        const id = event.target.dataset.id;

        if (!confirm("Möchten Sie diesen Kunden wirklich löschen?")) {
            return;
        }

        try {
            const response = await fetch(`${apiBaseUrl}/${id}`, { method: "DELETE" });

            if (!response.ok) {
                throw new Error("Fehler beim Löschen des Kunden.");
            }

            showToast("Kunde erfolgreich gelöscht!", "success");

            // Entferne die gelöschte Zeile aus der Tabelle
            const row = event.target.closest("tr");
            if (row) {
                row.remove();
            }
            loadCustomers(); // Tabelle neu laden
        } catch (error) {
            showToast("Fehler bei der DELETE-Anfrage: " + error.message, "error");
        }
    }

    // Kunden hinzufügen
    document.getElementById("saveCustomerBtn").addEventListener("click", async () => {
        const firstName = document.getElementById("firstName").value.trim();
        const lastName = document.getElementById("lastName").value.trim();
        const birthDateInput = document.getElementById("birthDate").value;
        const genderInput = document.getElementById("gender").value;

        // Konvertiere Geburtsdatum und Geschlecht ins richtige Format
        const [year, month, day] = birthDateInput.split("-");
        const birthDate = `${year}-${month}-${day}`;
        const gender = genderInput;

        // Überprüfe, ob alle Felder ausgefüllt sind
        if (!firstName || !lastName || !birthDate || !gender) {
            showToast("Alle Felder müssen ausgefüllt sein.", "warning");
            return;
        }

        try {
            const response = await fetch(apiBaseUrl, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstName, lastName, birthDate, gender }),
            });

            if (!response.ok) {
                throw new Error("Fehler beim Hinzufügen des Kunden.");
            }

            showToast("Kunde erfolgreich hinzugefügt!", "success");

            // Modal schließen und Tabelle aktualisieren
            const modal = bootstrap.Modal.getInstance(document.getElementById("addCustomerModal"));
            modal.hide();
            loadCustomers();
        } catch (error) {
            showToast("Fehler bei der POST-Anfrage: " + error.message, "error");
        }
    });

    // Importieren von Daten
    document.getElementById("importSubmitBtn").addEventListener("click", async () => {
        const fileInput = document.getElementById("importFile");
        const file = fileInput.files[0];
        if (!file) {
            showToast("Bitte wählen Sie eine Datei aus.", "warning");
            return;
        }

        const reader = new FileReader();
        reader.onload = async (event) => {
            const fileContent = event.target.result;
            try {
                const response = await fetch(`${apiBaseUrl}/import`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: fileContent,
                });
                if (!response.ok) throw new Error("Import fehlgeschlagen.");
                showToast("Daten erfolgreich importiert!", "success");
                loadCustomers();
            } catch (error) {
                showToast("Fehler beim Import: " + error.message, "error");
            }
        };

        reader.readAsText(file);
    });

    // Exportieren von Daten
    async function exportData(format) {
        try {
            const response = await fetch(`${apiBaseUrl}/export?format=${format}`);
            if (!response.ok) throw new Error("Export fehlgeschlagen.");
            const data = await response.blob();
            const url = window.URL.createObjectURL(data);
            const a = document.createElement("a");
            a.href = url;
            a.download = `export.${format}`;
            a.click();
            window.URL.revokeObjectURL(url);
            showToast(`Daten als ${format.toUpperCase()} exportiert!`, "success");
        } catch (error) {
            showToast("Fehler beim Export: " + error.message, "error");
        }
    }

    // Event-Listener für Export-Buttons
    document.getElementById("exportJsonBtn").addEventListener("click", () => exportData("json"));
    document.getElementById("exportCsvBtn").addEventListener("click", () => exportData("csv"));
    document.getElementById("exportXmlBtn").addEventListener("click", () => exportData("xml"));

    // Toast-Nachricht anzeigen
    function showToast(message, type) {
        const toastContainer = document.getElementById("toast-container");
        if (!toastContainer) return;

        const toast = document.createElement("div");
        toast.className = `toast toast-${type}`;
        toast.innerText = message;

        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    // Suchfunktion
    document.getElementById("searchCustomer").addEventListener("input", (event) => {
        const searchValue = event.target.value.toLowerCase();
        document.querySelectorAll("#customerTable tbody tr").forEach((row) => {
            const rowText = row.textContent.toLowerCase();
            row.style.display = rowText.includes(searchValue) ? "" : "none";
        });
    });

    // Kunden initial laden
    loadCustomers();
});
